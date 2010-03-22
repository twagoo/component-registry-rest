package clarin.cmdi.componentregistry.editor {
	import clarin.cmdi.componentregistry.browser.XMLBrowser;
	import clarin.cmdi.componentregistry.common.ItemDescription;
	import clarin.cmdi.componentregistry.common.StyleConstants;
	import clarin.cmdi.componentregistry.common.components.ExpandingComponentLabel;
	import clarin.cmdi.componentregistry.editor.model.CMDComponent;
	import clarin.cmdi.componentregistry.editor.model.CMDComponentElement;
	
	import flash.display.DisplayObject;
	import flash.events.Event;
	import flash.events.MouseEvent;
	
	import mx.collections.ArrayCollection;
	import mx.containers.Form;
	import mx.containers.FormItem;
	import mx.containers.HBox;
	import mx.controls.Button;
	import mx.controls.HRule;
	import mx.core.Container;
	import mx.core.UIComponent;
	import mx.events.DragEvent;
	import mx.managers.DragManager;

	[Event(name="removeComponent", type="flash.events.Event")]
	public class ComponentEdit extends Form {
		public static const REMOVE_COMPONENT_EVENT:String = "removeComponent";

		private var _parent:UIComponent;
		private var _component:CMDComponent;
		private var currentElementIndex:int = -1;

		public function ComponentEdit(component:CMDComponent, parent:UIComponent) {
			super();
			_component = component;
			_parent = parent;
			styleName = StyleConstants.XMLBROWSER;
			if (!component.componentId) { // new empty component, otherwise it would be an already existed component which cannot be edited.
				addEventListener(DragEvent.DRAG_ENTER, dragEnterHandler);
				addEventListener(DragEvent.DRAG_OVER, dragOverHandler);
				addEventListener(DragEvent.DRAG_DROP, dragDropHandler);
			}
		}

		public function get component():CMDComponent {
			return _component;
		}

		private function dragEnterHandler(event:DragEvent):void {
			DragManager.acceptDragDrop(event.currentTarget as UIComponent);
			UIComponent(event.currentTarget).drawFocus(true);
		}


		private function dragOverHandler(event:DragEvent):void {
			if (event.dragSource.hasFormat("items")) {
				DragManager.showFeedback(DragManager.COPY);
			} else if (event.dragSource.hasFormat(CMDComponentXMLEditor.DRAG_NEW_COMPONENT)) {
				DragManager.showFeedback(DragManager.COPY);
			} else if (event.dragSource.hasFormat(CMDComponentXMLEditor.DRAG_NEW_ELEMENT)) {
				DragManager.showFeedback(DragManager.COPY);
			} else {
				DragManager.showFeedback(DragManager.NONE);
			}
		}

		private function dragDropHandler(event:DragEvent):void {
			if (event.dragSource.hasFormat("items")) {
				var items:Array = event.dragSource.dataForFormat("items") as Array;
				for each (var item:ItemDescription in items) {
					var comp:CMDComponent = new CMDComponent();
					comp.componentId = item.id;
					_component.cmdComponents.addItem(comp);
					addComponent(comp);
				}
			} else if (event.dragSource.hasFormat(CMDComponentXMLEditor.DRAG_NEW_COMPONENT)) {
				var emptyComp:CMDComponent = event.dragSource.dataForFormat(CMDComponentXMLEditor.DRAG_NEW_COMPONENT) as CMDComponent;
				_component.cmdComponents.addItem(emptyComp);
				addComponent(emptyComp);
			} else if (event.dragSource.hasFormat(CMDComponentXMLEditor.DRAG_NEW_ELEMENT)) {
				var element:CMDComponentElement = event.dragSource.dataForFormat(CMDComponentXMLEditor.DRAG_NEW_ELEMENT) as CMDComponentElement;
				_component.cmdElements.addItem(element);
				addElement(element, currentElementIndex);
			}
		}

		private function fireRemoveComponent(mouseEvent:MouseEvent):void {
			drawFocus(false);
			var event:Event = new Event(REMOVE_COMPONENT_EVENT);
			dispatchEvent(event);
		}

		protected override function createChildren():void {
			super.createChildren();
			var ruler:HRule = new HRule();
			ruler.percentWidth = 80;
			addChild(ruler);
			addChild(createComponentEditBar());

			var componentLink:FormItem = createComponentLink(_component);
			if (componentLink != null) {
				addChild(componentLink);
			} else {
				addChild(new FormItemInputLine("Name", _component.name, function(val:String):void {
						_component.name = val;
					}));
				addChild(new ConceptLinkInput(XMLBrowser.CONCEPTLINK, _component.conceptLink, function(val:String):void {
						_component.conceptLink = val;
					}));
				addChild(new FormItemInputLine("CardinalityMin", _component.cardinalityMin, function(val:String):void {
						_component.cardinalityMin = val;
					}));
				addChild(new FormItemInputLine("CardinalityMax", _component.cardinalityMax, function(val:String):void {
						_component.cardinalityMax = val;
					}));
				handleCMDAttributeList();
				handleElements(_component.cmdElements);
				handleComponents(_component.cmdComponents); //recursion
			}
		}

		private function createComponentEditBar():HBox {
			var editBar:HBox = new HBox();
			editBar.addChild(createHeading());
			var removeButton:Button = new Button();
			removeButton.height = 20;
			removeButton.label = "X";
			removeButton.addEventListener(MouseEvent.CLICK, fireRemoveComponent);
			removeButton.addEventListener(MouseEvent.MOUSE_OVER, function(event:MouseEvent):void {
					drawFocus(true);
				});
			removeButton.addEventListener(MouseEvent.MOUSE_OUT, function(event:MouseEvent):void {
					drawFocus(false);
				});

			editBar.addChild(removeButton);
			return editBar;
		}

		private function createComponentLink(component:CMDComponent):FormItem {
			if (component.componentId != "" && component.componentId != null) {
				var result:FormItem = new FormItem();
				result.styleName = StyleConstants.XMLBROWSER_FIELD;
				result.label = XMLBrowser.COMPONENT_ID;
				result.addChild(new ExpandingComponentLabel(component.componentId, false));
				return result;
			}
			return null;
		}

		private function handleCMDAttributeList():void {
			if (_component.attributeList.length > 0)
				addChild(new AttributeListEdit(_component.attributeList, this));
		}

		private function handleComponents(components:ArrayCollection):void {
			for each (var component:CMDComponent in components) {
				addComponent(component);
			}
		}

		public function addComponent(component:CMDComponent, index:int = -1):void {
			var comp:Container = new ComponentEdit(component, this);
			comp.addEventListener(ComponentEdit.REMOVE_COMPONENT_EVENT, removeComponent);
			comp.setStyle("paddingLeft", "50");
			if (index == -1) {
				addChild(comp);
			} else {
				addChildAt(comp, index);
			}
		}

		private function removeComponent(event:Event):void {
			var comp:CMDComponent = ComponentEdit(event.currentTarget).component;
			_component.removeComponent(comp);
			removeChild(event.currentTarget as DisplayObject);
		}

		private function handleElements(elements:ArrayCollection):void {
			for each (var element:CMDComponentElement in elements) {
				addElement(element);
			}
		}

		public function addElement(element:CMDComponentElement, index:int = -1):void {
			var elem:Container = new ElementEdit(element, this);
			elem.setStyle("paddingLeft", "50");
			elem.addEventListener(ElementEdit.REMOVE_ELEMENT_EVENT, removeElement);
			if (index == -1) {
				addChild(elem);
			} else {
				addChildAt(elem, index);
			}
			currentElementIndex = getChildIndex(elem) + 1;
		}

		private function removeElement(event:Event):void {
			var elem:CMDComponentElement = ElementEdit(event.currentTarget).element;
			_component.removeElement(elem);
			removeChild(event.currentTarget as DisplayObject);
			currentElementIndex--;
		}

		private function createHeading():FormItem {
			var heading:FormItem = new FormItem();
			heading.label = XMLBrowser.COMPONENT;
			heading.styleName = StyleConstants.XMLBROWSER_HEADER;
			return heading;
		}


	}
}
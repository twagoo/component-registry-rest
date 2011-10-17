package clarin.cmdi.componentregistry.editor {
	import clarin.cmdi.componentregistry.common.ItemDescription;
	import clarin.cmdi.componentregistry.common.LabelConstants;
	import clarin.cmdi.componentregistry.common.StyleConstants;
	import clarin.cmdi.componentregistry.common.components.AddComponentLabelButton;
	import clarin.cmdi.componentregistry.common.components.AddElementLabelButton;
	import clarin.cmdi.componentregistry.common.components.DownIconButton;
	import clarin.cmdi.componentregistry.common.components.ExpandingComponentLabel;
	import clarin.cmdi.componentregistry.common.components.RemoveLabelButton;
	import clarin.cmdi.componentregistry.common.components.UpIconButton;
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
	import mx.controls.Label;
	import mx.core.Container;
	import mx.core.UIComponent;
	import mx.events.DragEvent;
	import mx.managers.DragManager;
	
	[Event(name="removeComponent", type="flash.events.Event")]
	public class ComponentEdit extends Form {
		public static const REMOVE_COMPONENT_EVENT:String = "removeComponent";
		private static const DRAG_ITEMS:String = "items";
		
		private var _parent:UIComponent;
		private var _component:CMDComponent;
		private var _parentComponent:CMDComponent;
		private var addComponentLabel:Label
		private var addElementLabel:Label
		
		
		public function ComponentEdit(component:CMDComponent, parent:UIComponent, parentComponent:CMDComponent) {
			super();
			_component = component;
			_parentComponent = parentComponent;
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
		
		public function get parentComponent():CMDComponent {
			return _parentComponent;
		}
		
		private function dragEnterHandler(event:DragEvent):void {
			DragManager.acceptDragDrop(event.currentTarget as UIComponent);
			UIComponent(event.currentTarget).drawFocus(true);
		}
		
		
		private function dragOverHandler(event:DragEvent):void {
			if (event.dragSource.hasFormat(DRAG_ITEMS)) {
				DragManager.showFeedback(DragManager.COPY);
			} else {
				DragManager.showFeedback(DragManager.NONE);
			}
		}
		
		private function dragDropHandler(event:DragEvent):void {
			if (event.dragSource.hasFormat(DRAG_ITEMS)) {
				var items:Array = event.dragSource.dataForFormat(DRAG_ITEMS) as Array;
				for each (var item:ItemDescription in items) {
					var comp:CMDComponent = new CMDComponent();
					comp.componentId = item.id;
					_component.cmdComponents.addItem(comp);
					addComponent(comp);
				}
			}
		}
		
		private function fireRemoveComponent(mouseEvent:MouseEvent):void {
			drawFocus(false);
			var event:Event = new Event(REMOVE_COMPONENT_EVENT);
			dispatchEvent(event);
		}
		
		protected override function createChildren():void {
			super.createChildren();
			addRuler();
			createComponentEditBar();
			
			var componentLink:FormItem = createComponentLink(_component);
			if (componentLink != null) {
				addCardinalityInput();
				addChild(componentLink);
			} else {
				addNameInput();
				addConceptLink();
				addCardinalityInput();
				handleCMDAttributeList();
				handleElements(_component.cmdElements);
				addElementAddButton();
				handleComponents(_component.cmdComponents); //recursion
				addComponentAddButton();
			}
		}
		
		private function addComponentAddButton():void {
			addComponentLabel = new AddComponentLabelButton();
			addComponentLabel.addEventListener(MouseEvent.CLICK, function(event:MouseEvent):void {
				var comp:CMDComponent = CMDComponent.createEmptyComponent();
				_component.cmdComponents.addItem(comp);
				addComponent(comp);
			});
			addComponentLabel.addEventListener(MouseEvent.MOUSE_OVER, function(event:MouseEvent):void {
				drawFocus(true);
			});
			addComponentLabel.addEventListener(MouseEvent.MOUSE_OUT, function(event:MouseEvent):void {
				drawFocus(false);
			});
			addChild(addComponentLabel);
		}
		
		private function addElementAddButton():void {
			addElementLabel = new AddElementLabelButton();
			addElementLabel.addEventListener(MouseEvent.CLICK, function(event:MouseEvent):void {
				var element:CMDComponentElement = CMDComponentElement.createEmptyElement();
				_component.cmdElements.addItem(element);
				addElement(element);
				
			});
			addElementLabel.addEventListener(MouseEvent.MOUSE_OVER, function(event:MouseEvent):void {
				drawFocus(true);
			});
			addElementLabel.addEventListener(MouseEvent.MOUSE_OUT, function(event:MouseEvent):void {
				drawFocus(false);
			});
			addChild(addElementLabel);
		}
		
		private function addConceptLink():void {
			addChild(new ConceptLinkInput(LabelConstants.CONCEPTLINK, _component.conceptLink, function(val:String):void {
				_component.conceptLink = val;
			}));
		}
		
		private function addRuler():void {
			var ruler:HRule = new HRule();
			ruler.percentWidth = 80;
			addChild(ruler);
		}
		
		private function addNameInput():void {
			var nameInput:FormItemInputLine = new NameInputLine(_component.name, function(val:String):void {
				_component.name = val;
			});
			addChild(nameInput);
		}
		
		private function addCardinalityInput():void {
			addChild(new CardinalityInput(LabelConstants.CARDINALITY_MIN, _component.cardinalityMin, function(val:String):void {
				_component.cardinalityMin = val;
			}));
			addChild(new CardinalityInput(LabelConstants.CARDINALITY_MAX, _component.cardinalityMax, function(val:String):void {
				_component.cardinalityMax = val;
			}));
		}
		
		private function createComponentEditBar():void {
			var editBar:HBox = new HBox();
			editBar.addChild(createHeading());
			var removeButton:Label = new RemoveLabelButton();
			addFocusListeners(removeButton).addEventListener(MouseEvent.CLICK, fireRemoveComponent);
			editBar.addChild(removeButton);
			
			var downButton:Button = new DownIconButton();
			addFocusListeners(downButton).addEventListener(MouseEvent.CLICK, moveDownComponent);
			editBar.addChild(downButton);
			
			var upButton:Button = new UpIconButton();
			addFocusListeners(upButton).addEventListener(MouseEvent.CLICK, moveUpComponent);
			editBar.addChild(upButton);
			addChild(editBar);
		}
		
		private function moveDownComponent(event:Event):void {
			var comp:CMDComponent = component;
			if (parentComponent.moveDownComponent(comp)) {
				var index:int = _parent.getChildIndex(this);
				if (index != _parent.numChildren - 1) {
					_parent.removeChild(this);
					_parent.addChildAt(this, index + 1);
				}
			}
		}
		
		private function moveUpComponent(event:Event):void {
			var comp:CMDComponent = component;
			if (parentComponent.moveUpComponent(comp)) {
				var index:int = _parent.getChildIndex(this);
				if (index != 0) {
					_parent.removeChild(this);
					_parent.addChildAt(this, index - 1);
				}
			}
		}
		
		private function addFocusListeners(comp:UIComponent):UIComponent {
			comp.addEventListener(MouseEvent.MOUSE_OVER, function(event:MouseEvent):void {
				drawFocus(true);
			});
			comp.addEventListener(MouseEvent.MOUSE_OUT, function(event:MouseEvent):void {
				drawFocus(false);
			});
			return comp;
		}
		
		private function createComponentLink(component:CMDComponent):FormItem {
			if (component.componentId != "" && component.componentId != null) {
				var result:FormItem = new FormItem();
				result.styleName = StyleConstants.XMLBROWSER_FIELD;
				result.label = LabelConstants.COMPONENT_ID;
				result.addChild(new ExpandingComponentLabel(component.componentId, false));
				return result;
			}
			return null;
		}
		
		private function handleCMDAttributeList():void {
			addChild(new AttributeListEdit(_component.attributeList, this));
		}
		
		private function handleComponents(components:ArrayCollection):void {
			for each (var component:CMDComponent in components) {
				addComponent(component);
			}
		}
		
		public function addComponent(component:CMDComponent):void {
			var comp:Container = new ComponentEdit(component, this, _component);
			comp.addEventListener(ComponentEdit.REMOVE_COMPONENT_EVENT, removeComponent);
			comp.setStyle("paddingLeft", "50");
			if (!addComponentLabel) {
				addChild(comp);
			} else {
				addChildAt(comp, getChildIndex(addComponentLabel));
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
		
		public function addElement(element:CMDComponentElement):void {
			var elem:Container = new ElementEdit(element, this, _component);
			elem.setStyle("paddingLeft", "50");
			elem.addEventListener(ElementEdit.REMOVE_ELEMENT_EVENT, removeElement);
			if (!addElementLabel) {
				addChild(elem);
			} else {
				addChildAt(elem, getChildIndex(addElementLabel));
			}
		}
		
		private function removeElement(event:Event):void {
			var elem:CMDComponentElement = ElementEdit(event.currentTarget).element;
			_component.removeElement(elem);
			removeChild(event.currentTarget as DisplayObject);
		}
		
		private function createHeading():FormItem {
			var heading:FormItem = new FormItem();
			heading.label = LabelConstants.COMPONENT;
			heading.styleName = StyleConstants.XMLBROWSER_HEADER;
			return heading;
		}
		
		
	}
}
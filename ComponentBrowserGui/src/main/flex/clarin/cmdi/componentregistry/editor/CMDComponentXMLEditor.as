package clarin.cmdi.componentregistry.editor {
	import clarin.cmdi.componentregistry.browser.XMLBrowser;
	import clarin.cmdi.componentregistry.common.ItemDescription;
	import clarin.cmdi.componentregistry.common.StyleConstants;
	import clarin.cmdi.componentregistry.editor.model.CMDComponent;
	import clarin.cmdi.componentregistry.editor.model.CMDSpec;

	import flash.display.DisplayObject;
	import flash.events.Event;
	import flash.utils.getTimer;

	import mx.binding.utils.BindingUtils;
	import mx.collections.ArrayCollection;
	import mx.containers.Form;
	import mx.containers.FormItem;
	import mx.core.Container;
	import mx.core.UIComponent;
	import mx.events.ChildExistenceChangedEvent;
	import mx.events.DragEvent;
	import mx.managers.DragManager;
	import mx.managers.IFocusManagerComponent;

	[Event(name="editorChange", type="flash.events.Event")]
	public class CMDComponentXMLEditor extends Form implements IFocusManagerComponent, CMDSpecRenderer {

		public static const DRAG_NEW_COMPONENT:String = "newComponent";
		public static const DRAG_NEW_ELEMENT:String = "newElement";
		public static const DRAG_NEW_ATTRIBUTE:String = "newAttribute";

		public static const EDITOR_CHANGE_EVENT:String = "editorChange";
		private var _spec:CMDSpec;

		public function CMDComponentXMLEditor() {
			super();
			focusEnabled = true;
			tabEnabled = true;
			styleName = StyleConstants.XMLBROWSER;
			addEventListener(DragEvent.DRAG_ENTER, dragEnterHandler);
			addEventListener(DragEvent.DRAG_OVER, dragOverHandler);
			addEventListener(DragEvent.DRAG_DROP, dragDropHandler);
			addEventListener(ChildExistenceChangedEvent.CHILD_ADD, dispatchEditorChangeEvent);
			addEventListener(ChildExistenceChangedEvent.CHILD_REMOVE, dispatchEditorChangeEvent);
		}

		private function dragEnterHandler(event:DragEvent):void {
			DragManager.acceptDragDrop(event.currentTarget as UIComponent);
			UIComponent(event.currentTarget).drawFocus(true);
		}

		private function dragOverHandler(event:DragEvent):void {
			if (event.dragSource.hasFormat("items")) {
				DragManager.showFeedback(DragManager.COPY);
			} else if (event.dragSource.hasFormat(DRAG_NEW_COMPONENT)) {
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
					_spec.cmdComponents.addItem(comp);
					addComponent(comp);
				}
			} else if (event.dragSource.hasFormat(DRAG_NEW_COMPONENT)) {
				var emptyComp:CMDComponent = event.dragSource.dataForFormat(DRAG_NEW_COMPONENT) as CMDComponent;
				_spec.cmdComponents.addItem(emptyComp);
				addComponent(emptyComp);
			}
		}

		public function set cmdSpec(cmdSpec:CMDSpec):void {
			_spec = cmdSpec;
			createNewEditor();
			dispatchEditorChangeEvent();
		}

		private function dispatchEditorChangeEvent(event:Event = null):void {
			dispatchEvent(new Event(EDITOR_CHANGE_EVENT));
		}

		[Bindable]
		public function get cmdSpec():CMDSpec {
			return _spec;
		}

		private function createNewEditor():void {
			var start:int = getTimer();
			removeAllChildren()
			handleHeader(_spec);
			handleComponents(_spec.cmdComponents);
			trace("Created editor view in " + (getTimer() - start) + " ms.");
		}

		private function handleHeader(spec:CMDSpec):void {
			addChild(new SelectTypeRadioButtons(spec));
			addChild(createOptionalGroupNameInput(spec));
			addChild(createHeading());
			addChild(new FormItemInputLine("Name", spec.headerName, function(val:String):void {
					spec.headerName = val;
				}));
			addChild(new FormItemInputText(XMLBrowser.DESCRIPTION, spec.headerDescription, function(val:String):void {
					spec.headerDescription = val;
				}));
			var idInput:FormItemInputLine = new FormItemInputLine("Id", spec.headerId, function(val:String):void {
					spec.headerId = val;
				}, false);
			idInput.toolTip = "Id will be generated";
			addChild(idInput);
		}

		private function createOptionalGroupNameInput(spec:CMDSpec):FormItem {
			var result:FormItem = new FormItemInputLine("Group Name", spec.groupName, function(val:String):void {
					spec.groupName = val;
				})
			BindingUtils.bindSetter(function(val:Boolean):void {
					result.visible = !val;
				}, spec, "isProfile");
			return result;
		}

		private function createHeading():FormItem {
			var heading:FormItem = new FormItem();
			heading.label = "Header";
			heading.styleName = StyleConstants.XMLBROWSER_HEADER;
			return heading;
		}

		private function handleComponents(components:ArrayCollection):void {
			for each (var component:CMDComponent in components) {
				addComponent(component);
			}
		}

		public function addComponent(component:CMDComponent, index:int = -1):void {
			var comp:Container = new ComponentEdit(component, this);
			comp.addEventListener(ComponentEdit.REMOVE_COMPONENT_EVENT, removeComponent);
			if (index == -1) {
				addChild(comp);
			} else {
				addChildAt(comp, index);
			}
		}

		private function removeComponent(event:Event):void {
			var comp:CMDComponent = ComponentEdit(event.currentTarget).component;
			_spec.removeComponent(comp);
			removeChild(event.currentTarget as DisplayObject);
		}

	}

}
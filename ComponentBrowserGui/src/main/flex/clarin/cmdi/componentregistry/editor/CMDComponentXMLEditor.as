package clarin.cmdi.componentregistry.editor {
	import clarin.cmdi.componentregistry.common.CMDSpecRenderer;
	import clarin.cmdi.componentregistry.common.ItemDescription;
	import clarin.cmdi.componentregistry.common.LabelConstants;
	import clarin.cmdi.componentregistry.common.StyleConstants;
	import clarin.cmdi.componentregistry.editor.model.CMDComponent;
	import clarin.cmdi.componentregistry.editor.model.CMDComponentElement;
	import clarin.cmdi.componentregistry.editor.model.CMDSpec;

	import flash.display.DisplayObject;
	import flash.events.Event;
	import flash.events.MouseEvent;
	import flash.utils.getTimer;

	import mx.binding.utils.BindingUtils;
	import mx.collections.ArrayCollection;
	import mx.containers.Form;
	import mx.containers.FormItem;
	import mx.controls.Label;
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
		private static const DRAG_ITEMS:String = "items";

		public static const EDITOR_CHANGE_EVENT:String = "editorChange";

		[ArrayElementType("CMDValidator")]
		public static var validators:ArrayCollection = new ArrayCollection();
		private var _spec:CMDSpec;
		private var _firstComponent:CMDComponent;
		private var currentElementIndex:int = -1;


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

		public function validate():Boolean {
			var result:Boolean = true;
			for each (var validator:CMDValidator in validators) {
				result = validator.validate() && result;
			}
			return result;
		}

		private function dragEnterHandler(event:DragEvent):void {
			DragManager.acceptDragDrop(event.currentTarget as UIComponent);
			UIComponent(event.currentTarget).drawFocus(true);
		}


		private function dragOverHandler(event:DragEvent):void {
			if (event.dragSource.hasFormat(DRAG_ITEMS)) {
				DragManager.showFeedback(DragManager.COPY);
			} else if (event.dragSource.hasFormat(DRAG_NEW_COMPONENT)) {
				DragManager.showFeedback(DragManager.COPY);
			} else if (event.dragSource.hasFormat(CMDComponentXMLEditor.DRAG_NEW_ELEMENT)) {
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
					_firstComponent.cmdComponents.addItem(comp);
					addComponent(comp);
				}
			} else if (event.dragSource.hasFormat(DRAG_NEW_COMPONENT)) {
				var emptyComp:CMDComponent = event.dragSource.dataForFormat(DRAG_NEW_COMPONENT) as CMDComponent;
				_firstComponent.cmdComponents.addItem(emptyComp);
				addComponent(emptyComp);
			} else if (event.dragSource.hasFormat(CMDComponentXMLEditor.DRAG_NEW_ELEMENT)) {
				var element:CMDComponentElement = event.dragSource.dataForFormat(CMDComponentXMLEditor.DRAG_NEW_ELEMENT) as CMDComponentElement;
				_firstComponent.cmdElements.addItem(element);
				addElement(element, currentElementIndex);
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
			validators = new ArrayCollection();
			this.currentElementIndex = -1;
			removeAllChildren()
			checkFirstDefiningComponent(_spec.cmdComponents);
			handleHeader(_spec);
			handleElements(_firstComponent.cmdElements);
			handleComponents(_firstComponent.cmdComponents);
			addAddButtons();
			trace("Created editor view in " + (getTimer() - start) + " ms.");
		}

		/**
		 * The xml model allows more component to be defined but we force only one component at the "root" level.
		 */
		private function checkFirstDefiningComponent(components:ArrayCollection):void {
			if (components.length != 1) {
				throw new Error("A profile/component must have 1 component defined at root level.");
			} else {
				_firstComponent = components.getItemAt(0) as CMDComponent;
			}
		}

		private function handleHeader(spec:CMDSpec):void {
			addChild(new SelectTypeRadioButtons(spec));
			addChild(createOptionalGroupNameInput(spec));
			var descriptionInput:FormItemInputText = new FormItemInputText(LabelConstants.DESCRIPTION, spec.headerDescription, function(val:String):void {
					spec.headerDescription = val;
				}, InputValidators.getIsRequiredValidator());
			addChild(descriptionInput);
			var nameInput:NameInputLine = new NameInputLine(_firstComponent.name, function(val:String):void {
					_firstComponent.name = val;
					_spec.headerName = val;
				})
			addChild(nameInput);
			addChild(new ComboBoxInputLine(LabelConstants.DOMAIN_NAME, _spec.domainName, LabelConstants.DOMAIN_NAME_DATA, LabelConstants.DOMAIN_NAME_PROMPT, function(val:Object):void {
					if (val) {
						_spec.domainName = val.data;
					}
				}));

//			var idInput:FormItemInputLine = new FormItemInputLine(XMLBrowser:"Id", spec.headerId, function(val:String):void {
//					spec.headerId = val;
//				}, false);
//			idInput.toolTip = "Id will be generated";
//			addChild(idInput);
			var link:ConceptLinkInput = new ConceptLinkInput(LabelConstants.CONCEPTLINK, _firstComponent.conceptLink, function(val:String):void {
					_firstComponent.conceptLink = val;
				});
			link.enabled = false;
			addChild(link);
		}

		private function addAddButtons():void {
			var addLabel:Label = new Label();
			addLabel.text = "+component";
			addLabel.toolTip = "click to add Component";
			addLabel.setStyle("color", "green");
			addLabel.addEventListener(MouseEvent.CLICK, function(event:MouseEvent):void {
					var comp:CMDComponent = new CMDComponent();
					_spec.cmdComponents.addItem(comp);
					addComponent(comp, currentElementIndex);

				});
			addLabel.addEventListener(MouseEvent.MOUSE_OVER, function(event:MouseEvent):void {
					drawFocus(true);
					event.currentTarget.setStyle("textDecoration", "underline");
				});
			addLabel.addEventListener(MouseEvent.MOUSE_OUT, function(event:MouseEvent):void {
					drawFocus(false);
					event.currentTarget.setStyle("textDecoration", "none");
				});
			addChild(addLabel);
		}

		private function createOptionalGroupNameInput(spec:CMDSpec):FormItem {
			var result:FormItemInputLine = new FormItemInputLine(LabelConstants.GROUP_NAME, spec.groupName, function(val:String):void {
					spec.groupName = val;
				}, true, InputValidators.getIsRequiredValidator());
			BindingUtils.bindSetter(function(val:Boolean):void {
					result.visible = !val;
				}, spec, "isProfile");
			return result;
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
			_firstComponent.removeComponent(comp);
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
			_firstComponent.removeElement(elem);
			removeChild(event.currentTarget as DisplayObject);
			currentElementIndex--;
		}

	}

}
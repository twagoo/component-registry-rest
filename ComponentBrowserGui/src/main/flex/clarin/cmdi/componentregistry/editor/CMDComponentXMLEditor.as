package clarin.cmdi.componentregistry.editor {
	import clarin.cmdi.componentregistry.common.CMDSpecRenderer;
	import clarin.cmdi.componentregistry.common.ItemDescription;
	import clarin.cmdi.componentregistry.common.LabelConstants;
	import clarin.cmdi.componentregistry.common.StyleConstants;
	import clarin.cmdi.componentregistry.common.components.AddComponentLabelButton;
	import clarin.cmdi.componentregistry.common.components.AddElementLabelButton;
	import clarin.cmdi.componentregistry.common.components.RemoveLabelButton;
	import clarin.cmdi.componentregistry.editor.model.CMDComponent;
	import clarin.cmdi.componentregistry.editor.model.CMDComponentElement;
	import clarin.cmdi.componentregistry.editor.model.CMDSpec;
	import clarin.cmdi.componentregistry.services.IsocatService;
	
	import flash.display.DisplayObject;
	import flash.events.Event;
	import flash.events.MouseEvent;
	import flash.utils.Dictionary;
	import flash.utils.getTimer;
	
	import mx.collections.ArrayCollection;
	import mx.containers.Form;
	import mx.containers.FormItem;
	import mx.containers.FormItemDirection;
	import mx.controls.Label;
	import mx.controls.Spacer;
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

		private var _spec:CMDSpec;
		private var _firstComponent:CMDComponent;
		private var addComponentLabel:Label
		private var addElementLabel:Label


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
			var componentDisplayPrioMap:Dictionary = new Dictionary(true);
			componentDisplayPrioMap[this] = new ArrayCollection();
			var result:Boolean = validateChildren(getChildren(), componentDisplayPrioMap, this);
			result = validateDisplayPriorityFields(componentDisplayPrioMap) && result;
			return result;
		}

		private function validateChildren(children:Array, componentDisplayPrioMap:Dictionary, key:Object):Boolean {
			var result:Boolean = true;
			for each (var o:Object in children) {
				if (o is CMDValidator) {
					result = CMDValidator(o).validate() && result;
				}
				if (o is Container) {
					var newKey:Object = key;
					if (o is ComponentEdit) {
						componentDisplayPrioMap[o] = new ArrayCollection();
						newKey = o;
					}
					result = validateChildren(Container(o).getChildren(), componentDisplayPrioMap, newKey) && result;
					if (o is DisplayPriorityInput) {
						componentDisplayPrioMap[newKey].addItem(o);
					}
				}
			}
			return result;
		}

		private function validateDisplayPriorityFields(componentDisplayPrioMap:Dictionary):Boolean {
			var result:Boolean = true;
			for (var key:Object in componentDisplayPrioMap) {
				var displayPrioritySet:Boolean = false;
				var displayPriorityFields:ArrayCollection = componentDisplayPrioMap[key];
				for each (var input:DisplayPriorityInput in displayPriorityFields) { // get values
					if (input.getValue() > 0) {
						displayPrioritySet = true;
						break;
					}
				}
				for each (var inputF:DisplayPriorityInput in displayPriorityFields) { // set results
					if (displayPrioritySet) {
						result = inputF.validate(input.getValue()) && result;
					} else {
						result = inputF.validate(null) && result;
					}
				}
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
			addComponentLabel = null
			addElementLabel = null
			removeAllChildren();
			checkFirstDefiningComponent(_spec.cmdComponents);
			handleHeader(_spec);
			handleElements(_firstComponent.cmdElements);
			addElementAddButton();
			handleComponents(_firstComponent.cmdComponents);
			addComponentAddButton();
			trace("Created editor view in " + (getTimer() - start) + " ms.");
		}

		private function clearEditor(event:Event):void {
			if (_spec && !_spec.isProfile) {
				cmdSpec = CMDSpec.createEmptyComponent();
			} else {
				cmdSpec = CMDSpec.createEmptyProfile();
			}
		}

		/**
		 * The xml model allows more component to be defined but we force only one component at the "root" level.
		 */
		private function checkFirstDefiningComponent(components:ArrayCollection):void {
			if (components.length != 1) {
				throw new Error("A profile/component must have 1 component defined at root level.");
			} else {
				_firstComponent = components.getItemAt(0) as CMDComponent;
				if (_firstComponent.componentId != "" && _firstComponent.componentId != null) {
					_firstComponent = new CMDComponent();
					_firstComponent.name = _spec.headerName;
					_firstComponent.cmdComponents = _spec.cmdComponents;
					_spec.cmdComponents = new ArrayCollection();
					_spec.cmdComponents.addItem(_firstComponent);
				}
			}
		}

		private function handleHeader(spec:CMDSpec):void {
			var head:FormItem = new FormItem();
			head.direction = FormItemDirection.HORIZONTAL;
			var buttons:FormItem = new SelectTypeRadioButtons(spec);
			head.addChild(buttons);
			var startOverLabel:Label = createStartOverButton();
			startOverLabel.setStyle("paddingTop", "2");
			startOverLabel.height = buttons.height;
			var space:Spacer = new Spacer();
			space.width = 55;
			head.addChild(space);
			head.addChild(startOverLabel);
			addChild(head);
			
			var nameInput:NameInputLine = new NameInputLine(_firstComponent.name, function(val:String):void {
					_firstComponent.name = val;
					_spec.headerName = val;
				}, InputValidators.getNameValidator());
			addChild(nameInput);
			
			var groupNameInput:FormItemInputLine = new FormItemInputLine(LabelConstants.GROUP_NAME, spec.groupName, function(val:String):void {
					spec.groupName = val;
				}); // editable, not required
			addChild(groupNameInput);
			
			var descriptionInput:FormItemInputText = new FormItemInputText(LabelConstants.DESCRIPTION, spec.headerDescription, function(val:String):void {
					spec.headerDescription = val;
				}, InputValidators.getIsRequiredValidator()); //editable, required
			addChild(descriptionInput);
			
			var domainInput:ComboBoxInputLine = new ComboBoxInputLine(LabelConstants.DOMAIN_NAME, _spec.domainName, LabelConstants.DOMAIN_NAME_DATA, LabelConstants.DOMAIN_NAME_PROMPT, function(val:Object):void {
					if (val) {
						_spec.domainName = val.data;
					}
				}); // editable, not required
			addChild(domainInput);

//			var idInput:FormItemInputLine = new FormItemInputLine(XMLBrowser:"Id", spec.headerId, function(val:String):void {
//					spec.headerId = val;
//				}, false);
//			idInput.toolTip = "Id will be generated";
//			addChild(idInput);
			var link:ConceptLinkInput = new ConceptLinkInput(LabelConstants.CONCEPTLINK, _firstComponent.conceptLink, function(val:String):void {
					_firstComponent.conceptLink = val;
				}, IsocatService.TYPE_CONTAINER);
			addChild(link);
			
			addChild(new AttributeListEdit(_firstComponent.attributeList, this))
		}

		private function addComponentAddButton():void {
			addComponentLabel = new AddComponentLabelButton();
			addComponentLabel.addEventListener(MouseEvent.CLICK, function(event:MouseEvent):void {
					var comp:CMDComponent = CMDComponent.createEmptyComponent();
					_firstComponent.cmdComponents.addItem(comp);
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
					_firstComponent.cmdElements.addItem(element);
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

		/* private function createOptionalGroupNameInput(spec:CMDSpec):FormItem {
		   var result:FormItemInputLine = new FormItemInputLine(LabelConstants.GROUP_NAME, spec.groupName, function(val:String):void {
		   spec.groupName = val;
		   }, true, InputValidators.getIsRequiredValidator());
		   BindingUtils.bindSetter(function(val:Boolean):void {
		   result.visible = !val;
		   }, spec, "isProfile");
		   return result;
		 }  */

		private function handleComponents(components:ArrayCollection):void {
			for each (var component:CMDComponent in components) {
				addComponent(component);
			}
		}

		public function addComponent(component:CMDComponent):void {
			var comp:Container = new ComponentEdit(component, this, _firstComponent);
			comp.addEventListener(ComponentEdit.REMOVE_COMPONENT_EVENT, removeComponent);
			if (!addComponentLabel) {
				addChild(comp);
			} else {
				addChildAt(comp, getChildIndex(addComponentLabel)); //Add components at the place of the button so button moves down
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

		public function addElement(element:CMDComponentElement):void {
			var elem:Container = new ElementEdit(element, this, _firstComponent);
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
			_firstComponent.removeElement(elem);
			removeChild(event.currentTarget as DisplayObject);
		}

		private function createStartOverButton():Label {
			var startOverButton:Label = new RemoveLabelButton();
			startOverButton.addEventListener(MouseEvent.CLICK, clearEditor);
			startOverButton.toolTip = "Clears all input and removes added components";
			startOverButton.text = "start over";
			return startOverButton;
		}
	}
}
package clarin.cmdi.componentregistry.editor {
	import clarin.cmdi.componentregistry.common.LabelConstants;
	import clarin.cmdi.componentregistry.common.StyleConstants;
	import clarin.cmdi.componentregistry.common.components.DownIconButton;
	import clarin.cmdi.componentregistry.common.components.RemoveLabelButton;
	import clarin.cmdi.componentregistry.common.components.UpIconButton;
	import clarin.cmdi.componentregistry.editor.model.CMDComponent;
	import clarin.cmdi.componentregistry.editor.model.CMDComponentElement;
	
	import flash.events.Event;
	import flash.events.MouseEvent;
	
	import mx.binding.utils.BindingUtils;
	import mx.containers.Box;
	import mx.containers.Form;
	import mx.containers.FormItem;
	import mx.containers.HBox;
	import mx.containers.VBox;
	import mx.controls.Button;
	import mx.controls.Label;
	import mx.core.Container;
	import mx.core.UIComponent;

	[Event(name="removeElement", type="flash.events.Event")]
	public class ElementEdit extends Form {
		public static const REMOVE_ELEMENT_EVENT:String = "removeElement";
		private var _element:CMDComponentElement;
		private var _parentComponent:CMDComponent;
		private var _parent:UIComponent;
		private var showToggleBox:ShowToggleBox;
		private var hideableForm:Form;

		public function ElementEdit(element:CMDComponentElement, parent:UIComponent, parentComponent:CMDComponent) {
			super();
			this._element = element;
			this._parentComponent = parentComponent;
			this._parent = parent;
			styleName = StyleConstants.XMLBROWSER;
			
			setStyle("paddingBottom","5");
			verticalScrollPolicy = "off";
			horizontalScrollPolicy = "off";
			
		}

		public function get element():CMDComponentElement {
			return _element;
		}

		public function get parentComponent():CMDComponent {
			return _parentComponent;
		}

		protected override function createChildren():void {
			super.createChildren();
						
			addChild(createEditBar());
			
			hideableForm = createHidableForm();			
			showToggleBox.visibleContainer = hideableForm;
			
			addChild(hideableForm);
			
			var summary:ElementSummary = new ElementSummary();
			summary.element = _element;
			summary.visible = false;
			showToggleBox.invisibleContainer = summary;

			addChild(summary);
			
			hideableForm.addChild(createNameInput());
			hideableForm.addChild(new ConceptLinkInput(LabelConstants.CONCEPTLINK, _element.conceptLink, function(val:String):void {
					_element.conceptLink = val;
				}));
			hideableForm.addChild(new FormItemInputLine(LabelConstants.DOCUMENTATION, _element.documentation, function(val:String):void {
					_element.documentation = val;
				}));
			hideableForm.addChild(new DisplayPriorityInput(LabelConstants.DISPLAY_PRIORITY, _element.displayPriority, function(val:String):void {
					_element.displayPriority = val;
				}));
			
			var cardinalityMinInput:CardinalityInput = new CardinalityInput(LabelConstants.CARDINALITY_MIN, _element.cardinalityMin, CardinalityInput.BOUNDED, function(val:String):void {
				_element.cardinalityMin = val;
			});
			hideableForm.addChild(cardinalityMinInput);
			
			var cardinalityMaxInput:CardinalityInput = new CardinalityInput(LabelConstants.CARDINALITY_MAX, _element.cardinalityMax, CardinalityInput.UNBOUNDED, function(val:String):void {
				_element.cardinalityMax = val;
			});
			// Max cardinatlity field should be disabled when multilingual is selected - binding to that field here
			BindingUtils.bindSetter(function(value:String):void {
				cardinalityMaxInput.enabled = _element.multilingual != "true";
			}, _element, "multilingual");
			hideableForm.addChild(cardinalityMaxInput);
			
			hideableForm.addChild(AttributeListEdit.createAndAddValueScheme(_element));
			var multiLingualCheck:CheckboxInput = new CheckboxInput(LabelConstants.MULTILINGUAL, _element.multilingual == "true", function(val:Boolean):void {
					_element.multilingual = String(val);
				});
			multiLingualCheck.toolTip = "Can the value of this element be in multiple languages? Setting this will cause \"Max occurences\" to be always unbounded.";
			BindingUtils.bindSetter(function(val:String):void {
					var show:Boolean = "string" == val;
					multiLingualCheck.visible = show;
					multiLingualCheck.includeInLayout = show;
					if (!show) {
						_element.multilingual = null;
					}
				}, _element, "valueSchemeSimple");
			
			hideableForm.addChild(multiLingualCheck);
			handleCMDAttributeList();
		}
		
		private function createHidableForm():Form {
			var form:Form = new Form();
			form.styleName = StyleConstants.XMLBROWSER;
			form.setStyle("paddingTop","0");
			form.setStyle("paddingBottom","0");
			form.verticalScrollPolicy = "off";
			form.horizontalScrollPolicy = "off";
			return form;
		}

		private function createEditBar():HBox {
			var editBar:HBox = new HBox();
			
			showToggleBox = new ShowToggleBox();
			showToggleBox.visibleState = true;
			editBar.addChild(showToggleBox);
			
			editBar.addChild(createHeading());
			var removeButton:Label = new RemoveLabelButton();
			addFocusListeners(removeButton).addEventListener(MouseEvent.CLICK, fireRemoveComponent);
			editBar.addChild(removeButton);

			var downButton:Button = new DownIconButton();
			addFocusListeners(downButton).addEventListener(MouseEvent.CLICK, moveDownElement);
			editBar.addChild(downButton);

			var upButton:Button = new UpIconButton();
			addFocusListeners(upButton).addEventListener(MouseEvent.CLICK, moveUpElement);
			editBar.addChild(upButton);
			return editBar;
		}

		private function moveDownElement(event:Event):void {
			var elem:CMDComponentElement = element;
			if (parentComponent.moveDownElement(elem)) {
				var index:int = _parent.getChildIndex(this);
				if (index != numChildren - 1) {
					_parent.removeChild(this);
					_parent.addChildAt(this, index + 1);
				}
			}
		}

		private function moveUpElement(event:Event):void {
			var elem:CMDComponentElement = element;
			if (parentComponent.moveUpElement(elem)) {
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

		private function fireRemoveComponent(mouseEvent:MouseEvent):void {
			drawFocus(false);
			var event:Event = new Event(REMOVE_ELEMENT_EVENT);
			dispatchEvent(event);
		}

		private function handleCMDAttributeList():void {
			var attributeEdit:Container = new AttributeListEdit(_element, this);
			hideableForm.addChild(attributeEdit);
		}

		private function createHeading():FormItem {
			var heading:FormItem = new FormItem();
			heading.label = LabelConstants.ELEMENT;
			heading.styleName = StyleConstants.XMLBROWSER_HEADER;
			return heading;
		}

		private function createNameInput():NameInputLine {
			var nameInput:NameInputLine = new NameInputLine(_element.name, function(val:String):void {
					_element.name = val;
			}, new ChildNameValidator(_parentComponent, element));
			return nameInput;
		}
	}
}
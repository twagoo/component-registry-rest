package clarin.cmdi.componentregistry.editor {
	import clarin.cmdi.componentregistry.common.LabelConstants;
	import clarin.cmdi.componentregistry.common.StyleConstants;
	import clarin.cmdi.componentregistry.common.components.RemoveLabelButton;
	import clarin.cmdi.componentregistry.editor.model.CMDComponentElement;
	
	import flash.events.Event;
	import flash.events.MouseEvent;
	
	import mx.binding.utils.BindingUtils;
	import mx.containers.Form;
	import mx.containers.FormItem;
	import mx.containers.HBox;
	import mx.controls.Label;
	import mx.core.Container;
	import mx.core.UIComponent;

	[Event(name="removeElement", type="flash.events.Event")]
	public class ElementEdit extends Form {
		public static const REMOVE_ELEMENT_EVENT:String = "removeElement";
		private var _element:CMDComponentElement;
		private var _parent:UIComponent;

		public function ElementEdit(element:CMDComponentElement, parent:UIComponent) {
			super();
			this._element = element;
			this._parent = parent;
			styleName = StyleConstants.XMLBROWSER;
		}

		public function get element():CMDComponentElement {
			return _element;
		}

		protected override function createChildren():void {
			super.createChildren();
			addChild(createEditBar());
			addNameInput();
			addChild(new ConceptLinkInput(LabelConstants.CONCEPTLINK, _element.conceptLink, function(val:String):void {
					_element.conceptLink = val;
				}));
			addChild(new FormItemInputLine(LabelConstants.DOCUMENTATION, _element.documentation, function(val:String):void {
					_element.documentation = val;
				}));
			addChild(new DisplayPriorityInput(LabelConstants.DISPLAY_PRIORITY, _element.displayPriority, function(val:String):void {
					_element.displayPriority = val;
				}));
			addChild(new CardinalityInput(LabelConstants.CARDINALITY_MIN, _element.cardinalityMin, function(val:String):void {
					_element.cardinalityMin = val;
				}));
			addChild(new CardinalityInput(LabelConstants.CARDINALITY_MAX, _element.cardinalityMax, function(val:String):void {
					_element.cardinalityMax = val;
				}));
			addChild(AttributeListEdit.createAndAddValueScheme(_element));
			var multiLingualCheck:CheckboxInput = new CheckboxInput(LabelConstants.MULTILINGUAL, _element.multilingual == "true", function(val:Boolean):void {
					_element.multilingual = String(val);
				});
			BindingUtils.bindSetter(function(val:String):void {
					var show:Boolean = "string" == val;
					multiLingualCheck.visible = show;
					multiLingualCheck.includeInLayout = show;
					if (!show) {
					    _element.multilingual = null;
					}
				}, _element, "valueSchemeSimple");

			addChild(multiLingualCheck);
			handleCMDAttributeList();
		}

		private function createEditBar():HBox {
			var editBar:HBox = new HBox();
			editBar.addChild(createHeading());
			var removeButton:Label = new RemoveLabelButton();
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

		private function fireRemoveComponent(mouseEvent:MouseEvent):void {
			drawFocus(false);
			var event:Event = new Event(REMOVE_ELEMENT_EVENT);
			dispatchEvent(event);
		}

		private function handleCMDAttributeList():void {
			var attributeEdit:Container = new AttributeListEdit(_element.attributeList, this);
			addChild(attributeEdit);
		}

		private function createHeading():FormItem {
			var heading:FormItem = new FormItem();
			heading.label = LabelConstants.ELEMENT;
			heading.styleName = StyleConstants.XMLBROWSER_HEADER;
			return heading;
		}

		private function addNameInput():void {
			var nameInput:NameInputLine = new NameInputLine(_element.name, function(val:String):void {
					_element.name = val;
				})
			addChild(nameInput);
		}

	}
}
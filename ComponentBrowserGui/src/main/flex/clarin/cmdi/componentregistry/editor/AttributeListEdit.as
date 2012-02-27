package clarin.cmdi.componentregistry.editor {
	import clarin.cmdi.componentregistry.common.LabelConstants;
	import clarin.cmdi.componentregistry.common.StyleConstants;
	import clarin.cmdi.componentregistry.common.components.AddAttributeLabelButton;
	import clarin.cmdi.componentregistry.common.components.RemoveLabelButton;
	import clarin.cmdi.componentregistry.editor.model.CMDAttribute;
	import clarin.cmdi.componentregistry.editor.model.ValueSchemeInterface;
	
	import flash.events.Event;
	import flash.events.MouseEvent;
	
	import mx.binding.utils.BindingUtils;
	import mx.binding.utils.ChangeWatcher;
	import mx.collections.ArrayCollection;
	import mx.containers.Form;
	import mx.containers.FormItem;
	import mx.containers.FormItemDirection;
	import mx.controls.Label;
	import mx.core.UIComponent;
	import mx.events.PropertyChangeEvent;

	[Event(name="removeAttribute", type="flash.events.Event")]
	public class AttributeListEdit extends Form {
		private static const ATTRIBUTE_CHANGE_EVENT:String = "attributeChange";

		private var _attributes:ArrayCollection;
		private var _parent:UIComponent;
		private var noAttributesLabel:Label = new Label();
		[Bindable]
		public var hasNoAttributes:Boolean = true;
		private var addAttributeLabelButton:AddAttributeLabelButton;

		public function AttributeListEdit(attributes:ArrayCollection, parent:UIComponent) {
			super();
			_attributes = attributes;
			hasNoAttributes = _attributes.length == 0;
			_parent = parent;
			styleName = StyleConstants.XMLBROWSER;
			noAttributesLabel.text = "No Attributes";
			BindingUtils.bindProperty(noAttributesLabel, "visible", this, "hasNoAttributes");
			noAttributesLabel.styleName = StyleConstants.XMLBROWSER_FIELD_VALUE;
		}


		protected override function createChildren():void {
			super.createChildren();
			addChild(createHeading());
			if (_attributes.length > 0) {
				for each (var attribute:CMDAttribute in _attributes) {
					addAttribute(createAttributeBox(attribute));
				}
			}
			addAttributeButton();
		}

		private function addAttributeButton():void {
			addAttributeLabelButton = new AddAttributeLabelButton();
			addAttributeLabelButton.addEventListener(MouseEvent.CLICK, function(event:MouseEvent):void {
					var attr:CMDAttribute = CMDAttribute.createEmptyAttribute();
					_attributes.addItem(attr);
					hasNoAttributes = _attributes.length == 0;
					addAttribute(createAttributeBox(attr));
				});
			addAttributeLabelButton.addEventListener(MouseEvent.MOUSE_OVER, function(event:MouseEvent):void {
					drawFocus(true);
				});
			addAttributeLabelButton.addEventListener(MouseEvent.MOUSE_OUT, function(event:MouseEvent):void {
					drawFocus(false);
				});
			addChild(addAttributeLabelButton);
		}

		private function createAttributeBox(attribute:CMDAttribute):Form {
			var attributeBox:Form = new Form();
			attributeBox.styleName = StyleConstants.XMLBROWSER;
			addEditBar(attribute, attributeBox);
			attributeBox.addChild(createAndAddValueScheme(attribute));
			
			attributeBox.addChild(new ConceptLinkInput(LabelConstants.CONCEPTLINK, attribute.conceptLink, function(val:String):void {
				attribute.conceptLink = val;
			}));
			
			return attributeBox;
		}

		private function addAttribute(attributeBox:Form):void {
			if (!addAttributeLabelButton) {
				addChild(attributeBox);
			} else {
				addChildAt(attributeBox, getChildIndex(addAttributeLabelButton));
			}

		}

		/**
		 * Public utility method to create ValueScheme Component. By lack of better placed put in this class
		 **/
		public static function createAndAddValueScheme(valueScheme:ValueSchemeInterface):UIComponent {
			var valueSchemeInput:ValueSchemeInput = new ValueSchemeInput(LabelConstants.VALUESCHEME);
			if (valueScheme.valueSchemeEnumeration == null) {
				if (valueScheme.valueSchemePattern) {
					valueSchemeInput.valueSchemePattern = valueScheme.valueSchemePattern;
				} else {
					valueSchemeInput.valueSchemeSimple = valueScheme.valueSchemeSimple;
				}
			} else {
				valueSchemeInput.valueSchemeEnumeration = valueScheme.valueSchemeEnumeration;
			}
			ChangeWatcher.watch(valueSchemeInput, "valueSchemeSimple", function(e:PropertyChangeEvent):void {
					valueScheme.valueSchemeSimple = e.newValue as String;
					valueScheme.valueSchemePattern = "";
					valueScheme.valueSchemeEnumeration = null;
				});
			ChangeWatcher.watch(valueSchemeInput, "valueSchemePattern", function(e:PropertyChangeEvent):void {
					valueScheme.valueSchemePattern = e.newValue as String;
					valueScheme.valueSchemeEnumeration = null;
					valueScheme.valueSchemeSimple = "";
				});
			ChangeWatcher.watch(valueSchemeInput, "valueSchemeEnumeration", function(e:PropertyChangeEvent):void {
					valueScheme.valueSchemeEnumeration = e.newValue as ArrayCollection;
					valueScheme.valueSchemeSimple = "";
					valueScheme.valueSchemePattern = "";
				});
			return valueSchemeInput;
		}

		private function addEditBar(attribute:CMDAttribute, attributeBox:Form):void {
			var name:NameInputLine = new NameInputLine(attribute.name, function(val:String):void {
					attribute.name = val;
				}, InputValidators.getNameValidator());
			name.direction = FormItemDirection.HORIZONTAL
			var removeButton:Label = new RemoveLabelButton();
			removeButton.addEventListener(MouseEvent.CLICK, function(event:MouseEvent):void {
					attributeBox.drawFocus(false);
					removeAttribute(attribute);
					removeChild(attributeBox);
				});
			removeButton.addEventListener(MouseEvent.MOUSE_OVER, function(event:MouseEvent):void {
					attributeBox.drawFocus(true);
				});
			removeButton.addEventListener(MouseEvent.MOUSE_OUT, function(event:MouseEvent):void {
					attributeBox.drawFocus(false);
				});
			attributeBox.addChild(name);
			name.addChild(removeButton);
		}

		private function removeAttribute(attribute:CMDAttribute):void {
			var index:int = _attributes.getItemIndex(attribute);
			if (index != -1) {
				_attributes.removeItemAt(index);
			}
			hasNoAttributes = _attributes.length == 0;
		}

		private function createHeading():FormItem {
			var heading:FormItem = new FormItem();
			heading.label = LabelConstants.ATTRIBUTELIST;
			heading.styleName = StyleConstants.XMLBROWSER_HEADER;
			heading.addChild(noAttributesLabel);
			return heading;
		}

	}
}
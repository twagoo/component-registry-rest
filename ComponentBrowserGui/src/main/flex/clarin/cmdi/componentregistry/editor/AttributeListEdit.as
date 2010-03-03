package clarin.cmdi.componentregistry.editor {
	import clarin.cmdi.componentregistry.common.StyleConstants;
	import clarin.cmdi.componentregistry.editor.model.CMDAttribute;

	import flash.display.DisplayObject;
	import flash.events.Event;
	import flash.events.IEventDispatcher;
	import flash.events.MouseEvent;

	import mx.collections.ArrayCollection;
	import mx.containers.Form;
	import mx.containers.FormItem;
	import mx.containers.FormItemDirection;
	import mx.controls.Button;
	import mx.core.UIComponent;

	[Event(name="removeAttribute", type="flash.events.Event")]
	public class AttributeListEdit extends Form {
		public static const REMOVE_ATTRIBUTE_EVENT:String = "removeAttribute";

		private var _attributes:ArrayCollection;
		private var _parent:UIComponent;

		public function AttributeListEdit(attributes:ArrayCollection, parent:UIComponent) {
			super();
			_attributes = attributes;
			_parent = parent;
			styleName = StyleConstants.XMLBROWSER;
		}

		protected override function createChildren():void {
			super.createChildren();
			addChild(createHeading());
			if (_attributes.length > 0) {
				for each (var attribute:CMDAttribute in _attributes) {
					addChild(createAttributeBox(attribute));
				}
			}
			var btn:Button = new Button();
			btn.label = "add Attribute";
			btn.addEventListener(MouseEvent.CLICK, handleAddAttributeClick);
			btn.addEventListener(MouseEvent.MOUSE_OVER, function(event:MouseEvent):void {
					drawFocus(true);
				});
			btn.addEventListener(MouseEvent.MOUSE_OUT, function(event:MouseEvent):void {
					drawFocus(false);
				});
			addChild(btn);
		}

		private function handleAddAttributeClick(event:MouseEvent):void {
			var attr:CMDAttribute = new CMDAttribute();
			_attributes.addItem(attr);
			var index:int = getChildIndex(event.currentTarget as DisplayObject);
			if (index == -1) {
				addChild(createAttributeBox(attr));
			} else {
				addChildAt(createAttributeBox(attr), index);
			}
		}

		private function createAttributeBox(attribute:CMDAttribute):Form {
			var attributeBox:Form = new Form();
			attributeBox.styleName = StyleConstants.XMLBROWSER;
			addEditBar(attribute, attributeBox);
			if (attribute.type != null) {
				attributeBox.addChild(new FormItemInputLine("Type", attribute.type, function(val:String):void {
						attribute.type = val;
					}));
			} else {
				attributeBox.addChild(createAndAddValueScheme(attribute));
			}
			return attributeBox;
		}

		private function createAndAddValueScheme(attribute:CMDAttribute):UIComponent {
			if (attribute.valueSchemeComplex == null) {
				return new FormItemInputLine("ValueScheme", attribute.valueSchemeSimple, function(val:String):void {
						attribute.valueSchemeSimple = val;
					});
			} else {
				return new EnumerationEdit(attribute.valueSchemeComplex, this);
			}
		}

		private function addEditBar(attribute:CMDAttribute, attributeBox:Form):void {
			var name:FormItemInputLine = new FormItemInputLine("Name", attribute.name, function(val:String):void {
					attribute.name = val;
				});
			name.direction = FormItemDirection.HORIZONTAL
			var removeButton:Button = new Button();
			removeButton.height = 20;
			removeButton.label = "X";
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
		}

		private function createHeading():FormItem {
			var heading:FormItem = new FormItem();
			heading.label = "AttributeList";
			heading.styleName = StyleConstants.XMLBROWSER_HEADER_SMALL;
			return heading;
		}

	}
}
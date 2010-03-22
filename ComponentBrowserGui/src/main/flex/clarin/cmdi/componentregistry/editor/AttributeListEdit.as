package clarin.cmdi.componentregistry.editor {
	import clarin.cmdi.componentregistry.common.StyleConstants;
	import clarin.cmdi.componentregistry.editor.model.CMDAttribute;
	import clarin.cmdi.componentregistry.editor.model.ValueSchemeInterface;

	import flash.events.Event;
	import flash.events.MouseEvent;

	import mx.binding.utils.ChangeWatcher;
	import mx.collections.ArrayCollection;
	import mx.collections.XMLListCollection;
	import mx.containers.Form;
	import mx.containers.FormItem;
	import mx.containers.FormItemDirection;
	import mx.controls.Button;
	import mx.core.UIComponent;
	import mx.events.DragEvent;
	import mx.managers.DragManager;

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
			addEventListener(DragEvent.DRAG_ENTER, dragEnterHandler);
			addEventListener(DragEvent.DRAG_OVER, dragOverHandler);
			addEventListener(DragEvent.DRAG_DROP, dragDropHandler);

		}

		protected override function createChildren():void {
			super.createChildren();
			addChild(createHeading());
			if (_attributes.length > 0) {
				for each (var attribute:CMDAttribute in _attributes) {
					addChild(createAttributeBox(attribute));
				}
			}
		}

		private function createAttributeBox(attribute:CMDAttribute):Form {
			var attributeBox:Form = new Form();
			attributeBox.styleName = StyleConstants.XMLBROWSER;
			addEditBar(attribute, attributeBox);
			attributeBox.addChild(createAndAddValueScheme(attribute));
			return attributeBox;
		}

		/**
		 * Public utility method to create ValueScheme Component. By lack of better placed put in this class
		 **/
		public static function createAndAddValueScheme(valueScheme:ValueSchemeInterface):UIComponent {
			var valueSchemeInput:ValueSchemeInput = new ValueSchemeInput("Type");
			if (valueScheme.valueSchemeEnumeration == null) {
				if (valueScheme.valueSchemePattern) {
					valueSchemeInput.valueSchemePattern = valueScheme.valueSchemePattern;
				} else {
					valueSchemeInput.valueSchemeSimple = valueScheme.valueSchemeSimple;
				}
			} else {
				valueSchemeInput.valueSchemeEnumeration = valueScheme.valueSchemeEnumeration;
			}
			ChangeWatcher.watch(valueSchemeInput, "valueSchemeSimple", function(val:String):void {
					valueScheme.valueSchemeSimple = val;
					valueScheme.valueSchemePattern = "";
					valueScheme.valueSchemeEnumeration = null;
				});
			ChangeWatcher.watch(valueSchemeInput, "valueSchemePattern", function(val:String):void {
					valueScheme.valueSchemePattern = val;
					valueScheme.valueSchemeEnumeration = null;
					valueScheme.valueSchemeSimple = "";
				});
			ChangeWatcher.watch(valueSchemeInput, "valueSchemeEnumeration", function(val:XMLListCollection):void {
					valueScheme.valueSchemeEnumeration = val;
					valueScheme.valueSchemeSimple = "";
					valueScheme.valueSchemePattern = "";
				});
			return valueSchemeInput;
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

		private function dragEnterHandler(event:DragEvent):void {
			DragManager.acceptDragDrop(event.currentTarget as UIComponent);
			UIComponent(event.currentTarget).drawFocus(true);
		}

		private function dragOverHandler(event:DragEvent):void {
			if (event.dragSource.hasFormat(CMDComponentXMLEditor.DRAG_NEW_ATTRIBUTE)) {
				DragManager.showFeedback(DragManager.COPY);
			} else {
				DragManager.showFeedback(DragManager.NONE);
			}
		}

		private function dragDropHandler(event:DragEvent):void {
			if (event.dragSource.hasFormat(CMDComponentXMLEditor.DRAG_NEW_ATTRIBUTE)) {
				var attr:CMDAttribute = event.dragSource.dataForFormat(CMDComponentXMLEditor.DRAG_NEW_ATTRIBUTE) as CMDAttribute;
				_attributes.addItem(attr);
				addChild(createAttributeBox(attr));
			}
		}

	}
}
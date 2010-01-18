package clarin.cmdi.componentregistry.common.components {
	import clarin.cmdi.componentregistry.common.StyleConstants;

	import flash.events.Event;

	import mx.containers.FormItem;
	import mx.controls.TextInput;

	public class XMLEditorField extends FormItem {

		[Bindable]
		public var editField:String;

		private var _editField:TextInput;
		private var _xmlElement:XML;
		private var _name:String


		public function XMLEditorField(xmlElement:XML, name:String) {
			super();
			_xmlElement = xmlElement;
			_name = name;
		}

		override protected function createChildren():void {
			_editField = new TextInput();
			_editField.text = editField;
			_editField.styleName = StyleConstants.XMLEDITOR_EDIT_FIELD;
			_editField.addEventListener(Event.CHANGE, handleEdit);
			addChild(_editField);
			super.createChildren();
		}

		private function handleEdit(event:Event):void {
			editField = _editField.text;
			if (_xmlElement.hasOwnProperty("@"+ _name)) {
				//edit an attribute
				_xmlElement.parent()["@" + _name] = editField;
			} else {
				//Edit an element
				_xmlElement.replace("*", editField);
			}
		}

	}
}
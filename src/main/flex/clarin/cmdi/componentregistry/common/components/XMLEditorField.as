package clarin.cmdi.componentregistry.common.components {
	import flash.events.Event;

	import mx.containers.FormItem;
	import mx.controls.TextInput;

	public class XMLEditorField extends FormItem {

		[Bindable]
		public var editField:String;

		private var _editField:TextInput;
		private var _xmlLine:XMLLine;


		public function XMLEditorField(xmlLine:XMLLine) {
			super();
			_xmlLine = xmlLine;
		}

		override protected function createChildren():void {
			_editField = new TextInput();
			_editField.text = editField;
			_editField.setStyle("borderStyle", "none");
			_editField.setStyle("fontWeight", "normal");
			_editField.addEventListener(Event.CHANGE, handleEdit);
			addChild(_editField);
			super.createChildren();
		}

		private function handleEdit(event:Event):void {
			editField = _editField.text;
			if (_xmlLine.isAttribute) {
				//edit an attribute
				_xmlLine.xml.parent()["@" + _xmlLine.name] = editField;
			} else {
				//Edit an element
				_xmlLine.xml.replace("*", editField);
			}
		}

	}
}
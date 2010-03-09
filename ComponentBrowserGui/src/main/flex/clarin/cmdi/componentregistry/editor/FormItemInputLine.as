package clarin.cmdi.componentregistry.editor {
	import clarin.cmdi.componentregistry.common.StyleConstants;
	
	import mx.binding.utils.BindingUtils;
	import mx.containers.FormItem;
	import mx.controls.TextInput;

	public class FormItemInputLine extends FormItem {
		private var editField:TextInput = new TextInput();

		public function FormItemInputLine(name:String, value:String, bindingFunction:Function) {
			super();
			label = name;
			styleName = StyleConstants.XMLBROWSER_FIELD;
			editField.styleName = StyleConstants.XMLEDITOR_EDIT_FIELD;
			editField.width = 300;
			editField.text = value;
			BindingUtils.bindSetter(bindingFunction, editField, "text");
		}

		protected override function createChildren():void {
			super.createChildren();
			addChild(editField);
		}

	}
}
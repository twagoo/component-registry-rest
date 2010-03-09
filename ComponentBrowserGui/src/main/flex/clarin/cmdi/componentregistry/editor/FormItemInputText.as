package clarin.cmdi.componentregistry.editor {
	import clarin.cmdi.componentregistry.common.StyleConstants;
	
	import mx.binding.utils.BindingUtils;
	import mx.containers.FormItem;
	import mx.controls.TextArea;

	public class FormItemInputText extends FormItem {

		private var editArea:TextArea = new TextArea();

		public function FormItemInputText(name:String, value:String, bindingFunction:Function) {
			super();
			label = name;
			styleName = StyleConstants.XMLBROWSER_FIELD;
			editArea.styleName = StyleConstants.XMLEDITOR_EDIT_FIELD;
			editArea.width = 300;
			editArea.text = value;
			BindingUtils.bindSetter(bindingFunction, editArea, "text");
		}

		protected override function createChildren():void {
			super.createChildren();
			addChild(editArea);
		}

	}
}
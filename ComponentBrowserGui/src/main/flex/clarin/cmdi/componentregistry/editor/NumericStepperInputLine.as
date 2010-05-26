package clarin.cmdi.componentregistry.editor {
	import clarin.cmdi.componentregistry.common.StyleConstants;
	
	import mx.binding.utils.BindingUtils;
	import mx.containers.FormItem;
	import mx.controls.NumericStepper;

	public class NumericStepperInputLine extends FormItem {
		protected var editField:NumericStepper = new NumericStepper();


		public function NumericStepperInputLine(name:String, value:String, bindingFunction:Function) {
			super();
			label = name;
			styleName = StyleConstants.XMLBROWSER_FIELD;
			editField.styleName = StyleConstants.XMLEDITOR_EDIT_FIELD;
			editField.width = 300;
			editField.value = Number(value);
			editField.maximum = 10;
			BindingUtils.bindSetter(bindingFunction, editField, "value");
		}

		protected override function createChildren():void {
			super.createChildren();
			addChild(editField);
		}

	}
}
package clarin.cmdi.componentregistry.editor {
	import clarin.cmdi.componentregistry.common.StyleConstants;
	
	import mx.binding.utils.BindingUtils;
	import mx.containers.FormItem;
	import mx.controls.TextArea;
	import mx.events.ValidationResultEvent;
	import mx.validators.Validator;

	public class FormItemInputText extends FormItem implements CMDValidator{

		private var editArea:TextArea = new TextArea();
		private var _validator:Validator;

		public function FormItemInputText(name:String, value:String, bindingFunction:Function, validator:Validator = null) {
			super();
			label = name;
			styleName = StyleConstants.XMLBROWSER_FIELD;
			editArea.styleName = StyleConstants.XMLEDITOR_EDIT_FIELD;
			editArea.width = 300;
			editArea.text = value;
			BindingUtils.bindSetter(bindingFunction, editArea, "text");
			if (validator) {
			    _validator = validator;
			    _validator.listener = this.editArea;
			    CMDComponentXMLEditor.validators.addItem(this);
			}
		}


		public function validate():Boolean {
			if (this.visible) {
				var result:ValidationResultEvent = _validator.validate(this.editArea.text);
				return result.type == ValidationResultEvent.VALID;
			}
			return true;
		}

		protected override function createChildren():void {
			super.createChildren();
			addChild(editArea);
		}

	}
}
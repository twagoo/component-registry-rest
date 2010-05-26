package clarin.cmdi.componentregistry.editor {
	import clarin.cmdi.componentregistry.common.StyleConstants;
	
	import flash.events.Event;
	
	import mx.binding.utils.BindingUtils;
	import mx.containers.FormItem;
	import mx.controls.TextInput;
	import mx.events.FlexEvent;
	import mx.events.ValidationResultEvent;
	import mx.validators.Validator;

	public class FormItemInputLine extends FormItem implements CMDValidator {

		public var editField:TextInput = new TextInput();

		private var _validator:Validator;

		public function FormItemInputLine(name:String, value:String, bindingFunction:Function, editable:Boolean = true, validator:Validator = null) {
			super();
			label = name;
			styleName = StyleConstants.XMLBROWSER_FIELD;
			editField.styleName = StyleConstants.XMLEDITOR_EDIT_FIELD;
			editField.width = 300;
			editField.text = value;
			editField.editable = editable;
			BindingUtils.bindSetter(bindingFunction, editField, "text");
			if (validator) {
				_validator = validator;
				_validator.listener = this.editField;
			}
		}

		public function validate():Boolean {
			if (_validator && this.visible) {
				var result:ValidationResultEvent = _validator.validate(this.editField.text);
				return result.type == ValidationResultEvent.VALID;
			}
			return true;
		}

		protected override function createChildren():void {
			super.createChildren();
			addChild(editField);
		}

	}
}
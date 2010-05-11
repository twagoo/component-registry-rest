package clarin.cmdi.componentregistry.editor {
	import clarin.cmdi.componentregistry.common.LabelConstants;

	import mx.events.ValidationResultEvent;
	import mx.validators.Validator;

	public class NameInputLine extends FormItemInputLine {

		private var validator:Validator;

		public function NameInputLine(value:String, bindingFunction:Function) {
			super(LabelConstants.NAME, value, bindingFunction, true, InputValidators.getNameValidator());
		}

	}
}
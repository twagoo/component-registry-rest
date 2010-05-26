package clarin.cmdi.componentregistry.editor {
	import clarin.cmdi.componentregistry.common.LabelConstants;

	import mx.binding.utils.BindingUtils;
	import mx.events.ValidationResultEvent;
	import mx.validators.Validator;

	public class DisplayPriorityInput extends NumericStepperInputLine {
		private var _validator:Validator;

		public function DisplayPriorityInput(name:String, value:String, bindingFunction:Function) {
			super(name, value, bindingFunction);
			toolTip = LabelConstants.DISPLAY_PRIORITY_TOOLTIP;
			_validator = InputValidators.getIsRequiredValidator();
			_validator.listener = this.editField;
			_validator.requiredFieldError = "At least one element should have it's displayPriority set to 1 or higher.";
		}

		public function getValue():Number {
			return editField.value;
		}

		/**
		 * Validate with given input because the DisplayPriority is a global setting 
		 * which needs to be checked outside this class
		 **/
		public function validate(value:Object):Boolean {
			if (_validator && this.visible) {
				var result:ValidationResultEvent = _validator.validate(value);
				return result.type == ValidationResultEvent.VALID;
			}
			return true;
		}

	}
}
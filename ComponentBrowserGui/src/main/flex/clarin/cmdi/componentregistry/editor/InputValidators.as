package clarin.cmdi.componentregistry.editor {
	import mx.validators.RegExpValidator;
	import mx.validators.Validator;


	public final class InputValidators {
		public function InputValidators() {
		}

		public static function getNameValidator():RegExpValidator {
			var result:RegExpValidator = new RegExpValidator();
			result.expression = "^[A-Za-z0-9_\-]+$";
			result.required = true;
			result.noMatchError = "Name can only contain A-Z, a-z, 0-9, or _";
			return result;
		}
		
		public static function getIsRequiredValidator():Validator {
			var result:Validator = new Validator();
			result.required = true;
			return result;
		}
		
	}
}
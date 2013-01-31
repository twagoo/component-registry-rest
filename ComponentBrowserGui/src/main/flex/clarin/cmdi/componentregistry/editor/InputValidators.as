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
		
		public static function getConceptLinkValidator():RegExpValidator {
			var result:RegExpValidator = new RegExpValidator();
			// Concept link must be a valid absolute URI
			result.expression = "^([^:/?#]+):(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?$";
			result.required = false;
			result.noMatchError = "Concept link must be a valid absolute URI";
			return result;
		}
		
		public static function getIsRequiredValidator():Validator {
			var result:Validator = new Validator();
			result.required = true;
			return result;
		}
		
	}
}
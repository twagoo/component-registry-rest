package clarin.cmdi.componentregistry.editor
{
	import clarin.cmdi.componentregistry.editor.model.AttributeContainer;
	import clarin.cmdi.componentregistry.editor.model.CMDAttribute;
	import clarin.cmdi.componentregistry.editor.model.CMDComponent;
	import clarin.cmdi.componentregistry.editor.model.CMDComponentElement;
	
	import mx.events.ValidationResultEvent;
	import mx.validators.RegExpValidator;
	import mx.validators.ValidationResult;
	
	public class AttributeNameValidator extends RegExpValidator
	{
		private var _parentComponent:AttributeContainer;
		private var _childObject:CMDAttribute;
		private var unique:Boolean;
		
		public function AttributeNameValidator(parentComponent:AttributeContainer, childObject:CMDAttribute)
		{
			_parentComponent = parentComponent;
			_childObject = childObject;
			
			expression = "^[A-Za-z0-9_\-]+$";
			required = true;
			noMatchError = "Name can only contain A-Z, a-z, 0-9, or _";
		}
		
		override protected function doValidation(value:Object):Array{
			var validatorResults:Array = new Array();
			validatorResults = super.doValidation(value);
			
			unique = true;
			if(_parentComponent){						
				var name:String = String(value);
				if(_childObject != null){
					for each (var attribute:CMDAttribute in _parentComponent.getAttributeList()) {
						if(attribute != _childObject && attribute.name == name){
							unique = false;
							validatorResults = new Array();
							validatorResults.push(new ValidationResult(true,null,"Duplicate attribute name","Name must be unqiue among attributes"));
							return validatorResults;
						}
					}
				}
			}
			
			return validatorResults; 
		}
		
		override protected function handleResults(errorResults:Array):ValidationResultEvent{
			var result:ValidationResultEvent = super.handleResults(errorResults);
			if(result.type == ValidationResultEvent.VALID){
				if(!unique){
					result = new ValidationResultEvent(ValidationResultEvent.INVALID);
					result.results = errorResults;
				}
			}
			return result;
		}
	}
}
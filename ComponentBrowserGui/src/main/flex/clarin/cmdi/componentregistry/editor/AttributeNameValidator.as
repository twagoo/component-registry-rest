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
		private var nameAllowed:Boolean;
		private var reservedComponentAttributes:Array = ["ref", "ComponentId"];
		
		public function AttributeNameValidator(parentComponent:AttributeContainer, childObject:CMDAttribute)
		{
			_parentComponent = parentComponent;
			_childObject = childObject;
			
			expression = "^[A-Za-z0-9_\-]+$";
			required = true;
			noMatchError = "Name can only contain A-Z, a-z, 0-9, or _";
			
			//TODO: Always fails for component attributes, find out why...
		}
		
		override protected function doValidation(value:Object):Array{
			var validatorResults:Array;// = new Array();
			validatorResults = super.doValidation(value);
			
			this.unique = true;
			this.nameAllowed = true;
			if(_parentComponent){								
				var attributeName:String = String(value);

				// check for illegal attribute names on components
				if(_parentComponent is CMDComponent){
					for each(var reservedName:String in reservedComponentAttributes){
						if(reservedName == attributeName){
							nameAllowed = false;
							validatorResults = new Array();
							validatorResults.push(new ValidationResult(true,null,"Illegal attribute name","'" + reservedName + "' is a reserved attribute name"));
							return validatorResults;
						}
					}
				}
				
				// check for duplicate attribute names
				if(_childObject != null){
					for each (var attribute:CMDAttribute in _parentComponent.getAttributeList()) {
						if(attribute != _childObject && attribute.name == attributeName){
							this.unique = false;
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
				if(!unique || !nameAllowed){
					result = new ValidationResultEvent(ValidationResultEvent.INVALID);
					result.results = errorResults;
				}
			}
			return result;
		}
	}
}
package clarin.cmdi.componentregistry.editor
{
	import clarin.cmdi.componentregistry.editor.model.CMDComponent;
	import clarin.cmdi.componentregistry.editor.model.CMDComponentElement;
	
	import mx.events.ValidationResultEvent;
	import mx.validators.RegExpValidator;
	import mx.validators.ValidationResult;
	
	public class ChildNameValidator extends RegExpValidator
	{
		private var _parentComponent:CMDComponent;
		private var _childObject:Object;
		private var unique:Boolean;

		public function ChildNameValidator(parentComponent:CMDComponent, childObject:Object)
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
					for each (var component:CMDComponent in _parentComponent.cmdComponents) {
						if(component != _childObject && component.name == name){
							unique = false;
							validatorResults = new Array();
							validatorResults.push(new ValidationResult(true,null,"Duplicate component name","Name must be unqiue among siblings"));
							return validatorResults;
						}
					}
					for each (var element:CMDComponentElement in _parentComponent.cmdElements) {
						if(element != _childObject && element.name == name){
							unique = false;
							validatorResults = new Array();
							validatorResults.push(new ValidationResult(true,null,"Duplicate element name","Name must be unqiue among siblings"));
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
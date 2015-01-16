package clarin.cmdi.componentregistry.editor {
	import clarin.cmdi.componentregistry.common.StyleConstants;
	import clarin.cmdi.componentregistry.services.IsocatService;
	
	import flash.events.Event;
	import flash.events.MouseEvent;
	
	import mx.binding.utils.BindingUtils;
	import mx.containers.FormItem;
	import mx.containers.FormItemDirection;
	import mx.controls.Button;
	import mx.controls.TextInput;
	import mx.events.ValidationResultEvent;
	import mx.managers.PopUpManager;
	import mx.validators.Validator;
	
	public class ConceptLinkInput extends FormItem implements CMDValidator {
		
		private var editField:TextInput = new TextInput();
		private var searchConceptLink:Button = new Button();
		private var isocatPopup:IsocatSearchPopUp;
		private var conceptType:String;
		private var validator:Validator = InputValidators.getConceptLinkValidator();
		
		public function ConceptLinkInput(name:String, value:String, bindingFunction:Function, conceptType:String = null) {
			super();
			direction = FormItemDirection.HORIZONTAL;
			label = name;
			styleName = StyleConstants.XMLBROWSER_FIELD;
			editField.styleName = StyleConstants.XMLEDITOR_EDIT_FIELD;
			editField.width = 300;
			editField.text = value;
			BindingUtils.bindSetter(bindingFunction, editField, "text");
			createSearchConceptLinkButton();
			editField.addEventListener(Event.CHANGE, function():void {
				editField.toolTip = null
			});
			validator.listener = this.editField;
			this.conceptType = conceptType;			
		}
		
		public function validate():Boolean {
			var result:ValidationResultEvent = validator.validate(this.editField.text);
			return result.type == ValidationResultEvent.VALID;
		}
		
		protected override function createChildren():void {
			super.createChildren();
			addChild(editField);
			addChild(searchConceptLink);
		}
		
		private function createSearchConceptLinkButton():void {
			searchConceptLink.label = "Search in concept registry...";
			searchConceptLink.addEventListener(MouseEvent.CLICK, handleButtonClick);
		}
		
		private function getIsocatPopup():IsocatSearchPopUp {
			if (!isocatPopup) {
				isocatPopup = EditorManager.getIsocatSearchPopUp(conceptType);
				isocatPopup.addEventListener(IsocatSearchPopUp.OK_EVENT, function(e:Event):void {
					editField.text = isocatPopup.editField.text;
					editField.toolTip = isocatPopup.editField.toolTip;
				});
			}
			return isocatPopup;
		}
		
		private function handleButtonClick(event:MouseEvent):void {
			PopUpManager.addPopUp(getIsocatPopup(), this.parent, false);
		}
		
	}
}

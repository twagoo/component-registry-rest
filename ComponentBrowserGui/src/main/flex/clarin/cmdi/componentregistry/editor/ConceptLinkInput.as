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
	import mx.managers.PopUpManager;

	public class ConceptLinkInput extends FormItem {

		private var editField:TextInput = new TextInput();
		private var searchConceptLink:Button = new Button();
		private var isocatPopup:IsocatSearchPopUp;

		public function ConceptLinkInput(name:String, value:String, bindingFunction:Function) {
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
		}

		protected override function createChildren():void {
			super.createChildren();
			addChild(editField);
			addChild(searchConceptLink);
		}

		private function createSearchConceptLinkButton():void {
			searchConceptLink.label = "Search in isocat...";
			searchConceptLink.addEventListener(MouseEvent.CLICK, handleButtonClick);
		}

		private function getIsocatPopup():IsocatSearchPopUp {
			if (!isocatPopup) {
				isocatPopup = EditorManager.getIsocatSearchPopUp();
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
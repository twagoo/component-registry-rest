package clarin.cmdi.componentregistry.editor {
	import clarin.cmdi.componentregistry.common.StyleConstants;
	import clarin.cmdi.componentregistry.editor.model.CMDSpec;
	
	import flash.events.MouseEvent;
	
	import mx.containers.FormItem;
	import mx.containers.FormItemDirection;
	import mx.controls.ButtonLabelPlacement;
	import mx.controls.Label;
	import mx.controls.RadioButton;

	public class SelectTypeRadioButtons extends FormItem {

		private var _spec:CMDSpec;

		public function SelectTypeRadioButtons(spec:CMDSpec) {
			styleName = StyleConstants.XMLBROWSER_FIELD;
			_spec = spec;
			direction = FormItemDirection.HORIZONTAL;
		}

		protected override function createChildren():void {
			super.createChildren();
			var isProfileButton:RadioButton = new RadioButton();
			isProfileButton.groupName = "selectType";
			isProfileButton.label = "Profile";
			isProfileButton.selected = _spec.isProfile;
			isProfileButton.labelPlacement = ButtonLabelPlacement.LEFT;
			isProfileButton.addEventListener(MouseEvent.CLICK, handleIsProfile);
			addChild(isProfileButton);

			var isComponentButton:RadioButton = new RadioButton();
			isComponentButton.groupName = "selectType";
			isComponentButton.label = "Component";
			isComponentButton.selected = !_spec.isProfile;
			isComponentButton.labelPlacement = ButtonLabelPlacement.LEFT;
			isComponentButton.addEventListener(MouseEvent.CLICK, handleIsComponent);
			addChild(isComponentButton);
		}

		private function handleIsProfile(event:MouseEvent):void {
			_spec.isProfile = true;
		}

		private function handleIsComponent(event:MouseEvent):void {
			_spec.isProfile = false;
		}

	}
}
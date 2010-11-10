package clarin.cmdi.componentregistry.common.components {
	import clarin.cmdi.componentregistry.common.StyleConstants;
	
	import mx.controls.Button;

	public class UpIconButton extends Button {
		public function UpIconButton() {
			super();
			setStyle("icon", StyleConstants.upIcon);
			setStyle("paddingTop", 15);
			width = 5;
			height = 0;
			toolTip = "move up";
		}

	}
}
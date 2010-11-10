package clarin.cmdi.componentregistry.common.components {
	import clarin.cmdi.componentregistry.common.StyleConstants;

	import mx.controls.Button;

	public class DownIconButton extends Button {
		public function DownIconButton() {
			super();
			setStyle("icon", StyleConstants.downIcon);
			setStyle("paddingTop", 18);
			width = 5;
			height = 0;
			toolTip = "move down";
		}

	}
}
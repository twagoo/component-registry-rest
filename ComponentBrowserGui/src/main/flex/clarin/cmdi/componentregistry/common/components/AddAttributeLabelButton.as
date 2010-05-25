package clarin.cmdi.componentregistry.common.components {
	import flash.events.MouseEvent;

	import mx.controls.Label;

	public class AddAttributeLabelButton extends Label {
		public function AddAttributeLabelButton() {
			super();
			text = "+attribute";
			toolTip = "click to add Attribute";
			setStyle("color", "green");
			addEventListener(MouseEvent.MOUSE_OVER, function(event:MouseEvent):void {
					event.currentTarget.setStyle("textDecoration", "underline");
				});
			addEventListener(MouseEvent.MOUSE_OUT, function(event:MouseEvent):void {
					event.currentTarget.setStyle("textDecoration", "none");
				});


		}

	}
}
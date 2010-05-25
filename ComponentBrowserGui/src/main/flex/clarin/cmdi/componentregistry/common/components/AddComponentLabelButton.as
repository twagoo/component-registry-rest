package clarin.cmdi.componentregistry.common.components {
	import flash.events.MouseEvent;

	import mx.controls.Label;

	public class AddComponentLabelButton extends Label {
		public function AddComponentLabelButton() {
			super();
			text = "+component";
			toolTip = "click to add Component";
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
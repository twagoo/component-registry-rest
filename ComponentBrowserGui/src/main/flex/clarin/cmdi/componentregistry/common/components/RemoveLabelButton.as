package clarin.cmdi.componentregistry.common.components {
	import flash.events.MouseEvent;
	
	import mx.controls.Label;

	public class RemoveLabelButton extends Label {
		public function RemoveLabelButton() {
			super();
			text = "X";
			toolTip = "click to remove";
			setStyle("color", "red");
			addEventListener(MouseEvent.MOUSE_OVER, function(event:MouseEvent):void {
					event.currentTarget.setStyle("textDecoration", "underline");
				});
			addEventListener(MouseEvent.MOUSE_OUT, function(event:MouseEvent):void {
					event.currentTarget.setStyle("textDecoration", "none");
				});
		}



	}
}
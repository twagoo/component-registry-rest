package clarin.cmdi.componentregistry.common.components {
	import flash.events.MouseEvent;
	
	import mx.controls.Label;

	public class AddElementLabelButton extends Label {
	    
		public function AddElementLabelButton() {
			super();
			text = "+element";
			toolTip = "click to add Element";
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
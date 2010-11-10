package clarin.cmdi.componentregistry.common.components {
	import flash.events.MouseEvent;

	import mx.controls.Label;

	public class BasicLabelButton extends Label {
		public function BasicLabelButton(textString:String = null, tooltipString:String = null, color:String = null) {
			if (textString)
				text = textString;
			if (tooltipString)
				toolTip = tooltipString;
			if (color)
				setStyle("color", color);
			addEventListener(MouseEvent.MOUSE_OVER, function(event:MouseEvent):void {
					event.currentTarget.setStyle("textDecoration", "underline");
				});
			addEventListener(MouseEvent.MOUSE_OUT, function(event:MouseEvent):void {
					event.currentTarget.setStyle("textDecoration", "none");
				});

		}

	}
}
package clarin.cmdi.componentregistry.common.components {
	import flash.events.MouseEvent;
	
	import mx.controls.Label;

	public class LabelButton extends Label {
		public function LabelButton(clickHandler:Function, text:String=null) {
			super();
			if(text != null){
				this.text = text;
			}
			addEventListener(MouseEvent.MOUSE_OVER, function(event:MouseEvent):void {
				event.currentTarget.setStyle("textDecoration", "underline");
			});
			addEventListener(MouseEvent.MOUSE_OUT, function(event:MouseEvent):void {
				event.currentTarget.setStyle("textDecoration", "none");
			});
			addEventListener(MouseEvent.CLICK, clickHandler);
		}

	}
}
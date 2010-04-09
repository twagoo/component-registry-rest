package clarin.cmdi.componentregistry.common.components {
	import flash.events.MouseEvent;
	
	import mx.controls.TextArea;
	import mx.events.ScrollEvent;
	import mx.events.ScrollEventDirection;

    [Inspectable]
	public class ScrollableTextArea extends TextArea {
		public function ScrollableTextArea() {
			super();
		}

		/**
		 * Overrides the handler to be able to scroll, this is only needed for the shiny Macs out there. Implementation copied from mx.core.ScrollControlBase.as
		 * Use this instead of mx.controls.TextArea
		 **/
		override protected function mouseWheelHandler(event:MouseEvent):void {
			if (verticalScrollBar && verticalScrollBar.visible) {
				event.stopPropagation();

				var scrollDirection:int = event.delta <= 0 ? 1 : -1;

				// Make sure we scroll by at least one line
				var scrollAmount:Number = Math.max(Math.abs(event.delta), verticalScrollBar.lineScrollSize);

				// Multiply by 3 to make scrolling a little faster
				var oldPosition:Number = verticalScrollPosition;
				verticalScrollPosition += 3 * scrollAmount * scrollDirection;

				var scrollEvent:ScrollEvent = new ScrollEvent(ScrollEvent.SCROLL);
				scrollEvent.direction = ScrollEventDirection.VERTICAL;
				scrollEvent.position = verticalScrollPosition;
				scrollEvent.delta = verticalScrollPosition - oldPosition;
				dispatchEvent(scrollEvent);
			}
		}
	}
}
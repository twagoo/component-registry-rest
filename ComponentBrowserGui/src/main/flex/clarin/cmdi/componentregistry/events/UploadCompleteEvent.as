package clarin.cmdi.componentregistry.events {
	import clarin.cmdi.componentregistry.ItemDescription;

	import flash.events.Event;

	public class UploadCompleteEvent extends Event {
		public static const UPLOAD_COMPLETE:String = "uploadComplete";
		private var _itemDescription:ItemDescription;

		public function UploadCompleteEvent(itemDescription:ItemDescription, bubbles:Boolean = false, cancelable:Boolean = false) {
			super(UPLOAD_COMPLETE, bubbles, cancelable);
			_itemDescription = itemDescription;
		}

		public function get itemDescription():ItemDescription {
			return _itemDescription;
		}

		override public function clone():Event {
			return new BrowserSelectionEvent(_itemDescription, bubbles, cancelable);
		}

	}
}
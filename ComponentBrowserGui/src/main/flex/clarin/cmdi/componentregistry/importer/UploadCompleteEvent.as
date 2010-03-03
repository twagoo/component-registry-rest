package clarin.cmdi.componentregistry.importer {
	import clarin.cmdi.componentregistry.common.ItemDescription;

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
			return new UploadCompleteEvent(_itemDescription, bubbles, cancelable);
		}

	}
}
package clarin.cmdi.componentregistry.browser {
	import clarin.cmdi.componentregistry.common.ItemDescription;
	
	import flash.events.Event;

	public class BrowserSelectionEvent extends Event {

		public static const BROWSER_ITEM_SELECTED:String = "browserItemSelected";
		private var _itemDescription:ItemDescription;
		public function BrowserSelectionEvent(itemDescription:ItemDescription, bubbles:Boolean = false, cancelable:Boolean = false) {
			super(BROWSER_ITEM_SELECTED, bubbles, cancelable);
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
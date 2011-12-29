package clarin.cmdi.componentregistry.services {
	import clarin.cmdi.componentregistry.common.ItemDescription;
	
	import com.adobe.net.URI;
	
	import mx.collections.ArrayCollection;
	
	import mx.rpc.events.ResultEvent;
	
	[Event(name="itemsLoaded", type="flash.events.Event")]
	public class BrowserService extends ComponentRegistryService {
        public static const ITEMS_LOADED:String = "itemsLoaded";

		/**
		 * Typed ArrayCollection publicly available for outside code to bind to and watch.
		 */
		[Bindable]
		[ArrayElementType("ItemDescription")]
		public var itemDescriptions:ArrayCollection;

		// Not bindable needed for lookups over the whole collections of itemDescriptions
		protected var unFilteredItemDescriptions:ArrayCollection;
		protected var userSpace:Boolean;

		public function BrowserService(restUrl:String, userSpace:Boolean) {
			super(restUrl);
			this.userSpace = userSpace;
		}
		
		override protected function initServiceUrl(url:URI):void{
			if (userSpace) {
				url.setQueryValue(Config.PARAM_USERSPACE, "true");
			}
		}
		
		/**
		 * Override in concrete subclasses
		 */
		override protected function result(resultEvent:ResultEvent):void {
			dispatchEvent(new Event(ITEMS_LOADED));
		}

		protected function setItemDescriptions(items:ArrayCollection):void {
			itemDescriptions = items;
			unFilteredItemDescriptions = new ArrayCollection(); //create a copy
			for each (var item:Object in items) {
				unFilteredItemDescriptions.addItem(item);
			}
		}

		/**
		 * Looks up itemDescription, returns null if not found.
		 */
		public function lookUpDescription(componentId:String):ItemDescription {
			for each (var item:ItemDescription in unFilteredItemDescriptions) {
				if (item.id == componentId) {
					return item;
				}
			}
			return null;
		}
	}
}


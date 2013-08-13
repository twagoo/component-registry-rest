package clarin.cmdi.componentregistry.services {
	import com.adobe.net.URI;
	
	import mx.collections.ArrayCollection;
	
	import clarin.cmdi.componentregistry.common.ItemDescription;	
	
	public class BrowserService extends ComponentRegistryService {

		/**
		 * Typed ArrayCollection publicly available for outside code to bind to and watch.
		 */
		[Bindable]
		[ArrayElementType("ItemDescription")]
		public var itemDescriptions:ArrayCollection;

		public function BrowserService(successEvent:String, restUrl:URI, userSpace:Boolean) {
			super(successEvent, restUrl);
			this.userSpace = userSpace;
		}
		
		override protected function dispatchRequest(url:URI):void {
			if (userSpace) {
				url.setQueryValue(Config.PARAM_USERSPACE, "true");
			}
			super.dispatchRequest(url);
		}
		
		public function findDescription(id:String):ItemDescription {			
			for each (var item:ItemDescription in itemDescriptions) {
				if (item.id == id) {
					trace("found "+ id+" in "+itemDescriptions.length+" userSpace= "+userSpace);
					return item;
				}
			}
			trace("not found "+ id+" in "+itemDescriptions+" userSpace= "+userSpace);
			return null;
		}
	}
}


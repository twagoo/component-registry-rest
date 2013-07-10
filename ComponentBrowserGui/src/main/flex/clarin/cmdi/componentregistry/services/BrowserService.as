package clarin.cmdi.componentregistry.services {
	import clarin.cmdi.componentregistry.common.ItemDescription;
	
	import com.adobe.net.URI;	
	import mx.collections.ArrayCollection;	
	import mx.rpc.events.ResultEvent;
	
	public class BrowserService extends ComponentRegistryService {

		/**
		 * Typed ArrayCollection publicly available for outside code to bind to and watch.
		 */
		[Bindable]
		[ArrayElementType("ItemDescription")]
		public var itemDescriptions:ArrayCollection;

		// Not bindable needed for lookups over the whole collections of itemDescriptions
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


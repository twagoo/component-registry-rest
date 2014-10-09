package clarin.cmdi.componentregistry.services {
	import clarin.cmdi.componentregistry.common.ItemDescription;
	
	import com.adobe.net.URI;
	
	import mx.collections.ArrayCollection;
	import mx.controls.Alert;

	public class BrowserService extends ComponentRegistryService {

		/**
		 * Typed ArrayCollection publicly available for outside code to bind to and watch.
		 */
		[Bindable]
		[ArrayElementType("ItemDescription")]
		public var itemDescriptions:ArrayCollection;
		
			
		protected var registrySpace:RegistrySpace;
		
		public function BrowserService(successEvent:String, restUrl:URI, registrySpace_:RegistrySpace) {
			super(successEvent, restUrl);
			this.registrySpace = registrySpace_;
		}
		
		[Bindable(event=REGISTRY_SPACE_TOGGLE_EVENT)]
		public function setRegistrySpace(registrySpace_:RegistrySpace):void{
			this.registrySpace = registrySpace_;
		}
		

		// propagates to two child calsses: component and profile list services
		override protected function dispatchRequest(url:URI):void {
			var copy:URI = new URI();
			copy.copyURI(url);
			if (this.registrySpace.space) {
				copy.setQueryValue(Config.REGISTRY_PARAM_SPACE, this.registrySpace.space);
				if ((this.registrySpace.space) == Config.SPACE_GROUP) {
					if (this.registrySpace.groupId == null || this.registrySpace.groupId == "") {
						throw "Group Id is not given";
					}
					copy.setQueryValue(Config.REGISTRY_PARAM_GROUP_ID, this.registrySpace.groupId);
				} 
			} 
			super.dispatchRequest(copy);
		}
		
		public function findDescription(id:String):ItemDescription {			
			for each (var item:ItemDescription in itemDescriptions) {
				if (item.id == id) {
					return item;
				}
			}
			return null;
		}
	}
}


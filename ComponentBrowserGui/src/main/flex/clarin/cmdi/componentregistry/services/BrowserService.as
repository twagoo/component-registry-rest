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
		
		protected var groupId:String;
		
		public function BrowserService(successEvent:String, restUrl:URI, space:String) {
			super(successEvent, restUrl);
			this.space = space;
		}
		
		public function setGroupId(id:String):void{
			this.groupId = id;
		}

		override protected function dispatchRequest(url:URI):void {
			var copy:URI = new URI();
			copy.copyURI(url);
			if (space  == Config.SPACE_USER) {
				copy.setQueryValue(Config.PARAM_USERSPACE, "true");
			}
			if (groupId){
				copy.setQueryValue("groupid",groupId);
			} else
				copy.setQueryValue("groupid",null);
			super.dispatchRequest(copy);
		}
		
		public function findDescription(id:String):ItemDescription {			
			for each (var item:ItemDescription in itemDescriptions) {
				if (item.id == id) {
					trace("found "+ id+" in "+itemDescriptions.length+" userSpace= "+space);
					return item;
				}
			}
			trace("not found "+ id+" in "+itemDescriptions+" userSpace= "+space);
			return null;
		}
	}
}


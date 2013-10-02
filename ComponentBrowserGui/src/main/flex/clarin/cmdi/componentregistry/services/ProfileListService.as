package clarin.cmdi.componentregistry.services {
	import clarin.cmdi.componentregistry.browser.BrowserColumns;
	import clarin.cmdi.componentregistry.common.ItemDescription;
	
	import mx.collections.ArrayCollection;
	import com.adobe.net.URI;
	
	
	public class ProfileListService extends BrowserService {
		
		public const PROFILES_LOADED:String = "profilesLoaded";
		
		public function ProfileListService(space:String) {
			super(PROFILES_LOADED, new URI(Config.instance.profileListUrl), space);
		}
		
		override protected function handleXmlResult(resultXml:XML):void{
			var nodes:XMLList = resultXml.profileDescription;
			var tempArray:ArrayCollection = new ArrayCollection();
			for each (var node:XML in nodes) {
				var item:ItemDescription = new ItemDescription();
				item.createProfile(node, space);
				tempArray.addItem(item);
			}
			tempArray.sort = BrowserColumns.getInitialSortForProfiles();
			tempArray.refresh();
			itemDescriptions = new ArrayCollection(tempArray.toArray());
		}		
	}
}
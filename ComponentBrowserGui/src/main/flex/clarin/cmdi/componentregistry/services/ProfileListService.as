package clarin.cmdi.componentregistry.services {
	import clarin.cmdi.componentregistry.browser.BrowserColumns;
	import clarin.cmdi.componentregistry.common.ItemDescription;
	
	import mx.collections.ArrayCollection;
	import com.adobe.net.URI;
	
	
	public class ProfileListService extends BrowserService {
		
		public const PROFILES_LOADED:String = "profilesLoaded";
		
		public function ProfileListService(registrySpace:RegistrySpace) {
			super(PROFILES_LOADED, new URI(Config.instance.profileListUrl), registrySpace);
		}
		
		override protected function handleXmlResult(resultXml:XML):void{
			var nodes:XMLList = resultXml.profileDescription;
			var tempArray:ArrayCollection = new ArrayCollection();
			for each (var node:XML in nodes) {
				var item:ItemDescription = new ItemDescription();
				var isPrivate:Boolean = (Config.instance.registrySpace.space == Config.SPACE_PRIVATE || Config.instance.registrySpace.space == Config.SPACE_GROUP);
				item.createProfile(node, isPrivate);
				tempArray.addItem(item);
			}
			tempArray.sort = BrowserColumns.getInitialSortForProfiles();
			tempArray.refresh();
			itemDescriptions = new ArrayCollection(tempArray.toArray());
		}		
	}
}
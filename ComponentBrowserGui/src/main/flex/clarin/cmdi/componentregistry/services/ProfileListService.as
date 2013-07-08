package clarin.cmdi.componentregistry.services {
	import clarin.cmdi.componentregistry.browser.BrowserColumns;
	import clarin.cmdi.componentregistry.common.ItemDescription;
	
	import mx.collections.ArrayCollection;
	import mx.rpc.events.ResultEvent;	
	import flash.events.Event;
	
	
	[Event(name="profilesLoaded", type="flash.events.Event")]
	public class ProfileListService extends BrowserService {
		
		public static const PROFILES_LOADED:String = "profilesLoaded";
		
		public function ProfileListService(userSpace:Boolean=false) {
			super(Config.instance.profileListUrl, userSpace);
		}
		
		override protected function result(resultEvent:ResultEvent):void {
			var resultXml:XML = resultEvent.result as XML;
			var nodes:XMLList = resultXml.profileDescription;
			var tempArray:ArrayCollection = new ArrayCollection();
			for each (var node:XML in nodes) {
				var item:ItemDescription = new ItemDescription();
				item.createProfile(node, userSpace);
				tempArray.addItem(item);
			}
			tempArray.sort = BrowserColumns.getInitialSortForProfiles();
			tempArray.refresh();
			setItemDescriptions(new ArrayCollection(tempArray.toArray()));	
			dispatchEvent(new Event(PROFILES_LOADED));
		}
		
	}
}
package clarin.cmdi.componentregistry.services {
	import clarin.cmdi.componentregistry.common.ItemDescription;
	
	import mx.collections.ArrayCollection;
	import mx.rpc.events.ResultEvent;

	public class ProfileListService extends BrowserService {
		
		private static var _instance:ProfileListService = new ProfileListService();
		private static var _userSpaceInstance:ProfileListService = new ProfileListService(true);

		public function ProfileListService(userSpace:Boolean=false) {
			super(Config.instance.profileListUrl, userSpace);
		}

		override protected function result(resultEvent:ResultEvent):void {
			var resultXml:XML = resultEvent.result as XML;
			var nodes:XMLList = resultXml.profileDescription;
			var tempArray:Array = new Array();
			for each (var node:XML in nodes) {
				var item:ItemDescription = new ItemDescription();
				item.createProfile(node, userSpace);
				tempArray[tempArray.length] = item;
			}
			setItemDescriptions(new ArrayCollection(tempArray));
		}

		public static function getInstance(userSpace:Boolean):ProfileListService {
			if (userSpace) {
				return _userSpaceInstance;
			} else {
				return _instance;
			}
		}
	}
}
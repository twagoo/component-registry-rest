package clarin.cmdi.componentregistry.services {
	import clarin.cmdi.componentregistry.common.ItemDescription;

	import mx.collections.ArrayCollection;
	import mx.rpc.events.ResultEvent;

	public class ComponentListService extends BrowserService {

		private static var _instance:ComponentListService = new ComponentListService();
		private static var _userSpaceInstance:ComponentListService = new ComponentListService(true);

		public function ComponentListService(userSpace:Boolean = false) {
			super(Config.instance.componentListUrl, userSpace);
		}

		override protected function result(resultEvent:ResultEvent):void {
			var resultXml:XML = resultEvent.result as XML;
			var nodes:XMLList = resultXml.componentDescription;

			var tempArray:Array = new Array();
			for each (var node:XML in nodes) {
				var item:ItemDescription = new ItemDescription();
				item.createComponent(node, userSpace);
				tempArray[tempArray.length] = item;
			}
			setItemDescriptions(new ArrayCollection(tempArray));
		}

		public static function getInstance(userSpace:Boolean):ComponentListService {
			if (userSpace) {
				return _userSpaceInstance;
			} else {
				return _instance;
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

		public static function findDescription(componentId:String):ItemDescription {
			var result:ItemDescription = getInstance(true).lookUpDescription(componentId);
			if (result == null) {
				result = getInstance(false).lookUpDescription(componentId);
			}
			return result
		}


	}
}
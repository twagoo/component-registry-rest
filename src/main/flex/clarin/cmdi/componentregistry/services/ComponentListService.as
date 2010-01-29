package clarin.cmdi.componentregistry.services {
	import clarin.cmdi.componentregistry.common.ItemDescription;

	import mx.collections.ArrayCollection;
	import mx.rpc.events.ResultEvent;

	public class ComponentListService extends BrowserService {


		private static var _instance:ComponentListService = new ComponentListService();

		public function ComponentListService() {
			super(Config.instance.componentListUrl);
			if (_instance != null) {
				throw new Error("Service can only be accessed through ComponentListService.instance");
			}
		}

		override protected function result(resultEvent:ResultEvent):void {
			var resultXml:XML = resultEvent.result as XML;
			var nodes:XMLList = resultXml.componentDescription;

			var tempArray:Array = new Array();
			for each (var node:XML in nodes) {
				var item:ItemDescription = new ItemDescription();
				item.createComponent(node);
				tempArray[tempArray.length] = item;
			}
			setItemDescriptions(new ArrayCollection(tempArray));
		}

		public static function get instance():ComponentListService {
			return _instance;
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
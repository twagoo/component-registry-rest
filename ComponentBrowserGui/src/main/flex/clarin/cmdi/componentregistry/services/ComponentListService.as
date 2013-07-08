package clarin.cmdi.componentregistry.services {
	import clarin.cmdi.componentregistry.browser.BrowserColumns;
	import clarin.cmdi.componentregistry.common.ItemDescription;
	
	import mx.collections.ArrayCollection;
	import mx.rpc.events.ResultEvent;
	import flash.events.Event;

	[Event(name="componentsLoaded", type="flash.events.Event")]
	public class ComponentListService extends BrowserService {

		public static const COMPONENTS_LOADED:String = "componentsLoaded";
		
		private static var _instance:ComponentListService = new ComponentListService();
		private static var _userSpaceInstance:ComponentListService = new ComponentListService(true);

		public function ComponentListService(userSpace:Boolean = false) {
			super(Config.instance.componentListUrl, userSpace);
		}

		override protected function result(resultEvent:ResultEvent):void {
			var resultXml:XML = resultEvent.result as XML;
			var nodes:XMLList = resultXml.componentDescription;

			var tempArray:ArrayCollection = new ArrayCollection();
			for each (var node:XML in nodes) {
				var item:ItemDescription = new ItemDescription();
				item.createComponent(node, userSpace);
				tempArray.addItem(item);
			}
			tempArray.sort = BrowserColumns.getInitialSortForComponents();
			tempArray.refresh();
            setItemDescriptions(new ArrayCollection(tempArray.toArray()));
			trace(itemDescriptions.length + " components are loaded");
			dispatchEvent(new Event(COMPONENTS_LOADED));
		    // super.result(resultEvent);
		}

		public static function getInstance(userSpace:Boolean):ComponentListService {
			if (userSpace) {
				return _userSpaceInstance;
			} else {
				return _instance;
			}
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
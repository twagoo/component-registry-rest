package clarin.cmdi.componentregistry.services {
	import clarin.cmdi.componentregistry.browser.BrowserColumns;
	import clarin.cmdi.componentregistry.common.ItemDescription;
	
	import mx.collections.ArrayCollection;
	import mx.rpc.events.ResultEvent;
	import flash.events.Event;
	
	[Event(name="componentsLoaded", type="flash.events.Event")]
	public class ComponentListService extends BrowserService {
		
		public static const COMPONENTS_LOADED:String = "componentsLoaded";
		
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
			dispatchEvent(new Event(COMPONENTS_LOADED));
		}
		
	}
}
package clarin.cmdi.componentregistry.services {
	import com.adobe.net.URI;
	
	import flash.events.Event;
	
	import mx.collections.ArrayCollection;
	
	import clarin.cmdi.componentregistry.browser.BrowserColumns;
	import clarin.cmdi.componentregistry.common.ItemDescription;
	
	public class ComponentListService extends BrowserService {
		
		public  const COMPONENTS_LOADED:String = "componentsLoaded";
		
		public function ComponentListService(space:String) {
			super(COMPONENTS_LOADED, new URI(Config.instance.componentListUrl), space);
		}
		
		override protected function handleXmlResult(resultXml:XML):void{
			var nodes:XMLList = resultXml.componentDescription;
			
			var tempArray:ArrayCollection = new ArrayCollection();
			for each (var node:XML in nodes) {
				var item:ItemDescription = new ItemDescription();
				item.createComponent(node, space);
				tempArray.addItem(item);
			}
			tempArray.sort = BrowserColumns.getInitialSortForComponents();
			tempArray.refresh();
			itemDescriptions = new ArrayCollection(tempArray.toArray());
			dispatchEvent(new Event(this.COMPONENTS_LOADED));
		}
		
	}
}
package clarin.cmdi.componentregistry.services {
	import clarin.cmdi.componentregistry.common.ItemDescription;

	import mx.collections.ArrayCollection;
	import mx.rpc.events.ResultEvent;

	public class ComponentListService extends BrowserService {

		public function ComponentListService() {
			super(Config.instance.componentListUrl);
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
	}
}
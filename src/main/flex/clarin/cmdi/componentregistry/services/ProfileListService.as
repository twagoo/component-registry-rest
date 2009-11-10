package clarin.cmdi.componentregistry.services {
	import clarin.cmdi.componentregistry.ItemDescription;

	import mx.collections.ArrayCollection;
	import mx.rpc.events.ResultEvent;

	public class ProfileListService extends BrowserService {
		public function ProfileListService() {
			super("http://localhost:8080/ComponentRegistry/rest/registry/profiles");
		}

		override protected function result(resultEvent:ResultEvent):void {
			var resultXml:XML = resultEvent.result as XML;
			var nodes:XMLList = resultXml.profileDescription;
			var tempArray:Array = new Array();
			for each (var node:XML in nodes) {
				var item:ItemDescription = new ItemDescription();
				item.create(node);
				tempArray[tempArray.length] = item;
			}
			setItemDescriptions(new ArrayCollection(tempArray));
		}

	}
}
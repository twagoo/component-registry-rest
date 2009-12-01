package clarin.cmdi.componentregistry {
	import clarin.cmdi.componentregistry.services.Config;

	[Bindable]
	public class ItemDescription {

		public var id:String;
		public var name:String;
		public var description:String;
		public var creatorName:String;
		public var groupName:String;
		public var dataUrl:String;

		public function ItemDescription() {

		}

		private function create(itemDescription:XML, infoUrl:String):void {
			this.id = itemDescription.id;
			this.name = itemDescription.name;
			this.description = itemDescription.description;
			this.creatorName = itemDescription.creatorName;
			this.groupName = itemDescription.groupName;
			this.dataUrl = infoUrl + itemDescription.id
		}

		public function createProfile(itemDescription:XML):void {
			create(itemDescription, Config.instance.profileInfoUrl);
		}

		public function createComponent(itemDescription:XML):void {
			create(itemDescription, Config.instance.componentInfoUrl);
		}

	}
}
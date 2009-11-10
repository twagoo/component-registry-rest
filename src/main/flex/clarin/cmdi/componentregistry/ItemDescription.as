package clarin.cmdi.componentregistry {
	import mx.collections.ArrayCollection;

	[Bindable]
	public class ItemDescription {

		public var id:String;
		public var name:String;
		public var description:String;
		public var creatorName:String;
		public var groupName:String;

		public function ItemDescription() {

		}

		public function create(itemDescription:XML):void {
			this.id = itemDescription.id;
			this.name = itemDescription.name;
			this.description = itemDescription.description;
			this.creatorName = itemDescription.creatorName;
			this.groupName = itemDescription.groupName;
		}

	}
}
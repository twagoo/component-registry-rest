package clarin.cmdi.componentregistry.common {
	import clarin.cmdi.componentregistry.services.Config;

	import mx.formatters.DateFormatter;

	[Bindable]
	public class ItemDescription {

		public var id:String;
		public var name:String;
		public var description:String;
		public var creatorName:String;
		public var groupName:String;
		public var dataUrl:String;
		public var isProfile:Boolean;
		public var registrationDate:String;
		public var registrationDateValue:Date;

		public function ItemDescription() {

		}

		private function create(itemDescription:XML, infoUrl:String, isProfileValue:Boolean):void {
			this.id = itemDescription.id;
			this.name = itemDescription.name;
			this.registrationDate = convertDate(itemDescription.registrationDate);
			this.description = itemDescription.description;
			this.creatorName = itemDescription.creatorName;
			this.groupName = itemDescription.groupName;
			this.dataUrl = infoUrl + itemDescription.id
			this.isProfile = isProfileValue;
		}

		private function convertDate(dateString:String):String {
			var validator:DateFormatter = new DateFormatter();
			var s:String = dateString.replace("CET", "UTC+0100");
			var n:Number = Date.parse(s);
			this.registrationDateValue = new Date(n);
			var result:String;
			if (isNaN(n)) {
				trace("cannot convert date: " + dateString);
				result = dateString;
			} else {
				validator.formatString = "DD MMMM YYYY H:NN:SS "; //e.g. 02 December 2009 13:48:39 
				result = validator.format(registrationDateValue);
			}
			return result;
		}

		public function createProfile(itemDescription:XML):void {
			create(itemDescription, Config.instance.profileInfoUrl, true);
		}

		public function createComponent(itemDescription:XML):void {
			create(itemDescription, Config.instance.componentInfoUrl, false);
		}

	}
}
package clarin.cmdi.componentregistry.common {
	import clarin.cmdi.componentregistry.services.Config;
	
	import mx.formatters.DateFormatter;
	
	[Bindable]
	public class CommentDescription {
		
		public var id:String;
		public var componentId:String;
		public var profileId:String;
		public var description:String;
		public var creatorName:String;
		public var dataUrl:String;
		public var isProfile:Boolean;
		public var registrationDate:String;
		public var registrationDateValue:Date;
		
		public function CommentDescription() {
		}
		
		private function create(comment:XML, infoUrl:String, isProfileValue:Boolean):void {
			this.id = comment.id;
			this.componentId = comment.componentId;
			this.profileId = comment.profileId;
			this.description = comment.description;
			this.registrationDate = convertDate(comment.registrationDate);
			this.creatorName = comment.creatorName;
			this.dataUrl = infoUrl + comment.id
			this.isProfile = isProfileValue;
		}

		/**
		 * getting ISO-8601 (GMT/UTC timezone) dates from the server. Need to convert this, flex does not support ISO-8601 times out of the box.
		 */
		private function convertDate(dateString:String):String {
			var validator:DateFormatter = new DateFormatter();
			var s:String = parseDate(dateString);
			var n:Number = Date.parse(s);
			this.registrationDateValue = new Date(n);
			var result:String;
			if (isNaN(n)) {
				trace("cannot convert date: " + dateString);
				result = dateString;
			} else {
				validator.formatString = "DD MMMM YYYY H:NN:SS"; //e.g. 02 December 2009 13:48:39 
				result = validator.format(registrationDateValue);
			}
			return result;
		}
		
		public static function parseDate(value:String):String {
			var dateStr:String = value;
			dateStr = dateStr.replace(/-/g, "/");
			dateStr = dateStr.replace("T", " ");
			dateStr = dateStr.replace("+00:00", " GMT-0000");
			return dateStr;
		}
		
		public function createComment(comment:XML):void {
			if(isProfile){
			return create(comment, Config.instance.commentProfileInfoUrl, true);
			} else {
			return create(comment, Config.instance.commentComponentInfoUrl, false);
			}
		}

	}
}
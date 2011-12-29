package clarin.cmdi.componentregistry.common
{
	import mx.formatters.DateFormatter;

	public class DateUtils
	{
		public static function convertDate(dateString:String):Date {
			var validator:DateFormatter = new DateFormatter();
			var s:String = parseDate(dateString);
			var n:Number = Date.parse(s);
			var registrationDateValue:Date = new Date(n);
			var result:String;
			if (isNaN(n)) {
				trace("cannot convert date: " + dateString);
				return null;
			} 
			return registrationDateValue;
		}
		
		public static function formatDateString(dateString:String):String {
			var s:String = parseDate(dateString);
			var n:Number = Date.parse(s);
			var registrationDateValue:Date = new Date(n);
			var result:String;
			if (isNaN(n)) {
				trace("cannot convert date: " + dateString);
				result = dateString;
			} else {
				result = convertDateToString(registrationDateValue);
			}
			return result;
		}
		
		public static function convertDateToString(date:Date):String{
			var validator:DateFormatter = new DateFormatter();
			validator.formatString = "DD MMMM YYYY H:NN:SS"; //e.g. 02 December 2009 13:48:39 
			return validator.format(date);
		}
		
		public static function parseDate(value:String):String {
			var dateStr:String = value;
			dateStr = dateStr.replace(/-/g, "/");
			dateStr = dateStr.replace("T", " ");
			dateStr = dateStr.replace("+00:00", " GMT-0000");
			return dateStr;
		}
	}
}
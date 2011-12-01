package clarin.cmdi.componentregistry.common {
	import mx.collections.ArrayCollection;
	
	[Bindable]
	public class CommentMD {
		
		/** 
		 * Claring Metadata xml constants
		 */
		//Element Names

		public static const COMPONENTID:String = "ComponentId";
		public static const PROFILEID:String = "ProfileId";
		public static const CMD_COMMENT:String = "CMD_Comment";
		public static const HEADER:String = "Header";
		public static const TYPE:String = "Type";
		public static const DESCRIPTION:String = "Description";
		//Attribute Names
		public static const APP_INFO:String = "AppInfo";
		public static const NAME:String = "name";
		
		public var name:String;
		
		
		private var _xml:XML;
		
		public function CommentMD() {
		}
		
		public function set xml(xml:XML):void {
			_xml = xml;
		}
		
		public function get xml():XML {
			return _xml;
		}
	}
	
}

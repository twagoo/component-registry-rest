package clarin.cmdi.componentregistry.services {
	import mx.core.ByteArrayAsset;

	public final class Config {
		public static const COMPONENT_LIST_SERVICE:String = "componentListService";
		public static const PROFILE_LIST_SERVICE:String = "profileListService";
		public static const UPLOAD_SERVICE:String = "uploadService";
		public static const PROFILE_INFO_SERVICE:String = "profileInfoService";
		public static const COMPONENT_INFO_SERVICE:String = "componentInfoService";

		private static var _instance:Config = new Config();

		[Embed("assets/config.xml", mimeType="application/octet-stream")]
		protected static const ConfigEmbed:Class;
		private var xml:XML;

		public function Config() {
			if (_instance != null) {
				throw new Error("Config can only be accessed through Config.instance");
			}
		}

		public static function get instance():Config {
			return _instance;
		}

		public function getUrl(id:String):String {
			if (!xml) {
				var ba:ByteArrayAsset = ByteArrayAsset(new ConfigEmbed());
				xml = new XML(ba.readUTFBytes(ba.length));
			}
			var value:String = xml.url.(@id == id).@value;
			return value;
		}


		public function getXMLConfig():XML {
			return xml;
		}
	}
}
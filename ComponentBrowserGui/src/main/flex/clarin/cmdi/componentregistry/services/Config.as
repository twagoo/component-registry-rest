package clarin.cmdi.componentregistry.services {
	import mx.core.Application;

	public final class Config {
		private static const COMPONENT_LIST_URL:String = "/rest/registry/components";
		private static const PROFILE_LIST_URL:String = "/rest/registry/profiles";
		private static const UPLOAD_SERVICE_URL:String = "/rest/registry/profiles";
		private static const PROFILE_INFO_URL:String = "/rest/registry/profiles/";
		private static const COMPONENT_INFO_URL:String = "/rest/registry/components/";

		private static var _instance:Config = new Config();

		private var _serviceRootUrl:String = "http://localhost:8080/ComponentRegistry"; 
		//Default _serviceRootUrl value can be useful for testing. Set the proper value in your (index.)html that embeds the flash object.
		//Like this: "FlashVars", "serviceRootUrl=http://localhost:8080/ComponentRegistry" 


		public function Config() {
			if (_instance != null) {
				throw new Error("Config can only be accessed through Config.instance");
			}
			var serviceRootUrl:String = Application.application.parameters.serviceRootUrl;
			if (serviceRootUrl != null) {
				_serviceRootUrl = serviceRootUrl;
			}
		}

		public function get profileListUrl():String {
			return _serviceRootUrl + PROFILE_LIST_URL;
		}

		public function get componentListUrl():String {
			return _serviceRootUrl + COMPONENT_LIST_URL;
		}

		public function get profileInfoUrl():String {
			return _serviceRootUrl + PROFILE_INFO_URL;
		}

		public function get componentInfoUrl():String {
			return _serviceRootUrl + COMPONENT_INFO_URL;
		}

		public function get uploadUrl():String {
			return _serviceRootUrl + UPLOAD_SERVICE_URL;
		}

		public static function get instance():Config {
			return _instance;
		}

	}
}
package clarin.cmdi.componentregistry.services {
	import mx.core.Application;

	public final class Config {
		private static const COMPONENT_LIST_URL:String = "/rest/registry/components";
		private static const PROFILE_LIST_URL:String = "/rest/registry/profiles";
		private static const UPLOAD_PROFILE_SERVICE_URL:String = "/rest/registry/profiles";
		private static const UPLOAD_COMPONENT_SERVICE_URL:String = "/rest/registry/components";
		private static const PROFILE_INFO_URL:String = "/rest/registry/profiles/";
		private static const COMPONENT_INFO_URL:String = "/rest/registry/components/";

		private static var _instance:Config = new Config();

		private var _serviceRootUrl:String = "http://localhost:8080/ComponentRegistry";
		//Default _serviceRootUrl value can be useful for testing. Set the proper value in your (index.)html that embeds the flash object.
		//Like this: "FlashVars", "serviceRootUrl=http://localhost:8080/ComponentRegistry"

		private var _isocatSearchUrl:String = "http://www.isocat.org/rest/user/guest/search";
		//Default _isocatSearchUrl value can be useful for testing. Set the proper value in your (index.)html that embeds the flash object.
		//Like this: "FlashVars", "isocatSearchUrl=http://www.isocat.org/rest/user/guest/search"

       // private var _sessionId:String = "";

		public function Config() {
			if (_instance != null) {
				throw new Error("Config can only be accessed through Config.instance");
			}
			var serviceRootUrl:String = Application.application.parameters.serviceRootUrl;
			if (serviceRootUrl != null) {
				_serviceRootUrl = serviceRootUrl;
			}
			var isocatSearchUrl:String = Application.application.parameters.isocatSearchUrl;
			if (isocatSearchUrl != null) {
				_isocatSearchUrl = isocatSearchUrl;
			}
//			var sessionId:String = Application.application.parameters.jsessionid;
//			if (sessionId != null) {
//				_sessionId = sessionId;
//			}
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

		public function get uploadProfileUrl():String {
			return _serviceRootUrl + UPLOAD_PROFILE_SERVICE_URL;
		}

		public function get uploadComponentUrl():String {
			return _serviceRootUrl + UPLOAD_COMPONENT_SERVICE_URL;
		}

		public function get isocatSearchUrl():String {
			return _isocatSearchUrl;
		}
		
//		public function get sessionId():String {
//			return _sessionId;
//		}

		public function get serviceRootUrl():String {
			return _serviceRootUrl;
		}


		public static function get instance():Config {
			return _instance;
		}
		
	}
}
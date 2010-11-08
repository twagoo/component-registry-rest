package clarin.cmdi.componentregistry.services {
	import flash.events.Event;
	import flash.events.EventDispatcher;

	[Event(name="userSpaceToggle", type="flash.events.Event")]
	public final class Config extends EventDispatcher {
		public static const USER_SPACE_TOGGLE_EVENT:String = "userSpaceToggle";

		public static const CLARIN_REGISTER_URL:String = "http://www.clarin.eu/user/register";
		public static const PARAM_USERSPACE:String = "userspace";
		public static const PARAM_VIEW:String = "view";
		
		//Possible views to start with.
		public static const VIEW_BROWSE:String = "browse";
		public static const VIEW_EDIT:String = "edit";
		public static const VIEW_IMPORT:String = "import";
		//Possible space to start with.
		public static const SPACE_USER:String = "user";
		public static const SPACE_PUBLIC:String = "public";

		private static const COMPONENT_LIST_URL:String = "/rest/registry/components";
		private static const PROFILE_LIST_URL:String = "/rest/registry/profiles";
		private static const UPLOAD_PROFILE_SERVICE_URL:String = "/rest/registry/profiles";
		private static const UPLOAD_COMPONENT_SERVICE_URL:String = "/rest/registry/components";
		private static const PROFILE_INFO_URL:String = "/rest/registry/profiles/";
		private static const COMPONENT_INFO_URL:String = "/rest/registry/components/";
		private static const PING_SESSION_URL:String = "/rest/registry/pingSession";
		private static const ISOCAT_SERVLET:String = "/isocat";
		

		private static var _instance:Config = new Config();

        private var _startupItem:String; //item to be selected at startup, can be specified as a url parameter
		private var _serviceRootUrl:String = "http://localhost:8080/ComponentRegistry";
		//Default _serviceRootUrl value can be useful for testing. Set the proper value in your (index.)html that embeds the flash object.
		//Like this: "FlashVars", "serviceRootUrl=http://localhost:8080/ComponentRegistry"

        private var _view:String = VIEW_BROWSE;
        private var _space:String = SPACE_PUBLIC;
		private var _userSpace:Boolean = false;

		public function Config() {
			if (_instance != null) {
				throw new Error("Config can only be accessed through Config.instance");
			}
		}

		private function init(applicationParameters:Object):void {
			var serviceRootUrl:String = applicationParameters.serviceRootUrl;
			if (serviceRootUrl != null) {
				_serviceRootUrl = serviceRootUrl;
			}
			var item:String = applicationParameters.item;
			if (item != null) {
				_startupItem = item;
			}
			var view:String = applicationParameters.view;
			if (view != null) {
				_view = view;
			}			
			var space:String = applicationParameters.space;
			if (space != null) {
				_space = space;
			}			
		}

		public static function create(applicationParameters:Object):void {
			_instance.init(applicationParameters);
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
			return _serviceRootUrl + ISOCAT_SERVLET;
		}

		public function get serviceRootUrl():String {
			return _serviceRootUrl;
		}
		
		public function get pingSessionUrl():String {
			return _serviceRootUrl+ PING_SESSION_URL;
		}

		public function set userSpace(userSpace:Boolean):void {
			if (_userSpace != userSpace) {
				_userSpace = userSpace;
				dispatchEvent(new Event(USER_SPACE_TOGGLE_EVENT));
			}
		}

		public function get userSpace():Boolean {
			return _userSpace;
		}
		
		public function get startupItem():String {
		    return _startupItem;
		}

        public function get view():String {
            return _view;
        }
        
        public function get space():String {
            return _space;
        }
        
		public static function get instance():Config {
			return _instance;
		}

	}
}
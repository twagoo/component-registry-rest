package clarin.cmdi.componentregistry.services {
	import clarin.cmdi.componentregistry.common.ItemDescription;
	
	import com.adobe.net.URI;
	
	import flash.events.Event;
	import flash.events.EventDispatcher;

	[Event(name="userSpaceToggle", type="flash.events.Event")]
	public final class Config extends EventDispatcher {
		public static const USER_SPACE_TOGGLE_EVENT:String = "userSpaceToggle";

		public static const CLARIN_REGISTER_URL:String = "http://www.clarin.eu/user/register";
		public static const PARAM_USERSPACE:String = "userspace";
		public static const REGISTRY_PARAM_VIEW:String = "view";
		public static const REGISTRY_PARAM_BROWSER_VIEW:String = "browserview";
		public static const REGISTRY_PARAM_ITEM:String = "item";
		public static const REGISTRY_PARAM_SPACE:String = "space";
		public static const REGISTRY_PARAM_DEBUG:String = "debug";

		//Possible views to start with.
		public static const VIEW_BROWSE:String = "browse";
		public static const VIEW_EDIT:String = "edit";
		public static const VIEW_IMPORT:String = "import";
		//Possible browser views to start with
		public static const BROWSER_PANEL_VIEW:String = "view"; 
		public static const BROWSER_PANEL_XML:String = "xml"; 
		public static const BROWSER_PANEL_COMMENTS:String = "comments"; 
		//Possible space to start with.
		public static const SPACE_USER:String = "user";
		public static const SPACE_PUBLIC:String = "public";

		private static const COMPONENT_LIST_URL:String = "/rest/registry/components";
		private static const PROFILE_LIST_URL:String = "/rest/registry/profiles";
		private static const COMPONENT_USAGE_URL:String = "/rest/registry/components/usage/";
		private static const UPLOAD_PROFILE_SERVICE_URL:String = "/rest/registry/profiles";
		private static const UPLOAD_COMPONENT_SERVICE_URL:String = "/rest/registry/components";
		private static const PROFILE_INFO_URL:String = "/rest/registry/profiles/";
		private static const COMPONENT_INFO_URL:String = "/rest/registry/components/";
		private static const PING_SESSION_URL:String = "/rest/registry/pingSession";
		private static const USER_SETTINGS_URL:String = "/admin/userSettings";
		private static const COMMENTS_URL_PATH:String = "/comments/";
		private static const ISOCAT_SERVLET:String = "/isocat";


		private static var _instance:Config = new Config();

		private var _startupItem:String; //item to be selected at startup, can be specified as a url parameter
		private var _serviceRootUrl:String = "http://localhost:8080/ComponentRegistry";
		//Default _serviceRootUrl value can be useful for testing. Set the proper value in your (index.)html that embeds the flash object.
		//Like this: "FlashVars", "serviceRootUrl=http://localhost:8080/ComponentRegistry"

		private var _view:String = VIEW_BROWSE;
		private var _browserPanel:String = BROWSER_PANEL_VIEW;
		private var _space:String = SPACE_PUBLIC;
		private var _userSpace:Boolean = false;
		private var _debug:Boolean = false;

		public function Config() {
			if (_instance != null) {
				throw new Error("Config can only be accessed through Config.instance");
			}
		}

		private function init(applicationParameters:Object):void {
			var serviceRootUrl:String = applicationParameters.serviceRootUrl;
			if (serviceRootUrl) {
				_serviceRootUrl = serviceRootUrl;
			}
			var item:String = applicationParameters.item;
			if (item) {
				_startupItem = item;
			}
			var view:String = applicationParameters.view;
			if (view) {
				_view = view;
			}
			var browserPanel:String = applicationParameters.browserview;
			if(browserPanel){
				_browserPanel = browserPanel;
			}
			var space:String = applicationParameters.space;
			if (space) {
				_space = space;
			}
			var debug:int = applicationParameters.debug;
			if(debug) {
				_debug = Boolean(debug);
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
		
		public function get componentUsageUrl():String {
			return _serviceRootUrl + COMPONENT_USAGE_URL;
		}

		public function get profileInfoUrl():String {
			return _serviceRootUrl + PROFILE_INFO_URL;
		}
		
		public function getProfileCommentsPath(id:String):String{
			return profileInfoUrl + id + COMMENTS_URL_PATH;
		}

		public function getComponentCommentsPath(id:String):String{
			return componentInfoUrl + id + COMMENTS_URL_PATH;
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
			return _serviceRootUrl + PING_SESSION_URL;
		}
		
		public function get userSettingsUrl():String{
			return _serviceRootUrl + USER_SETTINGS_URL;
		}

		public function set userSpace(userSpace:Boolean):void {
			if (_userSpace != userSpace) {
				_userSpace = userSpace;
				dispatchEvent(new Event(USER_SPACE_TOGGLE_EVENT));
			}
		}

		[Bindable(event="userSpaceToggle")]
		public function get userSpace():Boolean {
			return _userSpace;
		}

		public function get startupItem():String {
			return _startupItem;
		}

		public function get view():String {
			return _view;
		}
		
		public function get browserPanel():String {
			return _browserPanel;
		}

		public function get space():String {
			return _space;
		}
		
		public function get debug():Boolean {
			return _debug;
		}

		public static function getBookmarkUrl(item:ItemDescription):String {
			var uri:URI = new URI(Config.instance.serviceRootUrl);
		    uri.setQueryValue(REGISTRY_PARAM_ITEM, item.id);
			if (item.isInUserSpace) {
				uri.setQueryValue(REGISTRY_PARAM_SPACE, SPACE_USER);
			}
			return uri.toString();
		}
		
		public static function getXsdLink(item:ItemDescription):String {
			var uri:URI = new URI(item.dataUrl+"/xsd");
			return uri.toString();
		}

		public static function get instance():Config {
			return _instance;
		}

	}
}
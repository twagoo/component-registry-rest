package clarin.cmdi.componentregistry.services {
	import clarin.cmdi.componentregistry.browser.GroupSelectionEvent;
	import clarin.cmdi.componentregistry.common.ItemDescription;
	import clarin.cmdi.componentregistry.common.Profile;
	
	import com.adobe.net.URI;
	
	import flash.events.Event;
	import flash.events.EventDispatcher;
	
	import mx.controls.Alert;
	import mx.controls.List;
 
	[Event(name="registrySpaceToggle", type="flash.events.Event")]
	public final class Config extends EventDispatcher {
		public static const REGISTRY_SPACE_TOGGLE_EVENT:String = "registrySpaceToggle";

		public static const CLARIN_REGISTER_URL:String = "http://www.clarin.eu/user/register";
		public static const REGISTRY_PARAM_VIEW:String = "view";
		public static const REGISTRY_PARAM_BROWSER_VIEW:String = "browserview";
		public static const REGISTRY_PARAM_ITEM_ID:String = "itemId";
		public static const REGISTRY_PARAM_SPACE:String = "registrySpace";
		public static const REGISTRY_PARAM_DEBUG:String = "debug";
		public static const REGISTRY_PARAM_GROUP_ID:String = "groupId"
		
		// REGISTRY_ID, COMPONENT_PREFIX, and PROFILE_PREFIX must be the same as on the server!
		// REGISTRY_ID isdefined in ComponentRegistry
		// COMPONENT_PREFIX is defined in ComponentDescription
		// PROFILE_PREFIX is defined in ProfileDescription
		private static const REGISTRY_ID:String="clarin.eu:cr1:";
		public static const COMPONENT_PREFIX:String=REGISTRY_ID+"c_";
		public static const PROFILE_PREFIX:String=REGISTRY_ID+"p_";
		

		//Possible views to start with.
		public static const VIEW_BROWSE:String = "browse";
		public static const VIEW_EDIT:String = "edit";
		public static const VIEW_IMPORT:String = "import";
		public static const FLAVOUR_PROFILES:String = "profiles";
		public static const FLAVOUR_COMPONENTS:String = "components";
		//Possible browser views to start with
		public static const BROWSER_PANEL_VIEW:String = "view"; 
		public static const BROWSER_PANEL_XML:String = "xml"; 
		public static const BROWSER_PANEL_COMMENTS:String = "comments"; 
		//Possible space to start with.
		public static const SPACE_PRIVATE:String = "private";
		public static const SPACE_PUBLISHED:String = "published";
		public static const SPACE_GROUP:String = "group";
		
		public static const COMPONENT_LIST_URL:String = "/rest/registry/components";
		public static const ITEMS_URL:String= "/rest/registry/items";
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
		private static const USER_GROUPS_MEMBERSHIP_URL:String = "/rest/registry/groups/usermembership";
		
		
		
		public static var _instance:Config = new Config();
		
		
		private var _startupItemId:String; //item to be selected at startup, can be specified as a url parameter
		private var _serviceRootUrl:String = "http://localhost:8080/ComponentRegistry";
		//Default _serviceRootUrl value can be useful for testing. Set the proper value in your (index.)html that embeds the flash object.
		//Like this: "FlashVars", "serviceRootUrl=http://localhost:8080/ComponentRegistry"

		private var _view:String = VIEW_BROWSE;
		private var _browserPanel:String = BROWSER_PANEL_VIEW;
		private var _registrySpace:RegistrySpace = new RegistrySpace(SPACE_PUBLISHED, "");
		private var _debug:Boolean = false;
		private var _activeFlavour:String = FLAVOUR_PROFILES;
		
		private var componentsSrv:ComponentListService;
		private var publicComponentsSrv:ComponentListService;
		private var profilesSrv:ProfileListService;
		private var publicProfilesSrv:ProfileListService;
		private var listUserGroupsMembershipService:ListUserGroupsMembershipService;
		private var listGroupsOfItemService:ListGroupsOfItemService;
		
		public function Config() {
			if (_instance != null) {
				throw new Error("Config can only be accessed through Config.instance");
			}
		}
		
		public function get activeFlavour():String{
			return _activeFlavour;
		}
		
		public function set activeFlavour(value:String):void{
			_activeFlavour = value;
		}
		
		public static function get instance():Config {
			return _instance;
		}
		
		

		private function init(applicationParameters:Object):void {
			var serviceRootUrl_:String = applicationParameters.serviceRootUrl;
			if (serviceRootUrl_) {
				_serviceRootUrl = serviceRootUrl_;
			}
			var itemId:String = applicationParameters.itemId;
			if (itemId) {
				_startupItemId = itemId;
			}
			var view_:String = applicationParameters.view;
			if (view_) {
				_view = view_;
			}
			var browserPanel_:String = applicationParameters.browserview;
			if(browserPanel_){
				_browserPanel = browserPanel_;
			}
			
			var space_:String = applicationParameters.registrySpace;
			if (!space_) {
				space_ = SPACE_PUBLISHED;
			};
			
			var groupId_:String = applicationParameters.groupId;
			if (!groupId_) {
				groupId_ = "";
			};
			
			var debug_:int = applicationParameters.debug;
			if(debug_) {
				_debug = Boolean(debug_);				
			}
									
			listUserGroupsMembershipService = new ListUserGroupsMembershipService();
			listGroupsOfItemService = new ListGroupsOfItemService();

			publicProfilesSrv = new ProfileListService(new RegistrySpace(SPACE_PUBLISHED, ""));
			publicComponentsSrv = new ComponentListService(new RegistrySpace(SPACE_PUBLISHED, ""))
			
			registrySpace = new RegistrySpace(space_, groupId_);
		}
		
		public function getListUserGroupsMembershipService():ListUserGroupsMembershipService{
			return listUserGroupsMembershipService;
		}
		
		public function getCurrentProfilesSrv():ProfileListService{
			return profilesSrv;		
		}
		
		public function getPublicProfilesSrv():ProfileListService{
			return publicProfilesSrv;		
		}
		
		public function getCurrentComponentsSrv():ComponentListService{		
			return componentsSrv;
		}
		
		public function getPublicComponentsSrv():ComponentListService{
			return publicComponentsSrv;
		}
		
		public function getListGroupsOfItemService():ListGroupsOfItemService{
			return listGroupsOfItemService;
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

		public function getGroupsOfItemPath(itemId:String):String{
			return _serviceRootUrl + ITEMS_URL+"/"+itemId+"/groups";
		}

		
		public function getListGroupsOfUserPath():String{
			return _serviceRootUrl + USER_GROUPS_MEMBERSHIP_URL;
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
		
		public function getTransferItemOwnershipUrl(itemId:String,groupId:String):String{
			return _serviceRootUrl + ITEMS_URL+"/"+itemId+"/transferownership?groupId="+groupId;
		}

		public function set registrySpace(registrySpace:RegistrySpace):void {
			_registrySpace = registrySpace;
			
			if(registrySpace.space == SPACE_PUBLISHED) {
				profilesSrv = publicProfilesSrv;
				componentsSrv = publicComponentsSrv;
			} else {
				profilesSrv = new ProfileListService(registrySpace);			
				componentsSrv = new ComponentListService(registrySpace);
			}
			
			// everything is ready to open new componentBrowser instance
			dispatchEvent(new Event(REGISTRY_SPACE_TOGGLE_EVENT));
		}

		public function get startupItem():String {
			return _startupItemId;
		}

		public function get view():String {
			return _view;
		}
		
		public function get browserPanel():String {
			return _browserPanel;
		}

		[Bindable(event=REGISTRY_SPACE_TOGGLE_EVENT)]
		public function get registrySpace():RegistrySpace {
			return _registrySpace;
		}
		
		public function get debug():Boolean {
			return _debug;
		}

		public static function getBookmarkUrl(item:ItemDescription):String {
			var uri:URI = new URI(Config.instance.serviceRootUrl);	
			uri.setQueryValue(REGISTRY_PARAM_SPACE, Config.instance.registrySpace.space);
			if (Config.instance.registrySpace.space == SPACE_GROUP){
				uri.setQueryValue(REGISTRY_PARAM_GROUP_ID, Config.instance.registrySpace.groupId);				
			}
		    uri.setQueryValue(REGISTRY_PARAM_ITEM_ID, item.id);
			return uri.toString();
		}
		
		public static function getRssUriDescriptions(typeOfDescription:String, registrySpace:RegistrySpace):String {
			var baseUri:String = (new URI(Config.instance.serviceRootUrl)).toString();
			var result:String=baseUri+"/rest/registry/"+typeOfDescription+"/rss";
			if (registrySpace.space == Config.SPACE_PRIVATE) {
				result = result + "?registrySpace=private";
			}
			if (registrySpace.space == Config.SPACE_GROUP) {
				result = result + "?registrySpace=group&groupId="+registrySpace.groupId;
			}
			return result;
		}
		
		public static function getRssUriComments(item:ItemDescription):String {
			var baseUri:String = (new URI(Config.instance.serviceRootUrl)).toString();
			var typeOfDescription:String; 
			if (item.isProfile) {typeOfDescription="profiles/";}
			else typeOfDescription="components/";
			var result:String=baseUri+"/rest/registry/"+typeOfDescription+item.id+"/comments/rss";			
			return result;
		}
		
		public static function getXsdLink(item:ItemDescription):String {
			var uri:URI = new URI(item.dataUrl+"/xsd");
			return uri.toString();
		}
		
		public static  function getUriAllowedElementTypes():String{
			var baseUri:String = (new URI(Config.instance.serviceRootUrl)).toString();
			return (baseUri+"/rest/registry/AllowedTypes");
		}
		
	}
}
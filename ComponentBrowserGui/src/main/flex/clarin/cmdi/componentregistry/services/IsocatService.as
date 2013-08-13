package clarin.cmdi.componentregistry.services {
	import com.adobe.net.URI;
	
	import mx.managers.CursorManager;
	import mx.rpc.http.HTTPService;

	public class IsocatService extends BaseRemoteService {
		public static const PROFILE_LOADED:String = "ProfileLoaded";
		public static const TYPE_SIMPLE:String = "simple";
		public static const TYPE_COMPLEX:String = "complex";
		public static const TYPE_CONTAINER:String = "container";
		
		namespace dcif = "http://www.isocat.org/ns/dcif";


		private var service:HTTPService;

		[Bindable]
		public var searchResults:XMLList;

		public function IsocatService() {
			super(PROFILE_LOADED);
		}

        /**
         * keyword to search for
         * type can be one of IsocatService.TYPE_SIMPLE or IsocatService.TYPE_COMPLEX or null for all types
         **/ 
		public function load(keyword:String, type:String):void {
			if (keyword) {
				CursorManager.setBusyCursor();
				var uri:URI = new URI(Config.instance.isocatSearchUrl);
				uri.setQueryValue("keywords", keyword);
				if (type)
				    uri.setQueryValue("type", type);
				dispatchRequest(uri);
			}
		}

		public function close():void {
			if (service) {
				service.cancel();
			}
			CursorManager.removeBusyCursor();
		}

		override protected function handleXmlResult(result:XML):void {
			CursorManager.removeBusyCursor();
			searchResults = result.dcif::dataCategory;
		}


	}
}


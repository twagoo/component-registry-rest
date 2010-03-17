package clarin.cmdi.componentregistry.services {
	import com.adobe.net.URI;

	import flash.events.ErrorEvent;
	import flash.events.EventDispatcher;

	import mx.controls.Alert;
	import mx.managers.CursorManager;
	import mx.utils.StringUtil;

	import org.httpclient.HttpClient;
	import org.httpclient.events.HttpDataEvent;
	import org.httpclient.events.HttpResponseEvent;
	import org.httpclient.http.Get;

	public class IsocatService extends EventDispatcher {
		public static const PROFILE_LOADED:String = "ProfileLoaded";
		namespace dcif = "http://www.isocat.org/ns/dcif";


		private var service:HttpClient;

		[Bindable]
		public var searchResults:XMLList;

		public function IsocatService() {
		}

		public function load(keyword:String):void {
			if (keyword) {
				createClient();
				CursorManager.setBusyCursor();
				var uri:URI = new URI(Config.instance.isocatSearchUrl);
				uri.setQueryValue("keywords", keyword);
				uri.setQueryValue("dcif-mode", "list");

				var httpGet:Get = new Get();
				httpGet.addHeader("Accept", "application/dcif+xml");
				service.request(uri, httpGet);
			}
		}

		public function close():void {
			if (service) {
				service.close();
			}
			CursorManager.removeBusyCursor();
		}

		private function createClient():void {
			service = new HttpClient();
			service.listener.onComplete = handleResult;
			service.listener.onError = handleError;
			service.listener.onData = handleData;
		}

		private function handleData(event:HttpDataEvent):void {
			var data:XML = new XML(event.bytes);
			searchResults = data.dcif::dataCategory;
		}

		private function handleResult(resultEvent:HttpResponseEvent):void {
			CursorManager.removeBusyCursor();
			if (resultEvent.response.isClientError) {
				Alert.show("Unauthorized search, server return status:" + resultEvent.response.code);
			} else if (resultEvent.response.isServerError) {
				Alert.show("Unexpected error, server returned status: " + resultEvent.response.code);
			}
		}

		private function handleError(faultEvent:ErrorEvent):void {
			CursorManager.removeBusyCursor();
			var errorMessage:String = StringUtil.substitute("Error in {0}: {1}", this, faultEvent.text);
			Alert.show(errorMessage);
		}


	}
}


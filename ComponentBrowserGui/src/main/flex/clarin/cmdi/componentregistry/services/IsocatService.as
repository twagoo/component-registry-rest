package clarin.cmdi.componentregistry.services {
	import com.adobe.net.URI;
	
	import flash.events.EventDispatcher;
	
	import mx.controls.Alert;
	import mx.managers.CursorManager;
	import mx.messaging.messages.HTTPRequestMessage;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.http.HTTPService;
	import mx.utils.StringUtil;

	public class IsocatService extends EventDispatcher {
		public static const PROFILE_LOADED:String = "ProfileLoaded";
		namespace dcif = "http://www.isocat.org/ns/dcif";


		private var service:HTTPService;

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
				service.url = uri.toString();
				service.send();
			}
		}

		public function close():void {
			if (service) {
				service.cancel();
			}
			CursorManager.removeBusyCursor();
		}

		private function createClient():void {
			service = new HTTPService();
			service.method = HTTPRequestMessage.GET_METHOD;
			service.resultFormat = HTTPService.RESULT_FORMAT_E4X;
			service.addEventListener(ResultEvent.RESULT, handleResult);
			service.addEventListener(FaultEvent.FAULT, handleError);
		}


		private function handleResult(resultEvent:ResultEvent):void {
			CursorManager.removeBusyCursor();
			if (resultEvent.statusCode >= 200 && resultEvent.statusCode < 300) {
				var data:XML = new XML(resultEvent.result);
				searchResults = data.dcif::dataCategory;
			} else {
				Alert.show("Unexpected error, server returned status: " + resultEvent.statusCode + "\n Message = ");
			}
		}

		private function handleError(faultEvent:FaultEvent):void {
			CursorManager.removeBusyCursor();
			var errorMessage:String = StringUtil.substitute("Error in {0} status {1}: {2}", this, faultEvent.statusCode, faultEvent.fault.faultString);
			Alert.show(errorMessage);
		}


	}
}


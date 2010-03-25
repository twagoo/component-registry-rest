package clarin.cmdi.componentregistry.services {
	import clarin.cmdi.componentregistry.common.ItemDescription;
	
	import com.adobe.net.URI;
	import com.hurlant.util.Base64;
	
	import flash.events.ErrorEvent;
	import flash.events.Event;
	import flash.events.EventDispatcher;
	
	import mx.controls.Alert;
	import mx.managers.CursorManager;
	import mx.utils.StringUtil;
	
	import org.httpclient.HttpClient;
	import org.httpclient.events.HttpResponseEvent;
	import org.httpclient.http.Delete;

	[Event(name="itemDeleted", type="flash.events.Event")]

	public class DeleteService extends EventDispatcher {
		public static const ITEM_DELETED:String = "itemDeleted";
		private var service:HttpClient;

        private static var _instance:DeleteService = new DeleteService();

		public function DeleteService() {
   			if (_instance != null) {
				throw new Error("DeleteService should only be accessed through DeleteService.instance");
			}
			service = new HttpClient();
			service.listener.onComplete = handleResult;
			service.listener.onError = handleError;
		}

//		private function getCredentials():String {
//			return Base64.encode("tomcat:tomcat");
//		}

		public function deleteItem(item:ItemDescription):void {
			CursorManager.setBusyCursor();
			var uri:URI = new URI(item.dataUrl);//+";JSESSIONID="+Config.instance.sessionId
			var httpDelete:Delete = new Delete();
//			httpDelete.addHeader("Authorization", "BASIC " + getCredentials());
			service.request(uri, httpDelete);
		}

		private function handleResult(resultEvent:HttpResponseEvent):void {
			CursorManager.removeBusyCursor();
			if (resultEvent.response.isSuccess) {
				dispatchEvent(new Event(ITEM_DELETED));
			} else if (resultEvent.response.isClientError) {
				Alert.show("Unauthorized to delete item, you are not the creator.");
			} else if (resultEvent.response.isServerError) {
				Alert.show("Unexpected error, server returned status: " + resultEvent.response.code);
			}
		}

		public function handleError(faultEvent:ErrorEvent):void {
			CursorManager.removeBusyCursor();
			var errorMessage:String = StringUtil.substitute("Error in {0}: {1}", this, faultEvent.text);
		    Alert.show(errorMessage);
		}

		public static function get instance():DeleteService {
			return _instance;
		}
	}
}
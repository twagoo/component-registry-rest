package clarin.cmdi.componentregistry.services {
	import clarin.cmdi.componentregistry.common.ItemDescription;

	import com.adobe.net.URI;
	import com.hurlant.util.Base64;

	import flash.events.Event;
	import flash.events.EventDispatcher;

	import mx.controls.Alert;
	import mx.utils.StringUtil;

	import org.httpclient.HttpClient;
	import org.httpclient.events.HttpErrorEvent;
	import org.httpclient.events.HttpResponseEvent;
	import org.httpclient.events.HttpStatusEvent;
	import org.httpclient.http.Delete;

	[Event(name="itemDeleted", type="flash.events.Event")]

	public class DeleteService extends EventDispatcher {
		public static const ITEM_DELETED:String = "itemDeleted";
		private var service:HttpClient;

		public function DeleteService() {
			service = new HttpClient();
			service.listener.onComplete = handleResult;
			service.listener.onError = handleError;
			service.listener.onStatus = handleStatus;
		}

		private function getCredentials():String {
			return Base64.encode("tomcat:tomcat");
		}

		public function deleteItem(item:ItemDescription):void {
			var uri:URI = new URI(item.dataUrl);
			var httpDelete:Delete = new Delete();
			httpDelete.addHeader("Authorization", "BASIC " + getCredentials());
			service.request(uri, httpDelete);
		}

		private function handleResult(resultEvent:HttpResponseEvent):void {
			if (resultEvent.response.code == "200") {
				dispatchEvent(new Event(ITEM_DELETED));
			}
		}

		public function handleError(faultEvent:HttpErrorEvent):void {
			var errorMessage:String = StringUtil.substitute("Error in {0}: {1}", this, faultEvent.text);
			throw new Error(errorMessage);
		}

		private function handleStatus(event:HttpStatusEvent):void {
			if (event.code != "200") {
				if (event.code == "401") {
					Alert.show("Unauthorized to delete item, you are not the creator.");
				}
				trace("(httpstatus code was: " + event.code + ")");
			}
		}
	}
}
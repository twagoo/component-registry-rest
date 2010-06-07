package clarin.cmdi.componentregistry.services {
	import clarin.cmdi.componentregistry.common.ItemDescription;

	import com.adobe.net.URI;

	import flash.events.Event;
	import flash.events.EventDispatcher;

	import mx.controls.Alert;
	import mx.managers.CursorManager;
	import mx.messaging.messages.HTTPRequestMessage;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.http.HTTPService;
	import mx.utils.StringUtil;

	[Event(name="itemDeleted", type="flash.events.Event")]

	public class DeleteService extends EventDispatcher {
		public static const ITEM_DELETED:String = "itemDeleted";
		private var service:HTTPService;
		private static const DELETE_METHOD:Object = {"method": "delete"};

		private static var _instance:DeleteService = new DeleteService();

		public function DeleteService() {
			if (_instance != null) {
				throw new Error("DeleteService should only be accessed through DeleteService.instance");
			}
			service = new HTTPService();
			service.addEventListener(FaultEvent.FAULT, handleError);
			service.addEventListener(ResultEvent.RESULT, handleResult);
			service.method = HTTPRequestMessage.POST_METHOD;
		}

		public function deleteItem(item:ItemDescription):void {
			CursorManager.setBusyCursor();
			var url:URI = new URI(item.dataUrl);
			if (item.isInUserSpace) {
				url.setQueryValue(Config.USERSPACE_PARAM, "true");
			}
			service.url = url.toString();
			service.send(DELETE_METHOD);
		}

		private function handleResult(resultEvent:ResultEvent):void {
			CursorManager.removeBusyCursor();
			if (resultEvent.statusCode >= 200 && resultEvent.statusCode < 300) {
				dispatchEvent(new Event(ITEM_DELETED));
			} else {
				Alert.show("Unexpected error, server returned status: " + resultEvent.statusCode + "\n Message = ");
			}
		}

		public function handleError(faultEvent:FaultEvent):void {
			CursorManager.removeBusyCursor();
			if (faultEvent.statusCode == 401) {
				Alert.show("Item not deleted:" + faultEvent.message.body);
			} else if (faultEvent.statusCode == 403) {
				Alert.show("Item not deleted:" + faultEvent.message.body);
			} else {
				var errorMessage:String = StringUtil.substitute("Error in {0} status {1}: {2}", this, faultEvent.statusCode, faultEvent.fault.faultString);
				Alert.show(errorMessage);
			}
		}

		public static function get instance():DeleteService {
			return _instance;
		}
	}
}
package clarin.cmdi.componentregistry.services {
	import clarin.cmdi.componentregistry.common.Comment;
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
		public const ITEM_DELETED:String = "itemDeleted";

		private var service:HTTPService;
		private const DELETE_METHOD:Object = {"method": "delete"};
		
		
		public function DeleteService() {
			service = new HTTPService();
			service.addEventListener(FaultEvent.FAULT, handleError);
			service.addEventListener(ResultEvent.RESULT, handleResult);
			service.method = HTTPRequestMessage.POST_METHOD;
		}
		
		
		public function deleteItem(item:ItemDescription):void {
			CursorManager.setBusyCursor();
			
			var deleteUrl:URI = new URI(item.dataUrl);
			if (item.isInUserSpace) {
				deleteUrl.setQueryValue(Config.PARAM_USERSPACE, "true");
			}
			
			var  usageService:ComponentUsageService = new ComponentUsageService(item, item.isInUserSpace);
			usageService.addEventListener(ComponentUsageCheckEvent.COMPONENT_IN_USE, 
				function (event:ComponentUsageCheckEvent):void{
					if(event.isComponentInUse){
						onComponentInUse(event);
					} else {
						sendDelete(deleteUrl);
					}
				});
			usageService.checkUsage();
		}
		
		public function onComponentInUse(event:ComponentUsageCheckEvent):void{
			var messageBody:String = "The component cannot be deleted because it is used by the following component(s) and/or profile(s):\n\n";
			for each(var name:String in event.itemUsingComponent){
				messageBody += " - " + name + "\n";
			}
			CursorManager.removeBusyCursor();
			Alert.show(messageBody,"Component is used");
		}
		
		public function deleteComment(comment:Comment):void {
			CursorManager.setBusyCursor();
			var url:URI = new URI(comment.dataUrl);
			if (comment.itemDescription.isInUserSpace) {
				url.setQueryValue(Config.PARAM_USERSPACE, "true");
			}
			sendDelete(url);
		}
		
		private function sendDelete(url:URI):void {
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
			if (faultEvent.statusCode == 401) { //Apparrently depending on browser status codes and errormessages are sometimes not passed along to flash.
				Alert.show("Item not deleted:" + faultEvent.message.body);
			} else if (faultEvent.statusCode == 403) {
				Alert.show("Item not deleted:" + faultEvent.message.body);
			} else {
				Alert.show("Item not deleted: Item is either public or in the case of a component still referenced by other components/profiles.");
			}
		}
		
		
	}
}
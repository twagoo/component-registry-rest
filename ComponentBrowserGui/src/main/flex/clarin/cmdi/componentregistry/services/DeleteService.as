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
		public static const ITEM_DELETED:String = "itemDeleted";
		public static const COMMENT_DELETED:String = "commentDeleted";
		private var service:HTTPService;
		private static const DELETE_METHOD:Object = {"method": "delete"};
		
		
		// the item to be deleted can be either a comment, or profile/component
		// depending on int, different events should be issues because different processing will take place
		// default value is false
		private var _isComment:Boolean=false;
		
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
			// mark the moment that we want to delete a comment but not rofile/component
			_isComment = true;
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
				if (_isComment) {
					dispatchEvent(new Event(COMMENT_DELETED));
					// do not forget to reset the value back to false when a comment is deleated
					_isComment=false;
				}
				else  {
					dispatchEvent(new Event(ITEM_DELETED));
				}
			} else {
				Alert.show("Unexpected error, server returned status: " + resultEvent.statusCode + "\n Message = ");
			}
		}
		
		public function handleError(faultEvent:FaultEvent):void {
			_isComment = false;
			CursorManager.removeBusyCursor();
			if (faultEvent.statusCode == 401) { //Apparrently depending on browser status codes and errormessages are sometimes not passed along to flash.
				Alert.show("Item not deleted:" + faultEvent.message.body);
			} else if (faultEvent.statusCode == 403) {
				Alert.show("Item not deleted:" + faultEvent.message.body);
			} else {
				Alert.show("Item not deleted: Item is either public or in the case of a component still referenced by other components/profiles.");
			}
		}
		
		public static function get instance():DeleteService {
			return _instance;
		}
	}
}
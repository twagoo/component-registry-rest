package clarin.cmdi.componentregistry.services {
	import clarin.cmdi.componentregistry.common.Comment;
	import clarin.cmdi.componentregistry.common.ItemDescription;
	
	import com.adobe.net.URI;
	
	import flash.events.Event;
	import flash.events.EventDispatcher;
	import flash.events.HTTPStatusEvent;
	import flash.events.IOErrorEvent;
	import flash.events.ProgressEvent;
	import flash.events.SecurityErrorEvent;
	import flash.utils.ByteArray;
	
	import mx.managers.CursorManager;
	
	import ru.inspirit.net.MultipartURLLoader;
	
	[Event(name="postComplete", type="flash.events.Event")]
	public class CommentPostService extends EventDispatcher{
		
		public static const POST_COMPLETE:String = "postComplete";
		
		public function CommentPostService() {
		}
		
		[Bindable]
		public var message:String = "";
		
		private var ml:MultipartURLLoader;
		
		private function createAndInitRequest():void {
			CursorManager.setBusyCursor();
			ml = new MultipartURLLoader();
			ml.addEventListener(Event.COMPLETE, completeHandler);
			ml.addEventListener(ProgressEvent.PROGRESS, progressHandler);
			ml.addEventListener(HTTPStatusEvent.HTTP_STATUS, httpStatusHandler);
			ml.addEventListener(SecurityErrorEvent.SECURITY_ERROR, securityErrorHandler);
		}
		
		/**
		 * submits a comment
		 */
		public function upload(comment:Comment, itemDescription:ItemDescription):void {
			createAndInitRequest();
			var uri:URI = createUri(comment, itemDescription);
			submit(createByteArray(comment.toXml()), uri);
		}
		
		private function createUri(comment:Comment, itemDescription:ItemDescription):URI {
			var uri:URI = null;
			if(itemDescription){
				if (itemDescription.isProfile) {
				uri = new URI(Config.instance.getProfileCommentsPath(itemDescription.id));
				} else {
				uri = new URI(Config.instance.getComponentCommentsPath(itemDescription.id));
				}
			} else{
				return null;
			}
			
			return uri;
		}
		
		private function createByteArray(data:String):ByteArray {
			var result:ByteArray = null;
			if (data != null) {
				result = new ByteArray();
				result.writeUTFBytes(data);
				result.position = 0;
			}
			return result
		}
		
		private function submit(data:ByteArray, uri:URI):void {
			message = "";
			try {
				ml.addFile(data, "comment.xml", "data");
				ml.load(uri.toString());
			} catch (error:Error) {
				trace("Unable to post comment. Error: " + error);
				CursorManager.removeBusyCursor();
				throw error;
			}
		}
		
		private function completeHandler(event:Event):void {
			uploadComplete();
			var response:XML = new XML(ml.loader.data);
			handleResponse(response);
		}
		
		private function httpStatusHandler(event:HTTPStatusEvent):void {
			if (event.status != 200) {
				addToMessage("Server Failed to handle registration. Unexpected error, try again later. (httpstatus code was: " + event.status + ")\n");
			}
			CursorManager.removeBusyCursor();
		}
		
		private function securityErrorHandler(event:SecurityErrorEvent):void {
			addToMessage("Server Failed to handle registration. Unexpected error, try again later. (error: " + event.text + ")\n");
			CursorManager.removeBusyCursor();
		}
		
		private function progressHandler(event:ProgressEvent):void {
			trace("Uploading: " + event.bytesLoaded + "/" + event.bytesTotal);
		}
		
		private function startUploadHandler():void {
			trace("uploading start");
		}
		
		private function handleResponse(response:XML):void {
			if (response.@registered == true) {
				// TODO
				dispatchEvent(new Event(POST_COMPLETE));
			} else {
				createErrorMessage(response);
			}
		}
		
		private function uploadComplete():void {
			CursorManager.removeBusyCursor();
		}
		
		private function createErrorMessage(response:XML):void {
			addToMessage("Failed to register:");
			var errors:XMLList = response.errors.error;
			for each (var error:XML in errors) {
				message += " - " + error.toString() + "\n";
			}
		}
		
		private function addToMessage(text:String):void {
			message += text;
		}
		
	}
}
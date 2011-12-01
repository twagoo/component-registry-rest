package clarin.cmdi.componentregistry.services {
	
	import clarin.cmdi.componentregistry.common.Comment;
	import clarin.cmdi.componentregistry.common.CommentMD;
	import clarin.cmdi.componentregistry.common.CommentDescription;
	
	import com.adobe.net.URI;
	
	import flash.events.Event;
	import flash.events.EventDispatcher;
	
	import mx.controls.Alert;
	import mx.messaging.messages.HTTPRequestMessage;
	import mx.rpc.AsyncToken;
	import mx.rpc.Responder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.http.HTTPService;
	import mx.utils.StringUtil;
	
	
	[Event(name="CommentLoaded", type="flash.events.Event")]
	public class CommentInfoService extends EventDispatcher {
		public static const COMMENT_LOADED:String = "CommentLoaded";
		
		private var service:HTTPService;
		
		[Bindable]
		public var comment:Comment;

		public function CommentInfoService() {
			this.service = new HTTPService();
			this.service.method = HTTPRequestMessage.GET_METHOD;
			this.service.resultFormat = HTTPService.RESULT_FORMAT_E4X;
		}
		public function load(item:CommentDescription):void {
			comment = new Comment();
			comment.description = item;
			var url:URI = new URI(item.dataUrl);
			service.url = url.toString();
			var token:AsyncToken = this.service.send();
			token.addResponder(new Responder(result, fault));
		}
		
		private function result(resultEvent:ResultEvent):void {
			var resultXml:XML = resultEvent.result as XML;
			var metaData:CommentMD = new CommentMD();
			metaData.name = resultXml.CMD_Comment.@name;
			metaData.xml = resultXml;
			comment.commentMD = metaData;
			dispatchEvent(new Event(COMMENT_LOADED));
		}
		
		public function fault(faultEvent:FaultEvent):void {
			var errorMessage:String = StringUtil.substitute("Error in {0}: {1} - {2}", this, faultEvent.fault.faultString, faultEvent.fault.faultDetail);
			Alert.show(errorMessage);
		}
	}
}
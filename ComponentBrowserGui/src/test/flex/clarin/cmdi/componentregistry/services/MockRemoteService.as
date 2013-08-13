package clarin.cmdi.componentregistry.services
{
	import com.adobe.net.URI;
	
	import mx.rpc.AsyncToken;
	import mx.rpc.Responder;
	import mx.rpc.events.ResultEvent;
	
	import clarin.cmdi.componentregistry.services.remote.RemoteService;

	public class MockRemoteService implements RemoteService
	{
		
		public var token:AsyncToken;
		
		public function MockRemoteService()
		{
		}
		
		public function send():AsyncToken{
		
			token = new AsyncToken();
			return token;
		}
		
		public function setMethod(method:String):void{
		}
		
		public function setUrl(url:URI):void{
		}
		
		public function setResultFormat(format:String):void{
		}
		
		public function invokeResult(result:ResultEvent):void{
			var responder:Responder = token.responders[0];
			responder.result(result);
		}
	}
}
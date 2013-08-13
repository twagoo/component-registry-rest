package clarin.cmdi.componentregistry.services.remote
{
	import com.adobe.net.URI;
	
	import mx.rpc.AsyncToken;
	import mx.rpc.http.HTTPService;

	public class RemoteServiceWrapper implements RemoteService
	{
		protected var service:HTTPService;
		
		public function RemoteServiceWrapper()
		{
			service = new HTTPService();
		}
		
		
		public function setMethod(method:String):void{
			service.method = method;
		};
		
		public function setResultFormat(format:String):void{
			service.resultFormat = format;
		};
		
		public function setUrl(url:URI):void{
			service.url = url.toString();
		};
		
		public function send():AsyncToken{
			return service.send();
		};

	}
}
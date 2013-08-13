package clarin.cmdi.componentregistry.services.remote
{
	import com.adobe.net.URI;
	
	import mx.rpc.AsyncToken;

	public interface RemoteService
	{
		function setMethod(method:String):void;
		function setResultFormat(format:String):void;
		function setUrl(url:URI):void;
		function send():AsyncToken;
	}
}
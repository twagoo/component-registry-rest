package clarin.cmdi.componentregistry.services {
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
	
	import clarin.cmdi.componentregistry.services.remote.HttpServiceFactory;
	import clarin.cmdi.componentregistry.services.remote.RemoteService;
	
/**
 * Handles communication with any arbitrary XML backend service, implements error handling and dispatching of success events.
 * Extending classes must override handleXmlResult.
 * @author george.georgovassilis@mpi.nl
 */
	public class BaseRemoteService extends EventDispatcher {

		protected var successEventName:String;
		protected var readService:RemoteService;
		
		/**
		 * Constructor
		 * @param successEventName name of event that will be dispatched when the remote request succeeds
		 */
		public function BaseRemoteService(successEventName:String) {
			readService = HttpServiceFactory.createRemoteService();
			readService.setMethod(HTTPRequestMessage.GET_METHOD);
			readService.setResultFormat(HTTPService.RESULT_FORMAT_E4X);
			this.successEventName = successEventName;
		}

		/**
		 * Send a remote request to a url
		 * @param url URL to send request to
		 */
		protected function dispatchRequest(url:URI):void {
			readService.setUrl(url);
			var token:AsyncToken = readService.send();
			token.addResponder(new Responder(this.requestCallbackOk, this.requestCallbackFailed));
		}
		
		/**
		 * Invoked with XML result from a successful service call. Extending services must override.
		 */
		protected function handleXmlResult(resultXml:XML):void{
			throw new Error("Not implemented");
		}
		
		protected function requestCallbackOk(resultEvent:ResultEvent):void {
			var resultXml:XML = resultEvent.result as XML;
			this.handleXmlResult(resultXml);
			this.dispatchEvent(new Event(this.successEventName));
			
		}

		protected function requestCallbackFailed(faultEvent:FaultEvent):void {
			var errorMessage:String = StringUtil.substitute("Error in {0}: {1} - {2}", this, faultEvent.fault.faultString, faultEvent.fault.faultDetail);
			Alert.show(errorMessage);
		}
	}
}


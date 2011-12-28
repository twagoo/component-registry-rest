package clarin.cmdi.componentregistry.services
{
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
	
	[Event(name="itemsLoaded", type="flash.events.Event")]
	public class ComponentRegistryService extends EventDispatcher {
		private var service:HTTPService;
		
		private var serviceUrl:String;
		
		public function ComponentRegistryService(restUrl:String) {
			this.serviceUrl = restUrl;
			service = new HTTPService();
			service.method = HTTPRequestMessage.GET_METHOD;
			service.resultFormat = HTTPService.RESULT_FORMAT_E4X;
		}
		
		private function initService():void {
			var url:URI = new URI(serviceUrl);
			url.setQueryValue("unique", new Date().getTime().toString());
			initServiceUrl(url);
			service.url = url.toString();
		}
		
		public function load():void {
			initService();
			var token:AsyncToken = this.service.send();
			token.addResponder(new Responder(result, fault));
		}
		
		/**
		 * Override in concrete subclasses
		 */
		protected function result(resultEvent:ResultEvent):void {
		}
		
		public function fault(faultEvent:FaultEvent):void {
			var errorMessage:String = StringUtil.substitute("Error in {0}: \n Fault: {1} - {2}", this, faultEvent.fault.faultString, faultEvent.fault.faultDetail);
			trace(errorMessage);
			Alert.show("Internal Server error cannot process the data, try reloading the application.");
		}
		
		protected function initServiceUrl(url:URI):void{
		};
	}
}
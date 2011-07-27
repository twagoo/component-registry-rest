package clarin.cmdi.componentregistry.services
{
	import clarin.cmdi.componentregistry.common.ItemDescription;
	
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
	
	[Event(name="componentInUse", type="clarin.cmdi.componentregistry.services.ComponentUsageCheckEvent")]
	public class ComponentUsageService extends EventDispatcher
	{
		private var service:HTTPService;
		private var serviceUrl:String;
		
		public function ComponentUsageService(itemDescription:ItemDescription)
		{
			var url:URI = new URI(Config.instance.componentUsageUrl + itemDescription.id);

			service = new HTTPService();
			service.url = url.toString();
			service.method = HTTPRequestMessage.GET_METHOD;
			service.resultFormat = HTTPService.RESULT_FORMAT_E4X;
			service.showBusyCursor = true;
		}
				
		public function checkUsage():void{
			var token:AsyncToken = this.service.send();
			token.addResponder(new Responder(result, fault));
		}
		
		private function result(resultEvent:ResultEvent):void {
			var resultXml:XML = resultEvent.result as XML;
			dispatchEvent(new ComponentUsageCheckEvent(ComponentUsageCheckEvent.COMPONENT_IN_USE, true));
		}
		
		private function fault(faultEvent:FaultEvent):void {
			var errorMessage:String = StringUtil.substitute("Error in {0}: \n Fault: {1} - {2}", this, faultEvent.fault.faultString, faultEvent.fault.faultDetail);
			trace(errorMessage);
			Alert.show("Internal Server error cannot process the data, try reloading the application.");
		}
	}
}
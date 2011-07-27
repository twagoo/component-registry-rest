package clarin.cmdi.componentregistry.services
{
	import clarin.cmdi.componentregistry.common.ItemDescription;
	
	import com.adobe.net.URI;
	
	import flash.events.Event;
	import flash.events.EventDispatcher;
	
	import mx.collections.ArrayCollection;
	import mx.controls.Alert;
	import mx.messaging.messages.HTTPRequestMessage;
	import mx.rpc.AsyncToken;
	import mx.rpc.Responder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.http.HTTPService;
	import mx.utils.StringUtil;
	
	/**
	 * Service that calls registry/components/usage/{id} for the specified component, which is used to check
	 * whether the component is used by any other components or profiles in the same registry. 
	 */
	[Event(name="componentInUse", type="clarin.cmdi.componentregistry.services.ComponentUsageCheckEvent")]
	public class ComponentUsageService extends EventDispatcher
	{
		private var service:HTTPService;
		
		public function ComponentUsageService(itemDescription:ItemDescription, userSpace:Boolean)
		{
			var url:URI = new URI(Config.instance.componentUsageUrl + itemDescription.id);
			if (userSpace) {
				url.setQueryValue(Config.PARAM_USERSPACE, "true");
			}

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
			// Result contains list of items that use the specified component
			var resultXml:XML = resultEvent.result as XML;
			
			// Collect item names so the view can show them to the user
			var resultItems:ArrayCollection = new ArrayCollection();
			for each (var description:XML in resultXml.children()) {
				resultItems.addItem(description.name.toString());
			}
			
			var event:ComponentUsageCheckEvent = new ComponentUsageCheckEvent(ComponentUsageCheckEvent.COMPONENT_IN_USE, resultItems);					
			dispatchEvent(event);
		}
		
		private function fault(faultEvent:FaultEvent):void {
			var errorMessage:String = StringUtil.substitute("Error in {0}: \n Fault: {1} - {2}", this, faultEvent.fault.faultString, faultEvent.fault.faultDetail);
			trace(errorMessage);
			Alert.show("Internal Server error cannot process the data, try reloading the application.");
		}
	}
}
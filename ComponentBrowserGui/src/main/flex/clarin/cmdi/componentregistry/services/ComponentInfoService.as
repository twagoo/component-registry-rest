package clarin.cmdi.componentregistry.services {

	import clarin.cmdi.componentregistry.common.Component;
	import clarin.cmdi.componentregistry.common.ComponentMD;
	import clarin.cmdi.componentregistry.common.ItemDescription;
	
	import mx.controls.Alert;
	import mx.messaging.messages.HTTPRequestMessage;
	import mx.rpc.AsyncToken;
	import mx.rpc.Responder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.http.HTTPService;
	import mx.utils.StringUtil;

	[Event(name="ComponentLoaded", type="flash.events.Event")]
	public class ComponentInfoService {
		public static const COMPONENT_LOADED:String = "ComponentLoaded";

		private var service:HTTPService;

		[Bindable]
		public var component:Component;


		public function ComponentInfoService() {
			this.service = new HTTPService();
			this.service.method = HTTPRequestMessage.GET_METHOD;
			this.service.resultFormat = HTTPService.RESULT_FORMAT_E4X;
		}

		public function load(item:ItemDescription):void {
			this.component = new Component();
			component.description = item;
			this.service.url = item.dataUrl;
			var token:AsyncToken = this.service.send();
			token.addResponder(new Responder(result, fault));
		}

		private function result(resultEvent:ResultEvent):void {
			var resultXml:XML = resultEvent.result as XML;
			var metaData:ComponentMD = new ComponentMD();
			metaData.name = resultXml.CMD_Component.@name;
			metaData.xml = resultXml;
			component.componentMD = metaData;
			dispatchEvent(new Event(COMPONENT_LOADED));
		}

		public function fault(faultEvent:FaultEvent):void {
			var errorMessage:String = StringUtil.substitute("Error in {0}: {1} - {2}", this, faultEvent.fault.faultString, faultEvent.fault.faultDetail);
			Alert.show(errorMessage);
		}
	}
}


package clarin.cmdi.componentregistry.services {
	import clarin.cmdi.componentregistry.Component;
	import clarin.cmdi.componentregistry.ComponentMD;
	import clarin.cmdi.componentregistry.ItemDescription;
	
	import mx.messaging.messages.HTTPRequestMessage;
	import mx.rpc.AsyncToken;
	import mx.rpc.Responder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.http.HTTPService;
	import mx.utils.StringUtil;

	public class ComponentInfoService {

		private var service:HTTPService;

		[Bindable]
		public var component:Component;


		public function ComponentInfoService() {
			this.service=new HTTPService();
			this.service.method=HTTPRequestMessage.POST_METHOD;
			this.service.resultFormat=HTTPService.RESULT_FORMAT_E4X;
			//Registry.serviceUrl; IoC it in!
		}

		public function load(item:ItemDescription):void {
			this.component=new Component();
			component.description=item;
			this.service.url="http://localhost:8080/ComponentRegistry/rest/registry/components/" + item.id;
			var token:AsyncToken=this.service.send();
			token.addResponder(new Responder(result, fault));
		}

		private function result(resultEvent:ResultEvent):void {
			var resultXml:XML=resultEvent.result as XML;
			var metaData:ComponentMD=new ComponentMD();
			metaData.name=resultXml.CMD_Component.@name;
			metaData.xml=resultXml;
			component.componentMD=metaData;
		}

		public function fault(faultEvent:FaultEvent):void {
			var errorMessage:String=StringUtil.substitute("Error in {0}: {1} - {2}", this, faultEvent.fault.faultString, faultEvent.fault.faultDetail);
			throw new Error(errorMessage);
		}
	}
}


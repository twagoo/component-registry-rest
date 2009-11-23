package clarin.cmdi.componentregistry.services {
	import clarin.cmdi.componentregistry.ComponentMD;
	import clarin.cmdi.componentregistry.ItemDescription;
	import clarin.cmdi.componentregistry.Profile;
	
	import mx.collections.ArrayCollection;
	import mx.messaging.messages.HTTPRequestMessage;
	import mx.rpc.AsyncToken;
	import mx.rpc.Responder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.http.HTTPService;
	import mx.utils.StringUtil;

	public class ProfileInfoService {

		private var service:HTTPService;

		[Bindable]
		public var profile:Profile;


		public function ProfileInfoService() {
			this.service = new HTTPService();
			this.service.method = HTTPRequestMessage.GET_METHOD;
			this.service.resultFormat = HTTPService.RESULT_FORMAT_E4X;
			//Registry.serviceUrl; IoC it in!
		}

		public function load(item:ItemDescription):void {
			profile = new Profile();
			if (item != null) {
				profile.description = item;
				this.service.url = Config.instance.getUrl(Config.PROFILE_INFO_SERVICE) + item.id;
				var token:AsyncToken = this.service.send();
				token.addResponder(new Responder(result, fault));
			}
		}

		private function result(resultEvent:ResultEvent):void {
			var resultXml:XML = resultEvent.result as XML;
			var nodes:XMLList = resultXml.CMD_Component;

			profile.nrOfComponents = nodes.length();
			profile.profileSource = resultXml;
			var tempArray:Array = new Array();
			for each (var node:XML in nodes) {
				var component:ComponentMD = new ComponentMD();
				component.name = node.@name;
				component.xml = node;
				tempArray[tempArray.length] = component;
			}
			this.profile.components = new ArrayCollection(tempArray);
		}

		public function fault(faultEvent:FaultEvent):void {
			var errorMessage:String = StringUtil.substitute("Error in {0}: {1} - {2}", this, faultEvent.fault.faultString, faultEvent.fault.faultDetail);
			throw new Error(errorMessage);
		}
	}
}


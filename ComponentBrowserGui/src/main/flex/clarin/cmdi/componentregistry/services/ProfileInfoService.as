package clarin.cmdi.componentregistry.services {
	import clarin.cmdi.componentregistry.common.ComponentMD;
	import clarin.cmdi.componentregistry.common.ItemDescription;
	import clarin.cmdi.componentregistry.common.Profile;
	
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

	[Event(name="ProfileLoaded", type="flash.events.Event")]
	public class ProfileInfoService  extends EventDispatcher {
		public static const PROFILE_LOADED:String = "ProfileLoaded";

		private var service:HTTPService;

		[Bindable]
		public var profile:Profile;


		public function ProfileInfoService() {
			this.service = new HTTPService();
			this.service.method = HTTPRequestMessage.GET_METHOD;
			this.service.resultFormat = HTTPService.RESULT_FORMAT_E4X;
		}

		public function load(item:ItemDescription):void {
			profile = new Profile();
			profile.description = item;
			this.service.url = item.dataUrl;
			var token:AsyncToken = this.service.send();
			token.addResponder(new Responder(result, fault));
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
			dispatchEvent(new Event(PROFILE_LOADED));
		}

		public function fault(faultEvent:FaultEvent):void {
			var errorMessage:String = StringUtil.substitute("Error in {0}: {1} - {2}", this, faultEvent.fault.faultString, faultEvent.fault.faultDetail);
		    Alert.show(errorMessage);
		}
	}
}


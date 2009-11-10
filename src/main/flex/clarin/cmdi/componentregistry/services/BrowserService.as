package clarin.cmdi.componentregistry.services {
	import mx.collections.ArrayCollection;
	import mx.controls.Alert;
	import mx.messaging.messages.HTTPRequestMessage;
	import mx.rpc.AsyncToken;
	import mx.rpc.Responder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.http.HTTPService;
	import mx.utils.StringUtil;

	public class BrowserService {

		private var service:HTTPService;

		/**
		 * Typed ArrayCollection publicly available for outside code to bind to and watch.
		 */
		[Bindable]
		[ArrayElementType("ItemDescription")]
		public var itemDescriptions:ArrayCollection;

		public function BrowserService(restUrl:String) {
			this.service = new HTTPService();
			this.service.method = HTTPRequestMessage.GET_METHOD;
			this.service.resultFormat = HTTPService.RESULT_FORMAT_E4X;
			this.service.url = restUrl;
			//Registry.serviceUrl; IoC it in!
		}

		public function load():void {
			var token:AsyncToken = this.service.send();
			token.addResponder(new Responder(result, fault));
		}

		/**
		 * Override in concrete subclasses
		 */
		protected function result(resultEvent:ResultEvent):void {
		}

		public function fault(faultEvent:FaultEvent):void {
			var errorMessage:String = StringUtil.substitute("Error in {0}: {1} - {2}", this, faultEvent.fault.faultString, faultEvent.fault.faultDetail);
			Alert.show("Got profile error:" + errorMessage);
			throw new Error(errorMessage);
		}

		protected function setItemDescriptions(items:ArrayCollection):void {
			this.itemDescriptions = items;
		}
	}
}


package clarin.cmdi.componentregistry.services {
	import clarin.cmdi.componentregistry.common.ItemDescription;
	
	import flash.events.Event;
	
	import mx.collections.ArrayCollection;
	import mx.containers.TitleWindow;
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

		private var serviceUrl:String;

		// Not bindable needed for lookups over the whole collections of itemDescriptions
		protected var unFilteredItemDescriptions:ArrayCollection;

		public function BrowserService(restUrl:String) {
			this.serviceUrl = restUrl;
			service = new HTTPService();
			service.method = HTTPRequestMessage.GET_METHOD;
			service.resultFormat = HTTPService.RESULT_FORMAT_E4X;
			Config.instance.addEventListener(Config.USER_SPACE_TOGGLE_EVENT, toggleUserSpace);
		}

		private function toggleUserSpace(event:Event):void {
			load(Config.instance.userSpace);
		}

		private function initService(userSpace:Boolean):void {
			service.url = serviceUrl + "?" + new Date().getTime();
			if (userSpace) {
				service.url += "&userspace=true";
			}
		}

		public function load(userSpace:Boolean = false):void {
			initService(userSpace);
			var token:AsyncToken = this.service.send();
			token.addResponder(new Responder(result, fault));
		}

		/**
		 * Override in concrete subclasses
		 */
		protected function result(resultEvent:ResultEvent):void {
		}

		public function fault(faultEvent:FaultEvent):void {
			var errorMessage:String = StringUtil.substitute("Error in {0}: Message: {1} \n Fault: {2} - {3}", this, faultEvent.message, faultEvent.fault.faultString, faultEvent.fault.faultDetail);
			Alert.show(errorMessage);
		}

		protected function setItemDescriptions(items:ArrayCollection):void {
			itemDescriptions = items;
			unFilteredItemDescriptions = new ArrayCollection(); //create a copy
			for each (var item:Object in items) {
				unFilteredItemDescriptions.addItem(item);
			}
		}
	}
}


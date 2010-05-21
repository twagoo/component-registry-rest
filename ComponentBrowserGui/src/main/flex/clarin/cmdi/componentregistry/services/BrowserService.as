package clarin.cmdi.componentregistry.services {
	import clarin.cmdi.componentregistry.common.ItemDescription;

	import com.adobe.net.URI;

	import flash.net.URLRequest;
	import flash.net.navigateToURL;

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

		private var serviceUrl:String;

		// Not bindable needed for lookups over the whole collections of itemDescriptions
		protected var unFilteredItemDescriptions:ArrayCollection;
		protected var userSpace:Boolean;

		public function BrowserService(restUrl:String, userSpace:Boolean) {
			this.serviceUrl = restUrl;
			service = new HTTPService();
			service.method = HTTPRequestMessage.GET_METHOD;
			service.resultFormat = HTTPService.RESULT_FORMAT_E4X;
			this.userSpace = userSpace;
		}

		private function initService():void {
			var url:URI = new URI(serviceUrl);
			url.setQueryValue("unique", new Date().getTime().toString());
			if (userSpace) {
				url.setQueryValue(Config.USERSPACE_PARAM, "true");
			}
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

		protected function setItemDescriptions(items:ArrayCollection):void {
			itemDescriptions = items;
			unFilteredItemDescriptions = new ArrayCollection(); //create a copy
			for each (var item:Object in items) {
				unFilteredItemDescriptions.addItem(item);
			}
		}
	}
}


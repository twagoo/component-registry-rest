package clarin.cmdi.componentregistry.services {

	import flash.events.TimerEvent;
	import flash.utils.Timer;
	
	import mx.controls.Alert;
	import mx.messaging.messages.HTTPRequestMessage;
	import mx.rpc.AsyncToken;
	import mx.rpc.Responder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.http.HTTPService;
	import mx.utils.StringUtil;


	public class PingSessionService {

		[Bindable]
		public var stillActive:Boolean

		public static const INSTANCE:PingSessionService = new PingSessionService();
        private static const PING_INTERVAL:int = 1000 * 60 * 5; //5 minutes
		private var timer:Timer = new Timer(PING_INTERVAL, 0);
		private var service:HTTPService;

		/**
		 * Use INSTANCE field
		 */
		function PingSessionService() {
			this.service = new HTTPService();
			this.service.method = HTTPRequestMessage.GET_METHOD;
			this.service.resultFormat = HTTPService.RESULT_FORMAT_E4X;
		}

		public function startPinging():void {
			timer.addEventListener(TimerEvent.TIMER, timerHandler);
			timer.start();
		}

		public function timerHandler(event:TimerEvent):void {
			pingSession();
		}

		private function pingSession():void {
			service.url = Config.instance.pingSessionUrl;
			var token:AsyncToken = service.send();
			token.addResponder(new Responder(result, fault));
		}

		private function result(resultEvent:ResultEvent):void {
			stillActive = resultEvent.result.@stillActive == "true";
			if (!stillActive) {
			    Alert.show("Your session has expired, please reload the application to refresh the session.");
			    timer.stop(); 
			}
		}

		public function fault(faultEvent:FaultEvent):void {
			timer.stop(); // We stop to bother the user with more errors. Somethings is wrong user needs probably to reload which will start the timer again.
			var errorMessage:String = StringUtil.substitute("Error in {0}: {1} - {2}", this, faultEvent.fault.faultString, faultEvent.fault.faultDetail);
			Alert.show(errorMessage);
		}





	}
}
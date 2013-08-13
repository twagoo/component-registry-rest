package clarin.cmdi.componentregistry.services {

	import com.adobe.net.URI;
	
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
	
	import clarin.cmdi.componentregistry.common.Credentials;


	public class PingSessionService extends BaseRemoteService{

		[Bindable]
		public var stillActive:Boolean

		public static const INSTANCE:PingSessionService = new PingSessionService();
        private static const PING_INTERVAL:int = 1000 * 60 * 5; //5 minutes
		private var timer:Timer = new Timer(PING_INTERVAL, 0);

		/**
		 * Use INSTANCE field
		 */
		function PingSessionService() {
			super("PingOk");
		}

		public function startPinging():void {
			timer.addEventListener(TimerEvent.TIMER, timerHandler);
			timer.start();
		}

		public function timerHandler(event:TimerEvent):void {
			pingSession();
		}

		private function pingSession():void {
		    if (Credentials.instance.isLoggedIn()) {
				dispatchRequest(new URI(Config.instance.pingSessionUrl));
		    }
		}

		override protected function handleXmlResult(resultXml:XML):void{
			stillActive = resultXml.@stillActive == "true";
			if (!stillActive) {
			    Alert.show("Your session has expired, please reload the application to refresh the session.");
			    timer.stop(); 
			}
		}

		override protected function requestCallbackFailed(faultEvent:FaultEvent):void {
			timer.stop(); // We stop to bother the user with more errors. Somethings is wrong user needs probably to reload which will start the timer again.
			super.requestCallbackFailed(faultEvent);
		}
	}
}
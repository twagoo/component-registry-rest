package clarin.cmdi.componentregistry.common.components {
	import clarin.cmdi.componentregistry.services.Config;

	import com.adobe.net.URI;

	import flash.events.MouseEvent;
	import flash.net.URLRequest;
	import flash.net.navigateToURL;

	import mx.controls.Label;

	public class LoginLabelButton extends Label {

		[Bindable]
		public var viewType:String;
		[Bindable]
		public var spaceType:String;
		[Bindable]
		public var itemId:String;

		public function LoginLabelButton() {
			super();
			text = "login";
			toolTip = "Click to login";
			setStyle("color", "green");
			setStyle("textDecoration", "underline")
			addEventListener(MouseEvent.CLICK, handleLogin);
		}

		private function handleLogin(event:MouseEvent):void {
			var req:URLRequest = new URLRequest();

			var uri:URI = new URI(Config.instance.serviceRootUrl);
			uri.setQueryValue("shhaaDo", "lI");
			if (viewType) {
				uri.setQueryValue(Config.REGISTRY_PARAM_VIEW, viewType);
			}
			if (spaceType) {
				uri.setQueryValue(Config.REGISTRY_PARAM_SPACE, spaceType);
			}
			if (itemId) {
				uri.setQueryValue(Config.REGISTRY_PARAM_ITEM, itemId);
			}
			req.url = uri.toString();
			navigateToURL(req, "_top");
		}

	}
}
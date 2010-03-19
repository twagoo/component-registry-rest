package clarin.cmdi.componentregistry.common.components {
	import clarin.cmdi.componentregistry.common.ItemDescription;

	import flash.events.ErrorEvent;
	import flash.events.Event;
	import flash.net.FileReference;
	import flash.net.URLRequest;
	import flash.net.navigateToURL;

	import mx.controls.Alert;

	public class SaveItemDialog {


		public function SaveItemDialog() {
		}

		public function saveAsXML(item:ItemDescription):void {
			save(item, "xml");
		}

		public function saveAsXSD(item:ItemDescription):void {
			save(item, "xsd");
		}

		private function save(item:ItemDescription, extension:String, urlVariable:String = null):void {
			var req:URLRequest = new URLRequest();
			req.url = item.dataUrl + "/" + extension;
			navigateToURL(req, "_top");
		}

	}
}
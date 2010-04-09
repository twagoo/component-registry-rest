package clarin.cmdi.componentregistry.common.components {
	import clarin.cmdi.componentregistry.common.ItemDescription;

	import flash.events.ErrorEvent;
	import flash.events.Event;
	import flash.events.IOErrorEvent;
	import flash.events.SecurityErrorEvent;
	import flash.net.FileReference;
	import flash.net.URLRequest;
	import flash.net.URLVariables;

	import mx.controls.Alert;

	public class SaveItemDialog {

		private var ref:FileReference;

		public function SaveItemDialog() {
		}

		public function saveAsXML(item:ItemDescription):void {
			save(item, "xml");
		}

		public function saveAsXSD(item:ItemDescription):void {
			save(item, "xsd");
		}

		private function save(item:ItemDescription, extension:String, urlVariable:String = null):void {
			ref = new FileReference()
			var req:URLRequest = new URLRequest();
			req.url = item.dataUrl + "/" + extension;
			try {
				ref.download(req, item.name + "." + extension);
			} catch (error:Error) {
				trace("Unable to download file.");
			}
			ref.addEventListener(Event.COMPLETE, saveComplete);
			ref.addEventListener(IOErrorEvent.IO_ERROR, saveError);
			ref.addEventListener(SecurityErrorEvent.SECURITY_ERROR, saveError);
		}

		private function saveComplete(event:Event):void {
			Alert.show("Saved.", "OK", Alert.OK);
		}

		private function saveError(event:ErrorEvent):void {
			Alert.show("Error: " + event.text, event.type, Alert.OK);
		}


	}
}
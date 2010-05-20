package clarin.cmdi.componentregistry.services {
	import clarin.cmdi.componentregistry.common.ItemDescription;

	import com.adobe.net.URI;

	import flash.net.URLRequest;
	import flash.net.navigateToURL;

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
			var url:URI = new URI(item.dataUrl + "/" + extension);
			if (item.isInUserSpace) {
				url.setQueryValue(Config.USERSPACE_PARAM, "true");
			}
			req.url = url.toString();
			navigateToURL(req, "_top");
		}

	}
}
package clarin.cmdi.componentregistry.common.components {
	import flash.events.MouseEvent;

	public class XMLTextArea extends ScrollableTextArea {

		private static const BLOCKSIZE:int = 10;

		[Bindable]
		public var hasData:Boolean = false;

		public function XMLTextArea() {
			super();
		}

		/**
		 * data can be XML or XMLList any other type will result in super.setData() call.
		 * The XML/XMLList will be transformed into htmltext
		 *
		 */
		public function set xmlData(data:Object):void {
			var result:String;
			if (data is XML) {
				var xmlData:XML = data as XML;
				result = printElements(xmlData.elements(), 0);
				hasData = result.length > 0;
				super.htmlText = result;
			} else if (data is XMLList) {
				result = printElements(data as XMLList, 0);
				hasData = result.length > 0;
				super.htmlText = result;
			} else {
				super.data = data;
			}
		}


		private function printElement(elem:XML, indent:int):String {
			var result:String = "";
			var blockIndentSize:int = BLOCKSIZE * indent;
			if (elem.text().length() != 0) {
				result += "<textformat blockindent=\"" + blockIndentSize + "\"><b>" + elem.name() + "</b> = " + elem.text() + "</textformat><br>";
			} else {
				result += "<textformat blockindent=\"" + blockIndentSize + "\"><b>" + elem.name() + "</b>:</textformat><br>";
			}
			result += printAttributes(elem.attributes(), indent + 1);
			result += printElements(elem.elements(), indent + 1);
			return result;
		}

		private function printElements(elements:XMLList, indent:int):String {
			var result:String = "";
			for each (var elem:XML in elements) {
				result += printElement(elem, indent);
			}
			return result;
		}

		private function printAttributes(attributes:XMLList, indent:int):String {
			var result:String = "";
			var blockIndentSize:int = BLOCKSIZE * indent;
			for each (var attr:XML in attributes) {
				result += "<textformat blockindent=\"" + blockIndentSize + "\"><b>" + attr.name() + "</b> = " + attr + "</textformat><br>";
			}
			return result;
		}


	}
}
package clarin.cmdi.componentregistry.common.components {
	import mx.collections.ArrayCollection;

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
			var result:String = "";
			var xmlLines:ArrayCollection = XMLUtils.getXmlLines(data);
			for each (var elem:XMLLine in xmlLines) {
				var blockIndentSize:int = BLOCKSIZE * elem.indent;
				if (elem.hasValue()) {
					result += "<textformat blockindent=\"" + blockIndentSize + "\"><b>" + elem.name + "</b> = " + elem.value + "</textformat><br>";
				} else {
					result += "<textformat blockindent=\"" + blockIndentSize + "\"><b>" + elem.name + "</b>:</textformat><br>";
				}
			}
			hasData = result.length > 0;
			if (hasData) {
				super.htmlText = result;
			} else {
				super.data = data;
			}
		}

	}
}
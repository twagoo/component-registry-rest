package clarin.cmdi.componentregistry.common.components {
	import mx.collections.ArrayCollection;

	public class XMLUtils {
		public function XMLUtils() {
		}

		/**
		 * data can be XML or XMLList any other type will result in super.setData() call.
		 * returns ArrayCollections of XMLLines.
		 *
		 */
		public static function getXmlLines(data:Object):ArrayCollection {
			var result:ArrayCollection = new ArrayCollection();
			if (data is XML) {
				var xmlData:XML = data as XML;
				listElements(xmlData.elements(), 0, result);
			} else if (data is XMLList) {
				listElements(data as XMLList, 0, result);
			}
			return result;
		}


		private static function listElements(elements:XMLList, indent:int, xmlList:ArrayCollection):void {
			for each (var elem:XML in elements) {
				listElement(elem, indent, xmlList);
			}
		}

		private static function listElement(elem:XML, indent:int, xmlList:ArrayCollection):void {
			var xmlLine:XMLLine = new XMLLine(elem, elem.name(), elem.text(), indent);
			xmlList.addItem(xmlLine);
			listAttributes(elem.attributes(), indent + 1, xmlList);
			listElements(elem.elements(), indent + 1, xmlList);
		}


		private static function listAttributes(attributes:XMLList, indent:int, xmlList:ArrayCollection):void {
			for each (var attr:XML in attributes) {
				var xmlLine:XMLLine = new XMLLine(attr, attr.name(), attr.toString(), indent);
				xmlLine.isAttribute = true;
				xmlList.addItem(xmlLine);
			}
		}
	}
}
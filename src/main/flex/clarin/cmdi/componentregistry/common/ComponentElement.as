package clarin.cmdi.componentregistry.common {
	import mx.collections.XMLListCollection;

	[Bindable]
	public class ComponentElement {

		public var name:String;
		public var valueScheme:Object;
		public var conceptLink:String;

		public function ComponentElement() {
		}

		private static function setValueScheme(value:XML, item:ComponentElement):void {
			if (value.hasOwnProperty(ComponentMD.VALUE_SCHEME)) { 
				if (value.ValueScheme.hasOwnProperty(ComponentMD.PATTERN)) {
					var d:Object = value.ValueScheme.pattern
					item.valueScheme = d.name() + "(" + d.text().toXMLString() + ")";
				} else if (value.ValueScheme.hasOwnProperty(ComponentMD.ENUMERATION)) {
					item.valueScheme = new XMLListCollection(value.ValueScheme.enumeration.*);
				}
			} else if (value.hasOwnProperty("@"+ComponentMD.VALUE_SCHEME)) {
				item.valueScheme = value.@ValueScheme;
			} else if (value.hasOwnProperty(ComponentMD.TYPE)) {
				item.valueScheme = value.Type;
			}
		}

		public static function createAttributeItem(attribute:XML):ComponentElement {
			var item:ComponentElement = new ComponentElement();
			item.name = attribute.Name;
			item.valueScheme = attribute.Type;
			return item;
		}

		public static function createElementItem(element:XML):ComponentElement {
			var item:ComponentElement = new ComponentElement();
			item.name = element.@name;
			setValueScheme(element, item);
			item.conceptLink = element.@ConceptLink;
			return item;
		}

	}
}
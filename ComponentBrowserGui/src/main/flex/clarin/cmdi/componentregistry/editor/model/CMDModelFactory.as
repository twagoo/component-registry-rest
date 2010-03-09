package clarin.cmdi.componentregistry.editor.model {
	import clarin.cmdi.componentregistry.common.ComponentMD;

	import mx.collections.ArrayCollection;
	import mx.collections.XMLListCollection;

	public final class CMDModelFactory {
		public function CMDModelFactory() {
		}

		public static function createModel(xml:XML):CMDSpec {
			var result:CMDSpec = new CMDSpec(xml.@isProfile);
			result.headerName = xml.Header.Name;
			result.headerId = xml.Header.ID;
			result.headerDescription = xml.Header.Description;
			var components:XMLList = xml.elements(ComponentMD.CMD_COMPONENT);
			for each (var component:XML in components) {
				var cmdComp:CMDComponent = createComponent(component);
				result.cmdComponents.addItem(cmdComp);
			}
			return result;
		}

		public static function createComponent(xml:XML):CMDComponent {
			var result:CMDComponent = new CMDComponent();
			result.name = xml.@name;
			result.componentId = xml.@ComponentId;
			result.conceptLink = xml.@ConceptLink;
			result.filename = xml.@filename;
			result.cardinalityMin = xml.@CardinalityMin;
			result.cardinalityMax = xml.@CardinalityMax;
			result.attributeList = createAttributeList(xml);
			var elements:XMLList = xml.elements(ComponentMD.CMD_ELEMENT);
			for each (var element:XML in elements) {
				var cmdElement:CMDComponentElement = createComponentElement(element);
				result.cmdElements.addItem(cmdElement);
			}
			var components:XMLList = xml.elements(ComponentMD.CMD_COMPONENT);
			for each (var component:XML in components) {
				var cmdComponent:CMDComponent = createComponent(component);
				result.cmdComponents.addItem(cmdComponent);
			}
			return result;
		}

		public static function createComponentElement(xml:XML):CMDComponentElement {
			var result:CMDComponentElement = new CMDComponentElement();
			result.name = xml.@name;
			result.conceptLink = xml.@ConceptLink;
			result.valueSchemeSimple = xml.@ValueScheme;
			result.cardinalityMin = xml.@CardinalityMin;
			result.cardinalityMax = xml.@CardinalityMax;
			result.attributeList = createAttributeList(xml);
			if (xml.hasOwnProperty(ComponentMD.VALUE_SCHEME)) {
				if (xml.ValueScheme.hasOwnProperty(ComponentMD.PATTERN)) {
					result.valueSchemePattern = xml.ValueScheme.pattern;
				} else if (xml.ValueScheme.hasOwnProperty(ComponentMD.ENUMERATION)) {
					result.valueSchemeEnumeration = new XMLListCollection(xml.ValueScheme.enumeration.*);
				}
			}
			return result;
		}

		public static function createAttributeList(xml:XML):ArrayCollection {
			var result:ArrayCollection = new ArrayCollection();
			if (xml.hasOwnProperty(ComponentMD.ATTRIBUTE_LIST)) {
				var attributes:XMLList = xml.AttributeList.descendants(ComponentMD.ATTRIBUTE);
				for each (var attribute:XML in attributes) {
					var cmdAttribute:CMDAttribute = createAttribute(attribute);
					result.addItem(cmdAttribute);
				}
			}
			return result;
		}

		public static function createAttribute(xml:XML):CMDAttribute {
			var result:CMDAttribute = new CMDAttribute();
			result.name = xml.Name;
			if (xml.hasOwnProperty(ComponentMD.TYPE)) {
				result.type = xml.Type;
			} else {
				if (xml.ValueScheme.hasOwnProperty(ComponentMD.PATTERN)) {
					result.valueSchemePattern = xml.ValueScheme.pattern;
				} else if (xml.ValueScheme.hasOwnProperty(ComponentMD.ENUMERATION)) {
					result.valueSchemeEnumeration = new XMLListCollection(xml.ValueScheme.enumeration.*);
				}
			}
			return result;
		}

	}
}
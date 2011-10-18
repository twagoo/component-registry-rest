package clarin.cmdi.componentregistry.editor.model {
	import clarin.cmdi.componentregistry.common.ComponentMD;
	import clarin.cmdi.componentregistry.common.ItemDescription;
	import clarin.cmdi.componentregistry.editor.ValueSchemeItem;

	import mx.collections.ArrayCollection;
	import mx.collections.XMLListCollection;

	public final class CMDModelFactory {
		public function CMDModelFactory() {
		}

		public static function createModel(xml:XML, description:ItemDescription):CMDSpec {
			var result:CMDSpec = new CMDSpec(xml.@isProfile == "true");
			result.headerName = xml.Header.Name;
			result.headerId = xml.Header.ID;
			result.headerDescription = xml.Header.Description;
			result.groupName = description.groupName;
			result.domainName = description.domainName;
			var components:XMLList = xml.elements(ComponentMD.CMD_COMPONENT);
			for each (var component:XML in components) {
				var cmdComp:CMDComponent = createComponent(component);
				result.cmdComponents.addItem(cmdComp);
			}
			return result;
		}

		private static function createComponent(xml:XML):CMDComponent {
			var result:CMDComponent = new CMDComponent();
			result.name = xml.@name;
			result.componentId = xml.@ComponentId;
			result.conceptLink = xml.@ConceptLink;
			result.filename = xml.@filename;
			if (xml.hasOwnProperty("@CardinalityMin"))
				result.cardinalityMin = xml.@CardinalityMin;
			if (xml.hasOwnProperty("@CardinalityMax"))
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

		private static function createComponentElement(xml:XML):CMDComponentElement {
			var result:CMDComponentElement = new CMDComponentElement();
			result.name = xml.@name;
			result.conceptLink = xml.@ConceptLink;
			result.documentation = xml.@Documentation;
			result.displayPriority = xml.@DisplayPriority;
			if (xml.hasOwnProperty("@CardinalityMin"))
				result.cardinalityMin = xml.@CardinalityMin;
			if (xml.hasOwnProperty("@CardinalityMax"))
				result.cardinalityMax = xml.@CardinalityMax;
			if (xml.hasOwnProperty("@Multilingual"))
				result.multilingual = xml.@Multilingual;
			result.attributeList = createAttributeList(xml);
			if (xml.hasOwnProperty(ComponentMD.VALUE_SCHEME)) {
				if (xml.ValueScheme.hasOwnProperty(ComponentMD.PATTERN)) {
					result.valueSchemePattern = xml.ValueScheme.pattern;
				} else if (xml.ValueScheme.hasOwnProperty(ComponentMD.ENUMERATION)) {
					var xmlList:XMLListCollection = new XMLListCollection(xml.ValueScheme.enumeration.*);
					var valueSchemeItems:ArrayCollection = new ArrayCollection();
					for each (var xml:XML in xmlList) {
						valueSchemeItems.addItem(new ValueSchemeItem(String(xml.text()), String(xml.@AppInfo), String(xml.@ConceptLink)));
					}
					result.valueSchemeEnumeration = valueSchemeItems;
				}
			} else if (xml.hasOwnProperty("@ValueScheme")) {
				result.valueSchemeSimple = xml.@ValueScheme;
			} else {
				result.valueSchemeSimple = "";
			}
			return result;
		}

		private static function createAttributeList(xml:XML):ArrayCollection {
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

		private static function createAttribute(xml:XML):CMDAttribute {
			var result:CMDAttribute = new CMDAttribute();
			result.name = xml.Name;
			result.conceptLink = xml.ConceptLink;
			if (xml.hasOwnProperty(ComponentMD.TYPE)) {
				result.valueSchemeSimple = xml.Type;
			} else {
				if (xml.ValueScheme.hasOwnProperty(ComponentMD.PATTERN)) {
					result.valueSchemePattern = xml.ValueScheme.pattern;
				} else if (xml.ValueScheme.hasOwnProperty(ComponentMD.ENUMERATION)) {
					var xmlList:XMLListCollection = new XMLListCollection(xml.ValueScheme.enumeration.*);
					var valueSchemeItems:ArrayCollection = new ArrayCollection();
					for each (var xml:XML in xmlList) {
						valueSchemeItems.addItem(new ValueSchemeItem(String(xml.text()), String(xml.@AppInfo), String(xml.@ConceptLink)));
					}
					result.valueSchemeEnumeration = valueSchemeItems;
				}
			}
			return result;
		}

	}
}
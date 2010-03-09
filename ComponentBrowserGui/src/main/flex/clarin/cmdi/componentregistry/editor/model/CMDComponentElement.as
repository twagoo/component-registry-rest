package clarin.cmdi.componentregistry.editor.model {
	import clarin.cmdi.componentregistry.common.XmlAble;

	import mx.collections.ArrayCollection;
	import mx.collections.XMLListCollection;

	public class CMDComponentElement implements XmlAble {

		//Attributes
		public var name:String;
		public var conceptLink:String;
		public var valueSchemeSimple:String;
		public var cardinalityMin:String = "1";
		public var cardinalityMax:String = "1";


		//elements
		public var attributeList:ArrayCollection = new ArrayCollection();
		public var valueSchemeEnumeration:XMLListCollection;
		public var valueSchemePattern:String;

		public function CMDComponentElement() {
		}



		public function toXml():XML {
			var result:XML = <CMD_Element></CMD_Element>; //TODO PD implement
			if (name)
				result.@name = name;
			if (conceptLink)
				result.@ConceptLink = conceptLink;
			if (valueSchemeSimple)
				result.@ValueScheme = valueSchemeSimple;
			if (cardinalityMin)
				result.@CardinalityMin = cardinalityMin;
			if (cardinalityMax)
				result.@CardinalityMax = cardinalityMax;
			if (attributeList.length > 0) {
				var attributeListTag:XML = <AttributeList></AttributeList>;
				for each (var attribute:CMDAttribute in attributeList) {
					attributeListTag.appendChild(attribute.toXml());
				}
				result.appendChild(attributeListTag);
			}
			if (valueSchemePattern) {
				result.appendChild(<ValueScheme><pattern></pattern>{valueSchemePattern}</ValueScheme>)
			}
			if (valueSchemeEnumeration != null) {
				var enumerationScheme:XML = <ValueScheme><enumeration></enumeration></ValueScheme>;
				enumerationScheme.appendChild(valueSchemeEnumeration);
				result.appendChild(enumerationScheme);
			}
			return result;
		}


	}
}
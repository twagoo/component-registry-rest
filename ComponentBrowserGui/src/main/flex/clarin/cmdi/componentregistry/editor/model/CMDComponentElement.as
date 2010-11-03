package clarin.cmdi.componentregistry.editor.model {
	import clarin.cmdi.componentregistry.common.XmlAble;
	import clarin.cmdi.componentregistry.editor.ValueSchemePopUp;
	
	import mx.collections.ArrayCollection;
	import mx.collections.XMLListCollection;

    [Bindable]
	public class CMDComponentElement implements XmlAble, ValueSchemeInterface {

		//Attributes
		public var name:String;
		public var conceptLink:String;
		public var documentation:String;
		public var displayPriority:String;
		public var multilingual:String;
		private var _valueSchemeSimple:String;
		public var cardinalityMin:String = "1";
		public var cardinalityMax:String = "1";


		//elements
		public var attributeList:ArrayCollection = new ArrayCollection();
		private var _valueSchemeEnumeration:XMLListCollection;
		private var _valueSchemePattern:String;

		public function CMDComponentElement() {
		}
		
		public static function createEmptyElement():CMDComponentElement {
            var result:CMDComponentElement = new CMDComponentElement();
			result.valueSchemeSimple = ValueSchemePopUp.DEFAULT_VALUE;
			return result;
		}
		

		public function get valueSchemeSimple():String {
			return this._valueSchemeSimple
		}

		public function set valueSchemeSimple(valueSchemeSimple:String):void {
			this._valueSchemeSimple = valueSchemeSimple;
		}

		public function get valueSchemeEnumeration():XMLListCollection {
			return this._valueSchemeEnumeration
		}

		public function set valueSchemeEnumeration(valueSchemeEnumeration:XMLListCollection):void {
			this._valueSchemeEnumeration = valueSchemeEnumeration;
		}

		public function get valueSchemePattern():String {
			return this._valueSchemePattern;
		}

		public function set valueSchemePattern(valueSchemePattern:String):void {
			this._valueSchemePattern = valueSchemePattern;
		}

		public function toXml():XML {
			var result:XML = <CMD_Element></CMD_Element>;
			if (name)
				result.@name = name;
			if (conceptLink)
				result.@ConceptLink = conceptLink;
			if (documentation)
				result.@Documentation = documentation;
			if (displayPriority && displayPriority != "0")
				result.@DisplayPriority = displayPriority;
			if (valueSchemeSimple)
				result.@ValueScheme = valueSchemeSimple;
			if (cardinalityMin)
				result.@CardinalityMin = cardinalityMin;
			if (cardinalityMax)
				result.@CardinalityMax = cardinalityMax;
            if (multilingual != null)
                result.@Multilingual = multilingual;				
			if (attributeList.length > 0) {
				var attributeListTag:XML = <AttributeList></AttributeList>;
				for each (var attribute:CMDAttribute in attributeList) {
					attributeListTag.appendChild(attribute.toXml());
				}
				result.appendChild(attributeListTag);
			}
			if (valueSchemePattern) {
				result.appendChild(<ValueScheme><pattern>{valueSchemePattern}</pattern></ValueScheme>)
			}
			if (valueSchemeEnumeration != null) {
				var enumerationScheme:XML = <enumeration></enumeration>;
				for each (var item:XML in valueSchemeEnumeration) {
					enumerationScheme.appendChild(item);
				}
				result.appendChild(<ValueScheme>{enumerationScheme}</ValueScheme>);
			}
			return result;
		}


	}
}
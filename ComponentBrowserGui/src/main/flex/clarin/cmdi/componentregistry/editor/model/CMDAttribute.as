package clarin.cmdi.componentregistry.editor.model {
	import clarin.cmdi.componentregistry.common.XmlAble;

	import mx.collections.XMLListCollection;

	public class CMDAttribute implements XmlAble {
		//No Attributes

		//Elements
		public var name:String;
		public var type:String;
		public var valueSchemePattern:String; //pattern
		public var valueSchemeEnumeration:XMLListCollection; // enumeration 

		//TODO Patrick see element?? check usage in xmlBrowser


		public function CMDAttribute() {
		}

		public function toXml():XML {
			var result:XML = <Attribute></Attribute>;
			result.appendChild(<Name>{name}</Name>);
			if (valueSchemePattern) {
				result.appendChild(<pattern>{valueSchemePattern}</pattern>);
			} else if (valueSchemeEnumeration) {
				result.appendChild(<enumeration>{valueSchemeEnumeration}</enumeration>);
			} else {
				result.appendChild(<Type>{type}</Type>);
			}

			return result
		}
	}
}
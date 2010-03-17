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

		public function CMDAttribute() {
		}

		public function toXml():XML {
 			var result:XML = <Attribute></Attribute>;
			result.appendChild(<Name>{name}</Name>);
			if (valueSchemePattern) {
				result.appendChild(<ValueScheme><pattern>{valueSchemePattern}</pattern></ValueScheme>);
			} else if (valueSchemeEnumeration) {
   				var enumerationScheme:XML = <enumeration></enumeration>;
				for each(var item:XML in valueSchemeEnumeration) {
				    enumerationScheme.appendChild(item);
    			}
    			result.appendChild(<ValueScheme>{enumerationScheme}</ValueScheme>);
			} else {
				result.appendChild(<Type>{type}</Type>);
			}

			return result
		}
	}
}
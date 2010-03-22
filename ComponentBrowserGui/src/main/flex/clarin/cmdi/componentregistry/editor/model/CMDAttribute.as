package clarin.cmdi.componentregistry.editor.model {
	import clarin.cmdi.componentregistry.common.XmlAble;

	import mx.collections.XMLListCollection;

	public class CMDAttribute implements XmlAble, ValueSchemeInterface {
		//No Attributes

		//Elements
		public var name:String;
		private var type:String;
		private var _valueSchemePattern:String; //pattern
		private var _valueSchemeEnumeration:XMLListCollection; // enumeration 

		public function CMDAttribute() {
		}

		public function get valueSchemeSimple():String {
			return this.type
		}

		public function set valueSchemeSimple(valueSchemeSimple:String):void {
			this.type = valueSchemeSimple;
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
			var result:XML = <Attribute></Attribute>;
			result.appendChild(<Name>{name}</Name>);
			if (valueSchemePattern) {
				result.appendChild(<ValueScheme><pattern>{valueSchemePattern}</pattern></ValueScheme>);
			} else if (valueSchemeEnumeration) {
				var enumerationScheme:XML = <enumeration></enumeration>;
				for each (var item:XML in valueSchemeEnumeration) {
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
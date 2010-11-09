package clarin.cmdi.componentregistry.editor.model {
	import clarin.cmdi.componentregistry.common.ComponentMD;
	import clarin.cmdi.componentregistry.common.XmlAble;
	import clarin.cmdi.componentregistry.editor.ValueSchemeItem;
	import clarin.cmdi.componentregistry.editor.ValueSchemePopUp;
	
	import mx.collections.ArrayCollection;

	public class CMDAttribute implements XmlAble, ValueSchemeInterface {
		//No Attributes

		//Elements
		public var name:String;
		private var type:String;
		private var _valueSchemePattern:String; //pattern
		private var _valueSchemeEnumeration:ArrayCollection; // enumeration 

		public function CMDAttribute() {
		}

		public static function createEmptyAttribute():CMDAttribute {
			var result:CMDAttribute = new CMDAttribute();
			result.valueSchemeSimple = ValueSchemePopUp.DEFAULT_VALUE;
			return result;
		}

		public function get valueSchemeSimple():String {
			return this.type
		}

		public function set valueSchemeSimple(valueSchemeSimple:String):void {
			this.type = valueSchemeSimple;
		}

		public function get valueSchemeEnumeration():ArrayCollection {
			return this._valueSchemeEnumeration
		}

		public function set valueSchemeEnumeration(valueSchemeEnumeration:ArrayCollection):void {
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
				for each (var item:ValueSchemeItem in valueSchemeEnumeration) {
					enumerationScheme.appendChild(<item {ComponentMD.APP_INFO}={item.appInfo} {ComponentMD.CONCEPTLINK}={item.conceptLink}>{item.item}</item>);
				}
				result.appendChild(<ValueScheme>{enumerationScheme}</ValueScheme>);
			} else {
				result.appendChild(<Type>{type}</Type>);
			}

			return result
		}
	}
}
package clarin.cmdi.componentregistry.editor.model {
	import clarin.cmdi.componentregistry.common.ChangeTrackingCMDElement;
	import clarin.cmdi.componentregistry.common.ComponentMD;
	import clarin.cmdi.componentregistry.common.XmlAble;
	import clarin.cmdi.componentregistry.editor.ValueSchemeItem;
	//import clarin.cmdi.componentregistry.editor.ValueSchemePopUp;
	import clarin.cmdi.componentregistry.editor.ValueSchemePopUpNew;
	
	import mx.collections.ArrayCollection;

	public class CMDAttribute implements XmlAble, ValueSchemeInterface, ChangeTrackingCMDElement {
		//No Attributes

		//Elements
		public var name:String;
		private var type:String;
		public var conceptLink:String;
		private var _valueSchemePattern:String; //pattern
		private var _valueSchemeEnumeration:ArrayCollection; // enumeration 
		
		private var changed:Boolean = false;
		private var _changeTracking:Boolean = false;
		
		public function CMDAttribute() {
		}

		public static function createEmptyAttribute():CMDAttribute {
			var result:CMDAttribute = new CMDAttribute();
			result.valueSchemeSimple = ValueSchemePopUpNew.DEFAULT_VALUE;
			return result;
		}
		
		public function set changeTracking(value:Boolean):void{
			_changeTracking = value;
		}
		
		public function setChanged(value:Boolean):void {
			if(_changeTracking) {
				this.changed = value;
			}
		}
		
		public function get hasChanged():Boolean{
			return changed;
		}
		
		public function get valueSchemeSimple():String {
			return this.type
		}

		public function set valueSchemeSimple(valueSchemeSimple:String):void {
			this.type = valueSchemeSimple;
			setChanged(true);
		}

		public function get valueSchemeEnumeration():ArrayCollection {
			return this._valueSchemeEnumeration
		}

		public function set valueSchemeEnumeration(valueSchemeEnumeration:ArrayCollection):void {
			this._valueSchemeEnumeration = valueSchemeEnumeration;
			setChanged(true);
		}

		public function get valueSchemePattern():String {
			return this._valueSchemePattern;
		}

		public function set valueSchemePattern(valueSchemePattern:String):void {
			this._valueSchemePattern = valueSchemePattern;
			setChanged(true);
		}

		public function toXml():XML {
			var result:XML = <Attribute></Attribute>;
			result.appendChild(<Name>{name}</Name>);
			if (conceptLink) {
				result.appendChild(<ConceptLink>{conceptLink}</ConceptLink>);
			}
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
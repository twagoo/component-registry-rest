package clarin.cmdi.componentregistry.editor.model {
	import clarin.cmdi.componentregistry.common.ChangeTrackingCMDElement;
	import clarin.cmdi.componentregistry.common.ComponentMD;
	import clarin.cmdi.componentregistry.common.XmlAble;
	import clarin.cmdi.componentregistry.editor.ValueSchemeItem;
	import clarin.cmdi.componentregistry.editor.ValueSchemePopUp;
	//import clarin.cmdi.componentregistry.editor.ValueSchemePopUpNew;
	
	import mx.collections.ArrayCollection;
	import mx.events.CollectionEvent;
	import mx.events.CollectionEventKind;
	
	[Bindable]
	public class CMDComponentElement implements XmlAble, ValueSchemeInterface, AttributeContainer, ChangeTrackingCMDElement {
		
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
		private var _valueSchemeEnumeration:ArrayCollection;
		private var _valueSchemePattern:String;
		
		private var changed:Boolean = false;
		private var _changeTracking:Boolean = false;
		
		public function CMDComponentElement() {
			attributeList.addEventListener(CollectionEvent.COLLECTION_CHANGE, collectionChangedHandler);
		}
		
		public function set changeTracking(value:Boolean):void{
			_changeTracking = value;
			for each (var attribute:CMDAttribute in attributeList){
				attribute.changeTracking = value;
			}
		}
		
		private function collectionChangedHandler(event:CollectionEvent):void {
			if(event.kind == CollectionEventKind.ADD ||event.kind == CollectionEventKind.MOVE || event.kind == CollectionEventKind.REMOVE || event.kind == CollectionEventKind.REPLACE){ 
				setChanged(true);
			}
			if(event.kind == CollectionEventKind.ADD){
				for each(var item:ChangeTrackingCMDElement in event.items){
					item.changeTracking = _changeTracking;
				}
			}
		}
		
		public function setChanged(value:Boolean):void {
			if(_changeTracking) {
				this.changed = value;
			}
		}
		
		public function get hasChanged():Boolean{
			if(changed){
				return changed;
			} else {
				for each (var attribute:CMDAttribute in attributeList){
					if(attribute.hasChanged){
						return true;
					}
				}
			}
			return false;
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
				for each (var item:ValueSchemeItem in valueSchemeEnumeration) {
					enumerationScheme.appendChild(<item {ComponentMD.APP_INFO}={item.appInfo} {ComponentMD.CONCEPTLINK}={item.conceptLink}>{item.item}</item>);
				}
				result.appendChild(<ValueScheme>{enumerationScheme}</ValueScheme>);
			}
			return result;
		}
		
		public function getAttributeList():ArrayCollection {
			return attributeList;
		}
		
	}
}
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
		public var valueSchemeComplex:XMLListCollection;

		public function CMDComponentElement() {
		}



		public function toXml():XML {
			return <CMD_Element></CMD_Element>;//TODO PD implement
		}

	}
}
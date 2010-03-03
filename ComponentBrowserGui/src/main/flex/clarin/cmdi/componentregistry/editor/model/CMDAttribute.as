package clarin.cmdi.componentregistry.editor.model {
	import clarin.cmdi.componentregistry.common.XmlAble;
	
	import mx.collections.XMLListCollection;

	public class CMDAttribute implements XmlAble {
	    //No Attributes
	    
	    //Elements
	    public var name:String;
	    public var type:String;
	    public var valueSchemeSimple:String;
        public var valueSchemeComplex:XMLListCollection;
	    
	    
		public function CMDAttribute() {
		}

		public function toXml():XML {
			return <Attribute></Attribute>; //TODO PD implement
		}
	}
}
package clarin.cmdi.componentregistry.common {
	import mx.collections.ArrayCollection;

	[Bindable]
	public class ComponentMD {
	    
	    /** 
	    * Claring Metadata xml constants
	    */
	    //Element Names
	    public static const CONCEPTLINK:String = "ConceptLink";
	    public static const COMPONENTID:String = "ComponentId";
	    public static const CMD_COMPONENT:String = "CMD_Component";
	    public static const CMD_ELEMENT:String = "CMD_Element";
	    public static const HEADER:String = "Header";
	    public static const ENUMERATION:String = "enumeration";
	    public static const PATTERN:String = "pattern";
	    public static const TYPE:String = "Type";
	    public static const VALUE_SCHEME:String = "ValueScheme";
	    public static const ATTRIBUTE:String = "Attribute";
	    public static const ATTRIBUTE_LIST:String = "AttributeList";
        //Attribute Names
        public static const APP_INFO:String = "AppInfo";
        public static const NAME:String = "name";

		public var name:String;
		public var attributeList:XMLList;
		public var cmdElements:Array = new Array();


		private var _xml:XML;

		public function ComponentMD() {
		}

		public function set xml(xml:XML):void {
			_xml = xml;
		}

		public function get xml():XML {
			return _xml;
		}
	}

}

package clarin.cmdi.componentregistry.common.components {
    import mx.binding.utils.BindingUtils;
    
    [Inspectable]
	public class XMLLine {

		public var name:String;
		public var value:String;
		public var indent:int;
		public var isAttribute:Boolean = false;		
		public var xml:XML; //element or attribute


		public function XMLLine(xml:XML, name:String, value:String=null, indent:int=0) {
			this.name = name;
			this.value = value;
			this.indent = indent;
			this.xml = xml;
		}

		public function editable():Boolean {
			return value != null && value.length > 0;
		}

	}
}
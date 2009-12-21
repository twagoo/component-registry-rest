package clarin.cmdi.componentregistry.common {
	import mx.collections.ArrayCollection;

	[Bindable]
	public class ComponentMD {

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

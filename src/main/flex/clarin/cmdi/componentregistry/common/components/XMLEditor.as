package clarin.cmdi.componentregistry.common.components {
	import flash.display.DisplayObject;
	
	import mx.collections.ArrayCollection;
	import mx.containers.Form;
	import mx.containers.FormHeading;

	public class XMLEditor extends Form {


		private var _xml:XML = new XML();
		private var addedChildren:ArrayCollection = new ArrayCollection();

		public function XMLEditor() {
		}

		public function set xml(xml:XML):void {
			_xml = xml;
			var xmlList:ArrayCollection = XMLUtils.getXmlLines(_xml);
			createNewEditor(xmlList);
		}

		[Bindable]
		public function get xml():XML {
			return _xml;
		}


		private function createNewEditor(xmlList:ArrayCollection):void {
			removeAddedChildren();
			for (var i:Number = 0; i < xmlList.length; i++) {
				var xmlLine:XMLLine = xmlList.getItemAt(i) as XMLLine;
				var child:DisplayObject;
				if (xmlLine.editable()) {
					var f1:XMLEditorField = new XMLEditorField(xmlLine);
					f1.label = xmlLine.name;
					f1.setStyle("fontWeight", "bold");
					f1.editField = xmlLine.value;
					child = f1;
				} else {
					var heading:FormHeading = new FormHeading();
					heading.label = xmlLine.name;
					heading.setStyle("labelWidth", "0");
					child = heading;
				}
				addChildAt(child, i);//Add children at i so already added children (e.g. from mxml) are at the bottom. Can make this optional if needs be.
				addedChildren.addItem(child); //Only remove children we added based on xml so other GUI element with be retained.
			}
		}

        private function removeAddedChildren():void {
            for (var i:Number = 0; i < addedChildren.length; i++) {
                removeChild(addedChildren.getItemAt(i) as DisplayObject);
            }
            addedChildren = new ArrayCollection();
        }

	}

}
package clarin.cmdi.componentregistry.browser {
	import clarin.cmdi.componentregistry.common.StyleConstants;
	import clarin.cmdi.componentregistry.common.components.LinkRenderer;
	
	import mx.containers.HBox;
	import mx.controls.Label;

	public class XMLBrowserConceptLinkLine extends HBox {

		private var nameLabel:Label = new Label();
		private var fieldValue:LinkRenderer = new LinkRenderer();

		public function XMLBrowserConceptLinkLine(name:String, value:String) {
			super();
			nameLabel.text = name;
			nameLabel.styleName = StyleConstants.XMLBROWSER_FIELD;
			fieldValue.text = value;
			fieldValue.styleName = StyleConstants.XMLBROWSER_FIELD_VALUE;
			fieldValue.width = 500;
		}

		protected override function createChildren():void {
			super.createChildren();
			addChild(nameLabel);
			addChild(fieldValue);
		}
	}
}
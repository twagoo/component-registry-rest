package clarin.cmdi.componentregistry.browser {
	import clarin.cmdi.componentregistry.common.StyleConstants;
	import clarin.cmdi.componentregistry.common.components.ConceptLinkRenderer;
	
	import mx.containers.HBox;
	import mx.controls.Label;

	public class XMLBrowserConceptLinkLine extends HBox {

		private var nameLabel:Label = new Label();
		private var fieldValue:ConceptLinkRenderer = new ConceptLinkRenderer();

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
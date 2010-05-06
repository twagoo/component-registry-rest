package clarin.cmdi.componentregistry.browser {
	import clarin.cmdi.componentregistry.common.StyleConstants;
	
	import mx.containers.HBox;
	import mx.controls.Label;
	import mx.controls.Text;

	public class XMLBrowserLine extends HBox {

		private var nameLabel:Label = new Label();
		private var fieldValue:Text = null;

		public function XMLBrowserLine(name:String, value:String, nameStyle:String=StyleConstants.XMLBROWSER_FIELD_VALUE, valueStyle:String=StyleConstants.XMLBROWSER_FIELD_VALUE) {
			super();
			nameLabel.text = name;
			nameLabel.styleName = nameStyle;
			if (value) {
				fieldValue = new Text();
				fieldValue.text = value;
				fieldValue.styleName = valueStyle;
				fieldValue.width = 500;
			}
		}

		protected override function createChildren():void {
			super.createChildren();
			addChild(nameLabel);
			if (fieldValue != null) {
				addChild(fieldValue);
			}
		}

	}
}
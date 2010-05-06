package clarin.cmdi.componentregistry.browser {
	import clarin.cmdi.componentregistry.common.ComponentMD;
	import clarin.cmdi.componentregistry.common.StyleConstants;

	import flash.display.DisplayObject;

	import mx.collections.XMLListCollection;
	import mx.containers.HBox;
	import mx.controls.ComboBox;
	import mx.controls.Label;
	import mx.controls.Text;
	import mx.core.ClassFactory;
	import mx.core.UIComponent;

	public class XMLBrowserValueSchemeLine extends HBox {
		private var nameLabel:Label = new Label();
		private var valueObject:DisplayObject;

		public function XMLBrowserValueSchemeLine(name:String, value:String = null, valuePattern:String = null, valueList:XMLListCollection = null) {
			super();
			nameLabel.text = name;
			nameLabel.styleName = StyleConstants.XMLBROWSER_FIELD;
			valueObject = createValueScheme(value, valuePattern, valueList);
		}

		protected override function createChildren():void {
			super.createChildren();
			addChild(CMDComponentXMLBrowser.getIndentSpacer());
			addChild(nameLabel);
			addChild(valueObject);
		}

		public static function createValueScheme(value:String = null, valuePattern:String = null, valueList:XMLListCollection = null):UIComponent {
			if (valueList) {
				return createEnumeration(valueList);
			} else if (valuePattern) {
				return createText(valuePattern);
			} else {
				return createText(value);
			}
		}

		private static function createText(value:String):UIComponent {
			var result:Text = new Text();
			result.text = value;
			result.styleName = StyleConstants.XMLBROWSER_FIELD_VALUE;
			result.width = 500;
			return result;
		}

		private static function createEnumeration(enumeration:XMLListCollection):UIComponent {
			var result:ComboBox = createValueSchemeComboBox();
			result.dataProvider = enumeration;
			return result;
		}

		public static function createValueSchemeComboBox():ComboBox {
			var result:ComboBox = new ComboBox();
			result.itemRenderer = new ClassFactory(Label);
			result.labelFunction = function(item:Object):String {
				var xmlItem:XML = item as XML;
				if (item.hasOwnProperty("@" + ComponentMD.APP_INFO) && xmlItem.attribute(ComponentMD.APP_INFO) != "") { //TODO PD what about conceptlinks? also not shown in Browser.
					return xmlItem.attribute(ComponentMD.APP_INFO) + " - " + xmlItem.text();
				} else {
					return xmlItem.text();
				}
			};
			return result;
		}


	}
}
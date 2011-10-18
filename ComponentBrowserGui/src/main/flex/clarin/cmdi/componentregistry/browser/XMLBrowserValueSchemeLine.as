package clarin.cmdi.componentregistry.browser {
	import clarin.cmdi.componentregistry.common.StyleConstants;
	import clarin.cmdi.componentregistry.common.components.LinkRenderer;
	import clarin.cmdi.componentregistry.editor.ValueSchemeItem;
	
	import flash.display.DisplayObject;
	
	import mx.collections.ArrayCollection;
	import mx.containers.HBox;
	import mx.controls.ComboBox;
	import mx.controls.Label;
	import mx.controls.Text;
	import mx.core.ClassFactory;
	import mx.core.UIComponent;

	public class XMLBrowserValueSchemeLine extends HBox {
		private var nameLabel:Label = new Label();
		private var conceptLink:LinkRenderer;
		private var valueObject:DisplayObject;
		private var _indent:int = 0;

		public function XMLBrowserValueSchemeLine(name:String, indent:int, value:String = null, valuePattern:String = null, valueList:ArrayCollection = null, conceptLinkValue:String = null) {
			super();
			_indent = indent;
			nameLabel.text = name;
			nameLabel.styleName = StyleConstants.XMLBROWSER_FIELD;
			valueObject = createValueScheme(value, valuePattern, valueList);
			conceptLink = createConceptLink(conceptLinkValue);
		}

		protected override function createChildren():void {
			super.createChildren();
			if (_indent > 0)
				addChild(CMDComponentXMLBrowser.getIndentSpacer(_indent));
			addChild(nameLabel);
			addChild(valueObject);
			if(conceptLink){
				addChild(conceptLink);
			}
		}
		
		public static function createConceptLink(linkValue:String):LinkRenderer{
			if(linkValue){
				var conceptLink:LinkRenderer = new LinkRenderer();
				conceptLink.text = linkValue;
				conceptLink.styleName = StyleConstants.XMLBROWSER_FIELD_VALUE;
				return conceptLink;
			} else {
				return null;
			}
		}

		public static function createValueScheme(value:String = null, valuePattern:String = null, valueList:ArrayCollection = null):UIComponent {
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
			return result;
		}

		private static function createEnumeration(enumeration:ArrayCollection):UIComponent {
			var result:ComboBox = createValueSchemeComboBox();
			result.dataProvider = enumeration;
			return result;
		}

		public static function createValueSchemeComboBox():ComboBox {
			var result:ComboBox = new ComboBox();
			result.itemRenderer = new ClassFactory(Label);
			result.labelFunction = function(item:Object):String {
			    var valueSchemeItem:ValueSchemeItem = item as ValueSchemeItem;
			    if (item.appInfo != "") {
			        return item.item + "-" + item.appInfo;
			    } else {
			        return item.item;
			    }
			};
			return result;
		}


	}
}
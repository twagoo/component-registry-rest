package clarin.cmdi.componentregistry.common.components {
	import clarin.cmdi.componentregistry.common.ComponentMD;
	import clarin.cmdi.componentregistry.common.StyleConstants;

	import flash.display.DisplayObject;

	import mx.containers.FormItem;
	import mx.controls.ComboBox;

	/**
	 * Browser form created from CMDComponent xml
	 */
	public class CMDComponentXMLBrowser extends XMLBrowser {


		public function CMDComponentXMLBrowser() {
			styleName = StyleConstants.XMLBROWSER;
		}

		protected override function handleCMDAttributeList(atributeList:XML):void {
			for each (var cmdAttribute:XML in atributeList.descendants(ComponentMD.ATTRIBUTE)) {
				handleCMDAttributes(cmdAttribute.elements());
			}
		}

		private function handleCMDAttributes(cmdAttribute:XMLList):void {
			var name:FormItem = createFormItem(cmdAttribute[0].name(), cmdAttribute[0].text()); //The name
			addFormChild(name);
			var item:FormItem = createFormItem(cmdAttribute[1].name(), cmdAttribute[1].text(), cmdAttribute[1]); //type or valueScheme
			addFormChild(item);
		}

		protected override function createFormItemFieldValue(name:String, value:String, xmlElement:XML = null):DisplayObject {
			if (name == ComponentMD.CONCEPTLINK) {
				return createConceptLinkLabel(value);
			} else if (name == ComponentMD.COMPONENTID) {
				return createComponentIdLabel(value);
			} else if (name == ComponentMD.VALUE_SCHEME && xmlElement != null) {
			    if (xmlElement.hasOwnProperty(ComponentMD.ENUMERATION)) {
				    return createEnumeration(xmlElement.elements(ComponentMD.ENUMERATION));
			    } else {
			        return super.createFormItemFieldValue(name, xmlElement.elements(ComponentMD.PATTERN)[0].text());
			    }
			} else {
				return super.createFormItemFieldValue(name, value);
			}
		}

		private function createConceptLinkLabel(value:String):DisplayObject {
			var result:ConceptLinkRenderer = new ConceptLinkRenderer();
			result.text = value;
			result.styleName = StyleConstants.XMLBROWSER_FIELD_VALUE;
			return result;
		}


		private function createComponentIdLabel(value:String):DisplayObject {
			var componentLabel:ExpandingComponentLabel = new ExpandingComponentLabel(value);
			return componentLabel;
		}


		private function createEnumeration(enumeration:XMLList):DisplayObject {
			var result:ComboBox = new ComboBox();
			result.dataProvider = enumeration.item;
			result.labelFunction = function(item:Object):String {
				var xmlItem:XML = item as XML;
				if (item..hasOwnProperty("@" + ComponentMD.APP_INFO)) {
					return xmlItem.attribute(ComponentMD.APP_INFO) + " - " + xmlItem.text();
				} else {
					return xmlItem.text();
				}
			};
			return result;
		}

	}
}
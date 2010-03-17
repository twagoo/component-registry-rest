package clarin.cmdi.componentregistry.browser {
	import clarin.cmdi.componentregistry.common.StyleConstants;
	import clarin.cmdi.componentregistry.common.components.ConceptLinkRenderer;
	import clarin.cmdi.componentregistry.common.components.ExpandingComponentLabel;
	
	import flash.display.DisplayObject;

	/**
	 * Browser form created from CMDComponent xml
	 */
	public class CMDComponentXMLBrowser extends XMLBrowser {


		public function CMDComponentXMLBrowser() {
		    super();
			styleName = StyleConstants.XMLBROWSER;
		}

		protected override function createFormItemFieldValue(name:String, value:String):DisplayObject {
			if (name == CONCEPTLINK) {
				return createConceptLinkLabel(value);
			} else if (name == COMPONENT_ID) {
				return createComponentIdLabel(value);
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

	}
}
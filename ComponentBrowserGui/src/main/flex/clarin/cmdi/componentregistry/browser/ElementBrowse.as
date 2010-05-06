package clarin.cmdi.componentregistry.browser {
	import clarin.cmdi.componentregistry.common.LabelConstants;
	import clarin.cmdi.componentregistry.common.StyleConstants;
	import clarin.cmdi.componentregistry.editor.model.CMDComponentElement;
	
	import mx.containers.HBox;
	import mx.controls.Label;
	import mx.core.Container;
	import mx.core.UIComponent;

	public class ElementBrowse {
		private var _element:CMDComponentElement;
		private var container:Container;

		public function ElementBrowse(element:CMDComponentElement) {
			super();
			_element = element;
		}

		public function createAndAddChildren(container:Container):void {
			this.container = container;
			addHeading();
			if (_element.conceptLink)
				addChild(new XMLBrowserConceptLinkLine(LabelConstants.CONCEPTLINK, _element.conceptLink));
			addLine(LabelConstants.DOCUMENTATION, _element.documentation);
			addLine(LabelConstants.DISPLAY_PRIORITY, _element.displayPriority);
			if (_element.cardinalityMin != "" || _element.cardinalityMax != "") {
				addLine(LabelConstants.CARDINALITY, _element.cardinalityMin + " - " + _element.cardinalityMax);
			}
			CMDComponentXMLBrowser.handleCMDAttributeList(container, _element.attributeList);
		}

		private function addHeading():void {
			var line:HBox = new HBox();
			var heading:Label = new Label();
			heading.text = LabelConstants.ELEMENT;
			heading.styleName = StyleConstants.XMLBROWSER_HEADER;
			var name:Label = new Label();
			name.text = _element.name;
			name.styleName = StyleConstants.XMLBROWSER_HEADER;
			var value:UIComponent = XMLBrowserValueSchemeLine.createValueScheme(_element.valueSchemeSimple, _element.valueSchemePattern, _element.valueSchemeEnumeration);
			value.styleName = StyleConstants.XMLBROWSER_FIELD_VALUE;
			line.addChild(heading);
			line.addChild(name);
			line.addChild(value);
			addChild(line, false);
		}

		private function addLine(name:String, value:String):void {
			if (value) {
				addChild(new XMLBrowserLine(name, value));
			}
		}

		private function addChild(child:HBox, indent:Boolean = true):void {
			if (indent) {
				child.addChildAt(CMDComponentXMLBrowser.getIndentSpacer(), 0);
			}
			this.container.addChild(child);
		}

	}
}
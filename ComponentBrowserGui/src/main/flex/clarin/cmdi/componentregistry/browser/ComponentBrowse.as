package clarin.cmdi.componentregistry.browser {
	import clarin.cmdi.componentregistry.common.LabelConstants;
	import clarin.cmdi.componentregistry.common.StyleConstants;
	import clarin.cmdi.componentregistry.editor.model.CMDComponent;
	import clarin.cmdi.componentregistry.editor.model.CMDComponentElement;

	import mx.collections.ArrayCollection;
	import mx.containers.HBox;
	import mx.controls.HRule;
	import mx.core.Container;

	public class ComponentBrowse {
		private var _component:CMDComponent;
		private var container:Container;
		private var indent:int = 0;

		public function ComponentBrowse(component:CMDComponent) {
			super();
			_component = component;
		}

		public function createAndAddChildren(container:Container, indent:int = 0):void {
			this.container = container;
			this.indent = indent;
			if (indent == 0) {
				var ruler:HRule = new HRule();
				ruler.percentWidth = 80;
				container.addChild(ruler);
			}
			var componentLink:HBox = CMDComponentXMLBrowser.createComponentLink(_component);
			if (componentLink != null) {
				addChild(componentLink);
				addCardinalityLine();
			} else {
				addChild(new XMLBrowserLine(LabelConstants.COMPONENT, _component.name, StyleConstants.XMLBROWSER_HEADER, StyleConstants.XMLBROWSER_HEADER));
				if (_component.conceptLink) {
					addChild(new XMLBrowserConceptLinkLine(LabelConstants.CONCEPTLINK, _component.conceptLink));
				}
				addCardinalityLine();
				CMDComponentXMLBrowser.handleCMDAttributeList(container, _component.attributeList, indent);
				handleElements(_component.cmdElements);
				handleComponents(_component.cmdComponents); //recursion
			}
		}

		private function handleElements(elements:ArrayCollection):void {
			for each (var element:CMDComponentElement in elements) {
				var elementBrowse:ElementBrowse = new ElementBrowse(element);
				elementBrowse.createAndAddChildren(container, indent + 1);
			}
		}

		private function handleComponents(components:ArrayCollection):void {
			for each (var component:CMDComponent in components) {
				var componentBrowse:ComponentBrowse = new ComponentBrowse(component);
				componentBrowse.createAndAddChildren(container, indent + 1);
			}
		}


		private function addCardinalityLine():void {
			if (_component.cardinalityMin != "" || _component.cardinalityMax != "")
				addChild(new XMLBrowserLine(LabelConstants.CARDINALITY, _component.cardinalityMin + " - " + _component.cardinalityMax));

		}

		private function addLine(name:String, value:String):void {
			if (value) {
				addChild(new XMLBrowserLine(name, value));
			}
		}

		private function addChild(child:HBox):void {
			if (indent > 0) {
				child.addChildAt(CMDComponentXMLBrowser.getIndentSpacer(indent), 0);
			}
			this.container.addChild(child);
		}

	}
}
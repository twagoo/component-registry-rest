package clarin.cmdi.componentregistry.browser {
	import clarin.cmdi.componentregistry.common.CMDSpecRenderer;
	import clarin.cmdi.componentregistry.common.LabelConstants;
	import clarin.cmdi.componentregistry.common.StyleConstants;
	import clarin.cmdi.componentregistry.common.components.ExpandingComponentLabel;
	import clarin.cmdi.componentregistry.editor.model.CMDAttribute;
	import clarin.cmdi.componentregistry.editor.model.CMDComponent;
	import clarin.cmdi.componentregistry.editor.model.CMDComponentElement;
	import clarin.cmdi.componentregistry.editor.model.CMDSpec;

	import flash.utils.getTimer;

	import mx.collections.ArrayCollection;
	import mx.containers.HBox;
	import mx.containers.VBox;
	import mx.controls.HRule;
	import mx.controls.Label;
	import mx.controls.Spacer;
	import mx.core.Container;
	import mx.managers.IFocusManagerComponent;

	/**
	 * Browser view created from CMDComponent xml
	 */
	public class CMDComponentXMLBrowser extends VBox implements IFocusManagerComponent, CMDSpecRenderer {

		private var _spec:CMDSpec;
		private var _firstComponent:CMDComponent;

		public function CMDComponentXMLBrowser() {
			super();
			styleName = StyleConstants.XMLBROWSER;
			focusEnabled = true;
			tabEnabled = true;
		}

		public function set cmdSpec(cmdSpec:CMDSpec):void {
			_spec = cmdSpec;
			createNewBrowser();
		}

		[Bindable]
		public function get cmdSpec():CMDSpec {
			return _spec;
		}

		private function createNewBrowser():void {
			var start:int = getTimer();
			removeAllChildren()
			checkFirstDefiningComponent(_spec.cmdComponents);
			var componentLink:HBox = CMDComponentXMLBrowser.createComponentLink(_firstComponent);
			if (!componentLink) {
				handleHeader(_firstComponent.name);
			} else {
				handleHeader(_spec.headerName);
				//FirstComponent is link so creating that.
				addChild(componentLink);
				if (_firstComponent.cardinalityMin != "" || _firstComponent.cardinalityMax != "") {
					addChild(new XMLBrowserLine(LabelConstants.CARDINALITY, _firstComponent.cardinalityMin + " - " + _firstComponent.cardinalityMax));
				}
			}
			handleCMDAttributeList(this, _firstComponent.attributeList, 0);
			handleElements(_firstComponent.cmdElements);
			handleComponents(_firstComponent.cmdComponents);
			trace("Created browser view in " + (getTimer() - start) + " ms.");
		}

		public static function handleCMDAttributeList(parent:Container, attributes:ArrayCollection, indent:int):void {
			if (attributes.length > 0) {
				var heading:HBox = new HBox();
				var label:Label = new Label();
				label.text = LabelConstants.ATTRIBUTELIST;
				label.styleName = StyleConstants.XMLBROWSER_FIELD;
				var smallIndent:Spacer = new Spacer();
				smallIndent.width = 5;
				if (indent > 0)
					heading.addChild(getIndentSpacer(indent));
				heading.addChild(smallIndent);
				heading.addChild(label);
				parent.addChild(heading);
				for each (var attribute:CMDAttribute in attributes) {
					var child:XMLBrowserValueSchemeLine = new XMLBrowserValueSchemeLine(attribute.name, indent+1, attribute.valueSchemeSimple, attribute.valueSchemePattern, attribute.valueSchemeEnumeration);
					parent.addChild(child);
				}
			}
		}

		public static function getIndentSpacer(indent:int):Spacer {
			var result:Spacer = new Spacer();
			result.width = 56 * indent;
			return result;
		}

		/**
		 * The xml model allows more component to be defined but we force only one component at the "root" level.
		 */
		private function checkFirstDefiningComponent(components:ArrayCollection):void {
			if (components.length != 1) {
				throw new Error("A profile/component must have 1 component defined at root level.");
			} else {
				_firstComponent = components.getItemAt(0) as CMDComponent;
			}
		}

		private function handleHeader(name:String):void {
			addChild(new XMLBrowserLine(LabelConstants.NAME, name, StyleConstants.XMLBROWSER_FIELD, StyleConstants.XMLBROWSER_HEADER));
			addChild(new XMLBrowserLine(LabelConstants.DESCRIPTION, _spec.headerDescription));
			if (_firstComponent.conceptLink) {
				addChild(new XMLBrowserConceptLinkLine(LabelConstants.CONCEPTLINK, _firstComponent.conceptLink));
			}
			var ruler:HRule = new HRule();
			ruler.percentWidth = 80;
			addChild(ruler);
		}

		private function handleElements(elements:ArrayCollection):void {
			for each (var element:CMDComponentElement in elements) {
				var elementBrowse:ElementBrowse = new ElementBrowse(element);
				elementBrowse.createAndAddChildren(this, 1);
			}
		}

		private function handleComponents(components:ArrayCollection):void {
			for each (var component:CMDComponent in components) {
				var componentBrowse:ComponentBrowse = new ComponentBrowse(component);
				componentBrowse.createAndAddChildren(this);
			}
		}

		public static function createComponentLink(component:CMDComponent):HBox {
			if (component.componentId != "" && component.componentId != null) {
				var result:HBox = new HBox();
				var labelName:Label = new Label();
				labelName.styleName = StyleConstants.XMLBROWSER_HEADER;
				labelName.text = LabelConstants.COMPONENT;
				result.addChild(labelName);
				result.addChild(new ExpandingComponentLabel(component.componentId, false));
				return result;
			}
			return null;
		}

	}
}
package clarin.cmdi.componentregistry.browser {
	import clarin.cmdi.componentregistry.common.CMDSpecRenderer;
	import clarin.cmdi.componentregistry.common.ComponentMD;
	import clarin.cmdi.componentregistry.common.StyleConstants;
	import clarin.cmdi.componentregistry.editor.model.CMDAttribute;
	import clarin.cmdi.componentregistry.editor.model.CMDComponent;
	import clarin.cmdi.componentregistry.editor.model.CMDComponentElement;
	import clarin.cmdi.componentregistry.editor.model.CMDSpec;
	
	import flash.display.DisplayObject;
	import flash.utils.getTimer;
	
	import mx.collections.ArrayCollection;
	import mx.collections.XMLListCollection;
	import mx.containers.Form;
	import mx.containers.FormItem;
	import mx.controls.ComboBox;
	import mx.controls.HRule;
	import mx.controls.Label;
	import mx.controls.Text;
	import mx.core.ClassFactory;
	import mx.core.UIComponent;
	import mx.managers.IFocusManagerComponent;

	/**
	 * Generic XMLBrowser converts an xml file into a form. Use subclasses to override default methods and add custom behaviour.
	 */
	public class XMLBrowser extends Form implements IFocusManagerComponent, CMDSpecRenderer {

		//Label names
		public static const NAME:String = "Name";
		public static const CONCEPTLINK:String = "ConceptLink";
		public static const COMPONENT:String = "Component";
		public static const COMPONENT_ID:String = "ComponentId";
		public static const DESCRIPTION:String = "Description";
		public static const CARDINALITY_MIN:String = "CardinalityMin";
		public static const CARDINALITY_MAX:String = "CardinalityMax";
		public static const DOCUMENTATION:String = "Documentation";
		public static const DISPLAY_PRIORITY:String = "DisplayPriority";

		private var _spec:CMDSpec;
		private var addedChildren:ArrayCollection = new ArrayCollection();
		protected var indent:Boolean = false;

		public function XMLBrowser() {
			super();
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
			removeAddedChildren();
			handleHeader(_spec);
			handleComponents(_spec.cmdComponents);
			trace("Created browser2 view in " + (getTimer() - start) + " ms.");
		}

		protected function addFormChild(child:UIComponent):void {
			if (indent) {
				child.setStyle("paddingLeft", "50");
			}
			addChildAt(child, addedChildren.length); //Add children before already added children (e.g. from mxml), they are at the bottom then. Can make this optional if needs be.
			addedChildren.addItem(child); //Only remove children we added so other GUI element with be retained.
		}

		protected function handleHeader(spec:CMDSpec):void {
			createAndAddFormChild(DESCRIPTION, spec.headerDescription);
			createAndAddFormChild("Id", spec.headerId);
		}

		protected function createAndAddFormChild(name:String, value:String):void {
			if (value) { //only add if we have somekind of value
				addFormChild(createFormItem(name, value));
			}
		}

		protected function handleComponents(components:ArrayCollection):void {
			for each (var component:CMDComponent in components) {
				addComponent(component);
			}
		}

		public function addComponent(component:CMDComponent):void {
			var ruler:HRule = new HRule();
			ruler.percentWidth = 80;
			addFormChild(ruler);
			addFormHeading(COMPONENT);
			createAndAddFormChild(NAME, component.name);
			createAndAddFormChild(CONCEPTLINK, component.conceptLink);
			createAndAddFormChild("FileName", component.filename);
			if (component.cardinalityMin != "" || component.cardinalityMax != "")
				createAndAddFormChild("Cardinality", component.cardinalityMin + " - " + component.cardinalityMax);
			createAndAddFormChild(COMPONENT_ID, component.componentId);
			handleCMDAttributeList(component.attributeList);
			handleCMDElements(component.cmdElements);
			handleComponents(component.cmdComponents); //recursion
		}

		protected function handleCMDElements(elements:ArrayCollection):void {
			for each (var element:CMDComponentElement in elements) {
				indent = true;
				addFormHeading("Element");
				createAndAddFormChild(NAME, element.name);
				createAndAddFormChild(CONCEPTLINK, element.conceptLink);
				createAndAddFormChild(DOCUMENTATION, element.documentation);
				createAndAddFormChild(DISPLAY_PRIORITY, element.displayPriority);
				if (element.cardinalityMin != "" || element.cardinalityMax != "")
					createAndAddFormChild("Cardinality", element.cardinalityMin + " - " + element.cardinalityMax);
				createAndAddValueScheme(element.valueSchemeSimple, element.valueSchemePattern, element.valueSchemeEnumeration);
				handleCMDAttributeList(element.attributeList);
				indent = false;
			}
		}

		protected function handleCMDAttributeList(attributes:ArrayCollection):void {
			if (attributes.length > 0) {
				addFormHeading("AttributeList");
				for each (var attribute:CMDAttribute in attributes) {
					createAndAddFormChild("Name", attribute.name);
					createAndAddValueScheme(attribute.valueSchemeSimple, attribute.valueSchemePattern, attribute.valueSchemeEnumeration);
				}
			}
		}

		protected function createAndAddValueScheme(value:String = null, valuePattern:String = null, valueList:XMLListCollection = null):void {
			var formItem:FormItem;
			if (valueList) {
				formItem = createFormItem("Type", null);
				var enumeration:DisplayObject = createEnumeration(valueList);
				formItem.addChild(enumeration);
			} else if (valuePattern) {
				formItem = createFormItem("Type", valuePattern);
			} else {
				formItem = createFormItem("Type", value);
			}
			addFormChild(formItem);
		}

		private function createEnumeration(enumeration:XMLListCollection):DisplayObject {
			var result:ComboBox = createValueSchemeComboBox();
			result.dataProvider = enumeration;
			return result;
		}

		public static function createValueSchemeComboBox():ComboBox {
			var result:ComboBox = new ComboBox();
			result.itemRenderer=new ClassFactory(Label);			
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

		/**
		 * Responsible for creating a form item, override in subclasses to create different items if needed.
		 * value is optional and added as a Text field if not null by default.
		 * xmlElement is optional and ignored by default.
		 */
		protected function createFormItem(name:String, value:String = null):FormItem {
			var field:FormItem = new FormItem();
			field.label = name;
			field.styleName = StyleConstants.XMLBROWSER_FIELD;
			if (value != null && value != "") {
				var fieldValue:DisplayObject = createFormItemFieldValue(name, value);
				field.addChild(fieldValue);
			}
			return field;
		}

		protected function createFormItemFieldValue(name:String, value:String):DisplayObject {
			var fieldValue:Text = new Text();
			fieldValue.text = value;
			fieldValue.styleName = StyleConstants.XMLBROWSER_FIELD_VALUE;
			fieldValue.width = 500;
			return fieldValue;
		}

		protected function addFormHeading(name:String):void {
			addFormChild(createFormHeading(name));
		}

		protected function createFormHeading(name:String):UIComponent {
			var heading:FormItem = new FormItem();
			heading.label = name;
			if (name == ComponentMD.ATTRIBUTE_LIST) {
				heading.styleName = StyleConstants.XMLBROWSER_HEADER_SMALL;
			} else {
				heading.styleName = StyleConstants.XMLBROWSER_HEADER;
			}
			return heading;
		}

		private function removeAddedChildren():void {
			for each (var child:DisplayObject in addedChildren) {
				removeChild(child);
			}
			addedChildren = new ArrayCollection();
		}

	}


}
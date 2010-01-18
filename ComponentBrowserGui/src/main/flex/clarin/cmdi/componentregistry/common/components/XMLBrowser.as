package clarin.cmdi.componentregistry.common.components {
	import clarin.cmdi.componentregistry.common.ComponentMD;
	import clarin.cmdi.componentregistry.common.StyleConstants;
	
	import flash.display.DisplayObject;
	import flash.utils.getTimer;
	
	import mx.collections.ArrayCollection;
	import mx.containers.Form;
	import mx.containers.FormHeading;
	import mx.containers.FormItem;
	import mx.containers.HBox;
	import mx.controls.HRule;
	import mx.controls.Spacer;
	import mx.controls.Text;

	/**
	 * Generic XMLBrowser converts an xml file into a form. Use subclasses to override default methods and add custom behaviour.
	 */
	public class XMLBrowser extends Form {


		private var _xml:XML = new XML();
		private var addedChildren:ArrayCollection = new ArrayCollection();
		protected var indent:Boolean=false;

		public function XMLBrowser() {
		}

		public function set xml(xml:XML):void {
			_xml = xml;
			createNewBrowser();
		}

		[Bindable]
		public function get xml():XML {
			return _xml;
		}

		private function createNewBrowser():void {
			var start:int = getTimer();
			removeAddedChildren();
			var header:XMLList = _xml.elements(ComponentMD.HEADER);
			handleHeader(header);
			var components:XMLList = _xml.elements(ComponentMD.CMD_COMPONENT);
			handleComponents(components);
			trace("Created browser view in " + (getTimer() - start) + " ms.");
		}

		protected function addFormChild(child:DisplayObject):void {
			var result:DisplayObject = child;
			if (indent) {
				var spacer:Spacer = new Spacer();
				spacer.width = 100;
				var hbox:HBox = new HBox();
				hbox.addChild(spacer);
				hbox.addChild(child);
				result = hbox;
			}
			addChildAt(result, addedChildren.length); //Add children before already added children (e.g. from mxml), they are at the bottom then. Can make this optional if needs be.
			addedChildren.addItem(result); //Only remove children we added based on xml so other GUI element with be retained.

		}

		protected function handleHeader(headers:XMLList):void {
			for each (var header:XML in headers) { // always one Header 
				addFormHeading(header.name());
				addAttributes(header.attributes());
				addElements(header.elements());
			}
		}

		protected function handleComponents(components:XMLList):void {
			for each (var component:XML in components) {
				addFormChild(new HRule());
				addFormHeading(component.name());
				addAttributes(component.attributes());
				addElements(component.elements());
			}
		}

		protected function handleCMDElement(cmdElement:XML):void {
			addFormHeading(cmdElement.name());
			indent=true;
			addAttributes(cmdElement.attributes());
			addElements(cmdElement.elements());
			indent=false;
		}

		protected function handleCMDAttributeList(atributeList:XML):void {
			addFormHeading(atributeList.name());
			addAttributes(atributeList.attributes());
			addElements(atributeList.elements());
		}

		protected function addAttributes(attributes:XMLList):void {
			for each (var attr:XML in attributes) {
				var field:FormItem = createFormItem(attr.name(), attr.toString());
				addFormChild(field);
			}
		}

		protected function addElements(elements:XMLList):void {
			for each (var element:XML in elements) {
				if (element.name() == ComponentMD.CMD_ELEMENT) {
					handleCMDElement(element);
				} else if (element.name() == ComponentMD.CMD_COMPONENT) {
					handleComponents(XMLList(element)); //Recursively add components
				} else if (element.name() == ComponentMD.ATTRIBUTE_LIST) {
					handleCMDAttributeList(element);
				} else if (element.name() == ComponentMD.VALUE_SCHEME) {
					var valueSchemeField:FormItem = createFormItem(element.name(), element.text(), element);
					addFormChild(valueSchemeField);
				} else {
					var field:FormItem = createFormItem(element.name(), element.text());
					addFormChild(field);
					addAttributes(element.attributes());
					addElements(element.elements()); //Recursively also add child elements
				}
			}
		}


		/**
		 * Responsible for creating a form item, override in subclasses to create different items if needed.
		 * value is optional and added as a Text field if not null by default.
		 * xmlElement is optional and ignored by default.
		 */
		protected function createFormItem(name:String, value:String = null, xmlElement:XML = null):FormItem {
			var field:FormItem = new FormItem();
			field.label = name;
			field.styleName = StyleConstants.XMLBROWSER_FIELD;
			if (value != null) {
				var fieldValue:DisplayObject = createFormItemFieldValue(name, value, xmlElement);
				field.addChild(fieldValue);
			}
			return field;
		}

		protected function createFormItemFieldValue(name:String, value:String, xmlElement:XML = null):DisplayObject {
			var fieldValue:Text = new Text();
			fieldValue.text = value; //TODO PD normalize to fix layout???? See TEI description
			fieldValue.styleName = StyleConstants.XMLBROWSER_FIELD_VALUE;
			return fieldValue;
		}


		protected function addFormHeading(name:String):void {
			var heading:FormHeading = new FormHeading();
			heading.label = name;
			heading.styleName = StyleConstants.XMLBROWSER_HEADER;
			addFormChild(heading);
		}

		private function removeAddedChildren():void {
			for each(var child:DisplayObject in addedChildren) {
				removeChild(child);
			}
			addedChildren = new ArrayCollection();
		}

	}


}
package clarin.cmdi.componentregistry.common.components {
	import clarin.cmdi.componentregistry.common.ComponentMD;
	import clarin.cmdi.componentregistry.common.StyleConstants;
	
	import flash.display.DisplayObject;
	
	import mx.containers.FormItem;

	public class CMDComponentXMLEditor extends XMLBrowser {


		public function CMDComponentXMLEditor() {
			styleName = StyleConstants.XMLBROWSER;
		}

		protected override function createFormItem(name:String, value:String = null, xmlElement:XML = null):FormItem {
			var component:FormItem;
			if (name == ComponentMD.COMPONENTID) {
				component = createComponentIdLabel(name, value);
			} else {
				component = createDefaultEditField(name, value, xmlElement);
			}
			return component;
		}


		private function createDefaultEditField(name:String, value:String, xmlElement:XML):FormItem {
			var field:XMLEditorField = new XMLEditorField(xmlElement, name);
			field.label = name;
			field.styleName = StyleConstants.XMLBROWSER_FIELD;
			field.editField = value;
			return field;
		}

		private function createComponentIdLabel(name:String, value:String):FormItem {
			var fi:FormItem = new FormItem();
			fi.label = name;
			fi.styleName = StyleConstants.XMLBROWSER_FIELD;
			var componentLabel:ExpandingComponentLabel = new ExpandingComponentLabel(value, true);
			fi.addChild(componentLabel);
			return fi;
		}

	}

}
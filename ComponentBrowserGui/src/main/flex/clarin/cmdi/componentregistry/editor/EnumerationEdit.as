package clarin.cmdi.componentregistry.editor {
	import clarin.cmdi.componentregistry.common.ComponentMD;
	import clarin.cmdi.componentregistry.common.StyleConstants;
	
	import mx.collections.XMLListCollection;
	import mx.containers.FormItem;
	import mx.controls.ComboBox;
	import mx.core.UIComponent;

	public class EnumerationEdit extends FormItem {
		private var _parent:UIComponent;
		private var _valueList:XMLListCollection;

		public function EnumerationEdit(valueList:XMLListCollection, parent:UIComponent) {
			super();
			label = "ValueScheme";
			styleName = StyleConstants.XMLBROWSER_FIELD;
			_parent = parent;
			_valueList = valueList;
		}

		protected override function createChildren():void {
			super.createChildren();
			var result:ComboBox = new ComboBox();
			result.editable = true;
			result.dataProvider = _valueList;
			result.labelFunction = function(item:Object):String {
				var xmlItem:XML = item as XML;
				if (item.hasOwnProperty("@" + ComponentMD.APP_INFO)) {
					return xmlItem.attribute(ComponentMD.APP_INFO) + " - " + xmlItem.text();
				} else {
					return xmlItem.text();
				}
			};
			addChild(result);
		}
	}
}
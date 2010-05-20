package clarin.cmdi.componentregistry.editor {
	import clarin.cmdi.componentregistry.common.StyleConstants;
	
	import mx.binding.utils.BindingUtils;
	import mx.collections.ArrayCollection;
	import mx.containers.FormItem;
	import mx.controls.ComboBox;

	public class ComboBoxInputLine extends FormItem {

		public var box:ComboBox = new ComboBox();


		public function ComboBoxInputLine(name:String, value:String, dataProvider:ArrayCollection, bindingFunction:Function) {
			super();
			label = name;
			styleName = StyleConstants.XMLBROWSER_FIELD;
			box.width = 300;
			box.dataProvider = dataProvider;
			box.selectedIndex = -1;
			if (!value) {
			    box.prompt = "Select a domain...";
			} else {
			    box.prompt = value;
			}
			BindingUtils.bindSetter(bindingFunction, box, "selectedItem");
		}

		protected override function createChildren():void {
			super.createChildren();
			addChild(box);
		}


	}
}
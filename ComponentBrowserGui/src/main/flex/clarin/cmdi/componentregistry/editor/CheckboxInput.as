package clarin.cmdi.componentregistry.editor {
	import clarin.cmdi.componentregistry.common.StyleConstants;
	
	import mx.binding.utils.BindingUtils;
	import mx.collections.ArrayCollection;
	import mx.containers.FormItem;
	import mx.containers.FormItemDirection;
	import mx.controls.CheckBox;
	import mx.controls.ComboBox;

	public class CheckboxInput extends FormItem {
		private var checkbox:CheckBox = new CheckBox();

		public function CheckboxInput(name:String, value:Boolean, bindingFunction:Function) {
			super();
			label = name;
			direction = FormItemDirection.HORIZONTAL;
			styleName = StyleConstants.XMLBROWSER_FIELD;
			BindingUtils.bindSetter(bindingFunction, checkbox, "selected");
			checkbox.width = 300;
			checkbox.selected = value;
		}


		protected override function createChildren():void {
			super.createChildren();
			addChild(checkbox);
		}

	}
}
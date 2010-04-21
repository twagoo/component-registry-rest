package clarin.cmdi.componentregistry.editor {
	import clarin.cmdi.componentregistry.common.StyleConstants;

	import flash.events.Event;
	import flash.events.TextEvent;

	import mx.binding.utils.BindingUtils;
	import mx.collections.ArrayCollection;
	import mx.containers.FormItem;
	import mx.containers.FormItemDirection;
	import mx.controls.ComboBox;
	import mx.controls.TextInput;
	import mx.events.ListEvent;

	public class CardinalityInput extends FormItem {
		private var numericList:ComboBox = new ComboBox();

		public function CardinalityInput(name:String, value:String, bindingFunction:Function, editable:Boolean = true) {
			super();
			label = name;
			direction = FormItemDirection.HORIZONTAL;
			styleName = StyleConstants.XMLBROWSER_FIELD;
			BindingUtils.bindSetter(bindingFunction, numericList, "text");
			numericList.editable = true;
			numericList.prompt = value;
			numericList.toolTip = "Pick a value or type a number";
			numericList.dataProvider = new ArrayCollection([{label: "unbounded", data: "unbounded"}, {label: "0", data: "0"}, {label: "1", data: "1"}]);
		}


		protected override function createChildren():void {
			super.createChildren();
			addChild(numericList);
		}

	}
}
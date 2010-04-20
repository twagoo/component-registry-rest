package clarin.cmdi.componentregistry.editor {
	import clarin.cmdi.componentregistry.common.StyleConstants;

	import flash.events.Event;

	import mx.binding.utils.BindingUtils;
	import mx.collections.ArrayCollection;
	import mx.containers.FormItem;
	import mx.containers.FormItemDirection;
	import mx.controls.ComboBox;
	import mx.controls.TextInput;
	import mx.events.ListEvent;
	import mx.events.StateChangeEvent;

	public class CardinalityInput extends FormItem {
		private var editField:TextInput = new TextInput();
		private var numericList:ComboBox = new ComboBox();

		public function CardinalityInput(name:String, value:String, bindingFunction:Function, editable:Boolean = true) {
			super();
			label = name;
			direction = FormItemDirection.HORIZONTAL;
			styleName = StyleConstants.XMLBROWSER_FIELD;
			editField.styleName = StyleConstants.XMLEDITOR_EDIT_FIELD;
			editField.width = 250;
			editField.text = value;
			editField.editable = editable;
			//BindingUtils.bindSetter(bindingFunction, editField, "text");
			this.text = value;
			BindingUtils.bindSetter(bindingFunction, this, "text");
			numericList.editable = true;
			numericList.prompt = text;
			numericList.toolTip = "Pick a value or type a number";
			numericList.dataProvider = new ArrayCollection([{label: "unbounded", data: "unbounded"}, {label: "0", data: "0"}, {label: "1", data: "1"}]);

			numericList.addEventListener(ListEvent.CHANGE, handleChange);
			numericList.addEventListener(Event.CHANGE, handleChange2); //Need to handle key presses myself I think.
			// Listen for change and typed in data?
			//TODO PD selectedITem or Label should be set to Text
		}

		[Bindable]
		public var text:String = "";

		protected override function createChildren():void {
			super.createChildren();
			addChild(editField);
			addChild(numericList);
		}

		private function handleChange(event:ListEvent):void {
			var boxTarget:ComboBox = event.currentTarget as ComboBox;
			text = boxTarget.selectedLabel;
		}

		private function handleChange2(event:Event):void {
			var boxTarget:ComboBox = event.currentTarget as ComboBox;
			text = boxTarget.selectedLabel;
		}

	}
}
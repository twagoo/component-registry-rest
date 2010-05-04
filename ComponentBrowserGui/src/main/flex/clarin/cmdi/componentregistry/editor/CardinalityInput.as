package clarin.cmdi.componentregistry.editor {
	import clarin.cmdi.componentregistry.common.StyleConstants;

	import mx.binding.utils.BindingUtils;
	import mx.collections.ArrayCollection;
	import mx.containers.FormItem;
	import mx.containers.FormItemDirection;
	import mx.controls.ComboBox;

	public class CardinalityInput extends FormItem {
		private var numericList:ComboBox = new ComboBox();

		private static var cardinalityData:ArrayCollection = createCardinalityData();

		private static function createCardinalityData():ArrayCollection {
			var result:ArrayCollection = new ArrayCollection([{label: "unbounded", data: "unbounded"}]);
			for (var i:int = 0; i < 10; i++) {
				result.addItem({label: i, data: i});
			}
			return result;
		}

		public function CardinalityInput(name:String, value:String, bindingFunction:Function, editable:Boolean = true) {
			super();
			label = name;
			direction = FormItemDirection.HORIZONTAL;
			styleName = StyleConstants.XMLBROWSER_FIELD;
			BindingUtils.bindSetter(bindingFunction, numericList, "text");
			numericList.width = 300;
			numericList.editable = true;
			numericList.prompt = value;
			numericList.toolTip = "Pick a value or type any number";
			numericList.dataProvider = cardinalityData;
		}


		protected override function createChildren():void {
			super.createChildren();
			addChild(numericList);
		}

	}
}
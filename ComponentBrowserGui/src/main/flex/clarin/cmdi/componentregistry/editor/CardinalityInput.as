package clarin.cmdi.componentregistry.editor {
	import clarin.cmdi.componentregistry.common.StyleConstants;
	
	import mx.binding.utils.BindingUtils;
	import mx.collections.ArrayCollection;
	import mx.containers.FormItem;
	import mx.containers.FormItemDirection;
	import mx.controls.ComboBox;
	
	public class CardinalityInput extends FormItem {
		public static const BOUNDED:String = "bounded";
		public static const UNBOUNDED:String = "unbounded";
		
		private static var BOUNDED_CARDINALITY_DATA:ArrayCollection = createCardinalityData(false);
		private static var UNBOUNDED_CARDINALITY_DATA:ArrayCollection = createCardinalityData(true);
		
		private static function createCardinalityData(includeUnbounded:Boolean):ArrayCollection {
			var result:ArrayCollection = new ArrayCollection();
			if(includeUnbounded){
				result.addItem({label: "unbounded", data: "unbounded"});
			}
			for (var i:int = 0; i < 10; i++) {
				result.addItem({label: i, data: i});
			}
			return result;
		}
		
		private var numericList:ComboBox = new ComboBox();
		
		public function CardinalityInput(name:String, value:String, cardinalityType:String, bindingFunction:Function, editable:Boolean = true) {
			super();
			label = name;
			direction = FormItemDirection.HORIZONTAL;
			styleName = StyleConstants.XMLBROWSER_FIELD;
			BindingUtils.bindSetter(bindingFunction, numericList, "text");
			numericList.width = 300;
			numericList.editable = true;
			numericList.prompt = value;
			numericList.toolTip = "Pick a value or type any number";
			if(cardinalityType == BOUNDED){
				numericList.dataProvider = BOUNDED_CARDINALITY_DATA;
			} else if(cardinalityType == UNBOUNDED){
				numericList.dataProvider = UNBOUNDED_CARDINALITY_DATA
			}
		}
		
		
		protected override function createChildren():void {
			super.createChildren();
			addChild(numericList);
		}
		
	}
}
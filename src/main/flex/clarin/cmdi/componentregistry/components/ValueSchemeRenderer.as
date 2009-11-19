package clarin.cmdi.componentregistry.components {
	import mx.collections.XMLListCollection;
	import mx.containers.Box;
	import mx.controls.ComboBase;
	import mx.controls.ComboBox;
	import mx.controls.Label;

	public class ValueSchemeRenderer extends Box {

		private var _comboBox:ComboBox;
		private var _label:Label;

		public function ValueSchemeRenderer() {
			super();
			horizontalScrollPolicy = "off";
		}

		override public function set data(value:Object):void {
			super.data = value;
			var scheme:Object = value.valueScheme
			if (scheme is XMLListCollection) {
				if (_comboBox == null) {
					createComboBox()
					_comboBox.dataProvider = scheme;
				}
			} else {
				if (_label == null) {
					createLabel()
					_label.text = value.valueScheme;
				}
			}
		}

		private function createLabel():void {
			removeAllChildren();
			_label = new Label();
			addChild(_label);
		}

		private function createComboBox():void {
			removeAllChildren();
			_comboBox = new ComboBox();
			_comboBox.dropdownWidth = 500;
			addChild(_comboBox);
		}


		override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void {
			super.updateDisplayList(unscaledWidth, unscaledHeight);
			if (_comboBox != null) {
				_comboBox.width = unscaledWidth;
				_comboBox.height = unscaledHeight;
			}
			if (_label != null) {
				_label.width = unscaledWidth;
				_label.height = unscaledHeight;
			}
		}


	}
}
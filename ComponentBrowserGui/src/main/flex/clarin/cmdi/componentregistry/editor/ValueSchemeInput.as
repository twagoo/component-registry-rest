package clarin.cmdi.componentregistry.editor {
	import clarin.cmdi.componentregistry.browser.XMLBrowserValueSchemeLine;
	import clarin.cmdi.componentregistry.common.StyleConstants;
	
	import flash.events.MouseEvent;
	
	import mx.collections.XMLListCollection;
	import mx.containers.FormItem;
	import mx.containers.FormItemDirection;
	import mx.controls.Button;
	import mx.controls.ComboBox;
	import mx.controls.TextInput;
	import mx.managers.PopUpManager;

	public class ValueSchemeInput extends FormItem {

		private var textField:TextInput = new TextInput();
		private var enumeration:ComboBox;
		private var valueSchemeButton:Button = new Button();
		private var _valueSchemeEnumeration:XMLListCollection;
		private var _valueSchemePattern:String = "";
		private var _valueSchemeSimple:String = "";

		public function ValueSchemeInput(name:String) {
			super();
			direction = FormItemDirection.HORIZONTAL;
			label = name;
			styleName = StyleConstants.XMLBROWSER_FIELD;
			createValueSchemeButton();
			textField.styleName = StyleConstants.XMLEDITOR_EDIT_FIELD;
			textField.width = 300;
			textField.editable = false;
			enumeration = createEnumeration();
			enumeration.width = 300;
		}

		protected override function createChildren():void {
			super.createChildren();
			addChild(valueSchemeButton);
		}

		[Bindable]
		public function get valueSchemeSimple():String {
			return _valueSchemeSimple;
		}

		public function set valueSchemeSimple(valueSchemeSimple:String):void {
			_valueSchemeEnumeration = null
			_valueSchemePattern = "";
			_valueSchemeSimple = valueSchemeSimple
			textField.text = valueSchemeSimple as String;
			if (getChildren().length > 1) {
				removeChildAt(0);
			}
			addChildAt(textField, 0);
		}

		[Bindable]
		public function get valueSchemePattern():String {
			return _valueSchemePattern;
		}

		public function set valueSchemePattern(valueSchemePattern:String):void {
			_valueSchemeEnumeration = null;
			_valueSchemeSimple = "";
			_valueSchemePattern = valueSchemePattern
			textField.text = valueSchemePattern;
			if (getChildren().length > 1) {
				removeChildAt(0);
			}
			addChildAt(textField, 0);
		}

		[Bindable]
		public function get valueSchemeEnumeration():XMLListCollection {
			return _valueSchemeEnumeration;
		}

		public function set valueSchemeEnumeration(valueScheme:XMLListCollection):void {
			_valueSchemeEnumeration = valueScheme;
			_valueSchemeSimple = "";
			_valueSchemePattern = "";
			enumeration.dataProvider = valueScheme;
			if (getChildren().length > 1) {
				removeChildAt(0);
			}
			addChildAt(enumeration, 0);
		}

		private function createEnumeration():ComboBox {
			return XMLBrowserValueSchemeLine.createValueSchemeComboBox();
		}

		private function createValueSchemeButton():void {
			valueSchemeButton.label = "Edit...";
			valueSchemeButton.addEventListener(MouseEvent.CLICK, handleButtonClick);
		}

		private function handleButtonClick(event:MouseEvent):void {
			var popup:ValueSchemePopUp = new ValueSchemePopUp();
			popup.valueSchemeInput = this;
			PopUpManager.addPopUp(popup, this, false);
		}

	}
}
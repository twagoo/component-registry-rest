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
	import mx.events.FlexEvent;
	import mx.events.ValidationResultEvent;
	import mx.managers.PopUpManager;
	import mx.validators.Validator;

	public class ValueSchemeInput extends FormItem implements CMDValidator {

		private var textField:TextInput = new TextInput();
		private var enumeration:ComboBox;
		private var valueSchemeButton:Button = new Button();
		private var _valueSchemeEnumeration:XMLListCollection;
		private var _valueSchemePattern:String = "";
		private var _valueSchemeSimple:String = "";
		private var _validator:Validator = InputValidators.getIsRequiredValidator();

		public function ValueSchemeInput(name:String, required:Boolean = true) {
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

			CMDComponentXMLEditor.validators.addItem(this);
			addEventListener(FlexEvent.REMOVE, removeFromValidator);
		}

        private function removeFromValidator(event:FlexEvent):void {
            var index:int = CMDComponentXMLEditor.validators.getItemIndex(this);
            if (index != -1) {
                CMDComponentXMLEditor.validators.removeItemAt(index);
            }
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
 			_validator.listener = this.textField;
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
			_validator.listener = this.textField;
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
			_validator.listener = this.enumeration;
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

		public function validate():Boolean {
			var result:ValidationResultEvent;
			if (_valueSchemeEnumeration == null) {
				result = _validator.validate(this.textField.text);
			} else {
				result = _validator.validate(this.enumeration.dataProvider);
			}
			return result.type == ValidationResultEvent.VALID;
		} 
	}
}
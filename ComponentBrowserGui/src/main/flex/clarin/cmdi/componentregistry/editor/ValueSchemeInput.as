package clarin.cmdi.componentregistry.editor {
	import clarin.cmdi.componentregistry.browser.XMLBrowserValueSchemeLine;
	import clarin.cmdi.componentregistry.common.StyleConstants;
	import clarin.cmdi.componentregistry.services.ElementTypesListService;
	
	import flash.events.MouseEvent;
	
	import mx.collections.ArrayCollection;
	import mx.containers.FormItem;
	import mx.containers.FormItemDirection;
	import mx.controls.Button;
	import mx.controls.ComboBox;
	import mx.controls.TextInput;
	import mx.events.ValidationResultEvent;
	import mx.managers.PopUpManager;
	import mx.validators.Validator;
	
	import mx.binding.utils.ChangeWatcher;
	import mx.core.UIComponent;
	
	import clarin.cmdi.componentregistry.editor.model.ValueSchemeInterface;
	import clarin.cmdi.componentregistry.common.LabelConstants;
	
	public class ValueSchemeInput extends FormItem implements CMDValidator {

		private var textField:TextInput = new TextInput();
		private var enumeration:ComboBox;
		private var valueSchemeButton:Button = new Button();
		private var _valueSchemeEnumeration:ArrayCollection;
		private var _valueSchemePattern:String = "";
		private var _valueSchemeSimple:String = "";
		private var _validator:Validator = InputValidators.getIsRequiredValidator();
		
		
		[Bindable]
		public  var elementTypesService:ElementTypesListService;
		
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
			elementTypesService = new ElementTypesListService();
		}
		
		public static function makeValueSchemeInputFromValueScheme(valueScheme:ValueSchemeInterface):UIComponent{
			var valueSchemeInput:ValueSchemeInput = new ValueSchemeInput(LabelConstants.VALUESCHEME); 
			if (valueScheme.valueSchemeEnumeration == null) {
				if (valueScheme.valueSchemePattern) {
					valueSchemeInput.valueSchemePattern = valueScheme.valueSchemePattern;
				} else {
					valueSchemeInput.valueSchemeSimple = valueScheme.valueSchemeSimple;
				}
			} else {
				valueSchemeInput.valueSchemeEnumeration = valueScheme.valueSchemeEnumeration;
			}
			
			ChangeWatcher.watch(valueSchemeInput, "valueSchemeSimple", function(e:PropertyChangeEvent):void {
				valueScheme.valueSchemeSimple = e.newValue as String;
				valueScheme.valueSchemePattern = "";
				valueScheme.valueSchemeEnumeration = null;
			});
			
			ChangeWatcher.watch(valueSchemeInput, "valueSchemePattern", function(e:PropertyChangeEvent):void {
				valueScheme.valueSchemePattern = e.newValue as String;
				valueScheme.valueSchemeEnumeration = null;
				valueScheme.valueSchemeSimple = "";
			});
			
			ChangeWatcher.watch(valueSchemeInput, "valueSchemeEnumeration", function(e:PropertyChangeEvent):void {
				valueScheme.valueSchemeEnumeration = e.newValue as ArrayCollection;
				valueScheme.valueSchemeSimple = "";
				valueScheme.valueSchemePattern = "";
			});
			
			return valueSchemeInput;
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
		public function get valueSchemeEnumeration():ArrayCollection {
			return _valueSchemeEnumeration;
		}

		public function set valueSchemeEnumeration(valueScheme:ArrayCollection):void {
			_valueSchemeEnumeration = valueScheme;
			_valueSchemeSimple = "";
			_valueSchemePattern = "";
			if (getChildren().length > 1) {
				removeChildAt(0);
			}
			enumeration = createEnumeration();
			enumeration.dataProvider = valueScheme;
			addChildAt(enumeration, 0);
			_validator.listener = this.enumeration;
		}

		private function createEnumeration():ComboBox {
			var result:ComboBox = XMLBrowserValueSchemeLine.createValueSchemeComboBox();
			result.width = 300;
			return result;

		}

		private function createValueSchemeButton():void {
			valueSchemeButton.label = "Edit...";
			valueSchemeButton.addEventListener(MouseEvent.CLICK, handleButtonClick);
		}
		

		private function handleButtonClick(event:MouseEvent):void {
			
			var popup:ValueSchemePopUp = new ValueSchemePopUp();
			popup.valueSchemeInput = this;
			if (this.elementTypesService.allowedTypes == null) {
				this.elementTypesService.load();
			}
			
			PopUpManager.addPopUp(popup, this.parent, false);
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
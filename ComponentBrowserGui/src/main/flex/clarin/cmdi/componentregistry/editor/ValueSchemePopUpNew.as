package clarin.cmdi.componentregistry.editor
{
	import clarin.cmdi.componentregistry.services.IsocatService;
	
	import flash.events.Event;
	import flash.events.MouseEvent;
	
	import mx.collections.ArrayCollection;
	import mx.containers.HBox;
	import mx.containers.TabNavigator;
	import mx.containers.TitleWindow;
	import mx.containers.VBox;
	import mx.controls.Button;
	import mx.controls.ComboBox;
	import mx.controls.Label;
	import mx.controls.Spacer;
	import mx.controls.TextInput;
	import mx.core.Container;
	
	
	import mx.events.CloseEvent;
	import mx.events.FlexEvent;
	import mx.managers.PopUpManager;
	
	public class ValueSchemePopUpNew extends TitleWindow {
		
		public var valueSchemeInput:ValueSchemeInput;
		
		public static const DEFAULT_VALUE:String = "string";
		
		private var isocatPopup:IsocatSearchPopUp;
		
		public var typeTab:VBox;
		public var cvTab:VBox;
		public var patternTab:VBox;
		public var patternInput:TextInput ;
		public var enumerationGrid:ValueSchemeEnumerationGrid;
		public var simpleValueSchemeBox:ComboBox;
		
		public function ValueSchemePopUpNew(valueSchemeInputParent:ValueSchemeInput) {
			valueSchemeInput = valueSchemeInputParent;
			title="Edit and choose a type";
			showCloseButton=true;
			
			addChild(makeTabNav());
			addChild(makeHBoxCancel());	
			
			addEventListener(CloseEvent.CLOSE, cleanUp);
			addEventListener(FlexEvent.CREATION_COMPLETE, onCreationComplete);
		}
		
		public function onCreationComplete(e:FlexEvent):void{
			PopUpManager.centerPopUp(this);
		}
		
		////////////////////////////////////////////////////////
		private function makeComoBoxElementTypeSelection():ComboBox{
			var result:ComboBox = new ComboBox();
			result.selectedItem=valueSchemeInput.valueSchemeSimple;
			result.prompt=valueSchemeInput.valueSchemeSimple;
			result.dataProvider=valueSchemeInput.elementTypesService.allowedTypes;
			return result;
		}
		
		private function makeUseTypeButton():Button{
			var result:Button = new Button();
			result.label="Use type";
			result.addEventListener(MouseEvent.CLICK, setType);
			return result;
		}
		
		private function makeHBoxSimpleValueScheme():HBox{
			var result:HBox = new HBox();	
			result.percentWidth = 100; 
			
			simpleValueSchemeBox = makeComoBoxElementTypeSelection();
			result.addChild(simpleValueSchemeBox);
			
			var spacer:Spacer = new Spacer();
			spacer.percentWidth = 100;
			result.addChild(spacer);
			
			var useType:Button = makeUseTypeButton();
			result.addChild(useType);
			
			return result;
		}
		
		private function makeVBoxTypeTab():VBox{
			var result:VBox = new VBox();
			result.label="Type";
			result.percentWidth = 100;
			result.setStyle("paddingLeft", 5);
			result.setStyle("paddingRight", 5);
			
			var selectType:Label = new Label();
			selectType.text = "Select type: ";
			result.addChild(selectType);
			
			var simpleValueSchemeHBox:HBox = makeHBoxSimpleValueScheme();
			result.addChild(simpleValueSchemeHBox);
			
			return result;
		}
		
		/////////////////////////////////////////////////
		
		private function makeVBoxCvTab():VBox{
			var result:VBox = new VBox();
			result.label = "Controlled vocabulary";
			result.percentWidth = 100;
			result.setStyle("paddingLeft", 5);
			result.setStyle("paddingRight", 5);
			
			var label:Label = new Label();
			label.text="Create controlled vocabularies:";
			result.addChild(label);
			
			var hBox:HBox = new HBox();
			hBox.percentWidth = 100;
			
			enumerationGrid  = new ValueSchemeEnumerationGrid();
			enumerationGrid.addEventListener(FlexEvent.CREATION_COMPLETE, onCreationCompleteEnumerationGrid);	
			hBox.addChild(enumerationGrid); 
			
			var vBox:VBox = new VBox();
			var useControlledVocabulary:Button = new Button();
			useControlledVocabulary.label="Use Controlled Vocabulary";
			useControlledVocabulary.addEventListener(MouseEvent.CLICK, setControlledVocabulary);
			vBox.addChild(useControlledVocabulary);
			var isocatSearch:Button = new Button();
			isocatSearch.label="Search in isocat...";
			isocatSearch.addEventListener(MouseEvent.CLICK, handleIsocatButtonClick);
			isocatSearch.enabled=enumerationGrid.conceptLinkEdit;
			vBox.addChild(isocatSearch);
			
			hBox.addChild(vBox);
			
			result.addChild(hBox);
			return result;
		}
		
		
		public function onCreationCompleteEnumerationGrid(e:FlexEvent):void{
			enumerationGrid.initEnumeration(valueSchemeInput.valueSchemeEnumeration);
		}
		
		//////////////////////////////
		
		private function makeVBoxPatternTab():VBox{
			var result:VBox = new VBox();
			result.label = "Enter pattern:";
			
			var hBox:HBox  = new HBox();
			hBox.percentWidth = 100;
			patternInput = new TextInput();
			patternInput.text = valueSchemeInput.valueSchemePattern;
			hBox.addChild(patternInput);
			var spacer:Spacer = new Spacer();
			spacer.percentWidth = 100;
			hBox.addChild(spacer);
			var button:Button = new Button();
			button.label = "Use Pattern";
			button.addEventListener(MouseEvent.CLICK, setPattern)
			hBox.addChild(button);
			
			result.addChild(hBox);
			
			return result;
		}
		
		////////////////////////
		
		private function makeTabNav():TabNavigator{
			
			var result:TabNavigator = new TabNavigator();
			result.percentWidth=100;
			result.percentHeight=100;
			result.setStyle("color", 0x323232);
			result.y=59;
			result.setStyle("borderStyle", "outset");
			result.resizeToContent=true;
			result.minWidth=400;
			result.minHeight=200;
			
			typeTab = makeVBoxTypeTab();
			cvTab=makeVBoxCvTab();
			patternTab=makeVBoxPatternTab();
			result.addChild(typeTab);
			result.addChild(cvTab);
			result.addChild(patternTab);
			
			result.selectedChild = getInitialTab();
			
			return result;
		}
		
		//////////////////////////
		private function makeHBoxCancel():HBox{
			var result:HBox = new HBox();
			result.percentWidth = 100;
			
			var spacer:Spacer = new Spacer();
			result.addChild(spacer); 
			spacer.percentWidth = 100;
			
			var button:Button = new Button();
			button.label = "Cancel";
			button.addEventListener(MouseEvent.CLICK, cleanUp);
			result.addChild(button);
			
			return result;
		}
		
		//////////////////////////////
		
		private function setType(event:MouseEvent):void {
			valueSchemeInput.valueSchemeSimple = (simpleValueSchemeBox.selectedItem == null) ? DEFAULT_VALUE : simpleValueSchemeBox.selectedItem.data;
			cleanUp(event);
		}
		
		private function setPattern(event:MouseEvent):void {
			valueSchemeInput.valueSchemePattern = patternInput.text;
			cleanUp(event);
		}
		
		private function setControlledVocabulary(event:MouseEvent):void {
			var enumeration:ArrayCollection = new ArrayCollection();
			for (var i:int = 0; i < enumerationGrid.valueSchemeEnumeration.length - 1; i++) { //copy except the "Add new"
				var item:ValueSchemeItem = enumerationGrid.valueSchemeEnumeration.getItemAt(i) as ValueSchemeItem;
				enumeration.addItem(item);
			}
			valueSchemeInput.valueSchemeEnumeration = enumeration;
			cleanUp(event);
		}
		
		private function cleanUp(e:Event):void {
			PopUpManager.removePopUp(this);
		}
		
		private function getIsocatPopup():IsocatSearchPopUp {
			if (!isocatPopup) {
				isocatPopup = EditorManager.getIsocatSearchPopUp(IsocatService.TYPE_SIMPLE);
				isocatPopup.addEventListener(IsocatSearchPopUp.OK_EVENT, function(e:MouseEvent):void {
					enumerationGrid.setConceptLink(isocatPopup.editField);
				});
			}
			return isocatPopup;
		}
		
		private function handleIsocatButtonClick(event:MouseEvent):void {
			PopUpManager.addPopUp(getIsocatPopup(), this, false);
		}
		
		private function getInitialTab():Container {
			if(valueSchemeInput != null){
				if(valueSchemeInput.valueSchemeSimple) {
					return typeTab;
				}
				if(valueSchemeInput.valueSchemeEnumeration) {
					return cvTab;
				}
				if(valueSchemeInput.valueSchemePattern) {
					return patternTab;
				}
			}
			return typeTab;
		}
	}
}
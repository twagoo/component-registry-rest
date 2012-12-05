package clarin.cmdi.componentregistry.editor
{
	import clarin.cmdi.componentregistry.services.IsocatService;
	
	import mx.collections.ArrayCollection;
	import mx.containers.HBox;
	import mx.containers.VBox;
	import mx.controls.Button;
	import mx.controls.ComboBox;
	import mx.controls.Label;
	import mx.controls.Spacer;
	import mx.controls.TextInput;
	import mx.core.Container;
	import mx.managers.PopUpManager;
	
	
	//Stores the result
	[Bindable]
	public var valueSchemeInput:ValueSchemeInput;
	
	public static const DEFAULT_VALUE:String = "string";
	
	private var isocatPopup:IsocatSearchPopUp;
	
	public function ValueSchemePopUpNew(valueSchemeInputParent:ValueSchemeInput){
	valueSchemeInput = valueSchemeInputParent;
	
	}
	
	////////////////////////////////////////////////////////
	private function makeComoBoxElementTypeSelection():ComboBox{
		var result:ComboBox = new ComboBox();
		result. selectedItem=valueSchemeInput.valueSchemeSimple;
		result.prompt=valueSchemeInput.valueSchemeSimple;
		dataProvider=valueSchemeInput.elementTypesService.allowedTypes;
		return result;
	}
	
	private function makeUseTypeButton():Button{
		var result:Button = new Button();
		result.label="Use type";
		result.addEventListener(MouseEvent.CLICK, setType);
		reurn result;
	}
	
	private function makeHBoxSimpleValueScheme():HBox{
	var result:HBox = new HBox();	
	result.percentWidth = 100; 
	
	var simpleValueSchemeBox:ComboBox = makeComoBoxElementTypeSelection();
	result.addChild(simpleValueSchemeBox);
	
	var spacer:Spacer = new Spacer();
	spacer.width = 100;
	result.addChild(spacer);
	
	var useType:Button = makeUseTypebutton();
	result.addChild(useType);
	
	reurn result;
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
		
		var simpleValueSchemeBox = makeHBoxSimpleValueScheme();
		result.addChild(simpleValueSchemeBox);
		
		return result;
	}
	
	
	//////////////////////////////
	/*
	<mx:VBox id="cvTab" label="Controlled vocabulary"
	width="100%"
	paddingLeft="5" paddingRight="5">
		<!-- PD  TODO how should we implement open vocabularies?? -->
		<mx:Label text="Create controlled vocabularies:"/>
			<mx:HBox width="100%">
				<editor:ValueSchemeEnumerationGrid id="enumerationGrid"
												   creationComplete="enumerationGrid.initEnumeration(valueSchemeInput.valueSchemeEnumeration)"/>
				<mx:VBox>
					<mx:Button label="Use Controlled Vocabulary"
							   click="setControlledVocabulary()"/>
					<mx:Button label="Search in isocat..."
							   click="handleIsocatButtonClick(event)"
							   enabled="{enumerationGrid.conceptLinkEdit}"/>
				</mx:VBox>
			</mx:HBox>
		</mx:VBox>*/
	
	

	private function makeCvTab():VBox{
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
		
		var enumerationGrid:ValueSchemeEnumerationGrid  = new ValueSchemeEnumerationGrid();
		enumerationGrid.addEventListener(FlexEvent.CREATION_COMPLETE, enumerationGrid.initEnumeration(valueSchemeInput.valueSchemeEnumeration));	
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
	}
	
		
	
	private function setType(event:MouseEvent):void {
		valueSchemeInput.valueSchemeSimple = (simpleValueSchemeBox.selectedItem == null) ? 
			DEFAULT_VALUE : simpleValueSchemeBox.selectedItem.data;
		cleanUp();
	}
	
	private function setPattern():void {
		valueSchemeInput.valueSchemePattern = patternInput.text;
		cleanUp();
	}
	
	private function setControlledVocabulary():void {
		var enumeration:ArrayCollection = new ArrayCollection();
		for (var i:int = 0; i < enumerationGrid.valueSchemeEnumeration.length - 1; i++) { //copy except the "Add new"
			var item:ValueSchemeItem = enumerationGrid.valueSchemeEnumeration.getItemAt(i) as ValueSchemeItem;
			enumeration.addItem(item);
		}
		valueSchemeInput.valueSchemeEnumeration = enumeration;
		cleanUp();
	}
	
	private function cleanUp():void {
		PopUpManager.removePopUp(this);
	}
	
	private function getIsocatPopup():IsocatSearchPopUp {
		if (!isocatPopup) {
			isocatPopup = EditorManager.getIsocatSearchPopUp(IsocatService.TYPE_SIMPLE);
			isocatPopup.addEventListener(IsocatSearchPopUp.OK_EVENT, function(e:Event):void {
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
	
	
	
	public class ValueSchemePopUpNew
	{
		public function ValueSchemePopUpNew()
		{
		}
	}
}
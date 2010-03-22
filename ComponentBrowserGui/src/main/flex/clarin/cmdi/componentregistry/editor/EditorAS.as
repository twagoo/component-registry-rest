// ActionScript file
import clarin.cmdi.componentregistry.browser.BrowserColumns;
import clarin.cmdi.componentregistry.common.ItemDescription;
import clarin.cmdi.componentregistry.editor.model.CMDAttribute;
import clarin.cmdi.componentregistry.editor.model.CMDComponent;
import clarin.cmdi.componentregistry.editor.model.CMDComponentElement;
import clarin.cmdi.componentregistry.editor.model.CMDModelFactory;
import clarin.cmdi.componentregistry.editor.model.CMDSpec;
import clarin.cmdi.componentregistry.importer.UploadCompleteEvent;
import clarin.cmdi.componentregistry.services.ComponentInfoService;
import clarin.cmdi.componentregistry.services.ComponentListService;
import clarin.cmdi.componentregistry.services.ProfileInfoService;
import clarin.cmdi.componentregistry.services.UploadService;

import flash.events.Event;
import flash.net.FileReference;

import mx.core.DragSource;
import mx.core.UIComponent;
import mx.managers.DragManager;


private var currentDescription:ItemDescription;
private var profileSrv:ProfileInfoService = new ProfileInfoService();
private var componentSrv:ComponentInfoService = new ComponentInfoService();

[Bindable]
private var componentsSrv:ComponentListService = new ComponentListService(); //Don't create an instance we need a new one

[Bindable]
public var cmdComponent:XML;

[Bindable]
private var cmdSpec:CMDSpec = new CMDSpec(true);

[Bindable]
private var browserColumns:BrowserColumns = new BrowserColumns();

[Bindable]
private var uploadService:UploadService = new UploadService();


public function init():void {
	profileSrv.addEventListener(ProfileInfoService.PROFILE_LOADED, profileLoaded);
	componentSrv.addEventListener(ComponentInfoService.COMPONENT_LOADED, componentLoaded);
	componentsSrv.load();
	uploadService.init(uploadProgress);
}

private function profileLoaded(event:Event):void {
	var cmdComponent:XML = profileSrv.profile.profileSource;
	this.cmdSpec = CMDModelFactory.createModel(cmdComponent, profileSrv.profile.description);
}

private function componentLoaded(event:Event):void {
	var cmdComponent:XML = componentSrv.component.componentMD.xml;
	this.cmdSpec = CMDModelFactory.createModel(cmdComponent, componentSrv.component.description);
}


public function setDescription(itemDescription:ItemDescription):void {
	this.currentDescription = itemDescription;
	if (currentDescription.isProfile) {
		profileSrv.load(currentDescription);
	} else {
		componentSrv.load(currentDescription);
	}
}

public function clearEditor():void {
	this.cmdSpec = new CMDSpec(true);
}

private var ref:FileReference = new FileReference();

private function saveSpec():void {
//	Alert.show(xmlEditor.cmdSpec.toXml());
ref.save(xmlEditor.cmdSpec.toXml(), "test.xml");

//	var item:ItemDescription = new ItemDescription();
//	item.description = xmlEditor.cmdSpec.headerDescription;
//	item.name = xmlEditor.cmdSpec.headerName;
//	item.isProfile = xmlEditor.cmdSpec.isProfile;
//	item.groupName = xmlEditor.cmdSpec.groupName;
//	uploadService.addEventListener(UploadCompleteEvent.UPLOAD_COMPLETE, handleSaveComplete);
//	if (item.isProfile) {
//		uploadService.submitProfile(item, xmlEditor.cmdSpec.toXml());
//	} else {
//		uploadService.submitComponent(item, xmlEditor.cmdSpec.toXml());
//	}
}

private function handleSaveComplete(event:UploadCompleteEvent):void {
	parentApplication.viewStack.switchToBrowse(event.itemDescription);
}

private function handleEditorChange(event:Event):void {
	errorMessageField.text = "";
	uploadProgress.visible = false;
	uploadProgress.includeInLayout = false;
}


private function enableComponentDrag(event:MouseEvent):void {
	var ds:DragSource = new DragSource();
	ds.addHandler(function():CMDComponent {
			return new CMDComponent();
		}, CMDComponentXMLEditor.DRAG_NEW_COMPONENT);
	DragManager.doDrag(UIComponent(event.currentTarget), ds, event);
}

private function enableElementDrag(event:MouseEvent):void {
	var ds:DragSource = new DragSource();
	ds.addHandler(function():CMDComponentElement {
			return new CMDComponentElement();
		}, CMDComponentXMLEditor.DRAG_NEW_ELEMENT);
	DragManager.doDrag(UIComponent(event.currentTarget), ds, event);
}

private function enableAttributeDrag(event:MouseEvent):void {
	var ds:DragSource = new DragSource();
	ds.addHandler(function():CMDAttribute {
			return new CMDAttribute();
		}, CMDComponentXMLEditor.DRAG_NEW_ATTRIBUTE);
	DragManager.doDrag(UIComponent(event.currentTarget), ds, event);
}

private function initPaletteOverview():void {
	componentsPaletteOverview.dataGrid.dragEnabled = true;
	componentsPaletteOverview.dataGrid.allowMultipleSelection = true;
	componentsPaletteOverview.dataGrid.horizontalScrollPolicy = "auto";
	componentsPaletteOverview.dataGrid.resizableColumns = true;
}


// ActionScript file
import clarin.cmdi.componentregistry.browser.BrowserColumns;
import clarin.cmdi.componentregistry.common.ItemDescription;
import clarin.cmdi.componentregistry.editor.model.CMDComponent;
import clarin.cmdi.componentregistry.editor.model.CMDComponentElement;
import clarin.cmdi.componentregistry.editor.model.CMDModelFactory;
import clarin.cmdi.componentregistry.editor.model.CMDSpec;
import clarin.cmdi.componentregistry.importer.UploadCompleteEvent;
import clarin.cmdi.componentregistry.services.ComponentInfoService;
import clarin.cmdi.componentregistry.services.ComponentListService;
import clarin.cmdi.componentregistry.services.Config;
import clarin.cmdi.componentregistry.services.ProfileInfoService;
import clarin.cmdi.componentregistry.services.UploadService;

import flash.events.Event;

import mx.controls.Alert;
import mx.events.CloseEvent;
import mx.managers.CursorManager;


private var profileSrv:ProfileInfoService = new ProfileInfoService();
private var componentSrv:ComponentInfoService = new ComponentInfoService();
private var itemDescription:ItemDescription;

[Bindable]
private var componentsSrv:ComponentListService = ComponentListService.getInstance(Config.instance.userSpace);

[Bindable]
public var cmdComponent:XML;

[Bindable]
private var cmdSpec:CMDSpec = CMDSpec.createEmptyProfile();

[Bindable]
private var browserColumns:BrowserColumns = new BrowserColumns();

[Bindable]
private var uploadService:UploadService = new UploadService();


public function init():void {
	profileSrv.addEventListener(ProfileInfoService.PROFILE_LOADED, profileLoaded);
	componentSrv.addEventListener(ComponentInfoService.COMPONENT_LOADED, componentLoaded);
	uploadService.addEventListener(UploadCompleteEvent.UPLOAD_COMPLETE, handleSaveComplete);
	uploadService.init(uploadProgress);
	Config.instance.addEventListener(Config.USER_SPACE_TOGGLE_EVENT, toggleUserSpace);
}

private function toggleUserSpace(event:Event):void {
	componentsSrv = ComponentListService.getInstance(Config.instance.userSpace);
}

private function profileLoaded(event:Event):void {
	var cmdComponent:XML = profileSrv.profile.profileSource;
	this.cmdSpec = CMDModelFactory.createModel(cmdComponent, profileSrv.profile.description);
	CursorManager.removeBusyCursor();
}

private function componentLoaded(event:Event):void {
	var cmdComponent:XML = componentSrv.component.componentMD.xml;
	this.cmdSpec = CMDModelFactory.createModel(cmdComponent, componentSrv.component.description);
	CursorManager.removeBusyCursor();
}


public function setDescription(itemDescription:ItemDescription):void {
	if (itemDescription) {
		this.itemDescription = itemDescription;
		CursorManager.setBusyCursor();
		if (itemDescription.isProfile) {
			profileSrv.load(itemDescription);
		} else {
			componentSrv.load(itemDescription);
		}
	}
}

private function publishSpec():void {
	Alert.show("If your profile/component is ready to be used by other people press ok, otherwise press cancel and save it in your workspace or continue editing.", "Publish", Alert.OK | Alert.CANCEL, null, handlePublishAlert);
}

private function handlePublishAlert(event:CloseEvent):void {
	if (event.detail == Alert.OK) {
		saveSpec(false);
	}
}

private function saveSpec(inUserSpace:Boolean, update:Boolean = false):void {
//	Alert.show(xmlEditor.cmdSpec.toXml());
	if (xmlEditor.validate()) {
		var item:ItemDescription = new ItemDescription();
		item.description = xmlEditor.cmdSpec.headerDescription;
		item.name = xmlEditor.cmdSpec.headerName;
		item.isProfile = xmlEditor.cmdSpec.isProfile;
		item.groupName = xmlEditor.cmdSpec.groupName;
		item.domainName = xmlEditor.cmdSpec.domainName;
		item.isInUserSpace = inUserSpace;
		var doUpdate:Boolean = update && itemDescription && itemDescription.isInUserSpace;
		if (doUpdate) {
			item.id = xmlEditor.cmdSpec.headerId;
		}
		if (item.isProfile) {
			uploadService.submitProfile(item, xmlEditor.cmdSpec.toXml(), doUpdate);
		} else {
			uploadService.submitComponent(item, xmlEditor.cmdSpec.toXml(), doUpdate);
		}
	} else {
		errorMessageField.text = "Validation errors: red colored fields are invalid.";
	}
}

private function handleSaveComplete(event:UploadCompleteEvent):void {
	parentApplication.viewStack.switchToBrowse(event.itemDescription);
}


private function handleEditorChange(event:Event):void {
	errorMessageField.text = "";
	uploadProgress.visible = false;
	uploadProgress.includeInLayout = false;
}

private function initPaletteOverview():void {
	componentsPaletteOverview.dataGrid.dragEnabled = true;
	componentsPaletteOverview.dataGrid.allowMultipleSelection = true;
	componentsPaletteOverview.dataGrid.resizableColumns = true;
}

public function getType():String {
	return Config.VIEW_EDIT;
}


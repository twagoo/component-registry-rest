// ActionScript file
import clarin.cmdi.componentregistry.browser.BrowserColumns;
import clarin.cmdi.componentregistry.common.ItemDescription;
import clarin.cmdi.componentregistry.editor.model.CMDModelFactory;
import clarin.cmdi.componentregistry.editor.model.CMDSpec;
import clarin.cmdi.componentregistry.importer.UploadCompleteEvent;
import clarin.cmdi.componentregistry.services.ComponentInfoService;
import clarin.cmdi.componentregistry.services.ComponentListService;
import clarin.cmdi.componentregistry.services.ProfileInfoService;
import clarin.cmdi.componentregistry.services.UploadService;

import flash.events.Event;
import flash.net.FileReference;


private var currentDescription:ItemDescription;
private var profileSrv:ProfileInfoService = new ProfileInfoService();
private var componentSrv:ComponentInfoService = new ComponentInfoService();

[Bindable]
private var componentsSrv:ComponentListService = ComponentListService.instance;

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
	componentsSrv.load();
	uploadService.init(uploadProgress);
}

private function profileLoaded(event:Event):void {
	var cmdComponent:XML = profileSrv.profile.profileSource;
	this.cmdSpec = CMDModelFactory.createModel(cmdComponent);
}

public function setDescription(itemDescription:ItemDescription):void {
	this.currentDescription = itemDescription;
	if (currentDescription.isProfile) {
		profileSrv.load(currentDescription);
	}
}

public function createEmptyProfile():void {
	this.cmdSpec = new CMDSpec(true);
}

private var ref:FileReference = new FileReference();

private function saveProfile():void {
	//Alert.show(xmlEditor.cmdSpec.toXml());
	var item:ItemDescription = new ItemDescription();
	item.description = xmlEditor.cmdSpec.headerDescription;
	item.name = xmlEditor.cmdSpec.headerName;
	item.isProfile = xmlEditor.cmdSpec.isProfile;
	uploadService.addEventListener(UploadCompleteEvent.UPLOAD_COMPLETE, handleSaveComplete);
	uploadService.submitProfile(item, xmlEditor.cmdSpec.toXml());
}

private function handleSaveComplete(event:UploadCompleteEvent):void {
	parentApplication.viewStack.switchToBrowse(event.itemDescription);
}

private function handleEditorChange(event:Event):void {
	errorMessageField.text = "";
	uploadProgress.visible = false;
	uploadProgress.includeInLayout = false;
}




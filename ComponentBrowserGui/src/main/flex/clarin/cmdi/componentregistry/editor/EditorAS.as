// ActionScript file
import clarin.cmdi.componentregistry.browser.BrowserColumns;
import clarin.cmdi.componentregistry.browser.GroupSelectionEvent;
import clarin.cmdi.componentregistry.common.ItemDescription;
import clarin.cmdi.componentregistry.common.components.RegistryViewStack;
import clarin.cmdi.componentregistry.editor.model.CMDModelFactory;
import clarin.cmdi.componentregistry.editor.model.CMDSpec;
import clarin.cmdi.componentregistry.importer.UploadCompleteEvent;
import clarin.cmdi.componentregistry.services.ComponentInfoService;
import clarin.cmdi.componentregistry.services.ComponentListService;
import clarin.cmdi.componentregistry.services.ComponentUsageCheckEvent;
import clarin.cmdi.componentregistry.services.ComponentUsageService;
import clarin.cmdi.componentregistry.services.Config;
import clarin.cmdi.componentregistry.services.ProfileInfoService;
import clarin.cmdi.componentregistry.services.ProfileListService;
import clarin.cmdi.componentregistry.services.RegistrySpace;
import clarin.cmdi.componentregistry.services.UploadService;

import flash.events.Event;

import mx.collections.ArrayCollection;
import mx.controls.Alert;
import mx.events.CloseEvent;
import mx.managers.CursorManager;


private var profileSrv:ProfileInfoService = new ProfileInfoService();
private var componentSrv:ComponentInfoService = new ComponentInfoService();
private var itemDescription:ItemDescription;

[Bindable]
private var componentsSrv:ComponentListService = Config.instance.getComponentsSrv();

[Bindable]
private var profilesSrv:ProfileListService = Config.instance.getProfilesSrv();

[Bindable]
public var cmdComponent:XML;

[Bindable]
private var cmdSpec:CMDSpec;

[Bindable]
private var browserColumns:BrowserColumns = new BrowserColumns();

[Bindable]
private var uploadService:UploadService = new UploadService();

[Bindable]
private var viewStack:RegistryViewStack;

private var _registrySpaceEditor:RegistrySpace;

public function init():void {
	
	cmdSpec  = CMDSpec.createEmptyProfile();
	profileSrv.addEventListener(ProfileInfoService.PROFILE_LOADED, profileLoaded);
	componentSrv.addEventListener(ComponentInfoService.COMPONENT_LOADED, componentLoaded);
	uploadService.addEventListener(UploadCompleteEvent.UPLOAD_COMPLETE, handleSaveComplete);
	uploadService.init(uploadProgress);
	Config.instance.addEventListener(Config.REGISTRY_SPACE_TOGGLE_EVENT, toggleRegistrySpace);
	viewStack = this.parent as RegistryViewStack;
	
}

public function set registrySpaceEditor(rs:RegistrySpace):void{
	_registrySpaceEditor = rs;
	componentsPaletteOverview.registrySpaceComboBox.setSelectedIndex();
}

private function toggleRegistrySpace(event:Event):void {
	componentsSrv = Config.instance.getComponentsSrv();
	profilesSrv = Config.instance.getProfilesSrv();
}

private function determineSaveButtonEnabled():void {
	buttonBar.saveBtn.enabled =  (itemDescription != null && itemDescription.isPrivate && itemDescription.id != null && xmlEditor.cmdSpec.headerId != null && ( _registrySpaceEditor.space == Config.SPACE_PRIVATE || _registrySpaceEditor.space == Config.SPACE_GROUP));
}

private function determinePublishButtonEnabled():void {
	buttonBar.publishBtn.enabled =  (itemDescription != null && itemDescription.isPrivate && itemDescription.id != null && xmlEditor.cmdSpec.headerId != null && ( _registrySpaceEditor.space == Config.SPACE_PRIVATE || _registrySpaceEditor.space == Config.SPACE_GROUP));
}

private function profileLoaded(event:Event):void {
	var cmdComponent:XML = profileSrv.profile.profileSource;
	this.cmdSpec = CMDModelFactory.createModel(cmdComponent, profileSrv.profile.description);
	this.cmdSpec.changeTracking = true;
	determineSaveButtonEnabled();
	determinePublishButtonEnabled();	
	Config.instance.getListGroupsOfItemService().loadGroupsForItem(itemDescription.id);
	CursorManager.removeBusyCursor();
}

private function componentLoaded(event:Event):void {
	var cmdComponent:XML = componentSrv.component.componentMD.xml;
	this.cmdSpec = CMDModelFactory.createModel(cmdComponent, componentSrv.component.description);
	// Track changes for components being edited
	this.cmdSpec.changeTracking = true;
	determineSaveButtonEnabled();
	determinePublishButtonEnabled();
	
	Config.instance.getListGroupsOfItemService().loadGroupsForItem(itemDescription.id);
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
		buttonBar.saveBtn.enabled = false;
	}
}



private function publishSpec():void {
	Alert.show("If your profile/component is ready to be used by other people press ok, otherwise press cancel and save it in your workspace or continue editing.", "Publish", Alert.OK | Alert.CANCEL, null, handlePublishAlert);
	Config.instance.registrySpace = _registrySpaceEditor;
}

private function handlePublishAlert(event:CloseEvent):void {
	if (event.detail == Alert.OK) {
		saveSpec(UploadService.PUBLISH);
	}
}

private function saveSpec(uploadAction:int):void {
	if (xmlEditor.validate()) {
		var item:ItemDescription = new ItemDescription();
		item.description = xmlEditor.cmdSpec.headerDescription;
		item.name = xmlEditor.cmdSpec.headerName;
		item.isProfile = xmlEditor.cmdSpec.isProfile;
		item.groupName = xmlEditor.cmdSpec.groupName;
		item.domainName = xmlEditor.cmdSpec.domainName;
		item.isPrivate = (_registrySpaceEditor.space ==  Config.SPACE_PRIVATE || _registrySpaceEditor.space == Config.SPACE_GROUP);
		if (itemDescription && itemDescription.isPrivate ) {
			item.id = xmlEditor.cmdSpec.headerId;
		}
		
		// Private components that are in updated require usage check call. If in use, the user can choose whether or not to save the changes .
		if(( (_registrySpaceEditor.space) == Config.SPACE_PRIVATE || 
			(_registrySpaceEditor.space) == Config.SPACE_GROUP) && uploadAction == UploadService.UPDATE && !item.isProfile){
			checkUsage(item, _registrySpaceEditor.space);
		}else{
			doUpload(uploadAction,item);
		}
		
		if (uploadAction == UploadService.NEW) {
			_registrySpaceEditor=new RegistrySpace(Config.SPACE_PRIVATE, "");
		}
		
		if (uploadAction == UploadService.PUBLISH) {
			_registrySpaceEditor=new RegistrySpace(Config.SPACE_PUBLISHED, "");
		}
		
	} else {
		errorMessageField.text = "Validation errors: red colored fields are invalid.";
	}
	Config.instance.registrySpace = _registrySpaceEditor;
}



private function cancel():void {
	if(xmlEditor.specHasChanges){
	Alert.show("There are pending changes. Cancelling will discard these. Are you sure you want to proceed?", "Discard changes?", Alert.YES|Alert.NO, null, 
		function (eventObj:CloseEvent):void{
			if(eventObj.detail == Alert.YES){
				viewStack.switchToBrowse(itemDescription);				
			}
		});
	} else {
		viewStack.switchToBrowse(itemDescription);
	}
	Config.instance.registrySpace = _registrySpaceEditor;
}

/**
 * Calls usage check for the specified component. If in use, asks user whether to proceed; if positive, initiates update.
 */
private function checkUsage(item:ItemDescription, space:String, uploadAction:int = 0):void{
	var componentUsageService:ComponentUsageService = new ComponentUsageService(item);
	componentUsageService.addEventListener(ComponentUsageCheckEvent.COMPONENT_IN_USE, 
		function (event:ComponentUsageCheckEvent):void{
			if(event.isComponentInUse){
				var messageBody:String = "The component you are about to save is used by the following component(s) and/or profile(s):\n\n";
				for each(var name:String in event.itemUsingComponent){
					messageBody += " - " + name + "\n";
				}
				messageBody += "\nChanges in this component will affect the above. Do you want to proceed?";
				Alert.show(messageBody,"Component is used", Alert.YES|Alert.NO,null,
					function (eventObj:CloseEvent):void{
						if(eventObj.detail == Alert.YES){
							doUpload(uploadAction, item);
						}
					});
			} else {
				doUpload(uploadAction,item);
			}
		});
	componentUsageService.checkUsage();
}

private function doUpload(uploadAction:int, item:ItemDescription):void{
	uploadService.upload(uploadAction, item, xmlEditor.cmdSpec.toXml());
}

private function handleSaveComplete(event:UploadCompleteEvent):void {	
	setDescription(event.itemDescription);
	parentApplication.viewStack.switchToBrowse(event.itemDescription);	
}


private function handleEditorChange(event:Event):void {
	errorMessageField.text = "";
	uploadProgress.visible = false;
	uploadProgress.includeInLayout = false;
	determineSaveButtonEnabled();
	determinePublishButtonEnabled();
}

private function initPaletteOverview():void {
	componentsPaletteOverview.dataGrid.dragEnabled = true;
	componentsPaletteOverview.dataGrid.allowMultipleSelection = true;
	componentsPaletteOverview.dataGrid.resizableColumns = true;
	componentsPaletteOverview.topPlank.getChildByName("rssLink").visible = false;
	
}

public function getType():String {
	return Config.VIEW_EDIT;
}


private function onGroupsLoaded(event:Event):void{
	var groups:ArrayCollection = Config.instance.getListGroupsOfItemService().groups;
	if (groups.length < 1);
	else{
		var groupId:String = groups.getItemAt(0).id;
		var itemId:String = itemDescription.id;
	}
}

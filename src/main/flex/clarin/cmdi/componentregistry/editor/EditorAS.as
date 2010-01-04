// ActionScript file
import clarin.cmdi.componentregistry.common.ItemDescription;
import clarin.cmdi.componentregistry.services.ComponentInfoService;
import clarin.cmdi.componentregistry.services.ProfileInfoService;

import flash.events.Event;

private var currentDescription:ItemDescription;
private var profileSrv:ProfileInfoService = new ProfileInfoService();
private var componentSrv:ComponentInfoService = new ComponentInfoService();

private static const emptyProfile:XML = <CMD_ComponentSpec isProfile="true">
		<Header>
			<ID>p_1234</ID>
			<Name>Profile1</Name>
			<Description>MyDesc</Description>
		</Header>
	</CMD_ComponentSpec>;


[Bindable]
public var cmdComponent:XML = emptyProfile;


public function init():void {
	profileSrv.addEventListener(ProfileInfoService.PROFILE_LOADED, profileLoaded);
}

private function profileLoaded(event:Event):void {
	cmdComponent = profileSrv.profile.profileSource;
}

public function setDescription(itemDescription:ItemDescription):void {
	this.currentDescription = itemDescription;
	if (currentDescription.isProfile) {
		profileSrv.load(currentDescription);
	}
}

public function createEmptyProfile():void {
	cmdComponent = <CMD_ComponentSpec isProfile="true">
		<Header>
			<ID>p_1234</ID>
			<Name>Profile1</Name>
			<Description>MyDesc</Description>
		</Header>
	</CMD_ComponentSpec>;
}

public function addComponent():void {
	var temp:XML = xmlEditor.xml.copy();
	temp.appendChild(<CMD_Component filename="component-description.xml"/>);
	cmdComponent = temp;
}





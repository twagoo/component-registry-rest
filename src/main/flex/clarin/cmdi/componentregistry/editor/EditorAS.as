// ActionScript file
import clarin.cmdi.componentregistry.browser.BrowserColumns;
import clarin.cmdi.componentregistry.common.ItemDescription;
import clarin.cmdi.componentregistry.services.ComponentInfoService;
import clarin.cmdi.componentregistry.services.ComponentListService;
import clarin.cmdi.componentregistry.services.ProfileInfoService;

import flash.events.Event;

import mx.core.UIComponent;
import mx.events.DragEvent;
import mx.managers.DragManager;

private var currentDescription:ItemDescription;
private var profileSrv:ProfileInfoService = new ProfileInfoService();
private var componentSrv:ComponentInfoService = new ComponentInfoService();

[Bindable]
private var componentsSrv:ComponentListService = ComponentListService.instance;


private static const emptyProfile:XML = <CMD_ComponentSpec isProfile="true">
		<Header>
			<ID>p_1234</ID>
			<Name>Profile1</Name>
			<Description>MyDesc</Description>
		</Header>
	</CMD_ComponentSpec>;


[Bindable]
public var cmdComponent:XML = emptyProfile;

[Bindable]
private var browserColumns:BrowserColumns = new BrowserColumns();


public function init():void {
	profileSrv.addEventListener(ProfileInfoService.PROFILE_LOADED, profileLoaded);
	componentsSrv.load();
}

private function dragEnterHandler(event:DragEvent):void {
	DragManager.acceptDragDrop(event.currentTarget as UIComponent);
}

private function dragOverHandler(event:DragEvent):void {
	if (event.dragSource.hasFormat("items")) {
		DragManager.showFeedback(DragManager.COPY);
	}
}

private function dragDropHandler(event:DragEvent):void {
	var items:Array = event.dragSource.dataForFormat("items") as Array;
	addComponents(items);
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
				<ID>p_1</ID>
				<Name>Profile1</Name>
				<Description>MyDesc</Description>
			</Header>
		</CMD_ComponentSpec>;
}

public function addComponents(items:Array):void {
	var temp:XML = xmlEditor.xml.copy();
	for each (var item:ItemDescription in items) {
	    var comp:XML =<CMD_Component ComponentId={item.id}/>
		temp.appendChild(comp);
	}
	cmdComponent = temp;
}





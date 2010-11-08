package clarin.cmdi.componentregistry.common.components {
	import clarin.cmdi.componentregistry.browser.Browse;
	import clarin.cmdi.componentregistry.common.Credentials;
	import clarin.cmdi.componentregistry.common.ItemDescription;
	import clarin.cmdi.componentregistry.common.Login;
	import clarin.cmdi.componentregistry.editor.Editor;
	import clarin.cmdi.componentregistry.importer.Importer;
	import clarin.cmdi.componentregistry.services.Config;
	
	import flash.events.Event;
	
	import mx.containers.ViewStack;
	import mx.events.FlexEvent;

	public class RegistryViewStack extends ViewStack {
		public var browse:Browse = new Browse();
		private var editor:Editor = new Editor();
		private var importer:Importer = new Importer();
    
		private var loginPanel:Login;
		private var selectedItem:ItemDescription;

		public function RegistryViewStack() {
			loginPanel = new Login();
			loginPanel.addEventListener(Login.FAILED, loginFailed);
			browse.addEventListener(Browse.START_ITEM_LOADED, switchWithStartupItem);
			addChild(browse); //everyone can browse

			editor.addEventListener(FlexEvent.SHOW, checkLogin);
			addChild(editor);
			importer.addEventListener(FlexEvent.SHOW, checkLogin);
			addChild(importer);
		}

		private function switchWithStartupItem(even:Event):void {
			var item:ItemDescription = browse.getSelectedStartItem();
			switchView(Config.instance.view, item);
		}

		public function switchView(view:String, item:ItemDescription = null):void {
			if (view == Config.VIEW_BROWSE) {
				switchToBrowse(item);
			} else if (view == Config.VIEW_EDIT) {
				switchToEditor(item);
			} else if (view == Config.VIEW_IMPORT) {
				switchToImport();
			}
		}

		public function switchToBrowse(itemDescription:ItemDescription):void {
			if (itemDescription != null) {
				if (Config.instance.userSpace == itemDescription.isInUserSpace) {
					browse.refresh();
				} else {
					Config.instance.userSpace = itemDescription.isInUserSpace;
				}
				browse.setSelectedDescription(itemDescription);
			}
			this.selectedItem = itemDescription;
			this.selectedChild = browse;
		}

		public function switchToEditor(itemDescription:ItemDescription):void {
			this.selectedChild = editor;
			this.selectedItem = itemDescription;
			editor.setDescription(itemDescription);
		}

		public function switchToImport():void {
			this.selectedChild = importer;
		}

		private function checkLogin(event:Event):void {
			if (!Credentials.instance.isLoggedIn())
				loginPanel.show(this, RegistryView(this.selectedChild).getType(), null, selectedItem);
		}

		private function loginFailed(event:Event):void {
			this.selectedChild = browse;
		}

	}
}
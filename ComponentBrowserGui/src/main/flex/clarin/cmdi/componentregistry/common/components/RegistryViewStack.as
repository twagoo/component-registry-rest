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
	import mx.controls.Alert;
	import mx.events.CloseEvent;
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
			
			if (!Config.instance.debug) {
				editor.addEventListener(FlexEvent.SHOW, checkLogin);
				importer.addEventListener(FlexEvent.SHOW, checkLogin);
			}
			addChild(editor);
			addChild(importer);
		}
		
		private function switchWithStartupItem(even:Event):void {
			var item:ItemDescription = browse.getSelectedStartItem();
			switchView(Config.instance.view, item);
		}
		
		public function loadStartup():void {
			if (Config.instance.space == Config.SPACE_USER && !Credentials.instance.isLoggedIn()) {
				checkLogin();
			} else {
				if (Config.instance.startupItem) {
					browse.loadStartup();
				} else {
					switchView(Config.instance.view);
				}
			}
		}
		
		private function switchView(view:String, item:ItemDescription = null):void {
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
				if (Config.instance.space == itemDescription.space) {
					browse.refresh();
				} else {
					Config.instance.userSpace = itemDescription.space;
				}
				browse.setSelectedDescription(itemDescription);
			}
			this.selectedItem = itemDescription;
			this.selectedChild = browse;
		}
		
		public function switchToEditor(itemDescription:ItemDescription):void {
			doSwitchToEditor(itemDescription);
		}
		
		private function doSwitchToEditor(itemDescription:ItemDescription):void {
			this.selectedChild = editor;
			if(itemDescription != null) {
				this.selectedItem = itemDescription;
				editor.setDescription(itemDescription);
			}
		}
		
		public function switchToImport():void {
			this.selectedChild = importer;
		}
		
		private function checkLogin(event:Event = null):void {
			if (!Credentials.instance.isLoggedIn()) {
				var itemId:String = Config.instance.startupItem;
				if (selectedItem) {
					itemId = selectedItem.id;
				}
				loginPanel.show(this, RegistryView(this.selectedChild).getType(), Config.instance.space, itemId);
			}
		}
		
		private function loginFailed(event:Event):void {
			this.selectedChild = browse;
		}
		
		public function getEditor():Editor {
			return this.editor;
		}
		
	}
}
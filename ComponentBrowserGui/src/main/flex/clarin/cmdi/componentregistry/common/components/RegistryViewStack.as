package clarin.cmdi.componentregistry.common.components {
	import clarin.cmdi.componentregistry.browser.Browse;
	import clarin.cmdi.componentregistry.common.ItemDescription;
	import clarin.cmdi.componentregistry.common.Login;
	import clarin.cmdi.componentregistry.editor.Editor;
	import clarin.cmdi.componentregistry.importer.Importer;
	import clarin.cmdi.componentregistry.services.Config;

	import flash.display.DisplayObject;
	import flash.events.Event;

	import mx.containers.ViewStack;

	public class RegistryViewStack extends ViewStack {
		private var browse:Browse = new Browse();
		private var editor:Editor = new Editor();
		private var importer:Importer = new Importer();

		private var loginPanel:Login;

		public function RegistryViewStack() {
			loginPanel = new Login();
			loginPanel.addEventListener(Login.SUCCESS, loginSuccess);
			loginPanel.addEventListener(Login.FAILED, loginFailed);

			addChild(browse); //everyone can browse

//			editor.addEventListener(FlexEvent.SHOW, checkLogin);
			addChild(editor);
			//register.addEventListener(FlexEvent.SHOW, checkLogin);
			addChild(importer);

		}

		public function switchToBrowse(itemDescription:ItemDescription):void {
			if (Config.instance.userSpace == itemDescription.isInUserSpace) {
				browse.refresh();
			} else {
				Config.instance.userSpace = itemDescription.isInUserSpace;
			}
			browse.setSelectedDescription(itemDescription);
			this.selectedChild = browse;
		}

		public function switchToEditor(itemDescription:ItemDescription):void {
			this.selectedChild = editor;
			editor.setDescription(itemDescription);
		}

		private function checkLogin(event:Event):void {
			loginPanel.show(event.target as DisplayObject);
		}

		private function loginSuccess(event:Event):void {
			this.selectedChild = browse;
		}

		private function loginFailed(event:Event):void {
			this.selectedChild = browse;
		}

	}
}
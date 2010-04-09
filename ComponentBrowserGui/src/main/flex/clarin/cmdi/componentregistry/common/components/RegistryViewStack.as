package clarin.cmdi.componentregistry.common.components {
	import clarin.cmdi.componentregistry.browser.Browse;
	import clarin.cmdi.componentregistry.common.ItemDescription;
	import clarin.cmdi.componentregistry.common.Login;
	import clarin.cmdi.componentregistry.editor.Editor;
	import clarin.cmdi.componentregistry.register.Register;

	import flash.display.DisplayObject;
	import flash.events.Event;

	import mx.containers.ViewStack;
	import mx.events.FlexEvent;

	public class RegistryViewStack extends ViewStack {
		private var browse:Browse = new Browse();
		private var register:Register = new Register();
		private var editor:Editor = new Editor();

		private var loginPanel:Login;

		public function RegistryViewStack() {
			loginPanel = new Login();
			loginPanel.addEventListener(Login.SUCCESS, loginSuccess);
			loginPanel.addEventListener(Login.FAILED, loginFailed);

			addChild(browse); //everyone can browse

			register.addEventListener(FlexEvent.SHOW, checkLogin);
			addChild(register);

			editor.addEventListener(FlexEvent.SHOW, checkLogin);
			addChild(editor);
		}

		public function switchToBrowse(itemDescription:ItemDescription):void {
			browse.refresh();
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
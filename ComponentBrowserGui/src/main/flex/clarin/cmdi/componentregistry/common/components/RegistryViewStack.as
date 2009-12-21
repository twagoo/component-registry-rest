package clarin.cmdi.componentregistry.common.components {
	import clarin.cmdi.componentregistry.common.ItemDescription;
	import clarin.cmdi.componentregistry.browser.Browse;
	import clarin.cmdi.componentregistry.register.Register;

	import mx.containers.ViewStack;

	public class RegistryViewStack extends ViewStack {
		private var browse:Browse = new Browse();
		private var register:Register = new Register();
		private var editor:Register = new Register();

		public function RegistryViewStack() {
			addChild(browse);
			addChild(register);
			editor.label = "Editor...";
			addChild(editor);
		}

		public function switchToBrowse(itemDescription:ItemDescription):void {
			browse.refresh();
			this.selectedChild = browse;
			browse.setSelectedDescription(itemDescription);
		}

		public function switchToEditor():void {
			this.selectedChild = editor;
		}

	}
}
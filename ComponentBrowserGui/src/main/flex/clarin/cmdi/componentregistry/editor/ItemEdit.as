package clarin.cmdi.componentregistry.editor
{
	import clarin.cmdi.componentregistry.common.StyleConstants;
	
	import flash.display.DisplayObject;
	
	import mx.containers.Form;
	import mx.core.Container;
	
	public class ItemEdit extends Form
	{
		private var showToggleBox:ShowToggleBox;
		private var hideableForm:Form;
		
		public function ItemEdit()
		{
			super();
			hideableForm = createHidableForm();
			
			showToggleBox = new ShowToggleBox();
			showToggleBox.visibleState = true;
			
			showToggleBox.visibleContainer = hideableForm;
		}
		
		private function createHidableForm():Form {
			var form:Form = new Form();
			form.styleName = StyleConstants.XMLBROWSER;
			form.setStyle("paddingTop","0");
			form.setStyle("paddingBottom","0");
			form.verticalScrollPolicy = "off";
			form.horizontalScrollPolicy = "off";
			return form;
		}
		
		protected function getShowToggleBox():ShowToggleBox{
			return showToggleBox;
		}
		
		protected function setSummaryContainer(container:Container):void{
			showToggleBox.invisibleContainer = container;
		}
		
		protected function getHideableForm():Form{
			return hideableForm;
		}
		
		protected function addToHideableForm(child:DisplayObject):void{
			hideableForm.addChild(child);
		}
	}
}
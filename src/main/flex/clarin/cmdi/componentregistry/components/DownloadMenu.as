package clarin.cmdi.componentregistry.components {
	import clarin.cmdi.componentregistry.ItemDescription;

	import flash.events.ContextMenuEvent;
	import flash.events.MouseEvent;
	import flash.ui.ContextMenu;
	import flash.ui.ContextMenuItem;

	import mx.controls.Alert;
	import mx.controls.DataGrid;
	import mx.events.ListEvent;


	public class DownloadMenu {

		[Bindable]
		public var cm:ContextMenu;

		[Bindable]
		private var _dataGrid:DataGrid;

		private var rollOverIndex:int;
		private var saveItemDialog:SaveItemDialog = new SaveItemDialog();

		public function DownloadMenu() {
			cm = new ContextMenu();
			cm.hideBuiltInItems();
			cm.customItems = createMenuItems();
			cm.addEventListener(ContextMenuEvent.MENU_SELECT, menuSelect);
		}

		private function createMenuItems():Array {
			var result:Array = new Array();
			var cmi:ContextMenuItem = new ContextMenuItem("Save as XML...");
			cmi.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, saveAsXml);
			result.push(cmi);
			cmi = new ContextMenuItem("Save as XSD...");
			cmi.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, saveAsXsd);
			result.push(cmi);
			return result;
		}

		public function set dataGrid(dataGrid:DataGrid):void {
			_dataGrid = dataGrid;
			_dataGrid.addEventListener(ListEvent.ITEM_ROLL_OVER, setRollOverIndex);
		}

		private function setRollOverIndex(event:ListEvent):void {
			rollOverIndex = event.rowIndex;
		}

		private function menuSelect(event:ContextMenuEvent):void {
			_dataGrid.selectedIndex = rollOverIndex;
			_dataGrid.dispatchEvent(new MouseEvent(MouseEvent.CLICK));
		}

		private function saveAsXml(event:ContextMenuEvent):void {
			var item:ItemDescription = _dataGrid.selectedItem as ItemDescription;
			saveItemDialog.saveAsXML(item);
		}

		private function saveAsXsd(event:ContextMenuEvent):void {
			var item:ItemDescription = _dataGrid.selectedItem as ItemDescription;
			saveItemDialog.saveAsXSD(item);
		}

	}
}
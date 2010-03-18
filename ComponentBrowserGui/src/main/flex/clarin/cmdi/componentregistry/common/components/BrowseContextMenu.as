package clarin.cmdi.componentregistry.common.components {
	import clarin.cmdi.componentregistry.common.ItemDescription;
	import clarin.cmdi.componentregistry.services.DeleteService;

	import flash.events.ContextMenuEvent;
	import flash.events.Event;
	import flash.events.MouseEvent;
	import flash.ui.ContextMenu;
	import flash.ui.ContextMenuItem;

	import mx.controls.Alert;
	import mx.controls.DataGrid;
	import mx.events.ListEvent;


	public class BrowseContextMenu {

		[Bindable]
		public var cm:ContextMenu;

		[Bindable]
		public var viewStack:RegistryViewStack;
		[Bindable]
		public var deleteService:DeleteService;

		[Bindable]
		private var _dataGrid:DataGrid;

		private var rollOverIndex:int;
		private var saveItemDialog:SaveItemDialog = new SaveItemDialog();

		public function BrowseContextMenu() {
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
			cmi = new ContextMenuItem("Edit Item...");
			cmi.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, editItem);
			result.push(cmi);
			cmi = new ContextMenuItem("Delete...");
			cmi.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, handleDelete);
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

		private function editItem(event:ContextMenuEvent):void {
			var item:ItemDescription = _dataGrid.selectedItem as ItemDescription;
			viewStack.switchToEditor(item);
		}

		private function handleDelete(event:ContextMenuEvent):void {
		    deleteSelectedItems();
		}
		
		public function deleteSelectedItems():void {
			for each (var item:ItemDescription in _dataGrid.selectedItems) {
				deleteService.deleteItem(item);
			}
		}

	}
}
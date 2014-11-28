package clarin.cmdi.componentregistry.common.components {
	import clarin.cmdi.componentregistry.common.ItemDescription;
	import clarin.cmdi.componentregistry.common.ShowInfoPopUp;
	import clarin.cmdi.componentregistry.services.Config;
	import clarin.cmdi.componentregistry.services.DeleteService;
	import clarin.cmdi.componentregistry.services.SaveItemDialog;
	
	import flash.events.ContextMenuEvent;
	import flash.geom.Point;
	import flash.ui.ContextMenu;
	import flash.ui.ContextMenuItem;
	
	import mx.controls.Alert;
	import mx.controls.DataGrid;
	import mx.controls.Text;
	import mx.events.CloseEvent;
	import mx.managers.PopUpManager;


	public class BrowseContextMenu {

		[Bindable]
		public var cm:ContextMenu;

		[Bindable]
		public var viewStack:RegistryViewStack;
		[Bindable]
		public var deleteService:DeleteService;

		[Bindable]
		private var _dataGrid:DataGrid;
		
		
		private var saveItemDialog:SaveItemDialog = new SaveItemDialog();
		private var isComponent:Boolean = false;
		
		private var editMenuItem:ContextMenuItem;
		private var editAsNewMenuItem:ContextMenuItem;

		public function BrowseContextMenu(isComponent:Boolean = false) {
			this.isComponent = isComponent
			cm = new ContextMenu();
			cm.hideBuiltInItems();
			cm.customItems = createMenuItems();
			setItemStates();
			
			Config.instance.addEventListener(Config.REGISTRY_SPACE_TOGGLE_EVENT, setItemStates);
		}

		private function createMenuItems():Array {
			var result:Array = new Array();
			var cmi:ContextMenuItem = new ContextMenuItem("Show info");
			cmi.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, showInfo);
			result.push(cmi);
			cmi = new ContextMenuItem("Download as XML...");
			cmi.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, saveAsXml);
			result.push(cmi);
			if (!isComponent) {
				cmi = new ContextMenuItem("Download as XSD...");
				cmi.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, saveAsXsd);
				result.push(cmi);
			}
			editMenuItem = new ContextMenuItem("Edit Item...");
			editMenuItem.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, editItem);
			result.push(editMenuItem);
			
			editAsNewMenuItem = new ContextMenuItem("Edit Item as New...");
			editAsNewMenuItem.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, editItem);
			result.push(editAsNewMenuItem);
			
			cmi = new ContextMenuItem("Delete Item...", true);
			cmi.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, handleDelete);
			result.push(cmi);

			return result;
		}
		
		private function setItemStates(event:Event = null):void {
			editMenuItem.visible = (Config.instance.registrySpace.space == Config.SPACE_PRIVATE || Config.instance.registrySpace.space == Config.SPACE_GROUP);
			editAsNewMenuItem.visible = (Config.instance.registrySpace.space == Config.SPACE_PUBLISHED);
		}

		public function set dataGrid(dataGrid:DataGrid):void {
			_dataGrid = dataGrid;
		}

		private function showInfo(event:ContextMenuEvent):void {
			var item:ItemDescription = _dataGrid.selectedItem as ItemDescription;
			if (!item) {
				showSelectAlert();
				return;
			}
			var showInfoPanel:ShowInfoPopUp = new ShowInfoPopUp();
			showInfoPanel.setItem(item);
			var point:Point = new Point();
			point.x = event.mouseTarget.mouseX;
			point.y = event.mouseTarget.mouseY;
			point = event.mouseTarget.localToGlobal(point);
			showInfoPanel.x = point.x;
			showInfoPanel.y = point.y;
			PopUpManager.addPopUp(showInfoPanel, event.mouseTarget);
		}

		private function saveAsXml(event:ContextMenuEvent):void {
			var item:ItemDescription = _dataGrid.selectedItem as ItemDescription;
			if (!item) {
				showSelectAlert();
				return;
			}
			saveItemDialog.saveAsXML(item);
		}

		private function saveAsXsd(event:ContextMenuEvent):void {
			var item:ItemDescription = _dataGrid.selectedItem as ItemDescription;
			if (!item) {
				showSelectAlert();
				return;
			}
			saveItemDialog.saveAsXSD(item);
		}

		private function editItem(event:ContextMenuEvent):void {
			var item:ItemDescription = _dataGrid.selectedItem as ItemDescription;
			if (!item) {
			    showSelectAlert();
			    return;
			}
			viewStack.switchToEditor(item);
		}

		private function showSelectAlert():void {
			Alert.show("First select a profile or component");
		}

		private function handleDelete(event:ContextMenuEvent):void {
   			if (!_dataGrid.selectedItem) {
			    showSelectAlert();
			    return;
			}
			deleteSelectedItems();
		}

		public function deleteSelectedItems():void {
			var message:String = "";
			for each (var item:ItemDescription in _dataGrid.selectedItems) {
				if (message != "") {
					message += "\n";
				}
				message += item.name;
			}
			Alert.show("You will delete the following item(s):\n" + message + "\n\n This cannot be undone.", "Delete items", Alert.OK | Alert.CANCEL, null, handleDeleteAlert);
		}

		private function handleDeleteAlert(event:CloseEvent):void {
			if (event.detail == Alert.OK) {
				for each (var item:ItemDescription in _dataGrid.selectedItems) {
					deleteService.deleteItem(item);
				}
			}
		}

	}
}
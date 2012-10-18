package clarin.cmdi.componentregistry.common.components
{
	import clarin.cmdi.componentregistry.services.Config;
	
	import flash.events.ContextMenuEvent;
	import flash.system.System;
	import flash.ui.ContextMenu;
	import flash.ui.ContextMenuItem;
	
	
	public class RssContextMenu
	{
		
		[Bindable]
		public var cm:ContextMenu;
		
		private var _copyRssLinkMenuItem:ContextMenuItem;
		private var _showRssLinkMenuItem:ContextMenuItem;
		private var typeOfDescription:String;
		
		public function RssContextMenu(typeOfDescription:String)
		{ 
			this.typeOfDescription = typeOfDescription;
			cm = new ContextMenu();
			cm.hideBuiltInItems();
			cm.customItems = createMenuItems();
			
		}
		
		private function createMenuItems():Array {
			var result:Array = new Array();
			
			_showRssLinkMenuItem = new ContextMenuItem("Show link to the RSS feed");
			_showRssLinkMenuItem.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, showRssLink);
			_showRssLinkMenuItem.visible = true;
			result.push(_showRssLinkMenuItem);
			
			_copyRssLinkMenuItem = new ContextMenuItem("Copy link to the clipboard");
			_copyRssLinkMenuItem.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, copyRssLink);
			_copyRssLinkMenuItem.visible = true;
			result.push(_copyRssLinkMenuItem);
			
			
			return result;
		}
		
		
		private function copyRssLink(event:ContextMenuEvent):void {
			System.setClipboard(Config.getRssUriDescriptions(typeOfDescription));
		}
		
		private function showRssLink(event:ContextMenuEvent):void{
			
		}
		
	}
}
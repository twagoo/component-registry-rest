package clarin.cmdi.componentregistry.common.components
{
	import clarin.cmdi.componentregistry.services.Config;
	
	import flash.events.Event;
	import flash.events.ContextMenuEvent;
	import flash.geom.Point;
	import flash.system.System;
	import flash.ui.ContextMenu;
	import flash.ui.ContextMenuItem;
	
	public class RssContextMenu
	{
		
		[Bindable]
		public var cm:ContextMenu;
		
		private var _copyRssLinkMenuItem:ContextMenuItem;
		protected var _showRssLinkMenuItem:ContextMenuItem;
		
		
		public function RssContextMenu()
		{ 
			cm = new ContextMenu();
			cm.hideBuiltInItems();
			cm.customItems = createMenuItems();
			cm.addEventListener(ContextMenuEvent.MENU_SELECT, menuUpdateCaption);
		}
		
		private function createMenuItems():Array {
			var result:Array = new Array();
			
			_showRssLinkMenuItem = new ContextMenuItem(" ");
			_showRssLinkMenuItem.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, showRssLink);
			_showRssLinkMenuItem.visible = true;
			result.push(_showRssLinkMenuItem);
			
			_copyRssLinkMenuItem = new ContextMenuItem("Copy Rss link to clipboard");
			_copyRssLinkMenuItem.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, copyRssLink);
			_copyRssLinkMenuItem.visible = true;
			result.push(_copyRssLinkMenuItem);
			
			return result;
		}
		
		
		protected function copyRssLink(event:ContextMenuEvent):void {
		}
		
		protected function showRssLink(event:ContextMenuEvent):void{
		}
		
		protected function menuUpdateCaption(event:ContextMenuEvent):void{
		}
		
		
	}
}
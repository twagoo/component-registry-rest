package clarin.cmdi.componentregistry.common.components
{
	import clarin.cmdi.componentregistry.services.Config;
	
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
		private var _showRssLinkMenuItem:ContextMenuItem;
		
		
		
		public function RssContextMenu()
		{ 
			cm = new ContextMenu();
			cm.hideBuiltInItems();
			cm.customItems = createMenuItems();
			
		}
		
		private function createMenuItems():Array {
			var result:Array = new Array();
			
			_copyRssLinkMenuItem = new ContextMenuItem("Copy the link to the clipboard");
			_copyRssLinkMenuItem.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, copyRssLink);
			_copyRssLinkMenuItem.visible = true;
			result.push(_copyRssLinkMenuItem);
			
			_showRssLinkMenuItem = new ContextMenuItem("Show link to the RSS feed");
			_showRssLinkMenuItem.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, showRssLink);
			_showRssLinkMenuItem.visible = true;
			result.push(_showRssLinkMenuItem);
			
			return result;
		}
		
		
		protected function copyRssLink(event:ContextMenuEvent):void {
		}
		
		protected function showRssLink(event:ContextMenuEvent):void{
		}
		
		protected function positioning(xShift:int, event:ContextMenuEvent):Point{
			var point:Point = new Point();
			point.x = event.mouseTarget.mouseX;
			point.y = event.mouseTarget.mouseY;
			point = event.mouseTarget.localToGlobal(point);			
			return (new Point(point.x-xShift, point.y));
		}
	}
}
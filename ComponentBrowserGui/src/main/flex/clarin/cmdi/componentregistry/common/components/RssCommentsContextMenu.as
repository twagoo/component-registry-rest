package clarin.cmdi.componentregistry.common.components
{
	
	import clarin.cmdi.componentregistry.common.ItemDescription;
	import clarin.cmdi.componentregistry.services.Config;
	
	import flash.events.Event;
	import flash.events.ContextMenuEvent;
	import flash.ui.ContextMenuItem;
	import flash.system.System;
	import flash.net.URLRequest;
	import flash.net.navigateToURL;
	
	public class RssCommentsContextMenu extends RssContextMenu
	{
		private var _itemDescription:ItemDescription;
		
		public function RssCommentsContextMenu(itemDescription:ItemDescription)
		{   super();
			_itemDescription = itemDescription;
		}
		
		override protected function copyRssLink(event:ContextMenuEvent):void {
			System.setClipboard(Config.getRssUriComments(_itemDescription));
		}
		
		override protected function showRssLink(event:ContextMenuEvent):void{
			navigateToURL(new URLRequest(Config.getRssUriComments(_itemDescription)), "_blank");
		}
		
		override protected function menuUpdateCaption(event:ContextMenuEvent):void {
			_showRssLinkMenuItem.caption = Config.getRssUriComments(_itemDescription);
		}
		
	}
}
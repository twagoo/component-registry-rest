package clarin.cmdi.componentregistry.common.components
{
	import clarin.cmdi.componentregistry.common.RssLinkPopUp;
	import clarin.cmdi.componentregistry.common.ItemDescription;
	import clarin.cmdi.componentregistry.services.Config;
	
	import flash.geom.Point;
	import flash.events.ContextMenuEvent;
	import mx.managers.PopUpManager;
	import flash.system.System;
	
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
			
			var rssLinkPanel:RssLinkPopUp = new RssLinkPopUp();
			rssLinkPanel.uri=Config.getRssUriComments(_itemDescription);
			
			var xShift:int = 0;
			var position:Point = positioning(xShift, event);
			rssLinkPanel.x = position.x;
			rssLinkPanel.y=  position.y;
			
			PopUpManager.addPopUp(rssLinkPanel, event.mouseTarget);
		}
	}
}
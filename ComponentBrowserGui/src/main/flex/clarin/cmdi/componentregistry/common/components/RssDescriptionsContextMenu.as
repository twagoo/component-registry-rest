package clarin.cmdi.componentregistry.common.components
{
	import clarin.cmdi.componentregistry.services.Config;
	
	import flash.events.Event;
	import flash.events.ContextMenuEvent;
	import flash.ui.ContextMenuItem;
	import flash.system.System;
	import flash.net.URLRequest;
	import flash.net.navigateToURL;
	
	public class RssDescriptionsContextMenu extends RssContextMenu
	{
		private var _typeOfDescription:String;

		
		public function RssDescriptionsContextMenu(typeOfDescription:String)
		{   super();
			_typeOfDescription = typeOfDescription;
		}
		
		override protected function copyRssLink(event:ContextMenuEvent):void {
			System.setClipboard(Config.getRssUriDescriptions(_typeOfDescription));
		}
				
		override protected function showRssLink(event:ContextMenuEvent):void{
			navigateToURL(new URLRequest(Config.getRssUriDescriptions(_typeOfDescription)), "_blank");
		}
		
		override protected function menuUpdateCaption(event:ContextMenuEvent):void {
			_showRssLinkMenuItem.caption = Config.getRssUriDescriptions(_typeOfDescription);
		}
	}
}
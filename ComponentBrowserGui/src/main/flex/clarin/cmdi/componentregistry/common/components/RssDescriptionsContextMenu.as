package clarin.cmdi.componentregistry.common.components
{
	import clarin.cmdi.componentregistry.common.RssLinkPopUp;
	import clarin.cmdi.componentregistry.services.Config;
	
	import flash.geom.Point;
	import flash.events.ContextMenuEvent;
	import mx.managers.PopUpManager;
	import flash.system.System;
	
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
			
			var rssLinkPanel:RssLinkPopUp = new RssLinkPopUp();
			rssLinkPanel.uri = Config.getRssUriDescriptions(_typeOfDescription);
			
			var xShift:int = 350;
			var position:Point = positioning(xShift, event);
			rssLinkPanel.x = position.x;
			rssLinkPanel.y=position.y;
			
			PopUpManager.addPopUp(rssLinkPanel, event.mouseTarget);
		}
	}
}
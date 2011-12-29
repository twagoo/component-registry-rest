package clarin.cmdi.componentregistry.browser
{
	import clarin.cmdi.componentregistry.common.Comment;
	import clarin.cmdi.componentregistry.common.ItemDescription;
	import clarin.cmdi.componentregistry.services.CommentListService;
	
	import flash.events.Event;
	
	import mx.collections.ArrayCollection;
	import mx.containers.VBox;
	
	public class CommentsPanel extends VBox
	{
		[Bindable]
		private var _itemDescription:ItemDescription;
		private var service:CommentListService;
		
		public function set itemDescription(itemDescription:ItemDescription):void {
			_itemDescription = itemDescription;
		}
		
		public function CommentsPanel()
		{ }
		
		public function load():void{
			removeAllChildren();
			
			var postPanel:commentPostPanel = new commentPostPanel();
			postPanel.itemDescription = _itemDescription;
			
			addChild(postPanel);
			
			service = new CommentListService(_itemDescription, _itemDescription.isInUserSpace);
			service.addEventListener(CommentListService.COMMENTS_LOADED, commentsLoaded);
			service.load();
		}
		
		private function commentsLoaded(event:Event):void{
			if(service) {
				for each(var comment:Comment in service.comments){
					var panel:CommentPanel = new CommentPanel(comment);
					addChild(panel);
				}
			}
		}
	}
}
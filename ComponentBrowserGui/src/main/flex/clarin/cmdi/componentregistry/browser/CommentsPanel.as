package clarin.cmdi.componentregistry.browser
{
	import clarin.cmdi.componentregistry.common.Comment;
	import clarin.cmdi.componentregistry.common.Credentials;
	import clarin.cmdi.componentregistry.common.ItemDescription;
	import clarin.cmdi.componentregistry.services.CommentListService;
	import clarin.cmdi.componentregistry.services.CommentPostService;
	import clarin.cmdi.componentregistry.services.DeleteService;
	
	import flash.events.Event;
	
	import mx.containers.VBox;
	import mx.controls.HRule;
	import mx.controls.Label;
	
	[Event(name="commentsLoaded",type="flash.events.Event")]
	public class CommentsPanel extends VBox
	{
		public static const COMMENTS_LOADED:String = "commentsLoaded";
		
		[Bindable]
		private var _itemDescription:ItemDescription;
		private var service:CommentListService;
		private var commentsBox:VBox;
		
		public function get commentListService():CommentListService {
			return service;
		}
		
		public function set itemDescription(itemDescription:ItemDescription):void {
			_itemDescription = itemDescription;
		}
		
		public function CommentsPanel()
		{ 
			this.setStyle("paddingLeft", 5);
			this.setStyle("paddingTop", 5);
			this.setStyle("paddingBottom", 5);
			
			// this is for responding to the deletion of comments. At this point there is no way to distinghuish between item and component deletion
			// and that probably is fine since they mostly require the same response. It does mean that this component will also reload when a
			// component gets deleted, which is a bit superfluous.
			DeleteService.instance.addEventListener(DeleteService.ITEM_DELETED, commentDeletedHandler);
		}
		
		public function load():void{
			removeAllChildren();
			
			// A box for the comments (will be loaded in callback but should be shown first)
			commentsBox = new VBox();
			addChild(commentsBox);
			
			// A panel for posting a comment (or a message 'login to post');
			addPostPanel();
			
			// Do actual loading
			service = new CommentListService(_itemDescription, _itemDescription.isInUserSpace);
			service.addEventListener(CommentListService.COMMENTS_LOADED, commentsLoaded);
			service.load();
		}
		
		private function addPostPanel():void{
			if(Credentials.instance.isLoggedIn()){
				var postPanel:commentPostPanel = new commentPostPanel();
				postPanel.itemDescription = _itemDescription;
				postPanel.commentPostService.addEventListener(CommentPostService.POST_COMPLETE, postCompleteHandler);
				addChild(postPanel);
			} else{
				var loginToPostLabel:Label = new Label();
				loginToPostLabel.setStyle("fontWeight","bold");
				loginToPostLabel.text = "Login to leave a comment!";
				addChild(loginToPostLabel);
			}
		}
		
		private function commentsLoaded(event:Event):void{
			if(service) {
				var commentsCount:int = service.comments.length;
				_itemDescription.commentsCount = commentsCount;
				
				if(commentsCount > 0) {
					for each(var comment:Comment in service.comments) {
						var panel:CommentPanel = new CommentPanel(comment);
						commentsBox.addChild(panel);
					}
				} else {
					var noCommentsPostedLabel:Label = new Label();
					noCommentsPostedLabel.text = "No comments have been posted thus far.";
					commentsBox.addChild(noCommentsPostedLabel);
					
					var rule:HRule = new HRule();
					rule.percentWidth = 100;
					commentsBox.addChild(rule);
				}
			}
			dispatchEvent(new Event(COMMENTS_LOADED));
		}
		
		private function postCompleteHandler(event:Event):void{
			load();
		}
		
		private function commentDeletedHandler(event:Event):void {
			load();
		}
	}
}
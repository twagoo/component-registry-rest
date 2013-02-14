 package clarin.cmdi.componentregistry.browser
{
	import clarin.cmdi.componentregistry.common.Comment;
	import clarin.cmdi.componentregistry.common.Credentials;
	import clarin.cmdi.componentregistry.common.ItemDescription;
	import clarin.cmdi.componentregistry.common.StyleConstants;
	import clarin.cmdi.componentregistry.common.components.RssCommentsContextMenu;
	import clarin.cmdi.componentregistry.common.components.RssLinkButton;
	import clarin.cmdi.componentregistry.services.CommentListService;
	import clarin.cmdi.componentregistry.services.CommentPostService;
	import clarin.cmdi.componentregistry.services.Config;
	import clarin.cmdi.componentregistry.services.DeleteService;
	
	import flash.events.Event;
	import flash.events.MouseEvent;
	import flash.net.URLRequest;
	import flash.net.navigateToURL;
	
	import mx.containers.Canvas;
	import mx.containers.HBox;
	import mx.containers.VBox;
	import mx.controls.HRule;
	import mx.controls.Image;
	import mx.controls.Label;
	import mx.controls.Spacer;
	import mx.controls.scrollClasses.ScrollBar;
	
	
	
	[Event(name="commentsLoaded",type="flash.events.Event")]
	public class CommentsPanel extends HBox
	{
		public static const COMMENTS_LOADED:String = "commentsLoaded";
		
		[Bindable]
		private var _itemDescription:ItemDescription;
		private var service:CommentListService;
		
		private var commentsBox:VBox;
		
		private const hPadding:int = 5;
		private const vPadding:int = 5;
		
		public function get commentListService():CommentListService {
			return service;
		}
		
		public function set itemDescription(itemDescription:ItemDescription):void {
			_itemDescription = itemDescription;
		}
		
		
		public function CommentsPanel()
		{  
			this.setStyle("paddingLeft", vPadding);
			this.setStyle("paddingTop", hPadding);
			this.setStyle("paddingRight", vPadding);
			this.setStyle("paddingBottom", hPadding);
			
			// this is for responding to the deletion of comments. At this point there is no way to distinghuish between item and component deletion
			// and that probably is fine since they mostly require the same response. It does mean that this component will also reload when a
			// component gets deleted, which is a bit superfluous.
			DeleteService.instance.addEventListener(DeleteService.ITEM_DELETED, commentDeletedHandler);
		}
		
	   
		
		private function makeRssLinkButton():RssLinkButton{
			
			var rssButtonTmp:RssLinkButton = new RssLinkButton();
			rssButtonTmp.contextMenu = (new RssCommentsContextMenu(_itemDescription)).cm;
			rssButtonTmp.addEventListener(MouseEvent.CLICK,  goToFeed);
			return rssButtonTmp;
		}
		
		private function goToFeed(event:MouseEvent):void{
			navigateToURL(new URLRequest(Config.getRssUriComments(_itemDescription)), "_blank");
		}
		
		public function load():void{
			removeAllChildren();
			
			
			if(_itemDescription != null) {
				
				// A box for the comments (will be loaded in callback but should be shown first)
				commentsBox = new VBox();
				addChild(commentsBox);
				
				// A panel for posting a comment (or a message 'login to post');
				// duplication addPostPanel();
				
				// Do actual loading
				service = new CommentListService(_itemDescription, _itemDescription.isInUserSpace);
				service.addEventListener(CommentListService.COMMENTS_LOADED, commentsLoaded);
				service.load();
				
				// Rss feed "button"
				if (! _itemDescription.isInUserSpace){
					var spacer:Spacer = new Spacer();
					spacer.percentWidth=100;
					addChild(spacer);
					var rssButton:RssLinkButton  = makeRssLinkButton();
					addChild(rssButton);
				}
				
			}
		}
		
		private function addPostPanel():void{
			if(Credentials.instance.isLoggedIn()){
				var postPanel:commentPostPanel = new commentPostPanel();
				postPanel.itemDescription = _itemDescription;
				postPanel.commentPostService.addEventListener(CommentPostService.POST_COMPLETE, postCompleteHandler);
				commentsBox.addChild(postPanel);
			} else{
				var loginToPostLabel:Label = new Label();
				loginToPostLabel.setStyle("fontWeight","bold");
				loginToPostLabel.text = "Login to leave a comment!";
				commentsBox.addChild(loginToPostLabel);
			}
		}
		
		private function commentsLoaded(event:Event):void{
			if(service) {
				var commentsCount:int = service.comments.length;
				
				if (_itemDescription != null) {_itemDescription.commentsCount = commentsCount;}
				
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
			
			// A panel for posting a comment (or a message 'login to post');
			addPostPanel();
			
			
		}
		
		private function postCompleteHandler(event:Event):void{
			load();
		}
		
		private function commentDeletedHandler(event:Event):void {
			load();
		}
	}
}
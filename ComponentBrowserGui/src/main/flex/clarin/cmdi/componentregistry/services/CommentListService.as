package clarin.cmdi.componentregistry.services {
	import clarin.cmdi.componentregistry.common.Comment;
	import clarin.cmdi.componentregistry.common.ItemDescription;
	
	import com.adobe.net.URI;
	
	import mx.collections.ArrayCollection;
	import mx.rpc.events.ResultEvent;
	
	[Event(name="CommentsLoaded", type="flash.events.Event")]
	public class CommentListService extends ComponentRegistryService {
		public static const COMMENTS_LOADED:String = "CommentsLoaded";
		;
		private var userSpace:Boolean;
		
		/**
		 * Typed ArrayCollection publicly available for outside code to bind to and watch.
		 */
		[Bindable]
		[ArrayElementType("Comment")]
		public var comments:ArrayCollection;
		
		public function CommentListService(itemDescription:ItemDescription, userSpace:Boolean) {
			var url:String;
			if(itemDescription.isProfile){
				url = Config.instance.getProfileCommentsPath(itemDescription.id);
			} else{
				url = Config.instance.getComponentCommentsPath(itemDescription.id);
			}
			super(url);
			
			this.userSpace = userSpace;
		}
		
		override protected function initServiceUrl(url:URI):void{
			if (userSpace) {
				url.setQueryValue(Config.PARAM_USERSPACE, "true");
			}
		}
		
		override protected function result(resultEvent:ResultEvent):void {
			var resultXml:XML = resultEvent.result as XML;
			var nodes:XMLList = resultXml.comment;
			
			comments = new ArrayCollection();
			for each (var node:XML in nodes) {
				var comment:Comment = new Comment(node);
				comments.addItem(comment);
			}
			comments.refresh();
			dispatchEvent(new Event(COMMENTS_LOADED));
		}
	}
}
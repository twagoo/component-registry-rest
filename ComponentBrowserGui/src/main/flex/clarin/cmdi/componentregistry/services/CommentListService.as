package clarin.cmdi.componentregistry.services {
	
	import clarin.cmdi.componentregistry.browser.BrowserColumns;
	import clarin.cmdi.componentregistry.common.CommentDescription;
	
	import mx.collections.ArrayCollection;
	import mx.rpc.events.ResultEvent;
	
	public class CommentListService extends BrowserService {
			
			private static var _instance:CommentListService = new CommentListService();
			private static var _userSpaceInstance:CommentListService = new CommentListService(true);
			
			public function CommentListService(userSpace:Boolean=false) {
				super(Config.instance.commentListUrl, userSpace);
			}
			
			override protected function result(resultEvent:ResultEvent):void {
				var resultXml:XML = resultEvent.result as XML;
				var nodes:XMLList = resultXml.profileDescription;
				var tempArray:ArrayCollection = new ArrayCollection();
				for each (var node:XML in nodes) {
					var item:CommentDescription = new CommentDescription();
					item.createComment(node);
					tempArray.addItem(item);
				}
				tempArray.sort = BrowserColumns.getInitialSortForComments();
				tempArray.refresh();
				setItemDescriptions(new ArrayCollection(tempArray.toArray()));
				super.result(resultEvent);
			}
			
			public static function getInstance(userSpace:Boolean):CommentListService {
				if (userSpace) {
					return _userSpaceInstance;
				} else {
					return _instance;
				}
			}
			
			public static function findDescription(commentId:String):CommentDescription {
				var result:CommentDescription = getInstance(true).lookUpComDescription(commentId);
				if (result == null) {
					result = getInstance(false).lookUpComDescription(commentId);
				}
				return result
			}
			
		}
	}
package clarin.cmdi.componentregistry.services {
	import clarin.cmdi.componentregistry.common.Group;
	import clarin.cmdi.componentregistry.services.remote.HttpServiceFactory;
	import clarin.cmdi.componentregistry.services.remote.RemoteService;
	
	import com.adobe.net.URI;
	
	import flash.events.Event;
	
	import mx.collections.ArrayCollection;
	import mx.controls.Alert;
	import mx.rpc.AsyncToken;
	import mx.rpc.Responder;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.http.HTTPService;

/**
 * Client connector to component registry group service functions
 * @author george.georgovassilis@mpi.nl
 */	
	[Event(name="GroupsOfItemLoaded", type="flash.events.Event")]
	public class ListGroupsOfItemService extends BaseRemoteService {
		public static const GROUPS_LOADED:String = "GroupsOfItemLoaded";
		
		[Bindable]
		[ArrayElementType("Group")]
		public var groups:ArrayCollection;
		protected var postService:HTTPService;
		
		public function ListGroupsOfItemService() {
			super(GROUPS_LOADED);
			postService = new HTTPService();
			postService.method = "POST";
		}

		public function loadGroupsForItem(itemId:String):void{
			groups = new ArrayCollection();
			var url:String;
			url = Config.instance.getGroupsOfItemPath(itemId);
			dispatchRequest(new URI(url));
		}
		
		override protected function handleXmlResult(resultXml:XML):void{
			var nodes:XMLList = resultXml.group;
			groups = new ArrayCollection();
			for each (var node:XML in nodes) {
				var group:Group = new Group();
				group.create(node);
				groups.addItem(group);
			}
			groups.refresh();
		}

		
		public function transferOwnership(itemId:String, groupId:String, callback:Function):void{
			postService.url = Config.instance.getTransferItemOwnershipUrl(itemId, groupId);
			var parameters:Object = new Object();
			parameters["groupId"] = groupId;
			var token:AsyncToken = postService.send(parameters);
			token.addResponder(new Responder(callback, this.requestCallbackFailed));
		}

	}
}
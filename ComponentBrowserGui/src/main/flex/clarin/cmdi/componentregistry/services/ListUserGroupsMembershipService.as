package clarin.cmdi.componentregistry.services {
	import com.adobe.net.URI;
	
	import mx.collections.ArrayCollection;
	
	import clarin.cmdi.componentregistry.common.Group;
	import mx.controls.Alert;

/**
 * Client connector to component registry group service functions
 * @author george.georgovassilis@mpi.nl
 */	
	[Event(name="GroupsLoaded", type="flash.events.Event")]
	public class ListUserGroupsMembershipService extends BaseRemoteService {
		public static const GROUPS_LOADED:String = "GroupsLoaded";
		
		[Bindable]
		[ArrayElementType("Group")]
		public var groups:ArrayCollection;
		
		public function ListUserGroupsMembershipService() {
			super(GROUPS_LOADED);
		}

		public function loadGroupsForUser():void{
			if (groups!=null){
				this.dispatchEvent(new Event(this.successEventName));
				return;
			}
			groups = new ArrayCollection();
			var url:String;
			url = Config.instance.getListGroupsOfUserPath();
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
	}
}
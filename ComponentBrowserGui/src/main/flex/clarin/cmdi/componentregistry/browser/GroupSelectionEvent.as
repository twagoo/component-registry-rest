package clarin.cmdi.componentregistry.browser {
	import clarin.cmdi.componentregistry.common.ItemDescription;
	
	import flash.events.Event;

	/**
	 * This event models the selection of a group
	 * @author george.georgovassilis@mpi.nl 
	 */
	
	public class GroupSelectionEvent extends Event {

		public static const GROUP_SELECTED:String = "groupSelected";
		private var groupId:String;
		private var groupLabel:String;
		public var groupWasMoved:Boolean = false;

		public function GroupSelectionEvent(groupId:String, groupLabel:String) {
			super(GROUP_SELECTED, true, true);
			this.groupId = groupId;
			this.groupLabel = groupLabel;
		}

		override public function clone():Event {
			var copy:GroupSelectionEvent = new GroupSelectionEvent(groupId, groupLabel);
			copy.groupWasMoved = groupWasMoved;
			return copy;
		}

		public function getGroupId():String{
			return groupId;
		}

		public function getGroupLabel():String{
			return groupLabel;
		}

	}
}
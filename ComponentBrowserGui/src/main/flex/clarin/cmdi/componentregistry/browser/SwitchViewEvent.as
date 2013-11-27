package clarin.cmdi.componentregistry.browser {
	import clarin.cmdi.componentregistry.common.ItemDescription;
	
	import flash.events.Event;

	/**
	 * Switch views
	 * @author george georgovassilis
	 */
	public class SwitchViewEvent extends Event {

		public static const SWITCH_VIEW:String = "switchView";
		public var view:String;
		public var item:ItemDescription;

		public function SwitchViewEvent(view:String, item:ItemDescription) {
			super(SWITCH_VIEW);
			this.view = view;
			this.item = item;
		}

		override public function clone():Event {
			return new SwitchViewEvent(SWITCH_VIEW, item);
		}



	}
}
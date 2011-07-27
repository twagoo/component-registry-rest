package clarin.cmdi.componentregistry.services
{
	import flash.events.Event;
	
	public class ComponentUsageCheckEvent extends Event
	{	
		public static const COMPONENT_IN_USE:String = "componentInUse";
		
		public function ComponentUsageCheckEvent(type:String, componentInUse:Boolean,bubbles:Boolean = false, cancelable:Boolean = false)
		{
			super(type, bubbles, cancelable);
			this._isComponentInUse = componentInUse;
		}
		
		private var _isComponentInUse:Boolean;
		
		public function get isComponentInUse():Boolean{
			return _isComponentInUse;
		}
	}
}
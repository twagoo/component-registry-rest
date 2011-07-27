package clarin.cmdi.componentregistry.services
{
	import flash.events.Event;
	
	import mx.collections.ArrayCollection;
	
	public class ComponentUsageCheckEvent extends Event
	{	
		public static const COMPONENT_IN_USE:String = "componentInUse";
		private var _itemsUsingComponent:ArrayCollection;
		
		public function ComponentUsageCheckEvent(type:String, itemsUsingComponent:ArrayCollection, bubbles:Boolean = false, cancelable:Boolean = false)
		{
			super(type, bubbles, cancelable);
			_itemsUsingComponent = itemsUsingComponent;
		}
		
		private var _isComponentInUse:Boolean;
		
		public function get isComponentInUse():Boolean{
			return _itemsUsingComponent != null && _itemsUsingComponent.length > 0;
		}
		
		/**
		 * Names of components that use the specified item
		 */
		public function get itemUsingComponent():ArrayCollection{
			return _itemsUsingComponent;
		}
	}
}
package clarin.cmdi.componentregistry.browser
{
	import flash.events.Event;

	public class CreateNewComponentEvent extends Event
	{
		public static const CREATE_NEW_EVENT:String = "createNew";
		
		public function CreateNewComponentEvent( bubbles:Boolean = false, cancelable:Boolean = false)
		{
			super(CREATE_NEW_EVENT, bubbles, cancelable);
		}
	}
}
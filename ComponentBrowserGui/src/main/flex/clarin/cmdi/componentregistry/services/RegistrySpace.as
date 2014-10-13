package clarin.cmdi.componentregistry.services
{
	public class RegistrySpace
	{
		[Bindable(event=REGISTRY_SPACE_TOGGLE_EVENT)]
		public var space:String ="";
		
		[Bindable(event=REGISTRY_SPACE_TOGGLE_EVENT)]
		public var groupId:String ="";
		
		public function RegistrySpace(space:String, groupId:String)
		{
			this.space=space;
			this.groupId= groupId
		}
		
		
	}
}
package clarin.cmdi.componentregistry.services.remote
{
	public class ClientServiceFactoryImpl extends HttpServiceFactory
	{
		
		public function ClientServiceFactoryImpl()
		{
			HttpServiceFactory.impl = this;
		}
		
		override public function createNew():RemoteService{
			return new RemoteServiceWrapper();
		}
	}
}
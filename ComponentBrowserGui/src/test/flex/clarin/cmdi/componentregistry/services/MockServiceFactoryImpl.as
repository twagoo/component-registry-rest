package clarin.cmdi.componentregistry.services
{
	import clarin.cmdi.componentregistry.services.remote.HttpServiceFactory;
	import clarin.cmdi.componentregistry.services.remote.RemoteService;

	public class MockServiceFactoryImpl extends HttpServiceFactory
	{
		public var lastService:MockRemoteService;
		
		public function MockServiceFactoryImpl()
		{
			HttpServiceFactory.impl = this;
		}
		
		override public function createNew():RemoteService{
			lastService = new MockRemoteService();
			return lastService;
		
		}
	}
}
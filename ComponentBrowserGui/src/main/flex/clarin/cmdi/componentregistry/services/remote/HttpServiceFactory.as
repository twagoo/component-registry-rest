package clarin.cmdi.componentregistry.services.remote
{
/**
 * HttpService factory returns the right implementation of a RemoteService interface, which would typically be
 * a ClientHttpService for a flex app running in the browser and a MockHttpService for an app running in a flexunit test
 */
	
	public class HttpServiceFactory
	{
		
		public static var impl:HttpServiceFactory;
		
		public static function createRemoteService():RemoteService{
			if (impl == null)
				impl = new ClientServiceFactoryImpl();
			return impl.createNew();
		}
		
		public function createNew():RemoteService{
			if (impl == null)
				impl = new ClientServiceFactoryImpl();
			return impl.createNew();
		};	
	}
}
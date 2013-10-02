package clarin.cmdi.componentregistry.services //trunk 
{
	import clarin.cmdi.componentregistry.services.remote.RemoteService;
	
	import com.adobe.net.URI;
	
	import mx.rpc.http.HTTPService;
	
	public class ComponentRegistryService extends BaseRemoteService {
		
		protected var serviceUrl:URI;
		protected var space:String;
		
		public function ComponentRegistryService(successEvent:String, serviceUrl:URI) {
			super(successEvent);
			this.serviceUrl = serviceUrl;
		}
		
		override protected function dispatchRequest(url:URI):void {
			url.setQueryValue("unique", new Date().getTime().toString());
			if (space == Config.SPACE_USER) {
				url.setQueryValue(Config.PARAM_USERSPACE, "true");
			} else
				url.setQueryValue(Config.PARAM_USERSPACE, null);
			super.dispatchRequest(url);
		}
		
		public function load():void {
			dispatchRequest(serviceUrl);
		}
		
	}
}
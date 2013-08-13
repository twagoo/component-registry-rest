package clarin.cmdi.componentregistry.services //trunk 
{
	import com.adobe.net.URI;
	
	public class ComponentRegistryService extends BaseRemoteService {
		
		protected var serviceUrl:URI;
		protected var userSpace:Boolean;
		
		public function ComponentRegistryService(successEvent:String, serviceUrl:URI) {
			super(successEvent);
			this.serviceUrl = serviceUrl;
		}
		
		override protected function dispatchRequest(url:URI):void {
			url.setQueryValue("unique", new Date().getTime().toString());
			if (userSpace) {
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
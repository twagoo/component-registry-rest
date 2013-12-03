package clarin.cmdi.componentregistry.services //trunk 
{
	import clarin.cmdi.componentregistry.common.ItemDescription;
	import clarin.cmdi.componentregistry.services.remote.RemoteService;
	
	import com.adobe.net.URI;
	
	import mx.controls.Alert;
	import mx.rpc.AsyncToken;
	import mx.rpc.Responder;
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
		
		public function getComponent(componentId:String, callback:Function):void{
			readService.setUrl(new URI(Config.instance.serviceRootUrl+Config.ITEMS_URL+"/"+componentId+"?userspace=true"));
			var token:AsyncToken = readService.send();
			token.addResponder(new Responder(function(resultEvent):void{
				var resultXml:XML = resultEvent.result as XML;
				var item:ItemDescription = new ItemDescription();
				item.createComponent(resultXml, "user");
				//there's a problem (fixed in trunk): the return data class is an abstractDescription for which we don't know whether it's a component or profile (isProfile value is incorrect)
				//but we can fix that one missing property here because we can know the flavour from its ID
				item.isProfile = (componentId.indexOf(Config.PROFILE_PREFIX)==0);
				callback(item);
			}, this.requestCallbackFailed));
		}
		
	}
}
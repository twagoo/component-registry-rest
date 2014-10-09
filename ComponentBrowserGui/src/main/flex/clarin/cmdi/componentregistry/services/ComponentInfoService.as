package clarin.cmdi.componentregistry.services {

	import com.adobe.net.URI;
	
	import mx.controls.Alert;
	
	import clarin.cmdi.componentregistry.common.Component;
	import clarin.cmdi.componentregistry.common.ComponentMD;
	import clarin.cmdi.componentregistry.common.ItemDescription;
	
	
	[Event(name="ComponentLoaded", type="flash.events.Event")]
	public class ComponentInfoService extends BaseRemoteService{
		
		public static const COMPONENT_LOADED:String="ComponentLoaded";

		[Bindable]
		public var component:Component;

		public function ComponentInfoService() {
			super(COMPONENT_LOADED);
		}

		public function load(item:ItemDescription):void {
			this.component = new Component();
			component.description = item;
			var url:URI = new URI(item.dataUrl);
			dispatchRequest(url);
		}

		override protected function handleXmlResult(resultXml:XML):void {
			var metaData:ComponentMD = new ComponentMD();
			metaData.name = resultXml.CMD_Component.@name;
			metaData.xml = resultXml;
			component.componentMD = metaData;
		}

	}
}


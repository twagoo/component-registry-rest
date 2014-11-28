package clarin.cmdi.componentregistry.services {

	import clarin.cmdi.componentregistry.common.Component;
	import clarin.cmdi.componentregistry.common.ComponentMD;
	import clarin.cmdi.componentregistry.common.ItemDescription;
	
	import com.adobe.net.URI;
	
	import mx.controls.Alert;
	
	
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
		
		public function loadFromUrl(dataUrl:String):void {
			var description:ItemDescription = new ItemDescription();
			description.dataUrl = dataUrl;
			// rest of description will be filled in when handling result
			
			load(description);
		}

		override protected function handleXmlResult(resultXml:XML):void {
			var metaData:ComponentMD = new ComponentMD();
			metaData.name = resultXml.CMD_Component.@name;
			metaData.xml = resultXml;
			component.componentMD = metaData;
			
			if(component.description.id == null) {
				// was loaded from URL, not item - set available details
				component.description.id = resultXml.Header.ID;
				component.description.name = resultXml.Header.Name;
				component.description.description = resultXml.Header.Description;
			}
		}

	}
}


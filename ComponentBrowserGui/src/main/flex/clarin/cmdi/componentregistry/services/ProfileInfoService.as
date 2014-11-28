package clarin.cmdi.componentregistry.services {
	import clarin.cmdi.componentregistry.common.ComponentMD;
	import clarin.cmdi.componentregistry.common.ItemDescription;
	import clarin.cmdi.componentregistry.common.Profile;

	import com.adobe.net.URI;

	import mx.collections.ArrayCollection;

	[Event(name="ProfileLoaded", type="flash.events.Event")]
	public class ProfileInfoService extends BaseRemoteService {
		public static const PROFILE_LOADED:String = "ProfileLoaded";

		[Bindable]
		public var profile:Profile;

		public function ProfileInfoService() {
			super(PROFILE_LOADED);
		}

		public function load(item:ItemDescription):void {
			profile = new Profile();
			profile.description = item;
			var url:URI = new URI(item.dataUrl);			
			super.dispatchRequest(url);
		}
		
		override protected function handleXmlResult(resultXml:XML):void{
			var nodes:XMLList = resultXml.CMD_Component;
			
			profile.nrOfComponents = nodes.length();
			profile.profileSource = resultXml;
			var tempArray:Array = new Array();
			for each (var node:XML in nodes) {
				var component:ComponentMD = new ComponentMD();
				component.name = node.@name;
				component.xml = node;
				tempArray[tempArray.length] = component;
			}
			this.profile.components = new ArrayCollection(tempArray);
		}
	}
}


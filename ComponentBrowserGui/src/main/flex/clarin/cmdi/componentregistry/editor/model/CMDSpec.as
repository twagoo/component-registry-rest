package clarin.cmdi.componentregistry.editor.model {
	import clarin.cmdi.componentregistry.common.XmlAble;
	
	import mx.collections.ArrayCollection;

	[Bindable]
	public class CMDSpec implements XmlAble {
		//Attribute
		public var isProfile:Boolean;

		//Elements
		public var headerName:String;
		public var headerId:String;
		public var headerDescription:String;
		public var cmdComponents:ArrayCollection = new ArrayCollection();

		public function CMDSpec(isProfile:Boolean) {
			this.isProfile = isProfile;
		}
		
		public function removeComponent(component:CMDComponent):void {
		    var index:int = cmdComponents.getItemIndex(component);
		    if (index != -1) {
		        cmdComponents.removeItemAt(index);
		    }		    
		}

		public function toXml():XML {
			var result:XML = <CMD_ComponentSpec>
					<Header>
						<ID>{headerId}</ID>
						<Name>{headerName}</Name>
						<Description>{headerDescription}</Description>
					</Header>
				</CMD_ComponentSpec>;
			result.@isProfile=isProfile;	
			for each (var component:CMDComponent in cmdComponents) {
				result.appendChild(component.toXml());
			}
			return result;
		}

	}
}
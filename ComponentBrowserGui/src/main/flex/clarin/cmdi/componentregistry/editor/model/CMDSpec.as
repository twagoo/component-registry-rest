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

		public var groupName:String = ""; //Not in xml but stored in the description
		private var _domainName:String = ""; //Not in xml but stored in the description

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
			result.@isProfile = isProfile;
			for each (var component:CMDComponent in cmdComponents) {
				result.appendChild(component.toXml());
			}
			return result;
		}

		public function get domainName():String {
			return _domainName;
		}

		public function set domainName(domainName:String):void {
			if (domainName) {
				_domainName = domainName;
			} else {
				_domainName = "";
			}
		}

	}
}
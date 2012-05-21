package clarin.cmdi.componentregistry.editor.model {
	import clarin.cmdi.componentregistry.common.ChangeTrackingCMDElement;
	import clarin.cmdi.componentregistry.common.XmlAble;
	
	import mx.collections.ArrayCollection;
	import mx.events.CollectionEvent;
	import mx.events.CollectionEventKind;

	[Bindable]
	public class CMDSpec implements XmlAble, ChangeTrackingCMDElement {
		//Attribute
		public var isProfile:Boolean;

		//Elements
		public var headerName:String;
		public var headerId:String;
		public var headerDescription:String;
		public var cmdComponents:ArrayCollection = new ArrayCollection();

		public var groupName:String = ""; //Not in xml but stored in the description
		private var _domainName:String = ""; //Not in xml but stored in the description
		
		private var changed:Boolean = false;
		private var _changeTracking:Boolean = false;

		public function CMDSpec(isProfile:Boolean) {
			this.isProfile = isProfile;
			cmdComponents.addEventListener(CollectionEvent.COLLECTION_CHANGE, collectionChangedHandler);
		}
		
		
		public function set changeTracking(value:Boolean):void {
			_changeTracking = value;
			for each (var component:CMDComponent in cmdComponents){
				component.changeTracking = value;
			}
		}
		
		public function setChanged(value:Boolean):void {
			if(_changeTracking) {
				this.changed = value;
			}
		}
		
		public function get hasChanged():Boolean{
			if(changed){
				return changed;
			} else {
				for each (var component:CMDComponent in cmdComponents){
					if(component.hasChanged){
						return true;
					}
				}
			}
			return false;
		}
		
		private function collectionChangedHandler(event:CollectionEvent):void {
			if(event.kind == CollectionEventKind.ADD ||event.kind == CollectionEventKind.MOVE || event.kind == CollectionEventKind.REMOVE){ 
				setChanged(true);
			}
			if(event.kind == CollectionEventKind.ADD){
				for each(var item:ChangeTrackingCMDElement in event.items){
					item.changeTracking = _changeTracking;
				}
			}
		}

		public function removeComponent(component:CMDComponent):void {
			var index:int = cmdComponents.getItemIndex(component);
			if (index != -1) {
				cmdComponents.removeItemAt(index);
				setChanged(true);
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
			this.changed = true;
		}

		public static function createEmptyComponent():CMDSpec {
			var result:CMDSpec = new CMDSpec(false);
			var c:CMDComponent = CMDComponent.createEmptyComponent();
			result.cmdComponents.addItem(c);
			c.cmdElements.addItem(CMDComponentElement.createEmptyElement());
			return result
		}

		public static function createEmptyProfile():CMDSpec {
			var result:CMDSpec = new CMDSpec(true);
			var c:CMDComponent = CMDComponent.createEmptyComponent();
			result.cmdComponents.addItem(c);
			c.cmdElements.addItem(CMDComponentElement.createEmptyElement());
			return result
		}
	}
}
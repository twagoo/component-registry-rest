package clarin.cmdi.componentregistry.editor.model {
	import clarin.cmdi.componentregistry.common.ChangeTrackingCMDElement;
	import clarin.cmdi.componentregistry.common.XmlAble;
	
	import mx.collections.ArrayCollection;
	import mx.events.CollectionEvent;
	import mx.events.CollectionEventKind;

	public class CMDComponent implements XmlAble, AttributeContainer, ChangeTrackingCMDElement {

		//Attributes
		public var name:String;
		public var componentId:String;
		public var conceptLink:String;
		public var filename:String;
		public var cardinalityMin:String = "1";
		public var cardinalityMax:String = "1";

		//elements
		public var attributeList:ArrayCollection = new ArrayCollection();
		public var cmdElements:ArrayCollection = new ArrayCollection();
		public var cmdComponents:ArrayCollection = new ArrayCollection();

		private var changed:Boolean = false;
		private var _changeTracking:Boolean = false;

		public function CMDComponent() {
			attributeList.addEventListener(CollectionEvent.COLLECTION_CHANGE, collectionChangedHandler);
			cmdElements.addEventListener(CollectionEvent.COLLECTION_CHANGE, collectionChangedHandler);
			cmdComponents.addEventListener(CollectionEvent.COLLECTION_CHANGE, collectionChangedHandler);
		}

		public static function createEmptyComponent():CMDComponent {
			var result:CMDComponent = new CMDComponent();
			return result;
		}
		
		private function collectionChangedHandler(event:CollectionEvent):void {
			if(event.kind == CollectionEventKind.ADD ||event.kind == CollectionEventKind.MOVE || event.kind == CollectionEventKind.REMOVE || event.kind == CollectionEventKind.REPLACE){
				setChanged(true);
			}
			if(event.kind == CollectionEventKind.ADD){
				for each(var item:ChangeTrackingCMDElement in event.items){
					item.changeTracking = _changeTracking;
				}
			}
		}
		
		public function set changeTracking(value:Boolean):void {
			_changeTracking = value;
			for each (var component:CMDComponent in cmdComponents){
				component.changeTracking = value;
			}
			for each (var element:CMDComponentElement in cmdElements){
				element.changeTracking = value;
			}
			for each (var attribute:CMDAttribute in attributeList){
				attribute.changeTracking = value;
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
				for each (var attriubte:CMDAttribute in attributeList){
					if(attriubte.hasChanged){
						return true;
					}
				}
				for each (var element:CMDComponentElement in cmdElements){
					if(element.hasChanged){
						return true;
					}
				}
				for each (var component:CMDComponent in cmdComponents){
					if(component.hasChanged){
						return true;
					}
				}
			}
			return false;
		}

		public function removeComponent(component:CMDComponent):void {
			var index:int = cmdComponents.getItemIndex(component);
			if (index != -1) {
				cmdComponents.removeItemAt(index);
				setChanged(true);
			}
		}

		public function removeElement(element:CMDComponentElement):void {
			var index:int = cmdElements.getItemIndex(element);
			if (index != -1) {
				cmdElements.removeItemAt(index);
				setChanged(true);
			}
		}

		public function moveDownElement(element:CMDComponentElement):Boolean {
			var index:int = cmdElements.getItemIndex(element);
			if (index < cmdElements.length - 1) {
				cmdElements.removeItemAt(index);
    			cmdElements.addItemAt(element, index + 1);
				setChanged(true);
    			return true;
			}
			return false;
		}
		
		public function moveUpElement(element:CMDComponentElement):Boolean {
			var index:int = cmdElements.getItemIndex(element);
			if (index > 0) {
				cmdElements.removeItemAt(index);
    			cmdElements.addItemAt(element, index - 1);
				setChanged(true);
    			return true;
			}
			return false;
		}
		
		public function moveDownComponent(comp:CMDComponent):Boolean {
			var index:int = cmdComponents.getItemIndex(comp);
			if (index < cmdComponents.length - 1) {
				cmdComponents.removeItemAt(index);
    			cmdComponents.addItemAt(comp, index + 1);
				setChanged(true);
    			return true;
			}
			return false;
		}
		
		public function moveUpComponent(comp:CMDComponent):Boolean {
			var index:int = cmdComponents.getItemIndex(comp);
			if (index > 0) {
				cmdComponents.removeItemAt(index);
    			cmdComponents.addItemAt(comp, index - 1);
				setChanged(true);
    			return true;
			}
			return false;
		}

		public function toXml():XML {
			var result:XML = <CMD_Component></CMD_Component>;
			if (name)
				result.@name = name;
			if (componentId)
				result.@ComponentId = componentId;
			if (conceptLink)
				result.@ConceptLink = conceptLink;
			if (filename)
				result.@filename = filename;
			if (cardinalityMin)
				result.@CardinalityMin = cardinalityMin;
			if (cardinalityMax)
				result.@CardinalityMax = cardinalityMax;
			if (attributeList.length > 0) {
				var attributeListTag:XML = <AttributeList></AttributeList>;
				for each (var attribute:CMDAttribute in attributeList) {
					attributeListTag.appendChild(attribute.toXml());
				}
				result.appendChild(attributeListTag);
			}
			for each (var element:CMDComponentElement in cmdElements) {
				result.appendChild(element.toXml());
			}
			for each (var component:CMDComponent in cmdComponents) {
				result.appendChild(component.toXml());
			}
			return result;
		}
		
		public function getAttributeList():ArrayCollection {
			return attributeList;
		}
	}
}
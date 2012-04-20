package clarin.cmdi.componentregistry.editor.model {
	import clarin.cmdi.componentregistry.common.XmlAble;
	
	import mx.collections.ArrayCollection;

	public class CMDComponent implements XmlAble, AttributeContainer {

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


		public function CMDComponent() {
		}

		public static function createEmptyComponent():CMDComponent {
			var result:CMDComponent = new CMDComponent();
			return result;
		}

		public function removeComponent(component:CMDComponent):void {
			var index:int = cmdComponents.getItemIndex(component);
			if (index != -1) {
				cmdComponents.removeItemAt(index);
			}
		}

		public function removeElement(element:CMDComponentElement):void {
			var index:int = cmdElements.getItemIndex(element);
			if (index != -1) {
				cmdElements.removeItemAt(index);
			}
		}

		public function moveDownElement(element:CMDComponentElement):Boolean {
			var index:int = cmdElements.getItemIndex(element);
			if (index < cmdElements.length - 1) {
				cmdElements.removeItemAt(index);
    			cmdElements.addItemAt(element, index + 1);
    			return true;
			}
			return false;
		}
		
		public function moveUpElement(element:CMDComponentElement):Boolean {
			var index:int = cmdElements.getItemIndex(element);
			if (index > 0) {
				cmdElements.removeItemAt(index);
    			cmdElements.addItemAt(element, index - 1);
    			return true;
			}
			return false;
		}
		
		public function moveDownComponent(comp:CMDComponent):Boolean {
			var index:int = cmdComponents.getItemIndex(comp);
			if (index < cmdComponents.length - 1) {
				cmdComponents.removeItemAt(index);
    			cmdComponents.addItemAt(comp, index + 1);
    			return true;
			}
			return false;
		}
		
		public function moveUpComponent(comp:CMDComponent):Boolean {
			var index:int = cmdComponents.getItemIndex(comp);
			if (index > 0) {
				cmdComponents.removeItemAt(index);
    			cmdComponents.addItemAt(comp, index - 1);
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
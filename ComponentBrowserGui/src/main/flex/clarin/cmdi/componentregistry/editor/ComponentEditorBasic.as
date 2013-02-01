package clarin.cmdi.componentregistry.editor
{   
    import flash.events.Event;
	import flash.events.MouseEvent;
	
	import flash.display.DisplayObject;
	import flash.display.Sprite;
	
	import mx.managers.DragManager;
	import mx.managers.IFocusManagerComponent;
	import mx.core.Container;
	import mx.core.UIComponent;
	import mx.controls.Label;
	import mx.events.DragEvent;
	import mx.collections.ArrayCollection;
	
	import clarin.cmdi.componentregistry.editor.model.CMDComponent;
	import clarin.cmdi.componentregistry.common.ItemDescription;
	import clarin.cmdi.componentregistry.common.components.AddComponentLabelButton;
	import clarin.cmdi.componentregistry.common.components.AddElementLabelButton;
	import clarin.cmdi.componentregistry.editor.model.CMDComponentElement;
	
	public class ComponentEditorBasic extends Sprite{
		
		public static const REMOVE_COMPONENT_EVENT:String = "removeComponent";
		private static const DRAG_ITEMS:String = "items";
		
		private var _component:CMDComponent;
		private var _ownerEditor:UIComponent;
		public var addComponentLabel:Label;
		public var addElementLabel:Label;
		
		
		public function ComponentEditorBasic(ownerEditor:UIComponent, component:CMDComponent){
			_ownerEditor=ownerEditor;
			_component = component;
		}
		
		
		
		public function addDragEventListenersAndProcessors():void {
			addEventListener(DragEvent.DRAG_ENTER, dragEnterHandler);
			addEventListener(DragEvent.DRAG_OVER, dragOverHandler);
			addEventListener(DragEvent.DRAG_DROP, dragDropHandler);
		}
		
		
		public function dragEnterHandler(event:DragEvent):void {
			DragManager.acceptDragDrop(event.currentTarget as UIComponent);
			UIComponent(event.currentTarget).drawFocus(true);
		}
		
		private function dragOverHandler(event:DragEvent):void {
			if (event.dragSource.hasFormat(DRAG_ITEMS)) {
				DragManager.showFeedback(DragManager.COPY);
			} else {
				DragManager.showFeedback(DragManager.NONE);
			}
		}
		
		private function dragDropHandler(event:DragEvent):void {
			if (event.dragSource.hasFormat(DRAG_ITEMS)) {
				var items:Array = event.dragSource.dataForFormat(DRAG_ITEMS) as Array;
				for each (var item:ItemDescription in items) {
					var comp:CMDComponent = new CMDComponent();
					comp.componentId = item.id;
					_component.cmdComponents.addItem(comp);
					addComponent(comp);
				}
			}
		}
		
		public function addComponent(component:CMDComponent):void {
			var comp:Container = new ComponentEdit(component, _ownerEditor, _component);
			comp.addEventListener(ComponentEditorBasic.REMOVE_COMPONENT_EVENT, removeComponent);
			
			if (_ownerEditor is ComponentEdit) {comp.setStyle("paddingLeft", "50");};
			
			if (!(addComponentLabel)) {
				_ownerEditor.addChild(comp);
			} else {
				_ownerEditor.addChildAt(comp, _ownerEditor.getChildIndex(addComponentLabel)); //Add components at the place of the button so button moves down
			}
		}
		
		private function removeComponent(event:Event):void {
			var comp:CMDComponent = ComponentEdit(event.currentTarget).component;
			_component.removeComponent(comp);
			_ownerEditor.removeChild(event.currentTarget as DisplayObject);
		}
		
		
		public function addComponentAddButton():void {
			addComponentLabel = new AddComponentLabelButton();
			addComponentLabel.addEventListener(MouseEvent.CLICK, function(event:MouseEvent):void {
				var comp:CMDComponent = CMDComponent.createEmptyComponent();
				_component.cmdComponents.addItem(comp);
				addComponent(comp);
			});
			addComponentLabel.addEventListener(MouseEvent.MOUSE_OVER, function(event:MouseEvent):void {
				_ownerEditor.drawFocus(true);
			});
			addComponentLabel.addEventListener(MouseEvent.MOUSE_OUT, function(event:MouseEvent):void {
				_ownerEditor.drawFocus(false);
			});
			_ownerEditor.addChild(addComponentLabel);
		}
		
		public function handleComponents(components:ArrayCollection):void {
			for each (var component:CMDComponent in components) {
				addComponent(component);
			}
		}
		
		public function addElementAddButton():void {
			addElementLabel = new AddElementLabelButton();
			addElementLabel.addEventListener(MouseEvent.CLICK, function(event:MouseEvent):void {
				var element:CMDComponentElement = CMDComponentElement.createEmptyElement();
				_component.cmdElements.addItem(element);
				addElement(element);
				
			});
			addElementLabel.addEventListener(MouseEvent.MOUSE_OVER, function(event:MouseEvent):void {
				_ownerEditor.drawFocus(true);
			});
			addElementLabel.addEventListener(MouseEvent.MOUSE_OUT, function(event:MouseEvent):void {
				_ownerEditor.drawFocus(false);
			});
			_ownerEditor.addChild(addElementLabel);
		}
		
		public function handleElements(elements:ArrayCollection):void {
			for each (var element:CMDComponentElement in elements) {
				addElement(element);
			}
		}
		
		public function addElement(element:CMDComponentElement):void {
			var elem:Container = new ElementEdit(element, _ownerEditor, _component);
			elem.setStyle("paddingLeft", "50");
			elem.addEventListener(ElementEdit.REMOVE_ELEMENT_EVENT, removeElement);
			if (!addElementLabel) {
				_ownerEditor.addChild(elem);
			} else {
				_ownerEditor.addChildAt(elem, getChildIndex(addElementLabel));
			}
		}
		
		private function removeElement(event:Event):void {
			var elem:CMDComponentElement = ElementEdit(event.currentTarget).element;
			_component.removeElement(elem);
			removeChild(event.currentTarget as DisplayObject);
		}
		
	}
}
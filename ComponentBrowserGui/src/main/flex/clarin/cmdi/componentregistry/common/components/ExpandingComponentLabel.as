package clarin.cmdi.componentregistry.common.components {
	import clarin.cmdi.componentregistry.editor.CMDSpecRenderer;
	import clarin.cmdi.componentregistry.common.Component;
	import clarin.cmdi.componentregistry.common.ItemDescription;
	import clarin.cmdi.componentregistry.common.StyleConstants;
	import clarin.cmdi.componentregistry.editor.model.CMDModelFactory;
	import clarin.cmdi.componentregistry.services.ComponentInfoService;
	import clarin.cmdi.componentregistry.services.ComponentListService;
	
	import flash.display.DisplayObject;
	import flash.events.MouseEvent;
	
	import mx.containers.VBox;
	import mx.controls.Alert;
	import mx.controls.Label;

	public class ExpandingComponentLabel extends VBox {

		[Bindable]
		public var isExpanded:Boolean = false;

		private var expanded:DisplayObject;
		private var componentId:String;
		private var componentSrv:ComponentInfoService = new ComponentInfoService();

		private var editable:Boolean;

		public function ExpandingComponentLabel(componentId:String, editable:Boolean = false) {
			super();
			this.editable = editable;
			this.componentId = componentId;
			styleName = StyleConstants.EXPANDING_COMPONENT;
		}
		
		protected override function createChildren():void {
            super.createChildren();
			var id:Label = new Label();
			id.text = componentId;
			id.addEventListener(MouseEvent.CLICK, handleClick);
			id.addEventListener(MouseEvent.MOUSE_OVER, mouseOver);
			id.addEventListener(MouseEvent.MOUSE_OUT, mouseOut);
		    addChild(id);
		}
		
		private function handleClick(event:MouseEvent):void {
			if (isExpanded) {
				unexpand();
				isExpanded = false;
			} else {
				expand();
			}
		}


		private function unexpand():void {
			if (expanded != null) {
				removeChild(expanded);
			}
		}

		private function expand():void {
			var item:ItemDescription = ComponentListService.instance.lookUpDescription(componentId);
			if (item != null) {
				componentSrv.addEventListener(ComponentInfoService.COMPONENT_LOADED, handleComponentLoaded);
				componentSrv.load(item);
			} else {
				Alert.show("Error: component cannot be found");
			}
		}

		private function handleComponentLoaded(event:Event):void {
			var comp:Component = componentSrv.component;
			if (editable) {
				expanded = new CMDComponentXMLEditor();
			} else {
				expanded = new CMDComponentXMLBrowser();
			}
			(expanded as CMDSpecRenderer).cmdSpec = CMDModelFactory.createModel(comp.componentMD.xml);
			addChild(expanded);
			isExpanded = true;
		}


		private function mouseOver(event:MouseEvent):void {
			event.currentTarget.setStyle("color", "0x0000FF");
			event.currentTarget.setStyle("textDecoration", "underline");
		}

		private function mouseOut(event:MouseEvent):void {
			event.currentTarget.setStyle("color", "0x000000");
			event.currentTarget.setStyle("textDecoration", "none");
		}

	}
}
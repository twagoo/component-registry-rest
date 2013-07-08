package clarin.cmdi.componentregistry.common.components {
	import clarin.cmdi.componentregistry.browser.CMDComponentXMLBrowser;
	import clarin.cmdi.componentregistry.common.CMDSpecRenderer;
	import clarin.cmdi.componentregistry.common.Component;
	import clarin.cmdi.componentregistry.common.ItemDescription;
	import clarin.cmdi.componentregistry.common.StyleConstants;
	import clarin.cmdi.componentregistry.editor.CMDComponentXMLEditor;
	import clarin.cmdi.componentregistry.editor.model.CMDModelFactory;
	import clarin.cmdi.componentregistry.services.ComponentInfoService;
	import clarin.cmdi.componentregistry.services.ComponentListService;
	import clarin.cmdi.componentregistry.services.Config;
	
	import flash.display.DisplayObject;
	import flash.events.MouseEvent;

	import mx.containers.VBox;
	import mx.controls.Label;
	import mx.managers.CursorManager;

	public class ExpandingComponentLabel extends VBox {

		[Bindable]
		public var isExpanded:Boolean = false;
		private var expandBusy:Boolean = false;

		private var expanded:DisplayObject;
		private var componentId:String;
		private var item:ItemDescription;
		private var componentSrv:ComponentInfoService = new ComponentInfoService();

		private var editable:Boolean;

		public function ExpandingComponentLabel(componentId:String, editable:Boolean = false) {
			super();
			this.editable = editable;
			this.componentId = componentId;			
			this.item = Config.instance.getComponentsSrv(false).findDescription(componentId);
			if (this.item==null) {
				this.item = Config.instance.getComponentsSrv(true).findDescription(componentId);
			}
			styleName = StyleConstants.EXPANDING_COMPONENT;
			if (item && item.isInUserSpace) {
				this.setStyle("borderColor", StyleConstants.USER_BORDER_COLOR);
			}
		}

		protected override function createChildren():void {
			super.createChildren();
			var id:Label = new Label();
			if (item) {
				id.text = item.name;
				id.styleName = StyleConstants.XMLBROWSER_HEADER;
				id.addEventListener(MouseEvent.CLICK, handleClick);
				id.addEventListener(MouseEvent.MOUSE_OVER, mouseOver);
				id.addEventListener(MouseEvent.MOUSE_OUT, mouseOut);
			} else {
				id.text = "Component cannot be found (might not exist anymore).";
			}
			addChild(id);
		}

		private function handleClick(event:MouseEvent):void {
			if (!expandBusy) {
				expandStart();
				try {
					if (isExpanded) {
						isExpanded = false;
						unexpand();
					} else {
						isExpanded = true;
						expand();
					}
				} catch (err:Error) {
					trace(err);
					CursorManager.removeBusyCursor();
				}
			}
		}

		private function expandStart():void {
			expandBusy = true;
			CursorManager.setBusyCursor();
		}

		private function expandFinished():void {
			expandBusy = false;
			CursorManager.removeBusyCursor();
		}


		private function unexpand():void {
			if (expanded != null) {
				removeChild(expanded);
				expanded = null;
			}
			expandFinished();
		}

		private function expand():void {
			componentSrv.addEventListener(ComponentInfoService.COMPONENT_LOADED, handleComponentLoaded);
			componentSrv.load(item);
		}

		private function handleComponentLoaded(event:Event):void {
			trace("ExpandingComponentLable's handleComponentLoaded is called, it is calling CMDComponentLabel");
			var comp:Component = componentSrv.component;
			if (editable) {
				expanded = new CMDComponentXMLEditor();
			} else {
				expanded = new CMDComponentXMLBrowser();
			}
			(expanded as CMDSpecRenderer).cmdSpec = CMDModelFactory.createModel(comp.componentMD.xml, comp.description);
			addChild(expanded);
			expandFinished();
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
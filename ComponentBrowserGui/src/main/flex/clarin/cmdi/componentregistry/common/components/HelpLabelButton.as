package clarin.cmdi.componentregistry.common.components
{
	import clarin.cmdi.componentregistry.services.Config;
	
	import com.adobe.net.URI;
	
	import flash.events.MouseEvent;
	import flash.net.URLRequest;
	import flash.net.navigateToURL;
	
	public class HelpLabelButton extends LabelButton{
		
		public function HelpLabelButton(){
			super(goToDocumentationPage, "help");
			toolTip = "Click to navigate to component-registry page";
		}
		
		private function goToDocumentationPage(event:MouseEvent):void{
			var request:URLRequest = new URLRequest("documentation.jsp");
			navigateToURL(request, "_blank");
		}
		
	}
}
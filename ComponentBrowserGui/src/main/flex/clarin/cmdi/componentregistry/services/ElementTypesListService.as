package clarin.cmdi.componentregistry.services
{
    import flash.events.Event;
    import flash.events.MouseEvent;
    
    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;
	
	public class ElementTypesListService extends ComponentRegistryService
	{ 
		
		public var allowedTypes:ArrayCollection=null;
		
		public static const ALLOWED_TYPES_LOADED:String = "allowed_element_types_loaded";
		
		public function ElementTypesListService()
		{ 
		  super(Config.getUriAllowedElementTypes());
		}
		
		
		// called by load()
		override protected function result(resultEvent:ResultEvent):void {
			var resultXml:XML = resultEvent.result as XML;
			allowedTypes= new ArrayCollection();
			var currentValue:String;
			for each (var description:XML in resultXml.children()) {
				currentValue=description.valueOf().toString();
				allowedTypes.addItem({label: currentValue, data: currentValue});
			}
			dispatchEvent(new Event(ALLOWED_TYPES_LOADED));
		}
		
	}
}
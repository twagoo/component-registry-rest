package clarin.cmdi.componentregistry.services
{
    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;
	
	
	public class ElementTypesListService extends ComponentRegistryService
	{ 
		[Bindable]
		public var allowedTypes:ArrayCollection=null;
		
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
		}
		
	}
}
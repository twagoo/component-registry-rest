package clarin.cmdi.componentregistry.services //trunk
{
    
    import com.adobe.net.URI;
    
    import mx.collections.ArrayCollection;
	
	public class ElementTypesListService extends ComponentRegistryService
	{ 
		[Bindable]
		public var allowedTypes:ArrayCollection=null;
		
		public static const ALLOWED_TYPES_LOADED:String = "allowed_element_types_loaded"; 
		
		public function ElementTypesListService()
		{ 
		  super(ALLOWED_TYPES_LOADED, new URI(Config.getUriAllowedElementTypes()));
		}
		
		
		override protected function handleXmlResult(resultXml:XML):void{
			allowedTypes= new ArrayCollection();
			var currentValue:String;
			for each (var description:XML in resultXml.children()) {
				currentValue=description.valueOf().toString();
				allowedTypes.addItem({label: currentValue, data: currentValue});
			}
		}
		
	}
}
package clarin.cmdi.componentregistry.services
{
	import mx.rpc.events.ResultEvent;
	
	import clarin.cmdi.componentregistry.common.ItemDescription;
	
	import flexunit.framework.TestCase;

	public class ComponentInfoServiceTest extends TestCase
	{
		private var service:ComponentInfoService;
		private var serviceFactory:MockServiceFactoryImpl;
		
		//for some reason setup doesn't get called
		[Before]
		public function setup():void{
			serviceFactory = new MockServiceFactoryImpl();
			service = new ComponentInfoService();
		}
		
		public function ComponentInfoServiceTest()
		{
			super(); 
		}
		
		public function testInvocation():void{
			setup();
			var item:ItemDescription = new ItemDescription();
			service.load(item);
			var xml:XML = <CMD_ComponentSpec isProfile="false">
<Header>
<ID>clarin.eu:cr1:c_1376059207640</ID>
<Name>PublicComponent</Name>
<Description>Description</Description>
</Header>
<CMD_Component CardinalityMax="1" CardinalityMin="1" name="PublicComponent">
<CMD_Element Multilingual="false" DisplayPriority="1" CardinalityMax="1" CardinalityMin="0" ValueScheme="string" name="OneElement"></CMD_Element>
</CMD_Component>
</CMD_ComponentSpec>;

			var result:ResultEvent = new ResultEvent("xml", false, true, xml, serviceFactory.lastService.token, null);
			
			serviceFactory.lastService.invokeResult(result);
			assertNotNull(service.component);
			assertNotNull(service.component.componentMD);
			assertEquals("PublicComponent",service.component.componentMD.name);
		}
		
		
	}
}
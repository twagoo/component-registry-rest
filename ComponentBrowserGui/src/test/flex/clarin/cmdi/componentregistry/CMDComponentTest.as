package clarin.cmdi.componentregistry.editor.model {
	import flexunit.framework.TestCase;
	import flexunit.framework.TestSuite;

	public class CMDComponentTest extends TestCase {
		public function CMDComponentTest() {
			super();
		}

		public static function suite():TestSuite {
			return new TestSuite(CMDComponentTest);
		}

		public function testToXml():void {
		    var comp:CMDComponent = new CMDComponent();
		    comp.name = "aap";
		    var expected:XML = <CMD_Component name="aap" CardinalityMin="1" CardinalityMax="1"/>
			assertEquals(expected.toXMLString(), comp.toXml().toXMLString());
			
		}


	}
}
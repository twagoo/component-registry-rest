package clarin.cmdi.componentregistry.editor.model {
	import flexunit.framework.TestCase;
	import flexunit.framework.TestSuite;

	public class CMDSpecTest extends TestCase {
		public function CMDSpecTest() {
			super();
		}

		public static function suite():TestSuite {
			return new TestSuite(CMDSpecTest);
		}

		public function testToXml():void {
			var comp:CMDSpec = new CMDSpec(true);
			comp.headerName = "aap";
			comp.headerDescription = "noot";
			comp.headerId = "";
			var expected:XML = <CMD_ComponentSpec isProfile="true">
					<Header>
						<ID></ID>
						<Name>aap</Name>
						<Description>noot</Description>
					</Header>
				</CMD_ComponentSpec>

			assertEquals(expected.toXMLString(), comp.toXml().toXMLString());

		}
	}
}
package clarin.cmdi.componentregistry.editor.model {
	import flexunit.framework.TestCase;
	import flexunit.framework.TestSuite;

	public class CMDAttributeTest extends TestCase {
		public function CMDAttributeTest() {
			super();
		}

		public static function suite():TestSuite {
			return new TestSuite(CMDAttributeTest);
		}

		public function testToXml():void {
			var result:CMDAttribute = new CMDAttribute();
			result.name = "aap";
			result.type = "string";
			var expected:XML = <Attribute>
					<Name>aap</Name>
					<Type>string</Type>
				</Attribute>;
			assertEquals(expected.toXMLString(), result.toXml().toXMLString());
		}

//public function testToXmlValueSchemePattern():void {}
//public function testToXmlValueSchemeEnumeration():void {}
	}
}
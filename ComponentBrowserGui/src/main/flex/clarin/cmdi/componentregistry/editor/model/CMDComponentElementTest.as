package clarin.cmdi.componentregistry.editor.model {
	import flexunit.framework.TestCase;
	import flexunit.framework.TestSuite;

	public class CMDComponentElementTest extends TestCase {
		public function CMDComponentElementTest() {
			super();
		}

		public static function suite():TestSuite {
			return new TestSuite(CMDComponentElementTest);
		}

		public function testToXml():void {
			var result:CMDComponentElement = new CMDComponentElement();
			result.name = "aap";
			result.valueSchemeSimple = "string";
			var att:CMDAttribute = new CMDAttribute();
			att.name = "test";
			att.type = "string";
			result.attributeList.addItem(att);
			var expected:XML = <CMD_Element name="aap" ValueScheme="string" CardinalityMin="1" CardinalityMax="1">
					<AttributeList>
						<Attribute>
							<Name>test</Name>
							<Type>string</Type>
						</Attribute>  
					</AttributeList>
				</CMD_Element>;
			assertEquals(expected.toXMLString(), result.toXml().toXMLString());
		}

	}
}
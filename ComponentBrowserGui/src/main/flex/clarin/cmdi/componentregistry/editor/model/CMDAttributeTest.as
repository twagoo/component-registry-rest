package clarin.cmdi.componentregistry.editor.model {
	import clarin.cmdi.componentregistry.common.ComponentMD;
	
	import flexunit.framework.TestCase;
	import flexunit.framework.TestSuite;
	
	import mx.collections.XMLListCollection;

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
			result.valueSchemeSimple = "string";
			var expected:XML = <Attribute>
					<Name>aap</Name>
					<Type>string</Type>
				</Attribute>;
			assertEquals(expected.toXMLString(), result.toXml().toXMLString());
		}

		public function testToXmlValueSchemePattern():void {
			var result:CMDAttribute = new CMDAttribute();
			result.name = "aap";
			result.valueSchemePattern = "[a-zA-Z]+";
			var expected:XML = <Attribute>
					<Name>aap</Name>
					<ValueScheme>
					    <pattern>[a-zA-Z]+</pattern>
					</ValueScheme>
				</Attribute>;
			assertEquals(expected.toXMLString(), result.toXml().toXMLString());
		}

		public function testToXmlValueSchemeEnumeration():void {
			var result:CMDAttribute = new CMDAttribute();
			result.name = "aap";
			result.valueSchemeEnumeration = new XMLListCollection();
			result.valueSchemeEnumeration.addItem(<item {ComponentMD.APP_INFO}="" {ComponentMD.CONCEPTLINK}="">Male</item>);
			var expected:XML = <Attribute>
					<Name>aap</Name>
					<ValueScheme>
						<enumeration>
							<item AppInfo="" ConceptLink="">Male</item>
						</enumeration>
					</ValueScheme>
				</Attribute>;
			assertEquals(expected.toXMLString(), result.toXml().toXMLString());
		}
	}
}
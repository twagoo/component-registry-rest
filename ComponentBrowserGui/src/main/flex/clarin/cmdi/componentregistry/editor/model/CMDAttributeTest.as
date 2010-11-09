package clarin.cmdi.componentregistry.editor.model {
	import clarin.cmdi.componentregistry.common.ComponentMD;
	import clarin.cmdi.componentregistry.editor.ValueSchemeItem;
	
	import flexunit.framework.TestCase;
	import flexunit.framework.TestSuite;
	
	import mx.collections.ArrayCollection;

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
			result.valueSchemeEnumeration = new ArrayCollection();
			result.valueSchemeEnumeration.addItem(new ValueSchemeItem("Male", "test", "test2"));
			var expected:XML = <Attribute>
					<Name>aap</Name>
					<ValueScheme>
						<enumeration>
							<item AppInfo="test1" ConceptLink="test2">Male</item>
						</enumeration>
					</ValueScheme>
				</Attribute>;
			assertEquals(expected.toXMLString(), result.toXml().toXMLString());
		}
	}
}
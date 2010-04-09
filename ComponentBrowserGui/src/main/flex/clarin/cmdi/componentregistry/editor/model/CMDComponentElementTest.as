package clarin.cmdi.componentregistry.editor.model {
	import clarin.cmdi.componentregistry.common.ComponentMD;

	import flexunit.framework.TestCase;
	import flexunit.framework.TestSuite;

	import mx.collections.XMLListCollection;

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
			result.documentation = "Docu";
			var att:CMDAttribute = new CMDAttribute();
			att.name = "test";
			att.valueSchemeSimple = "string";
			result.attributeList.addItem(att);
			var expected:XML = <CMD_Element name="aap" Documentation="Docu" ValueScheme="string" CardinalityMin="1" CardinalityMax="1">
					<AttributeList>
						<Attribute>
							<Name>test</Name>
							<Type>string</Type>
						</Attribute>  
					</AttributeList>
				</CMD_Element>;
			assertEquals(expected.toXMLString(), result.toXml().toXMLString());
		}

		public function testToXmlValueScheme():void {
			var result:CMDComponentElement = new CMDComponentElement();
			result.name = "Sex";
			result.conceptLink = "http://www.isocat.org/datcat/DC-2560";
			result.valueSchemeEnumeration = new XMLListCollection();
			result.valueSchemeEnumeration.addItem(<item {ComponentMD.APP_INFO}="" {ComponentMD.CONCEPTLINK}="">Male</item>);
			result.valueSchemeEnumeration.addItem(<item {ComponentMD.APP_INFO}="" {ComponentMD.CONCEPTLINK}="">Female</item>);
			result.valueSchemeEnumeration.addItem(<item {ComponentMD.APP_INFO}="" {ComponentMD.CONCEPTLINK}="">Unknown</item>);
			var expected:XML = <CMD_Element name="Sex" ConceptLink="http://www.isocat.org/datcat/DC-2560" CardinalityMin="1" CardinalityMax="1">
					<ValueScheme>
						<enumeration>
							<item AppInfo="" ConceptLink="">Male</item>
							<item AppInfo="" ConceptLink="">Female</item>
							<item AppInfo="" ConceptLink="">Unknown</item>
						</enumeration>
					</ValueScheme>
				</CMD_Element>;

			assertEquals(expected.toXMLString(), result.toXml().toXMLString());
		}
		
		public function testToXmlValueSchemePattern():void {
			var result:CMDComponentElement = new CMDComponentElement();
			result.name = "Test";
			result.cardinalityMax="5"
			result.valueSchemePattern = "[a-z]";
			
			var expected:XML = <CMD_Element name="Test" CardinalityMin="1" CardinalityMax="5">
					<ValueScheme>
						<pattern>[a-z]</pattern>
					</ValueScheme>
				</CMD_Element>;

			assertEquals(expected.toXMLString(), result.toXml().toXMLString());
		}

	}
}
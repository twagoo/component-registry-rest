package clarin.cmdi.componentregistry.common {
    import mx.collections.ArrayCollection;
    

	public class LabelConstants {

		//Label names
		public static const NAME:String = "Name:";
		public static const CONCEPTLINK:String = "ConceptLink:";
		public static const COMPONENT:String = "Component:";
		public static const COMPONENT_ID:String = "ComponentId:";
		public static const DESCRIPTION:String = "Description:";
		public static const CARDINALITY_MIN:String = "Min Occurrences:";
		public static const CARDINALITY_MAX:String = "Max Occurrences:";
		public static const CARDINALITY:String = "Number of occurrences:"
		public static const DOCUMENTATION:String = "Documentation:";
		public static const DISPLAY_PRIORITY:String = "DisplayPriority:";
		public static const ATTRIBUTELIST:String = "AttributeList:"
		public static const ELEMENT:String = "Element:";
		public static const VALUESCHEME:String = "Type:";
		public static const GROUP_NAME:String = "Group Name:";
		public static const DOMAIN_NAME:String = "Domain Name:";
        public static const DOMAIN_NAME_PROMPT:String = "Select a domain...";
        

		//Data
		public static const DOMAIN_NAME_DATA:ArrayCollection = new ArrayCollection([{label: "Applied Linguistics", data: "applied_linguistics"},
			{label: "Other", data: "Other"},
			{label: "Unknown", data: "Unknown"}]); //TODO PD, Dieter knows what to fill in here!
			
/* <item>anthropological_linguistics</item>
<item>applied_linguistics</item>
<item>cognitive_science</item>
<item>computational_linguistics</item>
<item>discourse_analysis</item>
<item>forensic_linguistics</item>
<item>general_linguistics</item>
<item>historical_linguistics</item>
<item>history_of_linguistics</item>
<item>language_acquisition</item>
<item>language_documentation</item>
<item>lexicography</item>
<item>linguistics_and_literature</item>
<item>linguistic_theories</item>
<item>mathematical_linguistics</item>
<item>morphology</item>
<item>neurolinguistics</item>
<item>philosophy_of_language</item>
<item>phonetics</item>
<item>phonology</item>
<item>pragmatics</item>
<item>psycholinguistics</item>
<item>semantics</item>
<item>sociolinguistics</item>
<item>syntax</item>
<item>text_and_corpus_linguistics</item>
<item>translating_and_interpreting</item>
<item>typology</item>
<item>writing_systems</item>
 */	
 }
}
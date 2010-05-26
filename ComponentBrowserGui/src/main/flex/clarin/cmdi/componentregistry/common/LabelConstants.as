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

		//tooltip
		public static const DISPLAY_PRIORITY_TOOLTIP:String = "Display preference for external tools, 0 means ignore and 1 means highest priority. Element with name='Name' or name='Title' is usually a good candidate to set to highest priority."        

		//Data
		public static const DOMAIN_NAME_DATA:ArrayCollection = new ArrayCollection([
		{label: "Anthropological Linguistics", data: "anthropological_linguistics"},
		{label: "Applied Linguistics", data: "applied_linguistics"},
		{label: "Cognitive Science", data: "cognitive_science"},
		{label: "Computational Linguistics", data: "computational_linguistics"},
		{label: "Discourse Analysis", data: "discourse_analysis"},
		{label: "Forensic Linguistics", data: "forensic_linguistics"},
		{label: "General Linguistics", data: "general_linguistics"},
		{label: "Historical Linguistics", data: "historical_linguistics"},
		{label: "History of Linguistics", data: "history_of_linguistics"},
		{label: "Language Acquisition", data: "language_acquisition"},
		{label: "Language Documentation", data: "language_documentation"},
		{label: "Lexicography", data: "lexicography"},
		{label: "Linguistics and Literature", data: "linguistics_and_literature"},
		{label: "Linguistic Theories", data: "linguistic_theories"},
		{label: "Mathematical Linguistics", data: "mathematical_linguistics"},
		{label: "Morphology", data: "morphology"},
		{label: "Neurolinguistics", data: "neurolinguistics"},
		{label: "Philosophy of Language", data: "philosophy_of_language"},
		{label: "Phonetics", data: "phonetics"},
		{label: "Phonology", data: "phonology"},
		{label: "Pragmatics", data: "pragmatics"},
		{label: "Psycholinguistics", data: "psycholinguistics"},
		{label: "Semantics", data: "semantics"},
		{label: "Sociolinguistics", data: "sociolinguistics"},
		{label: "Syntax", data: "syntax"},
		{label: "Text and Corpus Linguistics", data: "text_and_corpus_linguistics"},
		{label: "Translating and Interpreting", data: "translating_and_interpreting"},
		{label: "Typology", data: "typology"},
		{label: "Writing Systems", data: "writing_systems"},
		{label: "Other", data: "Other"},
		{label: "Unknown", data: "Unknown"}]); //TODO PD, Dieter knows what to fill in here!
			
 }
}
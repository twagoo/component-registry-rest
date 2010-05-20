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


		//Data
		public static const DOMAIN_NAME_DATA:ArrayCollection = new ArrayCollection([{label: "Linguistics", data: "Linguistics"},
			{label: "Other", data: "Other"},
			{label: "Unknown", data: "Unknown"}]);
	}
}
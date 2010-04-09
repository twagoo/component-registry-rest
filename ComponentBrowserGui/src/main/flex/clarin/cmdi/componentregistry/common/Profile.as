package clarin.cmdi.componentregistry.common {
	import mx.collections.ArrayCollection;

    [Bindable]
	public class Profile {
	    
		public var description:ItemDescription;
		public var components:ArrayCollection;
		public var nrOfComponents:int;
		public var profileSource:XML;

		public function Profile() {
		}

	}
}
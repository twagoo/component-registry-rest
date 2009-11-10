package clarin.cmdi.componentregistry {
	import mx.collections.ArrayCollection;
	import mx.utils.ObjectProxy;

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
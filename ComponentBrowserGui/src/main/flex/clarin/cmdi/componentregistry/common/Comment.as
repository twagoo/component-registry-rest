package clarin.cmdi.componentregistry.common {
	import mx.collections.ArrayCollection;
	
	[Bindable]
	public class Comment {
		
		public var description:CommentDescription;
		public var commentMD:CommentMD;//ComponentMD
		public var nrOfComments:int;
		public var commentSource:XML;
		
		public function Comment() {
		}
		
	}
}
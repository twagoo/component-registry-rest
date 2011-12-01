package clarin.cmdi.componentregistry.editor.model {
	import mx.collections.ArrayCollection;
	
	public class CMDComment
	{
		//Attributes
		public var componentId:String;
		public var profileId:String;
		public var creatorName:String;
		public var registerDate:String;
		public var filename:String;
		public var commentId:String;
		
		//elements
		public var cmdComments:ArrayCollection = new ArrayCollection();
		
		public function CMDComment()
		{
		}
		
		public static function createEmptyComment():CMDComment {
			var result:CMDComment = new CMDComment();
			return result;
		}
	
		public function removeComment(comment:CMDComment):void {
			var index:int = cmdComments.getItemIndex(comment);
			if (index != -1) {
				cmdComments.removeItemAt(index);
			}
		}
		
		public function moveDownComment(com:CMDComment):Boolean {
			var index:int = cmdComments.getItemIndex(com);
			if (index < cmdComments.length - 1) {
				cmdComments.removeItemAt(index);
				cmdComments.addItemAt(com, index + 1);
				return true;
			}
			return false;
		}
		
		public function moveUpComponent(com:CMDComment):Boolean {
			var index:int = cmdComments.getItemIndex(com);
			if (index > 0) {
				cmdComments.removeItemAt(index);
				cmdComments.addItemAt(com, index - 1);
				return true;
			}
			return false;
		}
	}
}
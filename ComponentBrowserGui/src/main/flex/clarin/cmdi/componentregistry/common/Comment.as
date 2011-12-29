package clarin.cmdi.componentregistry.common
{
	public class Comment
	{
		[Bindable]
		public var comments:String;
		
		[Bindable]
		public var userName:String;
		
		[Bindable]
		public var commentDate:String;
		
		public var componentDescriptionId:String;
		public var profileDescriptionId:String;
		public var id:String;
		
		public function Comment(comment:XML):void{
			this.comments = comment.comments;
			this.commentDate  = comment.commentDate;
			this.componentDescriptionId = comment.componentDescriptionId;
			this.profileDescriptionId = comment.profileDescriptionId;
			this.id = comment.id;
			this.userName = comment.userName;
		}
	}
}
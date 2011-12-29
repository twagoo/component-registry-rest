package clarin.cmdi.componentregistry.common
{
	public class Comment implements XmlAble
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
		
		public function Comment():void{
			
		}
		
		public function create(comment:XML):void{
			this.comments = comment.comments;
			this.commentDate  = DateUtils.formatDateString(comment.commentDate);
			this.componentDescriptionId = comment.componentDescriptionId;
			this.profileDescriptionId = comment.profileDescriptionId;
			this.id = comment.id;
			this.userName = comment.userName;
		}
		
		public function toXml():XML {
			var result:XML = <comment></comment>;
			result.appendChild(<comments>{comments}</comments>);
			result.appendChild(<commentDate>{commentDate}</commentDate>);
			if(profileDescriptionId){
				result.appendChild(<profileDescriptionId>{profileDescriptionId}</profileDescriptionId>);
			}
			if(componentDescriptionId){
				result.appendChild(<componentDescriptionId>{componentDescriptionId}</componentDescriptionId>);
			}
			result.appendChild(<userName>{userName}</userName>);
			return result;
		}
	}
}
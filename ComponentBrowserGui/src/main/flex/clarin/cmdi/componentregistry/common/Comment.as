package clarin.cmdi.componentregistry.common
{
	import clarin.cmdi.componentregistry.services.Config;

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
		public var canDelete:Boolean;
		public var itemDescription:ItemDescription;
		public var dataUrl:String;
		
		public function Comment():void{
			
		}
		
		public function create(comment:XML, itemDescription:ItemDescription):void{
			this.comments = comment.comments;
			this.commentDate  = DateUtils.formatDateString(comment.commentDate);
			this.componentDescriptionId = comment.componentDescriptionId;
			this.profileDescriptionId = comment.profileDescriptionId;
			this.id = comment.id;
			this.userName = comment.userName;
			this.canDelete = comment.canDelete == "true";
			
			this.itemDescription = itemDescription;
			if(itemDescription.isProfile){
				dataUrl = Config.instance.getProfileCommentsPath(itemDescription.id) + id;
			} else {
				dataUrl = Config.instance.getComponentCommentsPath(itemDescription.id) + id;
			}
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
package clarin.cmdi.componentregistry.browser
{
	import clarin.cmdi.componentregistry.common.Comment;
	
	import mx.containers.HBox;
	import mx.containers.VBox;
	import mx.controls.HRule;
	import mx.controls.Label;
	import mx.controls.Text;

	public class CommentPanel extends VBox
	{
		private var userNameLabel:Label = new Label();
		private var dateLabel:Label = new Label();
		private var commentText:Text = new Text();
		private var comment:Comment;
		
		public function CommentPanel(comment:Comment)
		{
			super();
			this.comment = comment;
			
			initLayout();
			setValues();
		}
		
		private function initLayout():void{
			this.width = 400;
			
			var header:HBox = new HBox();
			header.percentWidth = 100;
			header.addChild(userNameLabel);
			header.addChild(dateLabel);
			
			userNameLabel.setStyle("fontWeight","bold");
			userNameLabel.setStyle("textDecoration","underline");
			userNameLabel.percentWidth = 70;
			dateLabel.setStyle("textAlign", "right");
			dateLabel.setStyle("fontWeight","bold");
			dateLabel.percentWidth = 30;
			
			addChild(header);
			
			commentText.percentWidth = 100;
			addChild(commentText);

			var rule:HRule = new HRule();
			rule.percentWidth = 100;
			addChild(rule);
		}
		
		private function setValues():void{
			userNameLabel.text = comment.userName;
			dateLabel.text = comment.commentDate;
			commentText.text = comment.comments;
		}
	}
}
package clarin.cmdi.componentregistry.browser {
	import clarin.cmdi.componentregistry.common.ItemDescription;
	
	import mx.collections.Sort;
	import mx.collections.SortField;
	import mx.controls.dataGridClasses.DataGridColumn;
	import mx.utils.ObjectUtil;
	
	public class BrowserColumns {
		public function BrowserColumns() {
		}
		
		public function getProfileColumns():Array {
			var columns:Array = new Array();
			columns.push(createColumn("name", "Name"));
			columns.push(createColumn("groupName", "Group Name"));
			columns.push(createColumn("domainName", "Domain Name"));
			columns.push(createColumn("creatorName", "Creator"));
			columns.push(createColumn("description", "Description"));
			columns.push(createDateColumn());
			columns.push(createCommentsColumn());
			return columns;
		}
		
		public function getComponentColumns():Array {
			var columns:Array = new Array();
			columns.push(createColumn("name", "Name"));
			columns.push(createColumn("groupName", "Group Name"));
			columns.push(createColumn("domainName", "Domain Name"));
			columns.push(createColumn("creatorName", "Creator Name"));
			columns.push(createColumn("description", "Description"));
			columns.push(createDateColumn());
			columns.push(createCommentsColumn());
			return columns;
		}
		
		private function createColumn(dataField:String, headerText:String):DataGridColumn {
			var c:DataGridColumn = new DataGridColumn();
			c.dataField = dataField;
			c.headerText = headerText;
			c.dataTipField = dataField;
			c.sortCompareFunction = function(itemA:Object, itemB:Object):int { //Sort all string fields case insensitive
				return ObjectUtil.stringCompare(itemA[dataField], itemB[dataField], true);
			}
			return c;
		}
		
		public static function getInitialSortForProfiles():Sort {
			var sort:Sort = new Sort();
			var sortByGroup:SortField = new SortField("groupName", true);
			var sortByName:SortField = new SortField("name", true);
			var sortByDate:SortField = new SortField("registrationDate", true, true);
			sortByDate.compareFunction = compareRegistrationDate;
			sort.fields = [sortByGroup, sortByName, sortByDate];
			return sort
		}
		
		public static function getInitialSortForComponents():Sort {
			var sort:Sort = new Sort();
			var sortByGroup:SortField = new SortField("groupName", true);
			var sortByName:SortField = new SortField("name", true);
			var sortByDate:SortField = new SortField("registrationDate", true, true);
			sortByDate.compareFunction = compareRegistrationDate;
			sort.fields = [sortByGroup, sortByName, sortByDate];
			return sort
		}
		
		private function createDateColumn():DataGridColumn {
			var c:DataGridColumn = createColumn("registrationDate", "Registration Date");
			c.sortCompareFunction = compareRegistrationDate;
			return c;
		}
		
		private static function compareRegistrationDate(itemA:Object, itemB:Object):int {
			return ObjectUtil.dateCompare(itemA.registrationDateValue, itemB.registrationDateValue);
		}
		
		private function createCommentsColumn():DataGridColumn {
			var c:DataGridColumn = createColumn("commentsCount","Comments");
			c.width = 75;
			c.sortCompareFunction = compareCommentsCount;
			return c;
		}
		
		private static function compareCommentsCount(objectA:Object, objectB:Object):int {
			//var stringA:String = itemA.toString();
			//var stringB:String = itemB.toString();
			var itemA:ItemDescription = ItemDescription(objectA);
			var itemB:ItemDescription = ItemDescription(objectB);
			return ObjectUtil.numericCompare(itemA.commentsCount, itemB.commentsCount);
		}
		
	}
}
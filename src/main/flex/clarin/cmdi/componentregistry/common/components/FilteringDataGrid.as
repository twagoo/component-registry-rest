package clarin.cmdi.componentregistry.common.components {
	import com.rictus.controls.SearchBox;
	
	import flash.events.Event;
	
	import mx.collections.ICollectionView;
	import mx.controls.DataGrid;
	import mx.controls.dataGridClasses.DataGridColumn;
	import mx.core.ClassFactory;
	import mx.core.IFactory;

	public class FilteringDataGrid extends DataGrid {

		private var _searchText:String = "";
		private var _searchInput:SearchBox;
		[Bindable]
		public var unfilteredLength:int = 0;

        private var highlightSelectionRenderer:IFactory = new ClassFactory(HighlightSelectionItemRenderer);

		public function FilteringDataGrid() {
			super();
		}
		
		override public function set columns(columns:Array):void {
		    for each (var column:DataGridColumn in columns) {
		        column.itemRenderer = highlightSelectionRenderer;
		    }
		    super.columns = columns;
		}

		override public function set dataProvider(dataProvider:Object):void {
			var data:ICollectionView = dataProvider as ICollectionView;
			data.filterFunction = filter;
			unfilteredLength = data.length
			super.dataProvider = data;
		}

		public function set searchInput(searchInput:SearchBox):void {
			_searchInput = searchInput;
			_searchInput.addEventListener(Event.CHANGE, doFiltering);
		}

        public function get searchText():String {
            return _searchText;
        }
        
		private function doFiltering(changeEvent:Event):void {
			_searchText = _searchInput.text;
			dataProvider.refresh();
			validateNow();
		}

		private function filter(item:Object):Boolean {
			return findMatchInColumns(item, _searchText);
		}

		private function findMatchInColumns(item:Object, searchText:String):Boolean {
			for each (var column:DataGridColumn in columns) {
				var value:String = item[column.dataField];
				if (getIndexOfMatch(value, searchText) != -1) {
					return true; //We have a match so whole row is in, can avoid matching the other columns
				}
			}
			return false;
		}

		public function getIndexOfMatch(value:String, searchText:String):int {
			var index:int = value.toLowerCase().indexOf(searchText.toLowerCase());
			return index;
		}
	}
}
package clarin.cmdi.componentregistry.common.components {
	import mx.controls.Label;

	public class HighlightSelectionItemRenderer extends Label {

		private static const OPEN_TAG:String = "<b>";
		private static const CLOSE_TAG:String = "</b>";


		public function HighlightSelectionItemRenderer() {
			super();
		}

		override public function set text(value:String):void {
			var highlight:Boolean = false;
			if (listData && listData.owner is FilteringDataGrid) {
				var _grid:FilteringDataGrid = listData.owner as FilteringDataGrid;
				var searchText:String = _grid.searchText;
				if (searchText != null && searchText.length > 0) {
					var index:int = _grid.getIndexOfMatch(value, searchText);
					if (index != -1) {
						value = highlightMatch(value, searchText.length, index);
						highlight = true;
					}
				}
			}
			if (highlight) {
				super.htmlText = value;
			} else {
				super.text = value;
			}
		}

		private function highlightMatch(value:String, len:int, index:int):String {
			return value.substr(0, index).concat(OPEN_TAG, value.substr(index, len), CLOSE_TAG, value.substr(index + len));
		}


	}
}


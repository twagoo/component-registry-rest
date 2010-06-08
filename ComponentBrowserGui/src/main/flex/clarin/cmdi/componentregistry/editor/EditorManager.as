package clarin.cmdi.componentregistry.editor {
	import clarin.cmdi.componentregistry.services.IsocatService;


	/**
	 * Manages static Editor components
	 *
	 **/
	public final class EditorManager {

		/**
		 * Specify isocatSearchType see IsocatService. default is null which means search in all types
		 **/
		public static function getIsocatSearchPopUp(isocatSearchType:String = null):IsocatSearchPopUp {
			var isocatSearchPopUp:IsocatSearchPopUp = new IsocatSearchPopUp();
			isocatSearchPopUp.searchService = new IsocatService();
			isocatSearchPopUp.isocatSearchType = isocatSearchType;
			return isocatSearchPopUp;
		}

	}
}
package clarin.cmdi.componentregistry.editor {
    import clarin.cmdi.componentregistry.services.IsocatService;
    

	/**
	 * Manages static Editor components
	 *
	 **/
	public final class EditorManager {

		private static var searchService:IsocatService = new IsocatService();

		public static function getIsocatSearchPopUp():IsocatSearchPopUp {
			var isocatSearchPopUp:IsocatSearchPopUp = new IsocatSearchPopUp();
			isocatSearchPopUp.searchService = searchService;
			return isocatSearchPopUp;
		}

	}
}
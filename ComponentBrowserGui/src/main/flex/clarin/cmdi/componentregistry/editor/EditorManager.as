package clarin.cmdi.componentregistry.editor {

	/**
	 * Manages static Editor components
	 *
	 **/
	public final class EditorManager {

		private static var _isocatSearchPopUp:IsocatSearchPopUp;

		public static function getIsocatSearchPopUp():IsocatSearchPopUp {
			if (!_isocatSearchPopUp) {
				_isocatSearchPopUp = new IsocatSearchPopUp();
			}
			return _isocatSearchPopUp;
		}

	}
}
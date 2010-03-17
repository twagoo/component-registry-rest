package clarin.cmdi.componentregistry.editor {

	/**
	 * Manages static Editor components
	 *
	 **/
	public final class EditorManager {

		private static var _isocatSearchPopUp:IsocatSearchPopUp;
		private static var _valueSchemePopUp:ValueSchemePopUp;

		public static function getIsocatSearchPopUp():IsocatSearchPopUp {
			if (!_isocatSearchPopUp) {
				_isocatSearchPopUp = new IsocatSearchPopUp();
			}
			return _isocatSearchPopUp;
		}

		public static function getValueSchemePopUp():ValueSchemePopUp {
			if (!_valueSchemePopUp) {
				_valueSchemePopUp = new ValueSchemePopUp();
			}
			return _valueSchemePopUp;
		}

	}
}
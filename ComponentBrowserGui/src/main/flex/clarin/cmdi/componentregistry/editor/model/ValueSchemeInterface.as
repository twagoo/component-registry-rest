package clarin.cmdi.componentregistry.editor.model {
	import mx.collections.ArrayCollection;

	public interface ValueSchemeInterface {
		function get valueSchemeSimple():String;
		function set valueSchemeSimple(valueSchemeSimple:String):void;

		/**
		 * ArrayCollection of ValueSchemeItem's
		 * */
		function get valueSchemeEnumeration():ArrayCollection;
		function set valueSchemeEnumeration(valueSchemeEnumeration:ArrayCollection):void;

		function get valueSchemePattern():String;
		function set valueSchemePattern(valueSchemePattern:String):void;
	}
}
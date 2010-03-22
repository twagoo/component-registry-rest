package clarin.cmdi.componentregistry.editor.model {
	import mx.collections.XMLListCollection;

	public interface ValueSchemeInterface {
		function get valueSchemeSimple():String;
		function set valueSchemeSimple(valueSchemeSimple:String):void;

		function get valueSchemeEnumeration():XMLListCollection;
		function set valueSchemeEnumeration(valueSchemeEnumeration:XMLListCollection):void;

		function get valueSchemePattern():String;
		function set valueSchemePattern(valueSchemePattern:String):void;
	}
}
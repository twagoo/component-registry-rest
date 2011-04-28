package clarin.cmdi.componentregistry.importer {
	import clarin.cmdi.componentregistry.common.ItemDescription;
	
	import flash.events.Event;
	import flash.net.FileReference;

	public class FileLoadedEvent extends Event {
		public static const FILE_LOADED:String = "fileLoaded";
		private var _fileReference:FileReference;

		public function FileLoadedEvent(fileReference:FileReference, bubbles:Boolean = false, cancelable:Boolean = false) {
			super(FILE_LOADED, bubbles, cancelable);
			_fileReference = fileReference;
		}

		public function get fileReference():FileReference {
			return _fileReference;
		}

		override public function clone():Event {
			return new FileLoadedEvent(_fileReference, bubbles, cancelable);
		}

	}
}
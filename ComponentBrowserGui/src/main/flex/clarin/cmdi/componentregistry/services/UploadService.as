package clarin.cmdi.componentregistry.services {
	import clarin.cmdi.componentregistry.common.ItemDescription;
	import clarin.cmdi.componentregistry.importer.UploadCompleteEvent;

	import com.adobe.net.URI;

	import flash.events.Event;
	import flash.events.HTTPStatusEvent;
	import flash.events.IOErrorEvent;
	import flash.events.ProgressEvent;
	import flash.events.SecurityErrorEvent;
	import flash.net.FileFilter;
	import flash.net.FileReference;
	import flash.net.URLVariables;
	import flash.utils.ByteArray;

	import mx.controls.ProgressBar;
	import mx.managers.CursorManager;

	import ru.inspirit.net.MultipartURLLoader;

	[Event(name="uploadComplete", type="clarin.cmdi.componentregistry.importer.UploadCompleteEvent")]
	public class UploadService {

		public function UploadService() {
		}

		[Bindable]
		public var selectedFile:String = "";
		[Bindable]
		public var message:String = "";

		private var fileRef:FileReference;
		private var pb:ProgressBar;
		private var ml:MultipartURLLoader;

		public function init(progressBar:ProgressBar):void {
			pb = progressBar;
		}

		private function createAndInitRequest():void {
			CursorManager.setBusyCursor();
			ml = new MultipartURLLoader();
			ml.addEventListener(Event.COMPLETE, completeHandler);
			ml.addEventListener(ProgressEvent.PROGRESS, progressHandler);
			ml.addEventListener(IOErrorEvent.IO_ERROR, ioErrorHandler);
			ml.addEventListener(HTTPStatusEvent.HTTP_STATUS, httpStatusHandler);
			ml.addEventListener(SecurityErrorEvent.SECURITY_ERROR, securityErrorHandler);
		}

		private function createAndInitFileReference():void {
			fileRef = new FileReference();
			fileRef.addEventListener(Event.SELECT, selectHandler);
			//no uploads are done through FileRef only loads so this UPLOAD_COMPLETE_DATA event will not be thrown
			//fileRef.addEventListener(DataEvent.UPLOAD_COMPLETE_DATA, responseHandler);
			fileRef.addEventListener(ProgressEvent.PROGRESS, progressHandler);
			fileRef.addEventListener(SecurityErrorEvent.SECURITY_ERROR, securityErrorHandler);
			fileRef.addEventListener(IOErrorEvent.IO_ERROR, ioErrorHandler);
		}

		/**
		 * submits a profile, data parameter can be null which implies a file was selected using selectXmlFile();
		 */
		public function submitProfile(description:ItemDescription, data:String = null):void {
			createAndInitRequest();
			submit(description, createByteArray(data), new URI(Config.instance.uploadProfileUrl));
		}

		/**
		 * submits a component, data parameter can be null which implies a file was selected using selectXmlFile();
		 */
		public function submitComponent(description:ItemDescription, data:String = null):void {
			createAndInitRequest();
			submit(description, createByteArray(data), new URI(Config.instance.uploadComponentUrl));
		}

		private function createByteArray(data:String):ByteArray {
			var result:ByteArray = null;
			if (data != null) {
				result = new ByteArray();
				result.writeUTFBytes(data);
				result.position = 0;
			}
			return result
		}

		private function submit(description:ItemDescription, data:ByteArray, uri:URI):void {
			message = "";
			try {
				if (data != null) {
					var params:URLVariables = new URLVariables();
					ml.addVariable("description", description.description);
					ml.addVariable("name", description.name);
					ml.addVariable("group", description.groupName);
					ml.addFile(data, description.name + ".xml", "data");
					ml.load(uri.toString());
				} else {
					// Cannot sent data that is not a file with FileReference.upload so just load the data and then submit through HttpClient.
					// FileReference also does not handle authentication tokens.
					startUploadHandler();
					if (fileRef.data == null) {
						//only load if not loaded before otherwise sent the already loaded file. You can only force a reload of the file by selecting it again (it is a flash thingy).
						fileRef.addEventListener(Event.COMPLETE, function(event:Event):void {
								submit(description, fileRef.data, uri);
							});
						fileRef.load();
					} else {
						submit(description, fileRef.data, uri);
					}
				}
			} catch (error:Error) {
				trace("Unable to upload file. Error: " + error);
				CursorManager.removeBusyCursor();
				throw error;
			}
		}

		public function selectXmlFile(event:Event):void {
			createAndInitFileReference();
			var filter:FileFilter = new FileFilter("Xml Files (*.xml)", "*.xml");
			fileRef.browse(new Array(filter));
		}

		private function selectHandler(event:Event):void {
			selectedFile = fileRef.name;
			message = "";
			pb.visible = false;
		}

		private function completeHandler(event:Event):void {
			uploadComplete();
			var response:XML = new XML(ml.loader.data);
			handleResponse(response);
		}

		private function httpStatusHandler(event:HTTPStatusEvent):void {
			if (event.status != 200) {
				addToMessage("Server Failed to handle registration. Unexpected error, try again later. (httpstatus code was: " + event.status + ")\n");
			}
			CursorManager.removeBusyCursor();
		}

		private function securityErrorHandler(event:SecurityErrorEvent):void {
			addToMessage("Server Failed to handle registration. Unexpected error, try again later. (error: " + event.text + ")\n");
			CursorManager.removeBusyCursor();
		}

		private function ioErrorHandler(event:IOErrorEvent):void {
			addToMessage("Unable to load file. (error: " + event.text + ")\n");
			CursorManager.removeBusyCursor();
		}

		private function progressHandler(event:ProgressEvent):void {
			trace("Uploading: " + event.bytesLoaded + "/" + event.bytesTotal);
			pb.setProgress(event.bytesLoaded, event.bytesTotal);
		}

		private function startUploadHandler():void {
			trace("uploading start");
			pb.label = "uploading %3%%";
			pb.visible = true;
			pb.includeInLayout = true;
		}

		private function handleResponse(response:XML):void {
			if (response.@registered == true) {
				var item:ItemDescription = new ItemDescription();
				if (response.@isProfile == true) {
					item.createProfile(response.description[0]);
				} else {
					item.createComponent(response.description[0]);
				}
				dispatchEvent(new UploadCompleteEvent(item));
			} else {
				createErrorMessage(response);
			}
		}

		private function uploadComplete():void {
			pb.label = "Upload complete";
			pb.setProgress(100, 100);
			CursorManager.removeBusyCursor();
		}

		private function createErrorMessage(response:XML):void {
			addToMessage("Failed to register:");
			var errors:XMLList = response.errors.error;
			for each (var error:XML in errors) {
				message += " - " + error.toString() + "\n";
			}
		}

		private function addToMessage(text:String):void {
			message += text;
		}

	}
}
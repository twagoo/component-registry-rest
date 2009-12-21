package clarin.cmdi.componentregistry.services {
	import clarin.cmdi.componentregistry.common.ItemDescription;
	import clarin.cmdi.componentregistry.register.UploadCompleteEvent;

	import flash.events.DataEvent;
	import flash.events.Event;
	import flash.events.HTTPStatusEvent;
	import flash.events.ProgressEvent;
	import flash.net.FileFilter;
	import flash.net.FileReference;
	import flash.net.URLRequest;
	import flash.net.URLRequestMethod;
	import flash.net.URLVariables;

	import mx.controls.ProgressBar;
	import mx.core.UIComponent;

	[Event(name="uploadComplete", type="clarin.cmdi.componentregistry.register.UploadCompleteEvent")]
	public class UploadService  {

		public function UploadService() {
		}

		[Bindable]
		public var selectedFile:String = "";
		[Bindable]
		public var message:String = "";

		private var fileRef:FileReference;
		private var request:URLRequest;
		private var pb:ProgressBar;

		public function init(progressBar:ProgressBar):void {
			pb = progressBar;
			fileRef = new FileReference();
			fileRef.addEventListener(Event.SELECT, selectHandler);
			fileRef.addEventListener(HTTPStatusEvent.HTTP_STATUS, errorHandler);
			fileRef.addEventListener(DataEvent.UPLOAD_COMPLETE_DATA, responseHandler);
			fileRef.addEventListener(ProgressEvent.PROGRESS, progressHandler);
			fileRef.addEventListener(Event.OPEN, startUploadHandler);
		}

		public function submitProfile(description:ItemDescription):void {
			request = new URLRequest(Config.instance.uploadProfileUrl);
			var params:URLVariables = new URLVariables();
			submit(description, params);
		}

		public function submitComponent(description:ItemDescription):void {
			request = new URLRequest(Config.instance.uploadComponentUrl);
			var params:URLVariables = new URLVariables();
			params.group = description.groupName;
			submit(description, params);
		}

		public function submit(description:ItemDescription, params:URLVariables):void {
			message = "";
			try {
				request.method = URLRequestMethod.POST;
				params.creatorName = description.creatorName;
				params.description = description.description;
				params.name = description.name;
				request.data = params;
				fileRef.upload(request, "data");
			} catch (error:Error) {
				trace("Unable to upload file.");
			}
		}

		public function selectXmlFile(event:Event):void {
			var filter:FileFilter = new FileFilter("Xml Files (*.xml)", "*.xml");
			fileRef.browse(new Array(filter));
		}

		private function selectHandler(event:Event):void {
			selectedFile = fileRef.name;
			message = "";
			pb.visible=false;
		}

		private function errorHandler(event:HTTPStatusEvent):void {
			message = "Server Failed to handle registration. Unexpected error, try again later. (httpstatus code was: " + event.status + ")";
		}

		private function progressHandler(event:ProgressEvent):void {
			trace("Uploading: " + event.bytesLoaded + "/" + event.bytesTotal);
			pb.setProgress(event.bytesLoaded, event.bytesTotal);
		}

		private function startUploadHandler(event:Event):void {
			trace("uploading start");
			pb.label = "uploading %3%%";
			pb.visible=true;
		}


		private function responseHandler(event:DataEvent):void {
			pb.label = "Upload complete";
			var response:XML = new XML(event.data);
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

		private function createErrorMessage(response:XML):void {
			message = "Failed to register:";
			var errors:XMLList = response.errors.error;
			for each (var error:XML in errors) {
				message += " - " + error.toString() + "\n";
			}
		}
	}
}
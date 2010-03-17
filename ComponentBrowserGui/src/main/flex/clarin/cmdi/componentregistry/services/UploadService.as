package clarin.cmdi.componentregistry.services {
	import clarin.cmdi.componentregistry.common.ItemDescription;
	import clarin.cmdi.componentregistry.importer.UploadCompleteEvent;
	
	import com.adobe.net.URI;
	import com.hurlant.util.Base64;
	
	import flash.events.DataEvent;
	import flash.events.ErrorEvent;
	import flash.events.Event;
	import flash.events.IOErrorEvent;
	import flash.events.ProgressEvent;
	import flash.events.SecurityErrorEvent;
	import flash.net.FileFilter;
	import flash.net.FileReference;
	import flash.net.URLRequest;
	import flash.net.URLVariables;
	
	import mx.controls.ProgressBar;
	
	import org.httpclient.HttpClient;
	import org.httpclient.events.HttpDataEvent;
	import org.httpclient.events.HttpResponseEvent;
	import org.httpclient.http.Post;
	import org.httpclient.http.multipart.Multipart;
	import org.httpclient.http.multipart.Part;

	[Event(name="uploadComplete", type="clarin.cmdi.componentregistry.importer.UploadCompleteEvent")]
	public class UploadService {

		public function UploadService() {
		}

		[Bindable]
		public var selectedFile:String = "";
		[Bindable]
		public var message:String = "";

		private var fileRef:FileReference;
		private var httpClient:HttpClient;
		private var request:URLRequest;
		private var pb:ProgressBar;

		public function init(progressBar:ProgressBar):void {
			pb = progressBar;
		}

		private function createAndInitRequest(url:String):void {
			request = new URLRequest(url);
			httpClient = new HttpClient();
			httpClient.listener.onError = httpclientErrorHandler;
			httpClient.listener.onData = httpclientDataHandler; 
			httpClient.listener.onComplete = httpclientCompleteHandler;
		}

		private function createAndInitFileReference():void {
			fileRef = new FileReference();
			fileRef.addEventListener(Event.SELECT, selectHandler);
			fileRef.addEventListener(DataEvent.UPLOAD_COMPLETE_DATA, responseHandler);
			fileRef.addEventListener(ProgressEvent.PROGRESS, progressHandler);
			fileRef.addEventListener(SecurityErrorEvent.SECURITY_ERROR, securityErrorHandler);
			fileRef.addEventListener(IOErrorEvent.IO_ERROR, ioErrorHandler);
		}

		/**
		 * submits a profile, data parameter can be null which implies a file was selected using selectXmlFile();
		 */
		public function submitProfile(description:ItemDescription, data:String = null):void {
			createAndInitRequest(Config.instance.uploadProfileUrl);
			var params:URLVariables = new URLVariables();
			submit(description, params, data);
		}

		/**
		 * submits a component, data parameter can be null which implies a file was selected using selectXmlFile();
		 */
		public function submitComponent(description:ItemDescription, data:String = null):void {
			createAndInitRequest(Config.instance.uploadComponentUrl);
			var params:URLVariables = new URLVariables();
			params.group = description.groupName;
			submit(description, params, data);
		}

		private function getCredentials():String {
			return Base64.encode("tomcat:tomcat");
		}

		private function submit(description:ItemDescription, params:URLVariables, data:String = null):void {
			message = "";
			try {
				if (data != null) {
					var parts:Array = new Array();
					parts.push(new Part("description", description.description));
					parts.push(new Part("name", description.name));
					parts.push(new Part("data", data, "application/octet-stream", [{name: "filename", value: description.name + ".xml"}]));

					var multipart:Multipart = new Multipart(parts);
					var uri:URI = new URI(Config.instance.uploadProfileUrl);
					startUploadHandler();
					var post:Post = new Post();
					post.addHeader("Authorization", "BASIC " + getCredentials());
					post.setMultipart(multipart);
					httpClient.request(uri, post);
				} else {
					// Cannot sent credentials with FileReference.upload so just load the data and then submit through HttpClient
					startUploadHandler();
					if (fileRef.data == null) {
						//only load if not loaded before otherwise sent the already loaded file. You can only force a reload of the file by selecting it again (it is a flash thingy).
						fileRef.addEventListener(Event.COMPLETE, function(event:Event):void {
								submit(description, params, new String(fileRef.data));
							});
						fileRef.load();
					} else {
						submit(description, params, new String(fileRef.data));
					}
				}
			} catch (error:Error) {
				trace("Unable to upload file. Error: " + error);
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

		private function httpclientCompleteHandler(event:HttpResponseEvent):void {
			if (!event.response.isSuccess) {
				addToMessage("Server Failed to handle registration. Unexpected error, try again later. (httpstatus code was: " + event.response.code + ")\n");
			}
            httpClient.close();
		}

		private function httpclientDataHandler(event:HttpDataEvent):void {
			uploadComplete();
			var response:XML = new XML(event.bytes);
			handleResponse(response);
		}

		private function httpclientErrorHandler(event:ErrorEvent):void {
			addToMessage("Server Failed to handle registration. Unexpected error, try again later. (error: " + event.text + ")\n");
		}

		private function securityErrorHandler(event:SecurityErrorEvent):void {
			addToMessage("Server Failed to handle registration. Unexpected error, try again later. (error: " + event.text + ")\n");
		}

		private function ioErrorHandler(event:IOErrorEvent):void {
			addToMessage("Unable to load file. (error: " + event.text + ")\n");
		}

		private function progressHandler(event:ProgressEvent):void {
			trace("Uploading: " + event.bytesLoaded + "/" + event.bytesTotal);
			pb.setProgress(event.bytesLoaded, event.bytesTotal);
		}


		private function responseHandler(event:DataEvent):void {
			uploadComplete();
			var response:XML = new XML(event.data);
			handleResponse(response);
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
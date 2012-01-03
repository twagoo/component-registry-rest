package clarin.cmdi.componentregistry.common {

	public class Credentials {
		private static var _instance:Credentials = new Credentials();

		[Bindable]
		public var userName:String = "";

		public function Credentials() {
			if (_instance != null) {
				throw new Error("Config can only be accessed through Credentials.instance");
			}
		}
		
        public function isLoggedIn():Boolean {
            return userName && userName != "" && userName != "anonymous";
        }

		private function init(applicationParameters:Object):void {
			if (applicationParameters.hasOwnProperty("userName")) {
				userName = applicationParameters.userName;
			}
		}

		public static function create(applicationParameters:Object):void {
			_instance.init(applicationParameters);
		}


		public static function get instance():Credentials {
			return _instance;
		}
	}
}
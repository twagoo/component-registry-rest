package clarin.cmdi.componentregistry.common
{
	import clarin.cmdi.componentregistry.services.Config;

/**
 * Models a group. Doesn't implement xmlable because groups are not sent back to the server
 * @author george.georgovassilis@mpi.nl
 */	
	public class Group
	{
		[Bindable]
		public var id:String;
		
		[Bindable]
		public var name:String;
		
		[Bindable]
		public var ownerId:uint;
		
		public function Group():void{
			
		}
		
		public function create(xml:XML):void{
			this.id = xml.id;
			this.name = xml.name;
			this.ownerId = xml.ownerId;
		}
		
	}
}
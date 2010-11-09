package clarin.cmdi.componentregistry.editor
{
    public class ValueSchemeItem
    {
        
        public var item:String;
        public var appInfo:String;
        public var conceptLink:String;
        
    
        public function ValueSchemeItem(item:String, appInfo:String, conceptLink:String)
        {
            this.item = item;
            this.appInfo = appInfo;
            this.conceptLink = conceptLink;
        }

    }
}
package clarin.cmdi.componentregistry.common
{
    import clarin.cmdi.componentregistry.editor.model.CMDSpec;
    
    public interface CMDSpecRenderer
    {
        
        function set cmdSpec(cmdSpec:CMDSpec):void;
        function get cmdSpec():CMDSpec;
        
    }
}
package clarin.cmdi.componentregistry.frontend;

public interface DisplayNode {

    public String getContent();

    public boolean hasContent();

    public boolean isDeleted();

    public boolean isEditable();

    public boolean isUserNode();

    public String getId();

}
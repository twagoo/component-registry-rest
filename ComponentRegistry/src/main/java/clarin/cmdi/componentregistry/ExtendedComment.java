/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.model.Comment;

/**
 *
 * @author olhsha
 */
public class ExtendedComment{
    
    private Comment com;
    
    private String href;
    
    public String getHref(){
        return href;
    }
    
    public void setHref(String href){
        this.href = href;
    }
    
    public Comment getCom(){
        return com;
    }
    
    public void setCom(Comment com){
        this.com = com;
    }
    
}

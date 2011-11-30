/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.model.Comment;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jeafer
 */
class CommentValidator implements Validator{
    private final Comment com;
    private List<String> errorMessages = new ArrayList<String>();

    public CommentValidator(Comment com) {
        this.com = com;
    }
    
    @Override
    public List<String> getErrorMessages() {
        return errorMessages;
    }

    @Override
    public boolean validate() {
        if (!isOk(com.getUserId(), com.getComment(), com.getCommentDate())) {
            errorMessages.add("Fields are not filled in correctly");
        }
        return errorMessages.isEmpty();
    }
    
    private boolean isOk(String... fields) {
        boolean isOk = true;
        for (String field : fields) {
            isOk &= StringUtils.isNotBlank(field);
        }
        return isOk;
    }
    
}

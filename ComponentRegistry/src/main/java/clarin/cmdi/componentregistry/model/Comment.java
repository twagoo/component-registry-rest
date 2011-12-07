package clarin.cmdi.componentregistry.model;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.IdSequence;
import java.text.ParseException;
import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;

/**
 *
 * @author jean-charles Ferrières <jean-charles.ferrieres@mpi.nl>
 */
@XmlRootElement(name = "comment")
@XmlAccessorType(XmlAccessType.FIELD)
public class Comment {

    private String comments;
    private String commentDate;
    private String componentDescriptionId;
    private String profileDescriptionId;
    private String userId;
    private String id;

    public void setComment(String comment) {
        this.comments = comment;
    }

    public String getComment() {
        return comments;
    }

    public void setCommentDate(String commentDate) {
        this.commentDate = commentDate;
    }

    public String getCommentDate() {
        return commentDate;
    }

    public void setId(String commentId) {
        this.id = commentId;
    }

    public String getId() {
        return id;
    }

    public String getComponentDescriptionId() {
        return componentDescriptionId;
    }

    public void setComponentDescriptionId(String ComponentDescriptionId) {
        this.componentDescriptionId = ComponentDescriptionId;
    }

    public void setProfileDescriptionId(String profileDescriptionId) {
        this.profileDescriptionId = profileDescriptionId;
    }

    public String getProfileDescriptionId() {
        return profileDescriptionId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public static Date getDate(String registrationDate) throws ParseException {
        return DateUtils.parseDate(registrationDate, new String[]{DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern()});
    }

    /*
     * Helper method to set the Date
     */
    public static String createNewDate() {
        return createNewDate(new Date().getTime());
    }
    
    /*
     * Helper method to set the Date in the same format
     * @param time, long that contains the time to be set 
     */

    public static String createNewDate(long time) {
        return DateFormatUtils.formatUTC(time, DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern());
    }

    public static Comment createANewComment() {
        String Id = ComponentRegistry.REGISTRY_ID + IdSequence.get();
        Comment com = new Comment();
        com.setId(Id);
        com.setCommentDate(createNewDate());
        return com;
    }
}

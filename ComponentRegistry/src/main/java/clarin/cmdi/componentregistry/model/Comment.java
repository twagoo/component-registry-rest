package clarin.cmdi.componentregistry.model;

import clarin.cmdi.componentregistry.DatesHelper;

import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;

/**
 *
 * @author Jean-Charles Ferrières <jean-charles.ferrieres@mpi.nl>
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
@XmlRootElement(name = "comment")
@XmlAccessorType(XmlAccessType.FIELD)
public class Comment {

    private String comments;
    private String commentDate;
    private String componentId;
    private String id;
    private String userName;
    private boolean canDelete;
    @XmlTransient // this prevents userId from being serialized to XML and thus exposed (which is useless and undesirable)
    private String userId;

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

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentDescriptionId) {
        this.componentId = componentDescriptionId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    /**
     *
     * @return userName, that is the user's 'real' name, not login name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName the user's 'real' name, not login name
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return whether comment can be deleted
     */
    public boolean isCanDelete() {
        return canDelete;
    }

    /**
     * @param canDelete whether comment can be deleted
     */
    public void setCanDelete(boolean canDelete) {
        this.canDelete = canDelete;
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
        Comment com = new Comment();
        com.setCommentDate(createNewDate());
        return com;
    }
    /**
     * Compares two comments by their value as returned by {@link Comment#getCommentDate()
     * }
     */
    public static final Comparator<Comment> COMPARE_ON_DATE = new Comparator<Comment>() {
        /**
         * @return 1 if o11 is older than o2, returns -1 if o1 is younger than
         * o2
         */
        @Override
        public int compare(Comment o1, Comment o2) {

            return (DatesHelper.compareDateStrings(o1.getCommentDate(), o2.getCommentDate()));
        }
    };
}

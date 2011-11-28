/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clarin.cmdi.componentregistry.model;

import java.text.ParseException;
import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;

/**
 *
 * @author jeafer
 */
public class CommentMapping {

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Comment {

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

        public void setUserID(String userId) {
            this.userId = userId;
        }


        public String getUserId() {
            return userId;
        }

        public static String createNewDate() {
            return createNewDate(new Date().getTime());
        }
        
            public static Date getDate(String registrationDate) throws ParseException {
        return DateUtils.parseDate(registrationDate, new String[] { DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern() });
    }

        public static String createNewDate(long time) {
            return DateFormatUtils.formatUTC(time, DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern());
        }
    }
}
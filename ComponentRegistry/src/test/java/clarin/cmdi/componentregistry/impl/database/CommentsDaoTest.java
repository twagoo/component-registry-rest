package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.model.Comment;
import clarin.cmdi.componentregistry.model.Comment;
import java.util.List;
import org.junit.runner.RunWith;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import org.junit.Before;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author jeafer
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/applicationContext.xml"})
public class CommentsDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private CommentsDao commentsDao;

    @Before
    public void init() {
        ComponentRegistryTestDatabase.resetDatabase(jdbcTemplate);
        ComponentRegistryTestDatabase.createTableComments(jdbcTemplate);
    }

    @Test
    public void testInjection() {
        assertNotNull(jdbcTemplate);
        assertNotNull(commentsDao);
    }

    @Test
    public void testInsertComment() {
        Comment testComment = createTestComment();



        String regDate = Comment.createNewDate();

        //testComment.setId(TEST_COMMENT_ID);
        testComment.setComment("test");
        //testComment.setUserID("1");
        //testComment.setComponentDescriptionId("A component1");
        //testComment.setProfileDescriptionId("A profile1");
        testComment.setCommentDate(regDate);

        assertEquals(0, commentsDao.getAllComments().size());
        Number newId = commentsDao.insertComment(testComment);
        assertNotNull(newId);

        List<Comment> comments = commentsDao.getAllComments();
        assertEquals(1, comments.size());

        assertEquals(TEST_COMMENT_NAME, comments.get(0).getComment());
       // assertEquals("A component1", comments.get(0).getComponentDescriptionId());
        //assertEquals("A profile1", comments.get(0).getProfileDescriptionId());
        assertEquals(regDate, comments.get(0).getCommentDate());
       // assertEquals("1", comments.get(0).getUserId());
       // assertEquals(TEST_COMMENT_ID, comments.get(0).getId());
    }
    
    @Test
    public void testGetCommentsFromProfile() {
        List<Comment> descriptions = commentsDao.getCommentsFromProfile(TEST_COMMENT_PROFILE_ID);
	assertNotNull(descriptions);
    }
    
    @Test
    public void testGetCommentFromComponent(){
        List<Comment> descriptions = commentsDao.getCommentsFromComponent(TEST_COMMENT_COMPONENT_ID);
	assertNotNull(descriptions);
    }

    @Test
    public void testGetAllComments() {
        assertEquals(0, commentsDao.getAllComments().size());
    }

    @Test
    public void testGetComment() {
        Comment testComment = createTestComment();
        commentsDao.insertComment(testComment);

        assertNotNull(commentsDao.getByComment(TEST_COMMENT_NAME));
        assertNull(commentsDao.getByComment("NOT_EXITING_COMMENT_NAME"));
    }

    public static Comment createTestComment() {
        Comment testComment = new Comment();
        testComment.setComment(TEST_COMMENT_NAME);
        testComment.setCommentDate(TEST_COMMENT_DATE);
        testComment.setId(TEST_COMMENT_ID);
        //testComment.setComponentDescriptionId(TEST_COMMENT_COMPONENT_ID);
        testComment.setProfileDescriptionId(TEST_COMMENT_PROFILE_ID);
        testComment.setUserId(TEST_COMMENT_USER_ID);
        return testComment;
    }
    
    public void testSetDelete(){
        Comment testComment = createTestComment();
        int count = commentsDao.getAllComments().size();
        
        commentsDao.insertComment(testComment);
	assertEquals(count + 1, commentsDao.getAllComments().size());

        commentsDao.deleteComment(testComment, true);
        assertEquals(count, commentsDao.getAllComments().size());
        
        assertNull(commentsDao.getByComment("NOT_EXISTING_COMMENT_NAME"));
    }
    
    public final static String TEST_COMMENT_ID = "1";
    public final static String TEST_COMMENT_PROFILE_ID = "clarin.eu:cr1:p_1297242111880";
    public final static String TEST_COMMENT_COMPONENT_ID = "clarin.eu:cr1:c_1290431694600";
    public final static String TEST_COMMENT_NAME = "test";
    public final static String TEST_COMMENT_USER_ID = "8";
    public final static String TEST_COMMENT_DATE = Comment.createNewDate();
}

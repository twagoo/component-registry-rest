package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.BaseUnitTest;
import clarin.cmdi.componentregistry.model.Comment;
import clarin.cmdi.componentregistry.persistence.CommentsDao;

import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * @author jean-charles Ferrières <jean-charles.ferrieres@mpi.nl>
 * @author George.Georgovassilis@mpi.nl
 */
public class CommentsDaoTest extends BaseUnitTest{

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
    public void testInsertProfileComment() {
	Comment comment = createTestComment();
	comment.setProfileDescriptionId(TEST_COMMENT_PROFILE_ID);

	assertEquals(0, commentsDao.getAllComments().size());
	Number newId = commentsDao.insertComment(comment, TEST_COMMENT_USER_ID);
	assertNotNull(newId);

	List<Comment> comments = commentsDao.getAllComments();
	assertNotNull(comments);
	assertEquals(1, comments.size());

	assertEquals(TEST_COMMENT_NAME, comments.get(0).getComment());
	assertEquals(TEST_COMMENT_PROFILE_ID, comments.get(0).getProfileDescriptionId());
	assertEquals(TEST_COMMENT_DATE, comments.get(0).getCommentDate());
	assertEquals(TEST_COMMENT_USER_NAME, comments.get(0).getUserName());
	assertEquals(Integer.toString(TEST_COMMENT_USER_ID), comments.get(0).getUserId());
    }

    @Test
    public void testInsertComponentComment() {
	Comment comment = createTestComment();
	comment.setComponentDescriptionId(TEST_COMMENT_COMPONENT_ID);

	assertEquals(0, commentsDao.getAllComments().size());
	Number newId = commentsDao.insertComment(comment, TEST_COMMENT_USER_ID);
	assertNotNull(newId);

	List<Comment> comments = commentsDao.getAllComments();
	assertNotNull(comments);
	assertEquals(1, comments.size());

	assertEquals(TEST_COMMENT_NAME, comments.get(0).getComment());
	assertEquals(TEST_COMMENT_COMPONENT_ID, comments.get(0).getComponentDescriptionId());
	assertEquals(TEST_COMMENT_DATE, comments.get(0).getCommentDate());
	assertEquals(TEST_COMMENT_USER_NAME, comments.get(0).getUserName());
	assertEquals(Integer.toString(TEST_COMMENT_USER_ID), comments.get(0).getUserId());
    }

    @Test
    public void testGetById() {
	Comment comment1 = createTestComment();
	comment1.setProfileDescriptionId(TEST_COMMENT_PROFILE_ID);
	Number commentId = commentsDao.insertComment(comment1, TEST_COMMENT_USER_ID);
	Comment commentById = commentsDao.getById(commentId);
	assertEquals(TEST_COMMENT_PROFILE_ID, commentById.getProfileDescriptionId());
	assertEquals(TEST_COMMENT_NAME, commentById.getComment());
    }

    @Test
    public void testGetCommentsFromProfile() {
	List<Comment> descriptions = commentsDao.getCommentsFromProfile(TEST_COMMENT_PROFILE_ID);
	assertNotNull(descriptions);

	int size = descriptions.size();
	Comment comment1 = createTestComment();
	comment1.setProfileDescriptionId(TEST_COMMENT_PROFILE_ID);
	commentsDao.insertComment(comment1, TEST_COMMENT_USER_ID);

	descriptions = commentsDao.getCommentsFromProfile(TEST_COMMENT_PROFILE_ID);
	assertEquals(size + 1, descriptions.size());

	Comment comment2 = createTestComment();
	comment2.setProfileDescriptionId(TEST_COMMENT_PROFILE_ID);
	commentsDao.insertComment(comment2, TEST_COMMENT_USER_ID);

	descriptions = commentsDao.getCommentsFromProfile(TEST_COMMENT_PROFILE_ID);
	assertEquals(size + 2, descriptions.size());
    }

    @Test
    public void testGetCommentsFromComponent() {
	List<Comment> descriptions = commentsDao.getCommentsFromComponent(TEST_COMMENT_COMPONENT_ID);
	assertNotNull(descriptions);

	int size = descriptions.size();
	Comment comment1 = createTestComment();
	comment1.setComponentDescriptionId(TEST_COMMENT_COMPONENT_ID);
	commentsDao.insertComment(comment1, TEST_COMMENT_USER_ID);

	descriptions = commentsDao.getCommentsFromComponent(TEST_COMMENT_COMPONENT_ID);
	assertEquals(size + 1, descriptions.size());

	Comment comment2 = createTestComment();
	comment2.setComponentDescriptionId(TEST_COMMENT_COMPONENT_ID);
	commentsDao.insertComment(comment2, TEST_COMMENT_USER_ID);

	descriptions = commentsDao.getCommentsFromComponent(TEST_COMMENT_COMPONENT_ID);
	assertEquals(size + 2, descriptions.size());
    }

    @Test
    public void testGetAllComments() {
	assertEquals(0, commentsDao.getAllComments().size());
    }

    @Test
    public void testGetComment() {
	Comment comment = createTestComment();
	commentsDao.insertComment(comment, 8);

	assertNotNull(commentsDao.getByComment(TEST_COMMENT_NAME));
	assertNull(commentsDao.getByComment("NON_EXISTENT_COMMENT_NAME"));
    }

    @Test
    public void testSetDelete() {
	Comment comment = createTestComment();
	int count = commentsDao.getAllComments().size();

	Number id = commentsDao.insertComment(comment, 8);
	assertNotNull(id);
	assertEquals(count + 1, commentsDao.getAllComments().size());
	assertNotNull(commentsDao.getByComment(comment.getComment()));

	comment.setId(id.toString());
	commentsDao.deleteComment(comment);
	assertEquals(count, commentsDao.getAllComments().size());

	assertNull(commentsDao.getByComment(comment.getComment()));
    }

    public static Comment createTestComment() {
	Comment testComment = new Comment();
	testComment.setComment(TEST_COMMENT_NAME);
	testComment.setCommentDate(TEST_COMMENT_DATE);
	testComment.setUserId(Integer.toString(TEST_COMMENT_USER_ID));
	testComment.setUserName(TEST_COMMENT_USER_NAME);
	return testComment;
    }
    public final static String TEST_COMMENT_ID = "1";
    public final static String TEST_COMMENT_PROFILE_ID = "clarin.eu:cr1:p_1297242111880";
    public final static String TEST_COMMENT_COMPONENT_ID = "clarin.eu:cr1:c_1290431694600";
    public final static String TEST_COMMENT_NAME = "test";
    public final static int TEST_COMMENT_USER_ID = 8;
    public final static String TEST_COMMENT_USER_NAME = "J. Unit";
    public final static String TEST_COMMENT_DATE = Comment.createNewDate();
}

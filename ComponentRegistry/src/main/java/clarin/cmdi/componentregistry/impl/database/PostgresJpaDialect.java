package clarin.cmdi.componentregistry.impl.database;

import java.sql.SQLException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Changing default transaction isolation level from read committed to serializable. 
 * Some background: When accessing the application the first time with a user that doesn't exist in the database I was frequently getting an exception about a unique constraint violation
 * while trying to insert into the database. The reason was that the browser was accessing components and profiles, which in the backend got the current user from tomcat and 
 * then tried to look it up in the database. If the user didn't exist, it would insert it. Those requests came in in parallel, spawning two simultaneous transactions. Because
 * of the default postgres isolation level (read committed) the second transaction would not (yet) see the user created by the first one and then try to insert it again, which
 * stumbled over the unique key constraints. Imposing a stricter isolation level solves this.
 * @author george.georgovassilis@mpi.nl
 *
 */
public class PostgresJpaDialect extends HibernateJpaDialect{

    @Override
    public Object beginTransaction(EntityManager entityManager,
            TransactionDefinition definition) throws PersistenceException,
            SQLException, TransactionException {
	DefaultTransactionDefinition dtd = new DefaultTransactionDefinition(definition);
	dtd.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
	return super.beginTransaction(entityManager, definition);
    }
    
}

package com.hibernate.envers;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import com.hibernate.envers.domain.Author;
import com.hibernate.envers.domain.Book;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditQuery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class ReAddingTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReAddingTest.class);
  @Autowired
  private EntityManager entityManager;

  @Before
  public void setUp() throws Exception {
    runTransactional(this::createAuthorWithTwoBooks);
  }

  @Test
  public void whenReAddingBooks_shouldRetrieveThemInRevision() throws Exception {
    // given
    Author author = retrieveAuthor();

    // when
    // we remove first and second book, and add a new instances of first, second and third book
    // this results in 2 books being removed from the database and 3 added.
    runTransactional(() -> removeAllBooksAndReAddOriginalTwoAndOneAdditional(author));

    // then
    runTransactional(() -> {
      AuditReader auditReader = AuditReaderFactory.get(entityManager);
      List<Number> revisions = auditReader.getRevisions(Author.class, author.getId());
      printRevisions(revisions);

      // determine the latest revision.
      Number latestRevisionNumber = revisions.get(revisions.size() - 1);

      // the current object should have 3 books.
      Author finalAuthor = retrieveAuthor();
      assertThat(finalAuthor).isNotNull();
      assertThat(finalAuthor.getBooks()).hasSize(3);

      // it should contain 3 books : First, Second and third, where first and second where deleted and re-added
      // in one transaction.
      Author latestAuthorRevision = fetchAuthorAtRevision(latestRevisionNumber);
      assertThat(latestAuthorRevision).isNotNull();
      assertThat(latestAuthorRevision.getBooks()).hasSize(3);
    });
  }

  private void runTransactional(Runnable runnable) {
    if (!TestTransaction.isActive()) {
      TestTransaction.start();
    }
    TestTransaction.flagForCommit();

    runnable.run();

    TestTransaction.end();
  }

  private void printRevisions(List<Number> revisions) {
    revisions.forEach(r -> {
      AuditQuery q = AuditReaderFactory.get(entityManager)
          .createQuery().forEntitiesAtRevision(Author.class, r);
      LOGGER.info("Revision[{}]: {}", r, q.getSingleResult());
    });
  }

  private void removeAllBooksAndReAddOriginalTwoAndOneAdditional(Author author) {
    author.removeAllBooks();
    author.addBook(createBook1());
    author.addBook(createBook2());
    author.addBook(createBook3());

    entityManager.merge(author);
  }

  private void createAuthorWithTwoBooks() {
    Author author = new Author();
    author.setFirstName("TestFirstName");
    author.setLastName("lastName");
    author.addBook(createBook1());
    author.addBook(createBook2());

    entityManager.persist(author);
  }

  private Book createBook1() {
    Book book = new Book();
    book.setTitle("First book");
    return book;
  }

  private Book createBook2() {
    Book book = new Book();
    book.setTitle("Second book");
    return book;
  }

  private Book createBook3() {
    Book book = new Book();
    book.setTitle("Third book");
    return book;
  }

  private Author fetchAuthorAtRevision(Number revision) {
    return (Author) AuditReaderFactory.get(entityManager).createQuery().forEntitiesAtRevision(Author.class, revision)
        .getSingleResult();
  }

  private Author retrieveAuthor() {
    return entityManager
        .createQuery("Select a from Author a left join fetch a.books  where a.firstName='TestFirstName'", Author.class)
        .getSingleResult();
  }
}

package com.hibernate.envers.application;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hibernate.envers.domain.Author;
import com.hibernate.envers.domain.Book;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class ApplicationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationService.class);

  @PersistenceContext
  private EntityManager entityManager;

  public void createBook() {
    Author author = new Author();
    author.setFirstName("firstName");
    author.setLastName("lastName");

    Book book = new Book();
    book.setTitle("Create book");
    Book book2 = new Book();
    book2.setTitle("Old book");

    author.addBook(book);
    author.addBook(book2);

    entityManager.persist(author);
  }

  public void updateBook() {
    Author author = entityManager.createQuery("Select a from Author a where a.firstName = 'firstName'", Author.class)
        .getSingleResult();

    Book book = new Book();
    book.setTitle("First book");
    Book book2 = new Book();
    book2.setTitle("Second book");
    author.getBook("Old book").setTitle("Changed Name book");

    author.addBook(book);
    author.addBook(book2);
  }

  public void reAddBooks() {
    Author author = entityManager.createQuery("Select a from Author a where a.firstName = 'firstName'", Author.class)
        .getSingleResult();

    author.removeBook("First book");
    author.removeBook("Second book");

    Book book = new Book();
    book.setTitle("First book");
    Book book2 = new Book();
    book2.setTitle("Second book");

    Book book3 = new Book();
    book3.setTitle("Fifth book");

    author.addBook(book);
    author.addBook(book2);
    author.addBook(book3);
  }

  public void print() {
    Author author = entityManager.createQuery("Select a from Author a where a.firstName = 'firstName'", Author.class)
        .getSingleResult();
    LOGGER.info("Fetched author: {}", author);

    List<Number> revisions = AuditReaderFactory.get(entityManager).getRevisions(Author.class, author.getId());

    revisions.forEach(r -> {
      AuditQuery q = AuditReaderFactory.get(entityManager)
          .createQuery().forEntitiesAtRevision(Author.class, r);
      LOGGER.info("Revision[{}]: {}", r, q.getSingleResult());
    });


  }
}

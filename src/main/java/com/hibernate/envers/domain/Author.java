package com.hibernate.envers.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Version;

import org.hibernate.envers.Audited;
import org.hibernate.envers.internal.tools.ArraysTools;

@Entity
@Audited
public class Author {

  @Id
  @GeneratedValue
  private Long id;
  @Version
  private Long version;
  private String firstName;
  private String lastName;
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "author_id", referencedColumnName = "id", nullable = false)
  private List<Book> books;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public void addBook(Book book) {
    if (this.books == null) {
      this.books = new ArrayList<>();
    }
    book.setAuthor(this);
    this.books.add(book);
  }

  @Override
  public String toString() {
    return "Author{" +
           "id=" + id +
           ", version=" + version +
           ", firstName='" + firstName + '\'' +
           ", lastName='" + lastName + '\'' +
           ", books=" + books +
           '}';
  }

  public void removeAllBooks() {
    if (this.books != null) {
      this.books.clear();
    }
  }

  public Book getBook(String title) {
    return books.stream().filter(book -> title.equals(book.getTitle())).findFirst().orElse(null);
  }

  public void removeBook(String title) {
    for (Iterator<Book> iterator = books.iterator(); iterator.hasNext(); ) {
      Book book = iterator.next();
      if (title.equals(book.getTitle())) {
        iterator.remove();
      }
    }
  }

  public List<Book> getBooks() {
    return Collections.unmodifiableList(books);
  }
}

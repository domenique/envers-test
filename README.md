# envers-test
This project illustrates some strange behavior we encountered while using envers.

https://hibernate.atlassian.net/browse/HHH-11063

## The scenario
We have an author object which contains a list of books. This relationship is bidirectionally mapped, so book has a reference to the author. 
Envers whill create mapping table for this relationship while hibernate does not need one because there is a foreign key in the book table.

The equals method of the book entity only looks at the title of the book and nothing else.
 
 The following scenario produces the wrong revisions:
 1. First transaction creates an Author with 2 books: 'First book' and 'Second book'
 2. Second transaction fetches the author using hibernate, then removes all books and create and adds 3 new instances of the book entity: 'First book', 'Second book'. 'Third book'
 3. Third transaction will fetch the Author using hibernate and verify that there are indeed 3 books linked to the author.
 4. Fourth transaction will fetch the latest revision of the author, but only 1 book is attached to it: 'Third book', while I would expect to find 3 books.
 
When looking at the data stored in the auditing tables, it seems that the equals method is playing a crucial role in determining what is stored in the join table used by envers. 
If the equals methods includes the primary key, the correct data is stored in the join table and the revision will contain the 3 books. 
If we don't include the id in the equals methods then the join table is not filled in correctly and we get a revision with just 1 book.

I can't exactly pin-point the place where it goes wrong, but it seems that the audit table is simply filled incorrectly because envers assumes that the equals method will include the primary key.
Hibernate however, does not rely on the equals method, and for good reason, the equals method should represent the business key and not the technical primary key. 
Otherwise an existing (stored in the db) book and a newly created book will never be equal anymore.

## running this application
clone this repo, then build it using gradlew:

```
./gradlew clean build
```

The application needs an database connection. So modify the application.properties located in src/main/resources:
```
spring.datasource.url=jdbc:postgresql://localhost/enverstest
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.generate-ddl=true
spring.jpa.hibernate.ddl-auto=create
spring.jpa.database=postgresql
```

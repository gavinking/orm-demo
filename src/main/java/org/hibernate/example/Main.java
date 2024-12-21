package org.hibernate.example;

import org.hibernate.jpa.HibernatePersistenceConfiguration;

import static java.lang.System.out;

public class Main {
    public static void main(String[] args) {
        try (var sessionFactory =
                     new HibernatePersistenceConfiguration("Bookshelf")
                             .managedClass(Book.class)
                             // use H2 in-memory database
                             .jdbcUrl("jdbc:h2:mem:db1")
                             .jdbcCredentials("sa", "")
                             // use Agroal connection pool
                             .property("hibernate.agroal.maxSize", "20")
                             // display SQL in console
                             .showSql(true, true, true)
                             .createEntityManagerFactory()) {

            // export the inferred database schema
            sessionFactory.getSchemaManager().create(true);

            // persist an entity
            sessionFactory.inTransaction(session -> {
                session.persist(new Book("9781932394153", "Hibernate in Action"));
            });

            // query data using HQL
            sessionFactory.inSession(session -> {
                out.println(session.createSelectionQuery("select isbn||': '||title from Book", String.class)
                        .getSingleResult());
            });

            // query data using criteria API
            sessionFactory.inSession(session -> {
                var builder = sessionFactory.getCriteriaBuilder();
                var query = builder.createQuery(String.class);
                var book = query.from(Book.class);
                query.select(builder.concat(builder.concat(book.get(Book_.isbn), builder.literal(": ")),
                        book.get(Book_.title)));
                out.println(session.createSelectionQuery(query).getSingleResult());
            });
        }
    }
}

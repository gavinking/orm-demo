package org.hibernate.example;

import org.hibernate.cfg.AgroalSettings;
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
                             // set the Agroal connection pool size
                             .property(AgroalSettings.AGROAL_MAX_SIZE, "15")
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

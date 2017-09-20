/*
 * MIT License
 *
 * Copyright (c) 2017 M.-Leander Reimer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package ignite.data2day.showcase;

import ignite.data2day.showcase.jdbc.CreateTablesCommand;
import ignite.data2day.showcase.jdbc.InsertDataCommand;
import ignite.data2day.showcase.jdbc.SelectQueryCommand;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.FieldsQueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class IgniteSqlApp {
    public static void main(String[] args) throws Exception {

        System.setProperty("IGNITE_PERFORMANCE_SUGGESTIONS_DISABLED", "true");
        Ignition.setClientMode(true);

        // Register JDBC driver and open connection
        Class.forName("org.apache.ignite.IgniteJdbcDriver");
        Connection connection = DriverManager.getConnection("jdbc:ignite:thin://127.0.0.1:10800?collocated=true");

        new CreateTablesCommand(connection).run();
        new InsertDataCommand(connection).run();
        new SelectQueryCommand(connection).run();

        System.out.println();
        System.out.println("----------------------------------------------------------------------------------------");
        System.out.println();

        Optional<String> configUri = Optional.ofNullable(System.getenv("CONFIG_URI"));
        try (Ignite ignite = Ignition.start(configUri.orElse("ignite-sql.xml"))) {
            // Getting a reference to City and Person caches created by CreateTablesCommand
            IgniteCache<Long, String> cityCache = ignite.cache("SQL_PUBLIC_CITY");
            IgniteCache<Object, Object> personCache = ignite.cache("SQL_PUBLIC_PERSON");

            queryData(cityCache);
            deleteAllPersons(personCache);
            cityCache.clear();
        }
    }

    private static void queryData(IgniteCache<Long, String> cityCache) {
        // Querying data from the cluster using a distributed JOIN.
        SqlFieldsQuery query = new SqlFieldsQuery("SELECT p.name, c.name FROM Person p, City c WHERE p.city_id = c.id");

        FieldsQueryCursor<List<?>> cursor = cityCache.query(query);
        Iterator<List<?>> iterator = cursor.iterator();

        System.out.println("Query result using API:");
        while (iterator.hasNext()) {
            List<?> row = iterator.next();
            System.out.printf("- %s lives in %s.%n", row.get(0), row.get(1));
        }
    }

    private static void deleteAllPersons(IgniteCache<Object, Object> personCache) {
        SqlFieldsQuery query = new SqlFieldsQuery("DELETE FROM Person");
        personCache.query(query).getAll();
    }
}

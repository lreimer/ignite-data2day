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
package ignite.data2day.showcase.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateTablesCommand implements Runnable {

    private final Connection connection;

    public CreateTablesCommand(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        System.out.println("Creating database tables and indexes.");

        try (Statement stmt = connection.createStatement()) {
            // Create reference City table based on REPLICATED template
            stmt.executeUpdate("CREATE TABLE city (id LONG PRIMARY KEY, name VARCHAR) " +
                    "WITH \"template=partitioned, backups=0\"");

            // Create table based on PARTITIONED template with one backup
            stmt.executeUpdate("CREATE TABLE person (id LONG, name VARCHAR, city_id LONG, " +
                    "PRIMARY KEY (id, city_id)) " +
                    "WITH \"template=replicated, affinityKey=city_id\"");

            // Create an index on the city table
            stmt.executeUpdate("CREATE INDEX idx_city_name ON city (name)");

            // Create an index on the person table
            stmt.executeUpdate("CREATE INDEX idx_person_name ON person (name)");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

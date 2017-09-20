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
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InsertDataCommand implements Runnable {
    private final Connection connection;

    public InsertDataCommand(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        System.out.println("Populating tables.");
        populateCityTable();
        populatePersonTable();
    }

    private void populateCityTable() {
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO city (id, name) VALUES (?, ?)")) {

            stmt.setLong(1, 1L);
            stmt.setString(2, "Rosenheim");
            stmt.executeUpdate();

            stmt.setLong(1, 2L);
            stmt.setString(2, "München");
            stmt.executeUpdate();

            stmt.setLong(1, 3L);
            stmt.setString(2, "Heidelberg");
            stmt.executeUpdate();

            stmt.setLong(1, 4L);
            stmt.setString(2, "New York");
            stmt.executeUpdate();

            stmt.setLong(1, 5L);
            stmt.setString(2, "San Francisco");
            stmt.executeUpdate();

            stmt.setLong(1, 6L);
            stmt.setString(2, "Berlin");
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void populatePersonTable() {
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO person (id, name, city_id) values (?, ?, ?)")) {

            stmt.setLong(1, 1L);
            stmt.setString(2, "Mario-Leander Reimer");
            stmt.setLong(3, 1L);
            stmt.executeUpdate();

            stmt.setLong(1, 2L);
            stmt.setString(2, "Kollege 1");
            stmt.setLong(3, 2L);
            stmt.executeUpdate();

            stmt.setLong(1, 3L);
            stmt.setString(2, "Kollege 2");
            stmt.setLong(3, 2L);
            stmt.executeUpdate();

            stmt.setLong(1, 4L);
            stmt.setString(2, "Visitor 1");
            stmt.setLong(3, 3L);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

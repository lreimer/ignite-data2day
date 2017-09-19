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
package ignite.data2day.showcase.cache;

import ignite.data2day.showcase.model.Employee;
import ignite.data2day.showcase.model.EmployeeKey;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CachePeekMode;
import org.apache.ignite.cache.query.FieldsQueryCursor;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.cache.query.TextQuery;
import org.apache.ignite.configuration.CacheConfiguration;

import javax.cache.Cache;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class EmployeeRepository {

    private final Ignite ignite;

    public EmployeeRepository(Ignite ignite) {
        this.ignite = ignite;
    }

    public CacheConfiguration<EmployeeKey, Employee> getCacheConfiguration() {
        CacheConfiguration<EmployeeKey, Employee> employeeCacheConfig = new CacheConfiguration<>("employeeCache");
        employeeCacheConfig.setCacheMode(CacheMode.REPLICATED);
        employeeCacheConfig.setAtomicityMode(CacheAtomicityMode.ATOMIC);
        employeeCacheConfig.setBackups(1);
        employeeCacheConfig.setIndexedTypes(String.class, Employee.class);

        return employeeCacheConfig;
    }

    public IgniteCache<EmployeeKey, Employee> getOrCreateCache() {
        return ignite.getOrCreateCache(getCacheConfiguration());
    }

    public void populate() {
        IgniteCache<EmployeeKey, Employee> cache = getOrCreateCache();
        cache.clear();

        Employee reimer = new Employee("11", "1", "M.-Leander Reimer", LocalDate.of(2009, 11, 1));
        EmployeeKey affinityKey = new EmployeeKey(reimer.getEmployeeId(), reimer.getCompanyId());
        cache.put(affinityKey, reimer);

        for (int i = 12; i <= 20; i++) {
            int nr = i - 11;
            Employee kollege = new Employee(Integer.toString(nr), "1", "Kollege " + nr, LocalDate.now());
            affinityKey = new EmployeeKey(kollege.getEmployeeId(), kollege.getCompanyId());
            cache.put(affinityKey, kollege);
        }
    }

    public void printStats() {
        IgniteCache<EmployeeKey, Employee> cache = getOrCreateCache();

        System.out.println("Employee cache size (Local): " + cache.localSize());
        System.out.println("Employee cache size (Primary): " + cache.size(CachePeekMode.PRIMARY));
        System.out.println("Employee cache size (Backup): " + cache.size(CachePeekMode.BACKUP));
    }

    public void findAllByCompanyName(String name) {
        // get all employees for a company by its name
        String sql = "select e.name from \"employeeCache\".Employee as e, \"companyCache\".Company as c where c.name = ? and e.companyId = c.companyId";

        // execute the query and obtain the results
        IgniteCache<EmployeeKey, Employee> cache = getOrCreateCache();
        SqlFieldsQuery query = new SqlFieldsQuery(sql, true).setArgs(name).setPageSize(25);

        System.out.println("Query for Employees by company name " + name);
        try (FieldsQueryCursor<List<?>> cursor = cache.query(query)) {
            List<?> results = cursor.getAll().stream().flatMap(List::stream).collect(Collectors.toList());
            System.out.println(results);
        }
    }

    public void queryByName(String name) {
        TextQuery<EmployeeKey, Employee> txt = new TextQuery<>(Employee.class, name);
        IgniteCache<EmployeeKey, Employee> cache = getOrCreateCache();

        System.out.println("Query for Employee entries with name " + name);
        try (QueryCursor<Cache.Entry<EmployeeKey, Employee>> cursor = cache.query(txt)) {
            List<Employee> employees = cursor.getAll().stream().map(Cache.Entry::getValue).collect(Collectors.toList());
            System.out.println(employees);
        }
    }
}

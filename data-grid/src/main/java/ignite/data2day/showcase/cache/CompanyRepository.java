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

import ignite.data2day.showcase.model.Company;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CachePeekMode;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlQuery;
import org.apache.ignite.cache.query.TextQuery;
import org.apache.ignite.configuration.CacheConfiguration;

import javax.cache.Cache;
import java.util.List;
import java.util.stream.Collectors;

public class CompanyRepository {

    private final Ignite ignite;

    public CompanyRepository(Ignite ignite) {
        this.ignite = ignite;
    }

    public CacheConfiguration<String, Company> getCacheConfiguration() {
        CacheConfiguration<String, Company> companyCacheConfig = new CacheConfiguration<>("companyCache");
        companyCacheConfig.setCacheMode(CacheMode.PARTITIONED);
        companyCacheConfig.setAtomicityMode(CacheAtomicityMode.ATOMIC);
        companyCacheConfig.setBackups(1);
        companyCacheConfig.setIndexedTypes(String.class, Company.class);

        return companyCacheConfig;
    }

    public IgniteCache<String, Company> getOrCreateCache() {
        return ignite.getOrCreateCache(getCacheConfiguration());
    }

    public void populate() {
        IgniteCache<String, Company> cache = getOrCreateCache();

        Company qaware = new Company("1", "QAware GmbH", Long.MAX_VALUE);
        cache.putIfAbsent(qaware.getCompanyId(), qaware);

        for (int i = 2; i <= 10; i++) {
            Company c = new Company(Integer.toString(i), "Company " + i, 100_000 * i);
            cache.putIfAbsent(c.getCompanyId(), c);
        }
    }

    public void queryRevenueGreater(long revenue) {
        IgniteCache<String, Company> cache = getOrCreateCache();
        SqlQuery<String, Company> sql = new SqlQuery<>(Company.class, "revenue > ?");

        System.out.println("SQL query for Company entries with revenue > " + revenue);
        try (QueryCursor<Cache.Entry<String, Company>> cursor = cache.query(sql.setArgs(revenue))) {
            List<Company> companies = cursor.getAll().stream().map(Cache.Entry::getValue).collect(Collectors.toList());
            System.out.println(companies);
        }

    }

    public void queryByName(String name) {
        TextQuery<String, Company> txt = new TextQuery<>(Company.class, name);
        IgniteCache<String, Company> cache = getOrCreateCache();

        System.out.println("Text query for Company entries with name " + name);
        try (QueryCursor<Cache.Entry<String, Company>> cursor = cache.query(txt)) {
            List<Company> companies = cursor.getAll().stream().map(Cache.Entry::getValue).collect(Collectors.toList());
            System.out.println(companies);
        }
    }

    public void printStats() {
        IgniteCache<String, Company> cache = getOrCreateCache();

        System.out.println("Company cache size (Local): " + cache.localSize());
        System.out.println("Company cache size (Primary): " + cache.size(CachePeekMode.PRIMARY));
        System.out.println("Company cache size (Backup): " + cache.size(CachePeekMode.BACKUP));
    }
}

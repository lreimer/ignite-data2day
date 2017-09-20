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

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteCompute;
import org.apache.ignite.Ignition;
import org.apache.ignite.lang.IgniteRunnable;
import org.apache.ignite.resources.IgniteInstanceResource;

import javax.cache.Cache;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

/**
 * The Ignite client test driver application to showcase compute grid features.
 */
public class ComputeGridApp {

    public static void main(String[] args) {
        System.setProperty("IGNITE_PERFORMANCE_SUGGESTIONS_DISABLED", "true");
        Ignition.setClientMode(true);

        Optional<String> configUri = Optional.ofNullable(System.getenv("CONFIG_URI"));
        try (Ignite ignite = Ignition.start(configUri.orElse("compute-grid.xml"))) {
            distributedClosures(ignite);

            collocatedClosures(ignite);

            executorService(ignite);
        }
    }

    /**
     * Showcase different ways of executing distributed closures on the cluster.
     *
     * @param ignite the Ignite instance
     */
    private static void distributedClosures(Ignite ignite) {
        IgniteCompute compute = ignite.compute(ignite.cluster().forRemotes());

        // this will only run on one node
        compute.run(() -> System.out.println("Running distributed closure (1) on " + ignite.cluster().localNode().id()));
        compute.run(() -> System.out.println("Running distributed closure (2) on " + ignite.cluster().localNode().id()));

        // Print out hello message on remote nodes in the cluster group.
        compute.broadcast(() -> System.out.println("Broadcasting distributed closure on " + ignite.cluster().localNode().id()));

        // this will actually return a result
        final String question = "How many characters has this sentence?";
        System.out.println(question);
        Collection<Integer> res = compute.apply(String::length, Arrays.asList(question.split(" ")));

        int total = res.stream().mapToInt(Integer::intValue).sum();
        System.out.printf("The answer to the questions is %s.%n", total);
    }

    /**
     * Run a closure on the cluster, but collocated at the node of the key
     * for local access to the entry.
     *
     * @param ignite the Ignite instance
     */
    private static void collocatedClosures(Ignite ignite) {
        IgniteCache<Integer, String> cache = ignite.cache("someCache");
        for (int i = 0; i < 50; i++) {
            cache.put(i, "Some data for " + i);
        }

        IgniteCompute compute = ignite.compute(ignite.cluster().forRemotes());
        for (int i = 0; i < 50; i++) {
            // This closure will execute on the remote node where
            // data with the 'key' is located.
            int key = i;
            compute.affinityRun("someCache", key, () -> {
                // Peek is a local memory lookup.
                System.out.println("Co-located processing [key= " + key + ", value= " + cache.localPeek(key) + ']');
            });
        }
    }

    /**
     * A simple Ignite clustered executor service example. Pretty cool since the API is unchanged like
     * you would use the ExecutorService locally.
     */
    private static void executorService(Ignite ignite) {
        ExecutorService executorService = ignite.executorService(ignite.cluster().forRemotes());

        IgniteCache<String, Integer> cache = ignite.cache("charCount");
        cache.clear();

        // Iterate through all words in the sentence and create jobs
        for (final String word : "Process individual words and count chars using executor service".split(" ")) {
            executorService.submit(new IgniteRunnable() {

                @IgniteInstanceResource
                private Ignite ignite;

                @Override
                public void run() {
                    System.out.println("Processing '" + word + "' on " + ignite.cluster().localNode().id() + " from grid job.");

                    IgniteCache<Object, Object> charCount = this.ignite.cache("charCount");
                    charCount.putIfAbsent(word, word.length());
                }
            });
        }

        for (Cache.Entry<String, Integer> entry : cache) {
            System.out.printf("The word '%s has %s characters.%n", entry.getKey(), entry.getValue());
        }
    }

}

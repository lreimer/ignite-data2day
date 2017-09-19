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
package ignite.data2day.showcase.queue;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteQueue;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CollectionConfiguration;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MessageProducerApp {
    public static void main(String[] args) {

        System.setProperty("IGNITE_PERFORMANCE_SUGGESTIONS_DISABLED", "true");

        Optional<String> configUri = Optional.ofNullable(System.getenv("CONFIG_URI"));
        Ignite ignite = Ignition.start(configUri.orElse("ignite-messaging.xml"));

        CollectionConfiguration colCfg = new CollectionConfiguration();
        colCfg.setCollocated(false);
        colCfg.setCacheMode(CacheMode.PARTITIONED);
        colCfg.setBackups(1);

        IgniteQueue<String> queue = ignite.queue("randomUuidQueue", 0, colCfg);

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {
                    String uuid = UUID.randomUUID().toString();
                    System.out.println("Adding random UUID " + uuid + " to the queue.");
                    queue.add(uuid);

                    System.out.println("Queue size: " + queue.size());
                },
                1, 1, TimeUnit.SECONDS);

    }
}

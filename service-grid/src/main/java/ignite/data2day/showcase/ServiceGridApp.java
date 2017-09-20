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

import ignite.data2day.showcase.services.DefaultRandomUuidService;
import ignite.data2day.showcase.services.PingService;
import ignite.data2day.showcase.services.RandomUuidService;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteAtomicLong;
import org.apache.ignite.IgniteServices;
import org.apache.ignite.Ignition;

import java.util.Optional;

/**
 * The Ignite client test driver application to showcase service grid features.
 */
public class ServiceGridApp {

    public static void main(String[] args) {
        System.setProperty("IGNITE_PERFORMANCE_SUGGESTIONS_DISABLED", "true");
        Ignition.setClientMode(true);

        Optional<String> configUri = Optional.ofNullable(System.getenv("CONFIG_URI"));
        try (Ignite ignite = Ignition.start(configUri.orElse("service-grid.xml"))) {
            pingService(ignite);

            clusterSingletonRandomUuid(ignite);
        }

        System.exit(0);
    }

    private static void pingService(Ignite ignite) {
        // get a service proxy to our ping service, the service proxy will be non sticky
        IgniteServices services = ignite.services();
        PingService pingService = services.serviceProxy("PingService", PingService.class, false);

        // get or create the invocation counter
        IgniteAtomicLong invocations = ignite.atomicLong("pingInvocations", 0, false);

        for (int i = 0; i < 10; i++) {
            long before = invocations.get();
            String response = pingService.ping();
            long after = invocations.get();

            System.out.printf("Before %s | ping() -> %s | After %s%n", before, response, after);
        }
    }

    private static void clusterSingletonRandomUuid(Ignite ignite) {
        IgniteServices services = ignite.services();

        // first we deploy the service programmatically
        // services.deployNodeSingleton("RandomUuidService", new DefaultRandomUuidService());
        services.deployClusterSingleton("RandomUuidService", new DefaultRandomUuidService());

        IgniteAtomicLong invocations = ignite.atomicLong("randomUuidInvocations", 0, false);

        // then we obtain a proxy and start using the service
        RandomUuidService randomUuidService = services.serviceProxy("RandomUuidService", RandomUuidService.class, false);
        for (int i = 0; i < 10; i++) {
            long before = invocations.get();
            String response = randomUuidService.randomUUID();
            long after = invocations.get();

            System.out.printf("Before %s | %s | After %s%n", before, response, after);
        }
    }

}

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
import org.apache.ignite.IgniteCompute;
import org.apache.ignite.Ignition;

import java.util.Optional;

public class HelloIgniteApp {
    public static void main(String[] args) {

        System.setProperty("IGNITE_PERFORMANCE_SUGGESTIONS_DISABLED", "true");
        Ignition.setClientMode(true);

        Optional<String> configUri = Optional.ofNullable(System.getenv("CONFIG_URI"));
        try (Ignite ignite = Ignition.start(configUri.orElse("example-ignite.xml"))) {
            // Limit broadcast to remote nodes only.
            IgniteCompute compute = ignite.compute(ignite.cluster().forRemotes());

            // Print out hello message on remote nodes in the cluster group.
            compute.broadcast(() -> System.out.println("Hello data2day from Apache Ignite " + ignite.cluster().localNode().id()));
        }
    }
}

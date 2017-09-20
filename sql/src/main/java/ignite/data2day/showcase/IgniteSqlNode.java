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
import org.apache.ignite.IgniteEvents;
import org.apache.ignite.Ignition;
import org.apache.ignite.events.EventType;

import java.util.Optional;

/**
 * The data node for the Ignite SQL showcase. Registers a local listener.
 */
public class IgniteSqlNode {

    public static void main(String[] args) {
        System.setProperty("IGNITE_PERFORMANCE_SUGGESTIONS_DISABLED", "true");

        Optional<String> configUri = Optional.ofNullable(System.getenv("CONFIG_URI"));
        Ignite ignite = Ignition.start(configUri.orElse("ignite-sql.xml"));

        // start listening for cache events
        IgniteEvents events = ignite.events();
        events.localListen(event -> {
                    // echo the event raised
                    System.out.println("Received " + event);
                    return true;
                },
                EventType.EVT_CACHE_OBJECT_PUT, EventType.EVT_CACHE_OBJECT_READ,
                EventType.EVT_CACHE_OBJECT_REMOVED, EventType.EVT_CACHE_QUERY_EXECUTED);

    }

}

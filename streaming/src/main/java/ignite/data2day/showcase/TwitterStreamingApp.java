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
import org.apache.ignite.IgniteDataStreamer;
import org.apache.ignite.Ignition;
import org.apache.ignite.stream.twitter.OAuthSettings;
import org.apache.ignite.stream.twitter.TwitterStreamer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TwitterStreamingApp {
    public static void main(String[] args) {

        System.setProperty("IGNITE_PERFORMANCE_SUGGESTIONS_DISABLED", "true");
        Ignition.setClientMode(true);

        Optional<String> configUri = Optional.ofNullable(System.getenv("CONFIG_URI"));
        Ignite ignite = Ignition.start(configUri.orElse("ignite-streaming.xml"));

        IgniteDataStreamer<Integer, String> dataStreamer = ignite.dataStreamer("tweetCache");
        dataStreamer.allowOverwrite(true);
        dataStreamer.autoFlushFrequency(10);

        // https://apps.twitter.com/app/14259728/keys
        OAuthSettings oAuthSettings = new OAuthSettings(
                "setting1",
                "setting2",
                "setting3",
                "setting4");

        TwitterStreamer<Integer, String> streamer = new TwitterStreamer<>(oAuthSettings);
        streamer.setIgnite(ignite);
        streamer.setStreamer(dataStreamer);

        Map<String, String> params = new HashMap<>();
        params.put("track", "data2day,ignite,apache,cloud");
        // @LeanderReimer,@qaware,@data2day
        params.put("follow", "1346627546,32837461,2650574588");

        streamer.setApiParams(params);// Twitter Streaming API params.

        // https://stream.twitter.com/1.1/statuses/filter.json
        streamer.setEndpointUrl("/statuses/filter.json");
        streamer.setThreadsCount(4);

        streamer.start();
    }
}

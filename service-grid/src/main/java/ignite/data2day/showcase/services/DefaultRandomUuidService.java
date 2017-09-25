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
package ignite.data2day.showcase.services;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteAtomicLong;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.apache.ignite.services.Service;
import org.apache.ignite.services.ServiceContext;

import java.util.UUID;

/**
 * Default service implementation. Counts invocations using IgniteAtomicLong.
 */
public class DefaultRandomUuidService implements RandomUuidService, Service {

    @IgniteInstanceResource
    private Ignite ignite;

    private IgniteAtomicLong invocations;
    private String executionId;

    @Override
    public void init(ServiceContext ctx) throws Exception {
        System.out.println("Initialized service: " + ctx.name());
    }

    @Override
    public void cancel(ServiceContext ctx) {
        System.out.println("Cancelled service: " + ctx.name());
    }

    @Override
    public void execute(ServiceContext ctx) throws Exception {
        this.invocations = ignite.atomicLong("randomUuidInvocations", 0, false);
        this.executionId = ctx.executionId().toString();
        System.out.printf("Executing service %s {executionId=%s}.%n", ctx.name(), executionId);
    }

    @Override
    public String randomUUID() {
        long counter = this.invocations.incrementAndGet();
        System.out.printf("[%s] randomUUID() called. %s invocations so far.%n", executionId, counter);

        return UUID.randomUUID().toString();
    }
}

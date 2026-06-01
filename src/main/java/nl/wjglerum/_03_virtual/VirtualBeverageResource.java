package nl.wjglerum._03_virtual;

import io.quarkus.logging.Log;
import io.quarkus.virtual.threads.VirtualThreads;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import nl.wjglerum.ErrorResult;
import nl.wjglerum.FloodResult;

import static jakarta.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;

@Path("/beverage/virtual")
@Transactional
@RunOnVirtualThread
public class VirtualBeverageResource {

    @Inject
    VirtualBartender bartender;

    @Inject
    FlakeyVirtualBartender flakeyBartender;

    @Inject
    VirtualBeverageRepository repository;

    @Inject
    @VirtualThreads
    ExecutorService executor;

    @GET
    public VirtualBeverage getBeverage() {
        Log.info("Going to get virtual beverage");
        var beverage = bartender.get();
        repository.save(beverage);
        return beverage;
    }

    @GET
    @Path("/sequential")
    public List<VirtualBeverage> getBeveragesSequential() {
        Log.info("Going to get virtual beverages sequential");
        var beverage1 = bartender.get();
        var beverage2 =  bartender.get();
        var beverage3 =  bartender.get();
        var beverages = List.of(beverage1, beverage2, beverage3);
        repository.save(beverages);
        return beverages;
    }

    @GET
    @Path("/parallel")
    public List<VirtualBeverage> getBeveragesParallel() {
        Log.info("Going to get virtual beverages parallel");
        try {
            var b1 = executor.submit(bartender::get);
            var b2 = executor.submit(bartender::get);
            var b3 = executor.submit(bartender::get);
            var beverages = List.of(b1.get(), b2.get(), b3.get());
            repository.save(beverages);
            return beverages;
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @GET
    @Path("/flood")
    public FloodResult flood(@QueryParam("count") @DefaultValue("100") int count) {
        Log.infof("Flooding with %d virtual-thread requests", count);
        var succeeded = new AtomicInteger();
        var failed = new AtomicInteger();
        var start = System.currentTimeMillis();
        var futures = IntStream.range(0, count)
                .mapToObj(_ -> executor.submit(() -> {
                    try {
                        bartender.get();
                        succeeded.incrementAndGet();
                    } catch (Exception e) {
                        failed.incrementAndGet();
                    }
                }))
                .toList();
        for (var f : futures) {
            try { f.get(); } catch (ExecutionException | InterruptedException ignored) {}
        }
        return new FloodResult(count, succeeded.get(), failed.get(), System.currentTimeMillis() - start);
    }

    @GET
    @Path("/custom")
    public List<VirtualBeverage> getBeveragesCustom() {
        Log.info("Going to get virtual beverages custom");
        var name = Thread.currentThread().getName();
        var threadFactory = Thread.ofVirtual().name(name + "-virtual-", 0).factory();
        try(var executor = Executors.newThreadPerTaskExecutor(threadFactory)) {
            var b1 = executor.submit(bartender::get);
            var b2 = executor.submit(bartender::get);
            var b3 = executor.submit(bartender::get);
            var beverages = List.of(b1.get(), b2.get(), b3.get());
            repository.save(beverages);
            return beverages;
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @GET
    @Path("/failfast")
    public Response getBeveragesFailFast() {
        Log.info("Going to get beverages fail-fast (virtual) — no sibling cancellation unlike StructuredTaskScope");
        var b1 = executor.submit(flakeyBartender::get);
        var b2 = executor.submit(flakeyBartender::get);
        var b3 = executor.submit(flakeyBartender::get);
        try {
            return Response.ok(List.of(b1.get(), b2.get(), b3.get())).build();
        } catch (ExecutionException e) {
            return error(SERVICE_UNAVAILABLE, e.getCause().getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return error(SERVICE_UNAVAILABLE, "interrupted");
        }
    }

    private static Response error(Response.Status status, String message) {
        return Response.status(status).entity(new ErrorResult(message)).build();
    }
}

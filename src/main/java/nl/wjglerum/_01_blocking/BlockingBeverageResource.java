package nl.wjglerum._01_blocking;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.context.ManagedExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import nl.wjglerum.ErrorResult;
import nl.wjglerum.FloodResult;

import static jakarta.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;

@Path("/beverage/blocking")
@Transactional
public class BlockingBeverageResource {

    @Inject
    BlockingBartender bartender;

    @Inject
    FlakeyBlockingBartender flakeyBartender;

    @Inject
    BlockingBeverageRepository repository;

    @Inject
    ManagedExecutor executor;

    @GET
    public BlockingBeverage getBeverage() {
        Log.info("Going to get blocking beverage");
        var beverage = bartender.get();
        repository.save(beverage);
        return beverage;
    }

    @GET
    @Path("/sequential")
    public List<BlockingBeverage> getBeveragesSequential() {
        Log.info("Going to get blocking beverages sequential");
        var b1 = bartender.get();
        var b2 = bartender.get();
        var b3 = bartender.get();
        var beverages = List.of(b1, b2, b3);
        repository.save(beverages);
        return beverages;
    }

    @GET
    @Path("/parallel")
    public List<BlockingBeverage> getBeveragesParallel() {
        Log.info("Going to get blocking beverages parallel");
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
        Log.infof("Flooding with %d blocking requests", count);
        var succeeded = new AtomicInteger();
        var failed = new AtomicInteger();
        var start = System.currentTimeMillis();
        var futures = new ArrayList<Future<?>>(count);
        for (int i = 0; i < count; i++) {
            try {
                futures.add(executor.submit(() -> {
                    try {
                        bartender.get();
                        succeeded.incrementAndGet();
                    } catch (Exception e) {
                        failed.incrementAndGet();
                    }
                }));
            } catch (RejectedExecutionException e) {
                failed.incrementAndGet();
            }
        }
        for (var f : futures) {
            try { f.get(); } catch (ExecutionException | InterruptedException ignored) {}
        }
        return new FloodResult(count, succeeded.get(), failed.get(), System.currentTimeMillis() - start);
    }

    @GET
    @Path("/failfast")
    public Response getBeveragesFailFast() {
        Log.info("Going to get beverages fail-fast (blocking) — no sibling cancellation unlike StructuredTaskScope");
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

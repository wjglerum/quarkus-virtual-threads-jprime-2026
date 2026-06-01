package nl.wjglerum._04_structured;

import io.quarkus.logging.Log;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import nl.wjglerum.ErrorResult;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.FailedException;
import java.util.concurrent.StructuredTaskScope.Joiner;
import java.util.concurrent.StructuredTaskScope.TimeoutException;

import static jakarta.ws.rs.core.Response.Status.REQUEST_TIMEOUT;
import static jakarta.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;

@Path("/beverage/structured")
@RunOnVirtualThread
@SuppressWarnings("preview")
public class StructuredBeverageResource {

    @Inject
    StructuredBartender bartender;

    @Inject
    FlakeyBartender flakeyBartender;

    @Inject
    StructuredBeverageRepository repository;

    @GET
    @Path("/simple")
    @Transactional
    public List<StructuredBeverage> getBeveragesSimple() throws InterruptedException {
        Log.info("Going to get structured beverages simple");
        try (var scope = StructuredTaskScope.open()) {
            var b1 = scope.fork(bartender::get);
            var b2 = scope.fork(bartender::get);
            var b3 = scope.fork(bartender::get);
            scope.join();
            var beverages = List.of(b1.get(), b2.get(), b3.get());
            repository.save(beverages);
            return beverages;
        }
    }

    @GET
    @Path("/custom")
    @Transactional
    public List<StructuredBeverage> getBeveragesCustom() throws InterruptedException {
        Log.info("Going to get structured beverages custom");
        var name = Thread.currentThread().getName();
        var threadFactory = Thread.ofVirtual().name(name + "-structured-", 0).factory();
        try (var scope = StructuredTaskScope.open(
                Joiner.<StructuredBeverage>allSuccessfulOrThrow(),
                cf -> cf.withThreadFactory(threadFactory)
        )) {
            scope.fork(bartender::get);
            scope.fork(bartender::get);
            scope.fork(bartender::get);
            var beverages = scope.join();
            repository.save(beverages);
            return beverages;
        }
    }

    @GET
    @Path("/race")
    @Transactional
    public StructuredBeverage getBeverageRace() throws InterruptedException {
        Log.info("Going to race 3 bartenders — first one wins, siblings cancelled");
        try (var scope = StructuredTaskScope.open(
                Joiner.<StructuredBeverage>anySuccessfulOrThrow()
        )) {
            scope.fork(bartender::get);
            scope.fork(bartender::get);
            scope.fork(bartender::get);
            var winner = scope.join();
            repository.save(winner);
            return winner;
        }
    }

    @GET
    @Path("/failfast")
    public Response getBeveragesFailFast() throws InterruptedException {
        Log.info("Going to get beverages fail-fast — one failure cancels siblings");
        try (var scope = StructuredTaskScope.open(
                Joiner.<StructuredBeverage>allSuccessfulOrThrow()
        )) {
            scope.fork(flakeyBartender::get);
            scope.fork(flakeyBartender::get);
            scope.fork(flakeyBartender::get);
            var beverages = scope.join();
            return Response.ok(beverages).build();
        } catch (FailedException e) {
            return error(SERVICE_UNAVAILABLE, e.getCause().getMessage());
        }
    }

    @GET
    @Path("/timeout")
    public Response getBeveragesWithTimeout() throws InterruptedException {
        Log.info("Going to get beverages with scope-level timeout");
        try (var scope = StructuredTaskScope.open(
                Joiner.<StructuredBeverage>allSuccessfulOrThrow(),
                cf -> cf.withTimeout(Duration.ofMillis(150))
        )) {
            scope.fork(bartender::get);
            scope.fork(bartender::get);
            scope.fork(bartender::get);
            var beverages = scope.join();
            return Response.ok(beverages).build();
        } catch (TimeoutException e) {
            return error(REQUEST_TIMEOUT, "scope timed out — all subtasks cancelled");
        } catch (FailedException e) {
            return error(SERVICE_UNAVAILABLE, e.getCause().getMessage());
        }
    }

    private static Response error(Response.Status status, String message) {
        return Response.status(status).entity(new ErrorResult(message)).build();
    }
}

package nl.wjglerum._02_reactive;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;

import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import nl.wjglerum.ErrorResult;
import nl.wjglerum.FloodResult;

import java.util.List;
import java.util.stream.IntStream;

import static jakarta.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;

@Path("/beverage/reactive")
public class ReactiveBeverageResource {

    @Inject
    ReactiveBartender bartender;

    @Inject
    FlakeyReactiveBartender flakeyBartender;

    @Inject
    ReactiveBeverageRepository repository;

    @GET
    @WithTransaction
    public Uni<ReactiveBeverage> getBeverage() {
        Log.info("Going to get reactive beverage");
        return bartender.get().flatMap(beverage -> repository.save(beverage));
    }

    @GET
    @Path("/sequential")
    @WithTransaction
    public Uni<List<ReactiveBeverage>> getBeverageSequential() {
        Log.info("Going to get reactive beverages sequential");
        return bartender.get().flatMap(b1 ->
                bartender.get().flatMap(b2 ->
                        bartender.get().flatMap(b3 -> {
                                    var beverages = List.of(b1, b2, b3);
                                    return repository.save(beverages);
                                }
                        )
                )
        );
    }

    @GET
    @Path("/parallel")
    @WithTransaction
    public Uni<List<ReactiveBeverage>> getBeveragesParallel() {
        Log.info("Going to get reactive beverages parallel");
        var b1 = bartender.get();
        var b2 = bartender.get();
        var b3 = bartender.get();
        return Uni.join().all(b1, b2, b3).andCollectFailures()
                .flatMap(beverages -> repository.save(beverages));
    }

    @GET
    @Path("/flood")
    public Uni<FloodResult> flood(@QueryParam("count") @DefaultValue("100") int count) {
        Log.infof("Flooding with %d reactive requests", count);
        var start = System.currentTimeMillis();
        var unis = IntStream.range(0, count)
                .mapToObj(i -> bartender.get().map(b -> 1).onFailure().recoverWithItem(0))
                .toList();
        return Uni.join().all(unis).andCollectFailures()
                .map(results -> {
                    var succeeded = results.stream().mapToInt(Integer::intValue).sum();
                    return new FloodResult(count, succeeded, count - succeeded, System.currentTimeMillis() - start);
                });
    }

    @GET
    @Path("/failfast")
    public Uni<Response> getBeveragesFailFast() {
        Log.info("Going to get beverages fail-fast (reactive) — first failure cancels the join");
        var b1 = flakeyBartender.get();
        var b2 = flakeyBartender.get();
        var b3 = flakeyBartender.get();
        return Uni.join().all(b1, b2, b3).andFailFast()
                .map(beverages -> Response.ok(beverages).build())
                .onFailure().recoverWithItem(e -> error(SERVICE_UNAVAILABLE, e.getMessage()));
    }

    private static Response error(Response.Status status, String message) {
        return Response.status(status).entity(new ErrorResult(message)).build();
    }
}

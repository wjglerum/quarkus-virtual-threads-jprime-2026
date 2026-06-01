package nl.wjglerum._05_pinning;

import io.quarkus.logging.Log;
import io.quarkus.virtual.threads.VirtualThreads;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

@Path("/beverage/pinning")
@RunOnVirtualThread
public class PinningBeverageResource {

    @Inject
    PinningBartender pinningBartender;

    @Inject
    UnpinningBartender unpinningBartender;

    @Inject
    @VirtualThreads
    ExecutorService executor;

    @GET
    @Path("/pinned")
    public PinningBeverage getPinned() {
        Log.info("Getting beverage via synchronized (used to pin pre Java 24)");
        return pinningBartender.get();
    }

    @GET
    @Path("/unpinned")
    public UnpinningBeverage getUnpinned() {
        Log.info("Getting beverage via ReentrantLock (never pinned)");
        return unpinningBartender.get();
    }

    @GET
    @Path("/pinned/parallel")
    public List<PinningBeverage> getPinnedParallel() throws ExecutionException, InterruptedException {
        Log.info("Getting 3 beverages in parallel via synchronized");
        var b1 = executor.submit(pinningBartender::get);
        var b2 = executor.submit(pinningBartender::get);
        var b3 = executor.submit(pinningBartender::get);
        return List.of(b1.get(), b2.get(), b3.get());
    }

    @GET
    @Path("/unpinned/parallel")
    public List<UnpinningBeverage> getUnpinnedParallel() throws ExecutionException, InterruptedException {
        Log.info("Getting 3 beverages in parallel via ReentrantLock");
        var b1 = executor.submit(unpinningBartender::get);
        var b2 = executor.submit(unpinningBartender::get);
        var b3 = executor.submit(unpinningBartender::get);
        return List.of(b1.get(), b2.get(), b3.get());
    }
}

package nl.wjglerum._05_pinning;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.virtual.ShouldNotPin;
import io.quarkus.test.junit.virtual.VirtualThreadUnit;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@ShouldNotPin
@VirtualThreadUnit
@TestHTTPEndpoint(PinningBeverageResource.class)
class PinningBeverageResourceTest {

    @Test
    void testSynchronizedDoesNotPin() {
        given()
                .when()
                .get("/pinned")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body(containsString("Pinning Coffee"));
    }

    @Test
    void testReentrantLockDoesNotPin() {
        given()
                .when()
                .get("/unpinned")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body(containsString("Unpinning Coffee"));
    }

    @Test
    void testSynchronizedParallelDoesNotPin() {
        given()
                .when()
                .get("/pinned/parallel")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("size()", is(3))
                .body(containsString("Pinning Coffee"));
    }

    @Test
    void testReentrantLockParallelDoesNotPin() {
        given()
                .when()
                .get("/unpinned/parallel")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("size()", is(3))
                .body(containsString("Unpinning Coffee"));
    }
}

package nl.wjglerum._01_blocking;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import org.hamcrest.MatcherAssert;

import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.in;

@QuarkusTest
@TestHTTPEndpoint(BlockingBeverageResource.class)
class BlockingBeverageResourceTest {

    @Test
    void testBlockingEndpoint() {
        given()
                .when()
                .get()
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body(containsString("Blocking Coffee"));
    }

    @Test
    void testBlockingSequentialEndpoint() {
        given()
                .when()
                .get("/sequential")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("size()", is(3))
                .body(containsString("Blocking Coffee"));
    }

    @Test
    void testBlockingParallelEndpoint() {
        given()
                .when()
                .get("/parallel")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("size()", is(3))
                .body(containsString("Blocking Coffee"));
    }

    @Test
    void testBlockingFloodEndpoint() {
        given()
                .queryParam("count", 5)
                .when()
                .get("/flood")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("requested", equalTo(5))
                .body("succeeded", equalTo(5))
                .body("failed", equalTo(0));
    }

    @Test
    void testBlockingFailFastEndpoint() {
        var status = given()
                .when()
                .get("/failfast")
                .then()
                .statusCode(in(List.of(200, 503)))
                .extract().statusCode();
        MatcherAssert.assertThat(status, in(List.of(200, 503)));
    }
}

package nl.wjglerum._04_structured;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.virtual.ShouldNotPin;
import io.quarkus.test.junit.virtual.VirtualThreadUnit;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.in;

@QuarkusTest
@ShouldNotPin(atMost = 5)
@VirtualThreadUnit
@TestHTTPEndpoint(StructuredBeverageResource.class)
class StructuredBeverageResourceTest {

    @Test
    void testStructuredSimpleEndpoint() {
        given()
                .when()
                .get("/simple")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("size()", equalTo(3))
                .body(containsString("Structured Coffee"));
    }

    @Test
    void testStructuredCustomEndpoint() {
        given()
                .when()
                .get("/custom")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("size()", equalTo(3))
                .body(containsString("Structured Coffee"));
    }

    @Test
    void testStructuredRaceEndpoint() {
        given()
                .when()
                .get("/race")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("name", equalTo("Structured Coffee"));
    }

    @Test
    void testStructuredFailFastEndpoint() {
        // Flakey bartender: 50% chance each, so success or 503 are both valid outcomes
        var status = given()
                .when()
                .get("/failfast")
                .then()
                .extract()
                .statusCode();
        assertThat(status, in(List.of(200, 503)));
    }

    @Test
    void testStructuredTimeoutEndpoint() {
        // test delay is 0.1s; scope timeout is 0.15s — with 3 parallel tasks it may just squeak through
        // In dev mode (3s delay) this always times out. The important thing is no 500.
        var status = given()
                .when()
                .get("/timeout")
                .then()
                .extract()
                .statusCode();
        assertThat(status, in(List.of(200, 408)));
    }
}

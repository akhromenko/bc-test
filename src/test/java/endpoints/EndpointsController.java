package endpoints;

import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class EndpointsController {

    private static final String token = "17179c7224cf41f4b19225ff82ff30c6";
    private static final String API_URL = "https://api.blockcypher.com";

    public static Response createAddress() {
        String path = "v1/bcy/test/addrs";
        Map<String, String> parameters = new HashMap<>();
        parameters.put("token", token);
        parameters.put("bech32", "true");

        return given()
                .contentType("application/json")
                .params(parameters)
                .when().post(API_URL + "/{path}", path)
                .then().extract().response();
    }

    public static Response createWallet(String name, String address) {
        String path = "v1/bcy/test/wallets";
        JSONArray addresses = new JSONArray().put(address);
        JSONObject body = new JSONObject()
                .put("name", name)
                .put("addresses", addresses);

        return given()
                .contentType("application/json")
                .body(body.toString())
                .queryParam("token", token)
                .when().post(API_URL + "/{path}", path)
                .then().extract().response();
    }

    public static Response getAddressBalance(String address) {
        String path = String.format("v1/bcy/test/addrs/%s/balance", address);
        return given()
                .contentType("application/json")
                .queryParam("token", token)
                .when().get(API_URL + "/{path}", path)
                .then().extract().response();
    }

    public static Response fundAddress(String address, int amount) {
        String path = "v1/bcy/test/faucet";
        JSONObject body = new JSONObject()
                .put("address", address)
                .put("amount", amount);
        return given()
                .contentType("application/json")
                .queryParam("token", token)
                .body(body.toString())
                .when().post(API_URL + "/{path}", path)
                .then().extract().response();
    }

    public static Response getTransaction(String trx) {
        String path = String.format("v1/bcy/test/txs/%s", trx);
        return given()
                .contentType("application/json")
                .when().get(API_URL + "/{path}", path)
                .then().extract().response();
    }

    public static Response getAddressInfo(String address) {
        String path = String.format("v1/bcy/test/addrs/%s", address);
        return given()
                .contentType("application/json")
                .queryParam("token", token)
                .when().get(API_URL + "/{path}", path)
                .then().extract().response();
    }
}

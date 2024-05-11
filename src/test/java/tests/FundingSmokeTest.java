package tests;

import endpoints.EndpointsController;
import endpoints.Utils;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FundingSmokeTest extends EndpointsController {

    private static String address;
    private static String firstTrxRef;
    private static final int amount = 100000;
    private final static String walletName = Utils.createWalletName();

    @BeforeAll
    public static void preconditions() {
        address = createAddress().jsonPath().getString("address");
    }

    @Test
    @Order(1)
    @DisplayName("Create wallet")
    public void createWalletTest() {
        Response wallet = createWallet(walletName, address);
        Assertions.assertEquals(wallet.getStatusCode(), SC_CREATED);
        Assertions.assertEquals(wallet.jsonPath().getString("name"), walletName);
        Assertions.assertEquals(wallet.jsonPath().get("addresses[0]").toString(), address);

        JsonPath addressInfo = getAddressInfo(address).jsonPath();
        Assertions.assertEquals(addressInfo.getInt("balance"), 0);
        Assertions.assertEquals(addressInfo.getInt("n_tx"), 0);
    }

    @Test
    @Order(2)
    @DisplayName("Fund wallet and check balance")
    public void fundWalletTest() {
        Response funding = fundAddress(address, amount);
        Assertions.assertEquals(funding.statusCode(), SC_OK);
        firstTrxRef = funding.jsonPath().getString("tx_ref");

        Response response = getAddressInfo(address);
        Assertions.assertEquals(response.statusCode(), SC_OK);
        JsonPath balance = response.jsonPath();
        Assertions.assertEquals(balance.getInt("balance"), 0);
        Assertions.assertEquals(balance.getInt("final_balance"), amount);
        Assertions.assertEquals(balance.getInt("final_n_tx"), 1);
        Assertions.assertEquals(balance.getInt("n_tx"), 0);
        Assertions.assertEquals(balance.getInt("unconfirmed_n_tx"), 1);
        Assertions.assertEquals(balance.getInt("total_received"), 0);
        Assertions.assertTrue(response.asString().contains(firstTrxRef));
    }

    @Test
    @Order(3)
    @DisplayName("Fund wallet again and check transactions")
    public void transactionsCountTest() {
        Response funding = fundAddress(address, amount);
        Assertions.assertEquals(funding.statusCode(), SC_OK);
        String secondTrxRef = funding.jsonPath().getString("tx_ref");

        Response response = getAddressInfo(address);
        Assertions.assertEquals(response.statusCode(), SC_OK);
        JsonPath balance = response.jsonPath();
        Assertions.assertEquals(balance.getInt("balance"), 0);
        Assertions.assertEquals(balance.getInt("final_balance"), amount*2);
        Assertions.assertEquals(balance.getInt("final_n_tx"), 2);
        Assertions.assertEquals(balance.getInt("n_tx"), 0);
        Assertions.assertEquals(balance.getInt("unconfirmed_n_tx"), 2);
        Assertions.assertEquals(balance.getInt("total_received"), 0);
        Assertions.assertTrue(response.asString().contains(firstTrxRef));
        Assertions.assertTrue(response.asString().contains(secondTrxRef));
    }
}

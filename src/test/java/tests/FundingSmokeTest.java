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
        // Create wallet
        Response wallet = createWallet(walletName, address);
        Assertions.assertEquals(wallet.getStatusCode(), SC_CREATED);
        Assertions.assertEquals(wallet.jsonPath().getString("name"), walletName);
        Assertions.assertEquals(wallet.jsonPath().get("addresses[0]").toString(), address);

        // Check that wallet was created
        JsonPath addressInfo = getAddressInfo(address).jsonPath();
        Assertions.assertEquals(addressInfo.getInt("balance"), 0);
        Assertions.assertEquals(addressInfo.getInt("n_tx"), 0);
    }

    @Test
    @Order(2)
    @DisplayName("Fund wallet and check balance")
    public void fundWalletTest() {
        // Fund wallet
        Response funding = fundAddress(address, amount);
        Assertions.assertEquals(funding.statusCode(), SC_OK);
        firstTrxRef = funding.jsonPath().getString("tx_ref");

        // Check that wallet contains trx_ref
        Response response = getAddressInfo(address);
        Assertions.assertEquals(response.statusCode(), SC_OK);
        Assertions.assertTrue(response.asString().contains(firstTrxRef));
    }

    @Test
    @Order(3)
    @DisplayName("Fund wallet again and check transactions")
    public void transactionsValidationTest() {
        // Fund wallet one more time
        Response funding = fundAddress(address, amount * 2);
        Assertions.assertEquals(funding.statusCode(), SC_OK);
        String secondTrxRef = funding.jsonPath().getString("tx_ref");

        //Check transaction details
        Response firstTrxDetails = getTransaction(firstTrxRef);
        Response secondTrxDetails = getTransaction(secondTrxRef);
        int firstTrxAmount = firstTrxDetails.jsonPath().getInt("outputs[0].value");
        int secondTrxAmount = secondTrxDetails.jsonPath().getInt("outputs[0].value");

        //Validate trx on address
        Response response = getAddressInfo(address);
        Assertions.assertEquals(response.statusCode(), SC_OK);
        JsonPath balance = response.jsonPath();
        if (response.jsonPath().get("txrefs") == null) {
            Assertions.assertEquals(balance.getInt("unconfirmed_txrefs[1].value"), secondTrxAmount);
            Assertions.assertEquals(balance.getInt("unconfirmed_txrefs[0].value"), firstTrxAmount);
            Assertions.assertEquals(balance.getString("unconfirmed_txrefs[1].tx_hash"), secondTrxRef);
            Assertions.assertEquals(balance.getString("unconfirmed_txrefs[0].tx_hash"), firstTrxRef);
        } else {
            Assertions.assertEquals(balance.getInt("txrefs[1].value"), firstTrxAmount);
            Assertions.assertEquals(balance.getInt("txrefs[0].value"), secondTrxAmount);
            Assertions.assertEquals(balance.getString("txrefs[1].tx_hash"), firstTrxRef);
            Assertions.assertEquals(balance.getString("txrefs[0].tx_hash"), secondTrxRef);
        }
    }
}

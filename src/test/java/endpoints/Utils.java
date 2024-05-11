package endpoints;

import org.apache.commons.lang3.RandomStringUtils;

public class Utils {
    public static String prefix = "test-";

    public static String createWalletName() {
        return prefix + RandomStringUtils.randomAlphabetic(6).toLowerCase();
    }
}

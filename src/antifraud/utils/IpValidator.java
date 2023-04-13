package antifraud.utils;

import antifraud.exception.IncorrectIp;

public class IpValidator {

    public static void validateIp(String ip) throws IncorrectIp {
        String pattern = "^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$";
        if (!ip.matches(pattern)) {
            throw new IncorrectIp("Not a valid IP");
        }
    }
}

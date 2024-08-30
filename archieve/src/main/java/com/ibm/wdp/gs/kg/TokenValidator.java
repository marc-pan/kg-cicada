package com.ibm.wdp.gs.kg;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static java.lang.System.exit;

public class TokenValidator {
    public static void main(String[] args) {
        // String serviceIdCredential = "aWNwNGQtZGV2OnMxZWJrYnVkd3liOGM=";  // for cp4d
        String serviceIdCredential = "b21weGM2NXdPbk81dVRNZ0tOdzVFZ1p5TlBQanBzdUdSQkVSS1BGaEN4cGI="; // ys1dev
        if (serviceIdCredential == null) {
            System.out.println("The service ID credential is null.");
            exit(-1);
        } else {
            try {
                byte[] decodedBytes = Base64.getDecoder()
                    .decode(serviceIdCredential.getBytes(StandardCharsets.UTF_8));
                serviceIdCredential = new String(decodedBytes, StandardCharsets.UTF_8);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                exit(-1);
            }

            int pos = serviceIdCredential.indexOf(":");

            String serviceUserId = serviceIdCredential.substring(0, pos);
            String serviceUserPwd = serviceIdCredential.substring(pos + 1);
        }
    }
}

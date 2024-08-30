package com.ibm.wdp.gs.kg;

import java.util.Locale;

public class MyLocale {
    public static void main(String[] args) {
        var langs = Locale.getISOLanguages();
        for (String lang: langs) {
            System.out.printf("%s, \t", lang);
        }

        var endpoint = "search/index_ro_mode";
        if ("index_ro_mode".equalsIgnoreCase(endpoint)) {
            System.out.printf("true");
        }
    }
}

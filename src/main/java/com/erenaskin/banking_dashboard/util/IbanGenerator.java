// src/main/java/com/erenaskin/banking_dashboard/util/IbanGenerator.java
package com.erenaskin.banking_dashboard.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class IbanGenerator {
    public String generateIban() {
        return "TR" + UUID.randomUUID().toString().substring(0, 16).replace("-", "").toUpperCase();
    }
}

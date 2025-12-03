package com.gateway.template.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class HashUtilTest {

    private HashUtil hashUtil;

    @BeforeEach
    void setUp() {
        hashUtil = new HashUtil();
    }

    @Test
    void testHashAppNameConsistency() {
        String appName = "TestApp";
        String hash1 = hashUtil.hashAppName(appName);
        String hash2 = hashUtil.hashAppName(appName);
        
        assertEquals(hash1, hash2, "Same app name should produce same hash");
    }

    @Test
    void testHashAppNameDifferentInputs() {
        String hash1 = hashUtil.hashAppName("App1");
        String hash2 = hashUtil.hashAppName("App2");
        
        assertNotEquals(hash1, hash2, "Different app names should produce different hashes");
    }

    @Test
    void testHashAppNameNotNull() {
        String hash = hashUtil.hashAppName("TestApp");
        
        assertNotNull(hash);
        assertFalse(hash.isEmpty());
    }

    @Test
    void testHashAppNameCaseSensitive() {
        String hash1 = hashUtil.hashAppName("TestApp");
        String hash2 = hashUtil.hashAppName("testapp");
        
        assertNotEquals(hash1, hash2, "Hash should be case-sensitive");
    }
}

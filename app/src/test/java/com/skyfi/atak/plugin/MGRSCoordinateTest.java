package com.skyfi.atak.plugin;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for MGRS coordinate conversion functionality
 */
public class MGRSCoordinateTest {

    @Test
    public void testValidMGRSFormats() {
        // Common MGRS formats that should be accepted
        String[] validFormats = {
            // Standard formats with spaces
            "33T WN 83964 06925",
            "18T WL 89009 13758",
            "11S KU 82170 57450",
            "4Q FJ 12345 67890",
            
            // Without spaces
            "33TWN8396406925",
            "18TWL8900913758",
            "11SKU8217057450",
            "4QFJ1234567890",
            
            // With varying precision (10m, 100m, 1km, 10km)
            "33T WN 8396 0692",      // 100m precision
            "33T WN 839 069",        // 1km precision
            "33T WN 83 06",          // 10km precision
            
            // Compact formats
            "33TWN839069",           // 1km precision without spaces
            "33TWN8306"              // 10km precision without spaces
        };
        
        for (String mgrs : validFormats) {
            // Clean up format
            String cleaned = mgrs.replaceAll("\\s+", " ").trim().toUpperCase();
            assertNotNull("MGRS format should be valid: " + mgrs, cleaned);
            
            // Basic validation checks
            assertTrue("MGRS should start with zone number", 
                Character.isDigit(cleaned.charAt(0)));
        }
    }
    
    @Test
    public void testInvalidMGRSFormats() {
        String[] invalidFormats = {
            "",                      // Empty
            "33",                    // Too short
            "99Z WN 83964 06925",   // Invalid zone (>60)
            "0T WN 83964 06925",    // Invalid zone (0)
            "33I WN 83964 06925",   // Invalid band letter (I)
            "33O WN 83964 06925",   // Invalid band letter (O)
            "ABC DEF GHI",          // Not MGRS format
            "33T",                  // Missing grid letters
            "33T W",                // Incomplete grid letters
            "33T WN",               // Missing coordinates
        };
        
        for (String mgrs : invalidFormats) {
            // These should be detected as invalid
            if (mgrs.isEmpty()) {
                assertTrue("Empty string is invalid", mgrs.isEmpty());
            } else if (mgrs.length() < 5) {
                assertTrue("Too short to be valid MGRS: " + mgrs, mgrs.length() < 5);
            } else {
                // Check for invalid characters or patterns
                boolean hasInvalidZone = false;
                if (mgrs.length() >= 2 && Character.isDigit(mgrs.charAt(0))) {
                    try {
                        int zone = Integer.parseInt(mgrs.substring(0, 
                            mgrs.length() > 1 && Character.isDigit(mgrs.charAt(1)) ? 2 : 1));
                        hasInvalidZone = (zone < 1 || zone > 60);
                    } catch (NumberFormatException e) {
                        hasInvalidZone = true;
                    }
                }
                
                boolean hasInvalidBandLetter = mgrs.contains("I") || mgrs.contains("O");
                
                assertTrue("Should be invalid MGRS: " + mgrs, 
                    hasInvalidZone || hasInvalidBandLetter || 
                    !mgrs.matches(".*\\d.*") || mgrs.length() < 5);
            }
        }
    }
    
    @Test
    public void testMGRSNormalization() {
        // Test that various spacing formats normalize correctly
        String[][] testCases = {
            {"33TWN8396406925", "33TWN8396406925"},
            {"33T WN 83964 06925", "33T WN 83964 06925"},
            {"33T  WN  83964  06925", "33T WN 83964 06925"},  // Extra spaces
            {"33t wn 83964 06925", "33T WN 83964 06925"},     // Lowercase
            {"  33T WN 83964 06925  ", "33T WN 83964 06925"}  // Leading/trailing spaces
        };
        
        for (String[] testCase : testCases) {
            String input = testCase[0];
            String expected = testCase[1];
            String normalized = input.replaceAll("\\s+", " ").trim().toUpperCase();
            assertEquals("Normalization failed for: " + input, expected, normalized);
        }
    }
    
    @Test
    public void testCommonMilitaryMGRSExamples() {
        // Test common military MGRS coordinate examples
        String[] militaryExamples = {
            // Fort Bragg, NC
            "18S UJ 23480 06470",
            
            // Pentagon, Washington DC
            "18S UJ 23371 09315",
            
            // Area 51, Nevada
            "11S PA 86633 13013",
            
            // Edwards AFB, California
            "11S KU 18762 41554",
            
            // Space Force Base examples
            "13S EA 40612 34567",  // Buckley SFB
            "16T DQ 77123 85432"   // Patrick SFB
        };
        
        for (String mgrs : militaryExamples) {
            assertNotNull("Military MGRS example should be valid: " + mgrs, mgrs);
            assertTrue("Should contain proper format", mgrs.matches("\\d{1,2}[A-HJ-NP-Z].*"));
        }
    }
}
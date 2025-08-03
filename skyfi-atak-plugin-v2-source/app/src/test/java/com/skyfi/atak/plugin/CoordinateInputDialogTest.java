package com.skyfi.atak.plugin;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import com.atakmap.coremap.conversions.CoordinateFormat;
import com.atakmap.coremap.conversions.CoordinateFormatUtilities;
import com.atakmap.coremap.maps.coords.GeoPoint;

/**
 * Unit tests for CoordinateInputDialog MGRS functionality
 */
public class CoordinateInputDialogTest {

    @Before
    public void setUp() {
        // Setup test environment if needed
    }

    @Test
    public void testValidMGRS() {
        // Test valid MGRS coordinates
        String[] validMGRS = {
            "13SDD1234567890",  // Denver area
            "14SNH7348234567",  // Another valid MGRS
            "32TPJ1234567890"   // European example
        };

        for (String mgrs : validMGRS) {
            try {
                GeoPoint result = CoordinateFormatUtilities.convert(mgrs, CoordinateFormat.MGRS);
                assertNotNull("MGRS conversion should not return null for: " + mgrs, result);
                assertTrue("Converted coordinates should be valid for: " + mgrs, result.isValid());
                
                // Check that coordinates are within valid Earth bounds
                assertTrue("Latitude should be within bounds", 
                    result.getLatitude() >= -90 && result.getLatitude() <= 90);
                assertTrue("Longitude should be within bounds", 
                    result.getLongitude() >= -180 && result.getLongitude() <= 180);
            } catch (Exception e) {
                fail("Valid MGRS coordinate should not throw exception: " + mgrs + " - " + e.getMessage());
            }
        }
    }

    @Test
    public void testInvalidMGRS() {
        // Test invalid MGRS coordinates that should throw exceptions
        String[] invalidMGRS = {
            "",                 // Empty string
            "INVALID",          // Invalid format
            "13SDD123456789",   // Too short
            "13SDD12345678901", // Too long
            "99XYZ1234567890",  // Invalid zone/band
            "13XXX1234567890"   // Invalid grid square
        };

        for (String mgrs : invalidMGRS) {
            try {
                GeoPoint result = CoordinateFormatUtilities.convert(mgrs, CoordinateFormat.MGRS);
                if (result != null) {
                    assertFalse("Invalid MGRS should produce invalid GeoPoint: " + mgrs, result.isValid());
                }
            } catch (Exception e) {
                // Expected behavior - invalid MGRS should throw exception
                assertTrue("Exception should be thrown for invalid MGRS: " + mgrs, true);
            }
        }
    }

    @Test
    public void testMGRSRoundTrip() {
        // Test known coordinate conversions
        double testLat = 39.7392;
        double testLon = -104.9903;
        GeoPoint originalPoint = new GeoPoint(testLat, testLon);

        try {
            // Convert to MGRS and back
            String mgrsString = CoordinateFormatUtilities.formatToString(originalPoint, CoordinateFormat.MGRS);
            assertNotNull("MGRS formatting should not return null", mgrsString);
            assertFalse("MGRS string should not be empty", mgrsString.trim().isEmpty());

            GeoPoint convertedBack = CoordinateFormatUtilities.convert(mgrsString, CoordinateFormat.MGRS);
            assertNotNull("MGRS conversion back should not return null", convertedBack);
            assertTrue("Converted coordinates should be valid", convertedBack.isValid());

            // Check that round-trip conversion is reasonably accurate (within 1 meter precision)
            double distance = originalPoint.distanceTo(convertedBack);
            assertTrue("Round-trip conversion should be accurate within 1 meter, but was: " + distance + "m", 
                distance < 1.0);

        } catch (Exception e) {
            fail("Round-trip MGRS conversion should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testMGRSCaseInsensitive() {
        // Test that MGRS parsing is case insensitive
        String lowerCase = "13sdd1234567890";
        String upperCase = "13SDD1234567890";
        String mixedCase = "13SdD1234567890";

        try {
            GeoPoint result1 = CoordinateFormatUtilities.convert(lowerCase, CoordinateFormat.MGRS);
            GeoPoint result2 = CoordinateFormatUtilities.convert(upperCase, CoordinateFormat.MGRS);
            GeoPoint result3 = CoordinateFormatUtilities.convert(mixedCase, CoordinateFormat.MGRS);

            assertNotNull("Lower case MGRS should be valid", result1);
            assertNotNull("Upper case MGRS should be valid", result2);
            assertNotNull("Mixed case MGRS should be valid", result3);

            // All should result in the same coordinates (within precision)
            assertTrue("Case variations should produce same result", 
                result1.distanceTo(result2) < 0.1);
            assertTrue("Case variations should produce same result", 
                result1.distanceTo(result3) < 0.1);

        } catch (Exception e) {
            fail("Case insensitive MGRS parsing should work: " + e.getMessage());
        }
    }
}
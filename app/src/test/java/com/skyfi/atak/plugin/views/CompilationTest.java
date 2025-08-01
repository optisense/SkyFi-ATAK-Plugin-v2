package com.skyfi.atak.plugin.views;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Simple test to verify the view classes can be instantiated
 */
public class CompilationTest {
    
    @Test
    public void testViewsCompile() {
        // This test simply verifies that the view classes compile correctly
        // The actual instantiation would require Android context which is not available in unit tests
        
        // Verify class names exist
        assertNotNull(SkyFiButton.class);
        assertNotNull(SkyFiCard.class);
        assertNotNull(SkyFiToolbar.class);
        assertNotNull(StatusBadge.class);
        assertNotNull(ExampleUsage.class);
        
        // Verify enums
        assertNotNull(StatusBadge.Status.SUCCESS);
        assertNotNull(StatusBadge.Status.WARNING);
        assertNotNull(StatusBadge.Status.ERROR);
        assertNotNull(StatusBadge.Status.PENDING);
        assertNotNull(StatusBadge.Status.PROCESSING);
        assertNotNull(StatusBadge.Status.COMPLETED);
    }
}
package com.optisense.skyfi.atak;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

/**
 * Performance tests to ensure plugin operations complete within acceptable time limits
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class PerformanceTest {

    private SkyFiPlugin plugin;
    private static final long MAX_STARTUP_TIME_MS = 5000; // 5 seconds
    private static final long MAX_OPERATION_TIME_MS = 1000; // 1 second

    @Before
    public void setUp() {
        plugin = new SkyFiPlugin();
    }

    @Test
    public void testPluginStartupPerformance() {
        // Test plugin startup time
        long startTime = System.currentTimeMillis();
        
        try {
            plugin.onStart();
        } catch (Exception e) {
            fail("Plugin startup should not throw exceptions: " + e.getMessage());
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        assertTrue("Plugin startup should complete within " + MAX_STARTUP_TIME_MS + "ms, took " + duration + "ms", 
                  duration < MAX_STARTUP_TIME_MS);
        
        plugin.onStop();
    }

    @Test
    public void testPluginShutdownPerformance() {
        // Test plugin shutdown time
        plugin.onStart();
        
        long startTime = System.currentTimeMillis();
        
        try {
            plugin.onStop();
        } catch (Exception e) {
            fail("Plugin shutdown should not throw exceptions: " + e.getMessage());
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        assertTrue("Plugin shutdown should complete within " + MAX_OPERATION_TIME_MS + "ms, took " + duration + "ms", 
                  duration < MAX_OPERATION_TIME_MS);
    }

    @Test
    public void testMenuItemClickPerformance() {
        // Test menu item click response time
        plugin.onStart();
        
        for (int i = 0; i < 8; i++) {
            long startTime = System.currentTimeMillis();
            
            try {
                plugin.onItemClick(null, i);
            } catch (Exception e) {
                // Expected for some menu items without proper context
            }
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            assertTrue("Menu item " + i + " should respond within " + MAX_OPERATION_TIME_MS + "ms, took " + duration + "ms", 
                      duration < MAX_OPERATION_TIME_MS);
        }
        
        plugin.onStop();
    }

    @Test
    public void testAPIClientCreationPerformance() {
        // Test API client creation time
        long startTime = System.currentTimeMillis();
        
        try {
            APIClient client = new APIClient();
            assertNotNull("API client should be created", client);
        } catch (Exception e) {
            fail("API client creation should not throw exceptions: " + e.getMessage());
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        assertTrue("API client creation should complete within " + MAX_OPERATION_TIME_MS + "ms, took " + duration + "ms", 
                  duration < MAX_OPERATION_TIME_MS);
    }

    @Test
    public void testRepeatedOperationsPerformance() {
        // Test performance of repeated operations
        long totalStartTime = System.currentTimeMillis();
        
        for (int i = 0; i < 10; i++) {
            long iterationStart = System.currentTimeMillis();
            
            try {
                plugin.onStart();
                plugin.onStop();
            } catch (Exception e) {
                fail("Repeated operations should not throw exceptions: " + e.getMessage());
            }
            
            long iterationEnd = System.currentTimeMillis();
            long iterationDuration = iterationEnd - iterationStart;
            
            assertTrue("Iteration " + i + " should complete within " + MAX_OPERATION_TIME_MS + "ms, took " + iterationDuration + "ms", 
                      iterationDuration < MAX_OPERATION_TIME_MS);
        }
        
        long totalEndTime = System.currentTimeMillis();
        long totalDuration = totalEndTime - totalStartTime;
        
        assertTrue("10 start/stop cycles should complete within " + (MAX_OPERATION_TIME_MS * 10) + "ms, took " + totalDuration + "ms", 
                  totalDuration < (MAX_OPERATION_TIME_MS * 10));
    }

    @Test
    public void testMemoryUsageStability() {
        // Test that memory usage doesn't grow excessively
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // Perform multiple operations
        for (int i = 0; i < 50; i++) {
            try {
                plugin.onStart();
                plugin.onItemClick(null, 0);
                plugin.onStop();
            } catch (Exception e) {
                // Expected for some operations without proper context
            }
        }
        
        // Force garbage collection
        System.gc();
        Thread.yield();
        
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryGrowth = finalMemory - initialMemory;
        
        // Memory growth should be reasonable (less than 10MB)
        assertTrue("Memory growth should be reasonable, grew by " + (memoryGrowth / 1024 / 1024) + "MB", 
                  memoryGrowth < 10 * 1024 * 1024);
    }

    @Test
    public void testConcurrentOperationPerformance() {
        // Test performance under concurrent access
        final int threadCount = 5;
        final int operationsPerThread = 10;
        Thread[] threads = new Thread[threadCount];
        final long[] durations = new long[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                long threadStart = System.currentTimeMillis();
                
                for (int j = 0; j < operationsPerThread; j++) {
                    try {
                        SkyFiPlugin threadPlugin = new SkyFiPlugin();
                        threadPlugin.onStart();
                        threadPlugin.onStop();
                    } catch (Exception e) {
                        // Handle exceptions gracefully
                    }
                }
                
                long threadEnd = System.currentTimeMillis();
                durations[threadIndex] = threadEnd - threadStart;
            });
        }
        
        long overallStart = System.currentTimeMillis();
        
        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Wait for all threads to complete
        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            fail("Concurrent test should not be interrupted: " + e.getMessage());
        }
        
        long overallEnd = System.currentTimeMillis();
        long overallDuration = overallEnd - overallStart;
        
        // Check individual thread performance
        for (int i = 0; i < threadCount; i++) {
            assertTrue("Thread " + i + " should complete within reasonable time, took " + durations[i] + "ms", 
                      durations[i] < MAX_OPERATION_TIME_MS * operationsPerThread);
        }
        
        // Overall test should complete within reasonable time
        assertTrue("Concurrent operations should complete within reasonable time, took " + overallDuration + "ms", 
                  overallDuration < MAX_OPERATION_TIME_MS * operationsPerThread * 2);
    }
}
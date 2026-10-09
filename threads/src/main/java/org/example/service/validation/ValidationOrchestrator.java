package org.example.service.validation;

import org.example.context.TimetableDataContext;
import org.example.repository.PlaceRepository;
import java.util.*;
import java.util.concurrent.*;

/**
 * ValidationOrchestrator - Runs all validators in correct order
 *
 * Execution order:
 * 1. V0 (must pass - blocks rest if fails)
 * 2. V1+V5, V2, V3+V6, V4 (run in parallel - non-blocking)
 * 3. Generate comprehensive report
 *
 * NOTE: WARN validators never block - they're informative!
 */
public class ValidationOrchestrator {
    private static final int THREAD_POOL_SIZE = 5;
    private final ExecutorService mainExecutor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    private final TimetableDataContext dataContext;
    private final PlaceRepository placeRepository;

    public ValidationOrchestrator(TimetableDataContext dataContext, PlaceRepository placeRepository) {
        this.dataContext = dataContext;
        this.placeRepository = placeRepository;
    }

    /**
     * Run all validations in correct order with proper dependency handling
     */
    public ValidationReport runAllValidations() {
        long totalStartTime = System.currentTimeMillis();
        List<ValidationResult> results = new ArrayList<>();

        TeacherDefinitionValidator v0 = new TeacherDefinitionValidator();
        ValidationResult v0Result = v0.validate(dataContext);
        results.add(v0Result);

        if (v0Result.getStatus() == ValidationResult.Status.FAIL) {
            long totalTime = System.currentTimeMillis() - totalStartTime;
            return new ValidationReport(results, totalTime);
        }

        List<Future<ValidationResult>> futures = new ArrayList<>();

        TeacherCapacityValidator v1v5 = new TeacherCapacityValidator();
        futures.add(mainExecutor.submit(() -> v1v5.validate(dataContext)));

        RoomCapacityValidator v2 = new RoomCapacityValidator(placeRepository);
        futures.add(mainExecutor.submit(() -> v2.validate(dataContext)));

        TeacherAvailabilityValidator v3v6 = new TeacherAvailabilityValidator();
        futures.add(mainExecutor.submit(() -> v3v6.validate(dataContext)));

        TimeSlotCollisionValidator v4 = new TimeSlotCollisionValidator();
        futures.add(mainExecutor.submit(() -> v4.validate(dataContext)));

        for (Future<ValidationResult> future : futures) {
            try {
                results.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Validation interrupted", e);
            }
        }

        v1v5.shutdown();
        v2.shutdown();
        v4.shutdown();

        long totalTime = System.currentTimeMillis() - totalStartTime;
        return new ValidationReport(results, totalTime);
    }

    /**
     * Shutdown executor gracefully
     */
    public void shutdown() {
        mainExecutor.shutdown();
        try {
            if (!mainExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                mainExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            mainExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Validation Report - summarizes all results
     */
    public static class ValidationReport {
        private final List<ValidationResult> results;
        private final long totalTimeMs;

        public ValidationReport(List<ValidationResult> results, long totalTimeMs) {
            this.results = Collections.unmodifiableList(new ArrayList<>(results));
            this.totalTimeMs = totalTimeMs;
        }

        public List<ValidationResult> getResults() {
            return results;
        }

        public long getTotalTimeMs() {
            return totalTimeMs;
        }

        /**
         * Get overall status:
         * - FAIL if any critical validator fails
         * - WARN if any validator warns (but no failures)
         * - PASS if all pass
         *
         * IMPORTANT: WARN does NOT block generation!
         */
        public ValidationResult.Status getOverallStatus() {
            // Only FAIL blocks
            if (results.stream().anyMatch(r -> r.getStatus() == ValidationResult.Status.FAIL)) {
                return ValidationResult.Status.FAIL;
            }

            // WARN is informative but non-blocking
            if (results.stream().anyMatch(r -> r.getStatus() == ValidationResult.Status.WARN)) {
                return ValidationResult.Status.WARN;
            }

            return ValidationResult.Status.PASS;
        }
    }
}

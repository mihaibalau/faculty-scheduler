package org.example.ui;

import org.example.context.TimetableDataContext;
import org.example.model.Activity;
import org.example.service.generation.*;
import org.example.service.validation.ValidationOrchestrator;
import org.example.service.validation.ValidationResult;
import java.util.*;

/**
 * ConsoleUI - Handles ALL console output and user interaction.
 * This is the ONLY place that prints to console.
 * No service logic here - ONLY presentation.
 */
public class ConsoleUI {
    private static final String HEADER_COLOR = "\u001B[36m";    // Cyan
    private static final String SUCCESS_COLOR = "\u001B[32m";   // Green
    private static final String WARNING_COLOR = "\u001B[33m";   // Yellow
    private static final String ERROR_COLOR = "\u001B[31m";     // Red
    private static final String INFO_COLOR = "\u001B[34m";
    private static final String RESET_COLOR = "\u001B[0m";

    private static final Scanner scanner = new Scanner(System.in);

    // ==================== WELCOME & BANNERS ====================

    public static void printWelcome() {
        System.out.println();
        System.out.println(HEADER_COLOR + "Faculty scheduler" + RESET_COLOR);
        System.out.println();
    }

    // ==================== INITIALIZATION PHASE ====================

    public static void printInitializationPhase() {
        System.out.println(INFO_COLOR + "\nInitialization" + RESET_COLOR);
    }

    public static void printLoadingRepositories() {
        System.out.println(INFO_COLOR + "Loading repositories..." + RESET_COLOR);
    }

    public static void printRepositoriesLoaded() {
        System.out.println(SUCCESS_COLOR + "Repositories loaded" + RESET_COLOR);
    }

    public static void printBuildingContext() {
        System.out.println(INFO_COLOR + "Building data context..." + RESET_COLOR);
    }

    public static void printContextBuilt() {
        System.out.println(SUCCESS_COLOR + "Context ready" + RESET_COLOR);
    }

    public static void printDataLoadedSummary(TimetableDataContext context) {
        System.out.println(SUCCESS_COLOR + "Data loaded:" + RESET_COLOR);
        System.out.println("  • Groups:   " + context.getGroups().size());
        System.out.println("  • Teachers: " + context.getTeachers().size());
        System.out.println("  • Subjects: " + context.getSubjects().size());
        System.out.println("  • Places:   " + context.getPlaces().size());
        System.out.println();
    }

    // ==================== VALIDATION PHASE ====================

    public static void printValidationPhase() {
        System.out.println(INFO_COLOR + "\nValidation" + RESET_COLOR);
    }

    /**
     * Display validation results and prompt user for confirmation
     * @return true if user wants to proceed, false otherwise
     */
    public static boolean displayValidationResults(ValidationOrchestrator.ValidationReport report) {
        System.out.println();
        System.out.println(HEADER_COLOR + "Validation results" + RESET_COLOR);
        System.out.println();

        int passCount = 0, warnCount = 0, failCount = 0;

        for (var result : report.getResults()) {
            String statusStr = switch (result.getStatus()) {
                case PASS -> SUCCESS_COLOR + "PASS" + RESET_COLOR;
                case WARN -> WARNING_COLOR + "WARN" + RESET_COLOR;
                case FAIL -> ERROR_COLOR + "FAIL" + RESET_COLOR;
            };

            System.out.println(statusStr + " | " + result.getValidatorName() + " (" + result.getExecutionTimeMs() + "ms)");
            System.out.println("      " + result.getMessage());

            // Print detailed information if validator has details
            if (result.getDetails() != null && !result.getDetails().isEmpty()) {
                printValidationDetails(result.getDetails());
            }

            System.out.println();

            switch (result.getStatus()) {
                case PASS: passCount++; break;
                case WARN: warnCount++; break;
                case FAIL: failCount++; break;
            }
        }

        System.out.println("─".repeat(50));
        System.out.println(SUCCESS_COLOR + "PASS: " + passCount + RESET_COLOR);
        System.out.println(WARNING_COLOR + "WARN: " + warnCount + RESET_COLOR);
        System.out.println(ERROR_COLOR + "FAIL: " + failCount + RESET_COLOR);
        System.out.println("Total Time: " + report.getTotalTimeMs() + "ms");
        System.out.println("─".repeat(50));
        System.out.println();

        // Check overall status
        boolean canProceed = report.getOverallStatus() != ValidationResult.Status.FAIL;

        // Print recommendation
        String recommendation = switch (report.getOverallStatus()) {
            case PASS -> SUCCESS_COLOR + "All checks passed." + RESET_COLOR;
            case WARN -> WARNING_COLOR + "Warnings found. Generation can still run." + RESET_COLOR;
            case FAIL -> ERROR_COLOR + "Critical issues found. Fix them before proceeding." + RESET_COLOR;
        };

        System.out.println(recommendation);
        System.out.println();

        // If cannot proceed, return false
        if (!canProceed) {
            System.out.println(ERROR_COLOR + "Validation failed. Generation will not start." + RESET_COLOR);
            System.out.println();
            return false;
        }

        return promptUserConfirmation();
    }

    /**
     * Print detailed information from validation results
     */
    private static void printValidationDetails(Map<String, Object> details) {
        if (details.containsKey("invalidSubjects")) {
            @SuppressWarnings("unchecked")
            List<String> invalidItems = (List<String>) details.get("invalidSubjects");
            System.out.println(ERROR_COLOR + "      Invalid items:" + RESET_COLOR);
            for (String item : invalidItems) {
                System.out.println(ERROR_COLOR + "        • " + item + RESET_COLOR);
            }
        }
    }

    // ==================== GENERATION PHASE ====================

    public static void printGenerationPhase() {
        System.out.println(INFO_COLOR + "\nGeneration" + RESET_COLOR);
    }

    /**
     * Display generation results with detailed metrics
     */
    public static void displayGenerationResults(GenerationResult result) {
        System.out.println();
        System.out.println(HEADER_COLOR + "Generation results" + RESET_COLOR);
        System.out.println();

        if (result.success()) {
            System.out.println(SUCCESS_COLOR + "Generation completed" + RESET_COLOR);
        } else {
            System.out.println(ERROR_COLOR + "Generation failed: " + result.errorMessage() + RESET_COLOR);
        }

        System.out.println();
        System.out.println("Statistics");
        System.out.println("  Activities scheduled: " + result.getTotalActivities());
        System.out.println("  Success rate: " + String.format("%.1f%%", result.getSuccessRate()));
        System.out.println("  Total time: " + result.getTotalTimeMs() + "ms");

        System.out.println();
        System.out.println("Phase breakdown");
        for (Map.Entry<String, Long> entry : result.metrics().getPhaseTimes().entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue() + "ms");
        }

        System.out.println();
        System.out.println("Subjects");
        for (TimetableGenerator.SubjectGenerationResult subjectResult : result.subjectResults()) {
            String status = subjectResult.success() ? SUCCESS_COLOR + "ok" : ERROR_COLOR + "failed";
            System.out.println(status + RESET_COLOR + "  " + subjectResult.subjectName() +
                    " - " + subjectResult.scheduledHours() + " hours (" + subjectResult.executionTimeMs() + "ms)");
        }

        System.out.println();
        System.out.println("Metrics");
        for (Map.Entry<String, String> entry : result.metrics().getMetrics().entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }

        if (!result.metrics().getErrors().isEmpty()) {
            System.out.println();
            System.out.println(ERROR_COLOR + "Errors" + RESET_COLOR);
            for (String error : result.metrics().getErrors()) {
                System.out.println("  " + error);
            }
        }

        System.out.println();
    }

    // ==================== SUCCESS & FAILURE ====================

    public static void displayDetailedTimetable(List<Activity> activities) {
        if (activities == null || activities.isEmpty()) {
            System.out.println(INFO_COLOR + "No activities scheduled to display." + RESET_COLOR);
            return;
        }

        System.out.println();
        System.out.println(HEADER_COLOR + "Timetable" + RESET_COLOR);

        // Sort activities by day, then by start time, then by group
        List<Activity> sortedActivities = new ArrayList<>(activities);
        List<String> daysOrder = Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY");
        
        sortedActivities.sort(Comparator
                .comparingInt((Activity a) -> daysOrder.indexOf(a.day().toUpperCase()))
                .thenComparing(Activity::startTime)
                .thenComparing(Activity::groupId));

        String currentDay = "";
        Set<String> displayedCourseSlots = new HashSet<>();

        for (Activity activity : sortedActivities) {
            // Skip busy/closed slots if you want a clean timetable
            if ("BUSY".equals(activity.activityType()) || "CLOSED".equals(activity.activityType())) {
                continue;
            }

            // Deduplicate course entries (which are repeated for each group)
            if ("COURSE".equals(activity.activityType())) {
                String slotKey = activity.day() + "|" + activity.startTime() + "|" + activity.subjectName() + "|" + activity.roomId();
                if (displayedCourseSlots.contains(slotKey)) {
                    continue;
                }
                displayedCourseSlots.add(slotKey);
            }

            if (!activity.day().equalsIgnoreCase(currentDay)) {
                currentDay = activity.day().toUpperCase();
                System.out.println("\n" + INFO_COLOR + currentDay + RESET_COLOR);
                System.out.println("─".repeat(125));
            }

            String timeStr = String.format("%s - %s", activity.startTime(), activity.endTime());
            String typeColor = switch (activity.activityType()) {
                case "COURSE" -> "\u001B[34m"; // Blue
                case "SEMINAR" -> "\u001B[32m"; // Green
                case "LABORATORY" -> "\u001B[33m"; // Yellow
                default -> RESET_COLOR;
            };

            String groupDisplay = "COURSE".equals(activity.activityType()) ? "ALL" : activity.groupId();
            if (!"COURSE".equals(activity.activityType()) && activity.subgroup() != null && !activity.subgroup().isEmpty()) {
                groupDisplay += "/" + activity.subgroup();
            }

            System.out.printf("  %-15s | %s%-11s%s | Gr: %-7s | %-5s | %-10s | %-35s | %-20s%n",
                    timeStr,
                    typeColor, activity.activityType(), RESET_COLOR,
                    groupDisplay,
                    activity.roomId(),
                    activity.frequency(),
                    activity.subjectName(),
                    activity.teacherName()
            );
        }
        System.out.println("\n" + "─".repeat(125));
    }

    public static void printUserConfirmed() {
        System.out.println(SUCCESS_COLOR + "Generation will start" + RESET_COLOR);
    }

    public static void printNextSteps() {
        System.out.println(INFO_COLOR + "Exporting the schedule" + RESET_COLOR);
        System.out.println();
    }

    public static void printUserDeclined() {
        System.out.println(INFO_COLOR + "Stopped before generation." + RESET_COLOR);
        System.out.println();
    }

    // ==================== GENERIC MESSAGES ====================

    public static void printError(String message) {
        System.out.println(ERROR_COLOR + "Error: " + message + RESET_COLOR);
    }

    public static void printInfo(String message) {
        System.out.println(INFO_COLOR + message + RESET_COLOR);
    }

    public static void printSuccess(String message) {
        System.out.println(SUCCESS_COLOR + message + RESET_COLOR);
    }

    // ==================== PRIVATE METHODS ====================

    private static boolean promptUserConfirmation() {
        System.out.print(INFO_COLOR + "Do you want to proceed with timetable generation? (y/n): " + RESET_COLOR);
        String input = scanner.nextLine().trim().toLowerCase();
        return input.equals("y") || input.equals("yes");
    }
}


package com.example.readyfit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Collections;

public class WorkoutGenerator {

    // יצירה רגילה של אימון
    public static WorkoutData generate(String targetUnit, String runTime) {
        return generate(targetUnit, runTime, "שמירה על הרמה");
    }

    // יצירת אימון בהתאם לניתוח המשובים
    public static WorkoutData generate(
            String targetUnit,
            String runTime,
            String recommendation
    ) {

        if (targetUnit == null) {
            targetUnit = "";
        }

        if (recommendation == null) {
            recommendation = "שמירה על הרמה";
        }

        int seconds = parseRunTime(runTime);

        String level;

        if (seconds < 510) {
            level = "מתקדם";
        } else if (seconds < 630) {
            level = "בינוני";
        } else {
            level = "מתחיל";
        }

        boolean painReported =
                recommendation.contains("עצירת העלאת עומס");

        boolean reduceLoad =
                recommendation.contains("הפחתת עומס");

        boolean increaseLoad =
                recommendation.contains("אפשר לשקול התקדמות");

        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        String title;
        String description;
        String category;
        String duration;
        boolean recovery = false;

        // דיווח על כאב: אימון התאוששות במקום אימון רגיל
        if (painReported) {

            title = "התאוששות בעקבות דיווח כאב";
            description = "לא מעלים עומס. יש להימנע מתרגילים שגרמו לכאב ולברר את הסיבה לפני חזרה אליהם.";
            category = "התאוששות";
            duration = "15-25 דקות";
            recovery = true;

        } else {

            switch (targetUnit) {

                case "שייטת 13":

                    switch (day) {
                        case Calendar.SUNDAY:
                            title = "ריצת סיבולת";
                            description = "בניית בסיס אירובי וסיבולת";
                            category = "ריצה";
                            duration = "25-35 דקות";
                            break;

                        case Calendar.MONDAY:
                            title = "כוח גוף עליון וליבה";
                            description = "חיזוק פלג גוף עליון ושרירי הליבה";
                            category = "כוח";
                            duration = "30-40 דקות";
                            break;

                        case Calendar.TUESDAY:
                            recovery = true;
                            title = "יום התאוששות";
                            description = "התאוששות לאחר אימוני כוח";
                            category = "התאוששות";
                            duration = "15-25 דקות";
                            break;

                        case Calendar.WEDNESDAY:
                            title = "שחייה טכנית";
                            description = "שיפור טכניקת שחייה וסיבולת במים";
                            category = "שחייה";
                            duration = "25-35 דקות";
                            break;

                        case Calendar.THURSDAY:
                            title = "אימון כוח וסיבולת";
                            description = "שילוב כוח גוף וסיבולת";
                            category = "כוח";
                            duration = "30-40 דקות";
                            break;

                        case Calendar.FRIDAY:
                            recovery = true;
                            title = "יום התאוששות";
                            description = "מנוחה והכנה לשבוע הבא";
                            category = "התאוששות";
                            duration = "15-25 דקות";
                            break;

                        default:
                            title = "ריצת סיבולת קלה";
                            description = "שיפור כושר אירובי בקצב נוח";
                            category = "ריצה";
                            duration = "25-35 דקות";
                            break;
                    }
                    break;

                case "סיירת מטכ״ל":

                    switch (day) {
                        case Calendar.SUNDAY:
                            title = "ריצת סיבולת";
                            description = "שיפור יכולת מאמץ ממושך";
                            category = "ריצה";
                            duration = "30-45 דקות";
                            break;

                        case Calendar.MONDAY:
                            title = "כוח פונקציונלי";
                            description = "חיזוק הגוף ויציבות";
                            category = "כוח";
                            duration = "35-40 דקות";
                            break;

                        case Calendar.TUESDAY:
                            recovery = true;
                            title = "יום התאוששות";
                            description = "התאוששות לאחר ריצה וכוח";
                            category = "התאוששות";
                            duration = "15-25 דקות";
                            break;

                        case Calendar.WEDNESDAY:
                            title = "אימון עליות";
                            description = "שיפור כוח רגליים וסיבולת";
                            category = "עליות";
                            duration = "25-35 דקות";
                            break;

                        case Calendar.THURSDAY:
                            title = "יציבות וכוח";
                            description = "שיפור יציבות ושליטה בתנועה";
                            category = "כוח";
                            duration = "30-35 דקות";
                            break;

                        case Calendar.FRIDAY:
                            recovery = true;
                            title = "יום התאוששות";
                            description = "מנוחה והתאוששות";
                            category = "התאוששות";
                            duration = "15-25 דקות";
                            break;

                        default:
                            title = "ריצת קצב";
                            description = "שיפור היכולת לשמור על קצב";
                            category = "ריצה";
                            duration = "25-35 דקות";
                            break;
                    }
                    break;

                case "שלדג":

                    switch (day) {
                        case Calendar.SUNDAY:
                            title = "אימון מהירות";
                            description = "שיפור האצה ומהירות";
                            category = "מהירות";
                            duration = "25-35 דקות";
                            break;

                        case Calendar.MONDAY:
                            title = "כוח רגליים וליבה";
                            description = "חיזוק הרגליים ושרירי הליבה";
                            category = "כוח";
                            duration = "30-40 דקות";
                            break;

                        case Calendar.TUESDAY:
                            recovery = true;
                            title = "יום התאוששות";
                            description = "התאוששות לאחר אימון מהירות";
                            category = "התאוששות";
                            duration = "15-25 דקות";
                            break;

                        case Calendar.WEDNESDAY:
                            title = "זריזות ושינויי כיוון";
                            description = "שיפור שליטה ותגובה";
                            category = "זריזות";
                            duration = "25-30 דקות";
                            break;

                        case Calendar.THURSDAY:
                            title = "כוח מתפרץ מבוקר";
                            description = "פיתוח כוח ושליטה בתנועה";
                            category = "כוח";
                            duration = "30 דקות";
                            break;

                        case Calendar.FRIDAY:
                            recovery = true;
                            title = "יום התאוששות";
                            description = "מנוחה והתאוששות";
                            category = "התאוששות";
                            duration = "15-25 דקות";
                            break;

                        default:
                            title = "ריצת שטח";
                            description = "שיפור סיבולת בשטח בטוח";
                            category = "ריצת שטח";
                            duration = "25-35 דקות";
                            break;
                    }
                    break;

                case "חטיבת הקומנדו":

                    switch (day) {
                        case Calendar.SUNDAY:
                            title = "מסלול תחנות";
                            description = "שילוב כוח וסיבולת";
                            category = "מסלול תחנות";
                            duration = "30-40 דקות";
                            break;

                        case Calendar.MONDAY:
                            title = "ריצת סיבולת";
                            description = "שיפור יכולת ריצה";
                            category = "ריצה";
                            duration = "25-40 דקות";
                            break;

                        case Calendar.TUESDAY:
                            recovery = true;
                            title = "יום התאוששות";
                            description = "התאוששות לאחר אימונים";
                            category = "התאוששות";
                            duration = "15-25 דקות";
                            break;

                        case Calendar.WEDNESDAY:
                            title = "כוח ועבודת צוות";
                            description = "שיפור כוח ותיאום";
                            category = "עבודת צוות";
                            duration = "25-35 דקות";
                            break;

                        case Calendar.THURSDAY:
                            title = "סיבולת שרירית";
                            description = "שיפור היכולת לבצע מאמצים חוזרים";
                            category = "סיבולת שרירית";
                            duration = "30-40 דקות";
                            break;

                        case Calendar.FRIDAY:
                            recovery = true;
                            title = "יום התאוששות";
                            description = "מנוחה והתאוששות";
                            category = "התאוששות";
                            duration = "15-25 דקות";
                            break;

                        default:
                            title = "אינטרוולים";
                            description = "שיפור מהירות וסיבולת";
                            category = "אינטרוולים";
                            duration = "25-35 דקות";
                            break;
                    }
                    break;

                default:
                    title = "אימון כוח כללי";
                    description = "שיפור כושר גופני כללי";
                    category = "כוח";
                    duration = "30 דקות";
                    break;
            }
        }

        List<String> exercises;

        if (recovery) {
            exercises = createRecoveryExercises();
        } else {
            exercises = selectExercises(
                    targetUnit,
                    category,
                    level,
                    increaseLoad,
                    reduceLoad
            );
        }

        if (increaseLoad && !recovery) {
            description += " | התאמה הדרגתית בעקבות משובים חיוביים";
        } else if (reduceLoad && !recovery) {
            description += " | עומס מופחת בעקבות המשובים האחרונים";
        }

        return new WorkoutData(
                title,
                description,
                duration,
                level,
                category,
                exercises
        );
    }

    private static List<String> selectExercises(
            String targetUnit,
            String category,
            String level,
            boolean increaseLoad,
            boolean reduceLoad
    ) {

        List<String> library = ExerciseLibrary.getExercises(targetUnit);
        List<String> selected = new ArrayList<>();

        if (library == null || library.isEmpty()) {
            selected.add("לא נמצאו תרגילים במאגר");
            return selected;
        }

        List<String> matching = new ArrayList<>();

        for (String exercise : library) {

            String value = exercise.toLowerCase();
            boolean match = false;

            switch (category) {

                case "ריצה":
                case "ריצת שטח":
                    match = containsAny(value,
                            "ריצ", "הליכת כוח", "מדרגות");
                    break;

                case "שחייה":
                    match = containsAny(value,
                            "שחיי", "חתירה", "רגליים במים");
                    break;

                case "כוח":
                    match = containsAny(value,
                            "שכיבות", "מתח", "סקוואט",
                            "לאנג", "פלאנק", "חתירה",
                            "גשר", "dead bug", "bird dog",
                            "הרמות עקבים", "כפיפת ירך",
                            "הליכת איכר", "מדרגה");
                    break;

                case "עליות":
                    match = containsAny(value,
                            "עליות", "מדרגות", "ריצת שטח");
                    break;

                case "מהירות":
                case "אינטרוולים":
                    match = containsAny(value,
                            "ספרינט", "האצות", "אינטרוולים",
                            "ריצת שאטל", "ריצות מהירות");
                    break;

                case "זריזות":
                    match = containsAny(value,
                            "זריזות", "קונוסים", "צעדי צד",
                            "שינוי כיוון", "שיווי משקל",
                            "סולם זריזות");
                    break;

                case "מסלול תחנות":
                case "סיבולת שרירית":
                    match = containsAny(value,
                            "שכיבות", "סקוואט", "לאנג",
                            "הליכת דוב", "פלאנק",
                            "זחילה", "תחנות");
                    break;

                case "עבודת צוות":
                    match = containsAny(value,
                            "צוות", "שליחים", "נשיאת",
                            "הליכה קבוצתית", "כדור");
                    break;
            }

            if (match) {
                matching.add(exercise);
            }
        }

        if (matching.isEmpty()) {
            matching.addAll(library);
        }

        Collections.shuffle(matching);

        int amount = level.equals("מתחיל") ? 4 :
                level.equals("בינוני") ? 5 : 6;

        // תוספת קטנה בלבד לאחר משובים חיוביים לאורך זמן
        if (increaseLoad) {
            amount = Math.min(amount + 1, 7);
        }

        // הפחתה מתונה לאחר משובים קשים
        if (reduceLoad) {
            amount = Math.max(amount - 1, 3);
        }

        for (int i = 0; i < Math.min(amount, matching.size()); i++) {
            selected.add(matching.get(i));
        }

        selected.add("מנוחה בין תרגילים: 60-90 שניות");
        selected.add("סיום: הליכה קלה ומתיחות עדינות");

        if (increaseLoad) {
            selected.add("התקדמות הדרגתית: תוספת תרגיל אחד בלבד");
        }

        if (reduceLoad) {
            selected.add("עומס מופחת: יש לבצע בקצב נוח ולהקפיד על התאוששות");
        }

        return selected;
    }

    private static boolean containsAny(String text, String... words) {

        for (String word : words) {
            if (text.contains(word)) {
                return true;
            }
        }

        return false;
    }

    private static List<String> createRecoveryExercises() {

        List<String> exercises = new ArrayList<>();

        exercises.add("הליכה קלה: 10 דקות, רק אם אינה גורמת לכאב");
        exercises.add("תנועתיות עדינה: 5 דקות, ללא כאב");
        exercises.add("מנוחה ושתיית מים");
        exercises.add("אין לבצע מאמץ עצים ביום התאוששות");
        exercises.add("אם הכאב נמשך, יש לפנות לבדיקה מקצועית לפני חזרה לאימון");

        return exercises;
    }

    private static int parseRunTime(String time) {

        if (time == null || time.trim().isEmpty()) {
            return 600;
        }

        try {
            String[] parts = time.trim().split(":");

            if (parts.length != 2) {
                return 600;
            }

            int minutes = Integer.parseInt(parts[0].trim());
            int seconds = Integer.parseInt(parts[1].trim());

            if (minutes < 0 || seconds < 0 || seconds > 59) {
                return 600;
            }

            return minutes * 60 + seconds;

        } catch (Exception e) {
            return 600;
        }
    }
}

package ui;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Input {

    private static final Scanner in = new Scanner(System.in);
    //date format (strict)
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    // prevent object creation
    private Input() {
    }
    //input line
    public static String line(String prompt) {
        System.out.print(prompt);
        try {
            return in.nextLine().trim();
        } catch (NoSuchElementException e) {
            throw new IllegalStateException("Không có dữ liệu nhập từ bàn phím");
        }
    }
    //constraint
    public static String required(String prompt) {
        while (true) {
            String s = line(prompt);
            if (!s.isEmpty()) {
                return s;
            }
            System.out.println("Lỗi: Không được để trống vui lòng nhập lại.");
        }
    }

    public static String optional(String prompt) {
        String s = line(prompt);
        return s.isEmpty() ? null : s;
    }
    //prevent integer input errors
    public static int integer(String prompt) {
        while (true) {
            String s = line(prompt);
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.println("Lỗi: Vui lòng nhập số nguyên hợp lệ.");
            }
        }
    }

    public static Integer optionalInt(String prompt) {
        while (true) {
            String s = line(prompt);
            if (s.isEmpty()) {
                return null;
            }
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.println("Lỗi: Vui lòng nhập số nguyên hợp lệ (Hoặc để trống).");
            }
        }
    }
    
    public static double decimal(String prompt) {
        while (true) {
            String s = line(prompt);
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                System.out.println("Lỗi: Vui lòng nhập số hợp lệ.");
            }
        }
    }

    public static Double optionalDecimal(String prompt) {
        while (true) {
            String s = line(prompt);
            if (s.isEmpty()) {
                return null;
            }
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                System.out.println("Lỗi: Vui lòng nhập số hợp lệ (Hoặc để trống).");
            }
        }
    }

    public static LocalDate date(String prompt) {
        while (true) {
            String s = line(prompt);
            try {
                return LocalDate.parse(s, DATE_FMT);
            } catch (DateTimeParseException e) {
                System.out.println("Lỗi: Vui lòng nhập đúng định dạng (dd/MM/yyyy).");
            }
        }
    }

    public static LocalDate optionalDate(String prompt) {
        while (true) {
            String s = line(prompt);
            if (s.isEmpty()) {
                return null;
            }
            try {
                return LocalDate.parse(s, DATE_FMT);
            } catch (DateTimeParseException e) {
                System.out.println("Lỗi: Vui lòng nhập đúng định dạng (d/MM/yyyy) hoặc để trống.");
            }
        }
    }

    private static <T> void printOptions(String prompt, T[] options, boolean allowSkip) {
        System.out.println(prompt);
        for (int i = 0; i < options.length; i++) {
            System.out.println("  " + (i + 1) + ". " + options[i]);
        }
        if (allowSkip) {
            System.out.println("  0. Bỏ qua");
        }
    }

    public static <T extends Enum<T>> T choose(String prompt, T[] values) {
    while (true) {
        printOptions(prompt, values, false);
        String s = line("Chon (1-" + values.length + "): ");
        try {
            int idx = Integer.parseInt(s);
            if (idx >= 1 && idx <= values.length) {
                return values[idx - 1];
            }
        } catch (NumberFormatException ignored) {

        }
        System.out.println("Lỗi: Lựa chọn không hợp lệ, vui lòng nhập lại.");
    }
}

    public static <T extends Enum<T>> T chooseOptional(String prompt, T[] values) {
        while (true) {
            printOptions(prompt, values, true);
            String s = line("Chon (0-" + values.length + "): ");
            try {
                int idx = Integer.parseInt(s);
                if (idx == 0) {
                    return null;
                }
                if (idx >= 1 && idx <= values.length) {
                    return values[idx - 1];
                }
            } catch (NumberFormatException ignored) {
            }
            System.out.println("Lỗi: Lựa chọn không hợp lệ, vui lòng nhập lại.");
        }
    }

    public static boolean confirm(String prompt) {
        while (true) {
            String s = line(prompt + " (y/n): ").toLowerCase();
            if (s.equals("y") || s.equals("yes")) {
                return true;
            }
            if (s.equals("n") || s.equals("no")) {
                return false;
            }
            System.out.println("Lỗi: Vui lòng nhập y hoặc n.");
        }
    }
    // thêm hàm integer với giới hạn min và max
    public static int integer(String prompt, int min, int max) {
    while (true) {
        int val = integer(prompt);
        if (val >= min && val <= max) {
            return val;
        }
        System.out.println("Lỗi: Vui lòng nhập số từ " + min + " đến " + max + "!");
    }
}
}

package exception;

public class NotFoundException extends Exception {
    public NotFoundException(String what, Object id) {
        super("Không tìm thấy " + what + " có mã: " + id);
    }
}

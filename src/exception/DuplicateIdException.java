package exception;

public class DuplicateIdException extends Exception {
    public DuplicateIdException(String what, Object id) {
        super("Mã " + what + " '" + id + "' đã tồn tại");
    }
}


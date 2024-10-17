package dataaccess;

/**
 * Indicates there is already info for this insert
 */
public class DuplicateInfoException extends DataAccessException {
    public DuplicateInfoException(String message) {
        super(message);
    }
}

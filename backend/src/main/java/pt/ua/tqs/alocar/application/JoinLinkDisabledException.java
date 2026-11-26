package pt.ua.tqs.alocar.application;

/** UC1 exception: the join link exists but the franchisee disabled it, so no account is created. */
public class JoinLinkDisabledException extends RuntimeException {

    public JoinLinkDisabledException(String message) {
        super(message);
    }
}

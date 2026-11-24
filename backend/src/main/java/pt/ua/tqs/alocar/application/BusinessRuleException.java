package pt.ua.tqs.alocar.application;

/** A request refused by a business rule of the Product Specs; {@code rule} is its ID (e.g. "BR6"). */
public class BusinessRuleException extends RuntimeException {

    private final String rule;

    public BusinessRuleException(String rule, String message) {
        super(message);
        this.rule = rule;
    }

    public String getRule() {
        return rule;
    }
}

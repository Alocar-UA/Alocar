package pt.ua.tqs.alocar.adapters.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pt.ua.tqs.alocar.application.port.PaymentGateway;

/**
 * Stand-in for the payment sandbox until the real one is integrated: approves every charge and
 * logs it.
 */
@Component
public class SandboxPaymentGateway implements PaymentGateway {

    private static final Logger LOG = LoggerFactory.getLogger(SandboxPaymentGateway.class);

    @Override
    public boolean charge(Long residentId, long amountCents, String description) {
        LOG.info("Sandbox payment approved: resident={} amountCents={} ({})", residentId, amountCents, description);
        return true;
    }
}

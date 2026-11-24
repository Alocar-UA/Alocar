package pt.ua.tqs.alocar.adapters.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pt.ua.tqs.alocar.application.BusinessRuleException;
import pt.ua.tqs.alocar.application.NotFoundException;
import pt.ua.tqs.alocar.application.PaymentDeclinedException;

/** Maps application errors to RFC 9457 problem details; refused rules carry the rule ID. */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    ProblemDetail notFound(NotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(BusinessRuleException.class)
    ProblemDetail ruleViolated(BusinessRuleException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
        problem.setProperty("rule", e.getRule());
        return problem;
    }

    @ExceptionHandler(PaymentDeclinedException.class)
    ProblemDetail paymentDeclined(PaymentDeclinedException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.PAYMENT_REQUIRED, e.getMessage());
    }

    /** Two residents tried to pick up the same scooter at the same time; the second one loses. */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    ProblemDetail concurrentPickUp(ObjectOptimisticLockingFailureException e) {
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "A trotinete acabou de ser levantada");
        problem.setProperty("rule", "BR3");
        return problem;
    }
}

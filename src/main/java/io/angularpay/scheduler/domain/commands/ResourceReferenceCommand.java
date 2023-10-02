package io.angularpay.scheduler.domain.commands;

public interface ResourceReferenceCommand<T, R> {

    R map(T referenceResponse);
}

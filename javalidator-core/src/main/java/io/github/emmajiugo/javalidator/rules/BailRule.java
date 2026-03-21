package io.github.emmajiugo.javalidator.rules;

import io.github.emmajiugo.javalidator.FlowControlRule;

public class BailRule implements FlowControlRule {
    @Override
    public String validate(String fieldName, Object value, String parameter) {
        return null; // Never fails -- flow control only
    }
}

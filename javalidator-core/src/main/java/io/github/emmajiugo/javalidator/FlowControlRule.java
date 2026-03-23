package io.github.emmajiugo.javalidator;

/**
 * Marker interface for flow-control rules that modify validation behavior
 * rather than performing validation themselves.
 *
 * Rules implementing this interface (e.g., bail) are detected by the Validator
 * during rule iteration to alter the processing flow. They always return null
 * from validate (never fail).
 */
public interface FlowControlRule extends ValidationRule {
}

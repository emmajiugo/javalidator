package io.github.emmajiugo.javalidator;

import io.github.emmajiugo.javalidator.annotations.Rule;
import io.github.emmajiugo.javalidator.model.ValidationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Concurrency Tests")
class ConcurrencyTest {

    record UserDTO(
            @Rule("required|min:3|max:20") String username,
            @Rule("required|email") String email
    ) {}

    @Test
    @DisplayName("should handle concurrent validation safely")
    void shouldHandleConcurrentValidation() throws Exception {
        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        List<Future<ValidationResponse>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            futures.add(executor.submit(() -> {
                latch.await();
                if (idx % 2 == 0) {
                    return Validator.validate(new UserDTO("john", "john@example.com"));
                } else {
                    return Validator.validate(new UserDTO("", "bad"));
                }
            }));
        }

        latch.countDown();

        for (int i = 0; i < threadCount; i++) {
            ValidationResponse response = futures.get(i).get(5, TimeUnit.SECONDS);
            if (i % 2 == 0) {
                assertThat(response.valid()).isTrue();
            } else {
                assertThat(response.valid()).isFalse();
            }
        }

        executor.shutdown();
    }

    @Test
    @DisplayName("should handle concurrent custom rule registration")
    void shouldHandleConcurrentRegistration() throws Exception {
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        List<Future<Void>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            futures.add(executor.submit(() -> {
                latch.await();
                RuleRegistry.register(new ValidationRule() {
                    @Override
                    public String validate(String fieldName, Object value, String parameter) { return null; }
                    @Override
                    public String getName() { return "custom_concurrent_" + idx; }
                });
                return null;
            }));
        }

        latch.countDown();
        for (Future<Void> f : futures) f.get(5, TimeUnit.SECONDS);
        for (int i = 0; i < threadCount; i++) {
            assertThat(RuleRegistry.hasRule("custom_concurrent_" + i)).isTrue();
        }
        executor.shutdown();
    }
}

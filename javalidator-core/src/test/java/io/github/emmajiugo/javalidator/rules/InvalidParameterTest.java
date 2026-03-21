package io.github.emmajiugo.javalidator.rules;

import io.github.emmajiugo.javalidator.Validator;
import io.github.emmajiugo.javalidator.annotations.Rule;
import io.github.emmajiugo.javalidator.config.ValidationConfig;
import io.github.emmajiugo.javalidator.model.ValidationResponse;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Invalid Rule Parameter Handling")
class InvalidParameterTest {

    @AfterEach
    void resetConfig() {
        Validator.setConfig(ValidationConfig.defaults());
    }

    @Nested
    @DisplayName("Strict Mode")
    class StrictModeTests {
        @BeforeEach
        void enableStrictMode() {
            Validator.setConfig(ValidationConfig.builder().strictMode(true).build());
        }

        @Test @DisplayName("should throw on non-numeric min parameter")
        void shouldThrowOnNonNumericMin() {
            record BadMin(@Rule("min:abc") String name) {}
            assertThatThrownBy(() -> Validator.validate(new BadMin("test")))
                    .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("Graceful Mode (default)")
    class GracefulModeTests {
        @Test @DisplayName("should convert non-numeric min to validation error")
        void shouldConvertBadMinToError() {
            record BadMin(@Rule("min:abc") String name) {}
            ValidationResponse response = Validator.validate(new BadMin("test"));
            assertThat(response.valid()).isFalse();
            assertThat(response.errors().get(0).messages().get(0)).contains("CONFIG ERROR");
        }
    }
}

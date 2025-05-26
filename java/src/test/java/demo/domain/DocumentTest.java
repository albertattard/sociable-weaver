package demo.domain;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class DocumentTest {

    @Test
    void runFinallyInTheReverseOder() {
        final AtomicLong counter = new AtomicLong();

        final Document document = new Document(List.of(
                ok(counter, 3),
                ok(counter, 2),
                ok(counter, 1)));

        document.run(_ -> {});

        assertThat(counter.get())
                .describedAs("All finally blocks should be executed")
                .isEqualTo(3);
    }

    @Test
    void runFinallyIsOnlyInvokedOnTheEntriesThatRan() {
        final AtomicLong counter = new AtomicLong();

        final Document document = new Document(List.of(
                ok(counter, 2),
                error(counter, 1),
                noCall()));

        document.run(_ -> {});

        assertThat(counter.get())
                .describedAs("Only the finally blocks of the entries that ran should be executed")
                .isEqualTo(2);
    }

    private static Entry ok(final AtomicLong counter, final long expectedFinallyExecutionOrder) {
        return new Entry() {

            @Override
            public Result run() {
                return Result.ok("Test Entry");
            }

            @Override
            public void runFinally() {
                assertThat(counter.incrementAndGet())
                        .describedAs("The finally block should be executed in reverse order")
                        .isEqualTo(expectedFinallyExecutionOrder);
            }
        };
    }

    private static Entry error(final AtomicLong counter, final long expectedFinallyExecutionOrder) {
        return new Entry() {
            @Override
            public Result run() {
                return Result.error("Test Entry");
            }

            @Override
            public void runFinally() {
                assertThat(counter.incrementAndGet())
                        .describedAs("The finally block should be executed in reverse order")
                        .isEqualTo(expectedFinallyExecutionOrder);
            }
        };
    }

    private static Entry noCall() {
        return new Entry() {
            @Override
            public Result run() {
                fail("This run block should have not been called");
                return Result.error("This run block should have not been called");
            }

            @Override
            public void runFinally() {
                fail("This finally block should have not been called");
            }
        };
    }
}
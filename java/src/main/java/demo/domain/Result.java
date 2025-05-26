package demo.domain;

public sealed interface Result {

    record Ok(String output) implements Result {}

    record Error(String error) implements Result {}

    static Result ok(final String output) {
        return new Ok(output);
    }

    static Result error(final String error) {
        return new Error(error);
    }
}

package nl.han.ica.exception;

public class OperationContainsUndefinedVariableException extends Exception {
    private final String name;

    public OperationContainsUndefinedVariableException(String name) {

        this.name = name;
    }

    public String getName() {
        return name;
    }
}

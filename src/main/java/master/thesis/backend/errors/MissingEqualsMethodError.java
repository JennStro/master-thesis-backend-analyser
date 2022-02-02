package master.thesis.backend.errors;

import java.util.Optional;

public class MissingEqualsMethodError extends BaseError {

    @Override
    public Optional<String> getSuggestion() {
        return Optional.of( "to add the method @Override public boolean equals(Object o) { \\Checks to decide if two objects are equal goes here }" );
    }

    @Override
    public Optional<String> getTip() {
        return Optional.of("Tip: Maybe your IDE has something like \"generate equals and hash methods\"?");
    }

    @Override
    public String getWhat() {
        return "The equals method is missing! If you do not implement the equals method of a class, it will use the default equals method of class Object.";
    }

    @Override
    public Optional<String> getLink() {
        return Optional.of("https://master-thesis-frontend-prod.herokuapp.com/equalsoperator");
    }

}

package master.thesis.backend.errors;

import java.util.Optional;

public class MissingEqualsMethodError extends BaseError {

    @Override
    public Optional<String> getSuggestion() {
        return Optional.of("You should add the method \n \n "
                + "@Override \n"
                + "public boolean equals(Object o) { \n"
                + "   //... Your implementation here... \n"
                + "}" +
                "\n" +
                "Tip: Maybe your IDE has something like \"generate equals and hash methods\"?");
    }

    @Override
    public String getWhat() {
        return "You should implement the equals method! If you do not implement the equals method of a class, it will use the default equals method of class Object.";
    }

    @Override
    public Optional<String> getLink() {
        return Optional.empty();
    }

}

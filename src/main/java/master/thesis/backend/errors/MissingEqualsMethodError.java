package master.thesis.backend.errors;

import java.util.Optional;

public class MissingEqualsMethodError extends BaseError {

    @Override
    public Optional<String> getSuggestion() {
        return Optional.of("You should add the method \n \n "
                + "@Override \n"
                + "public boolean equals(Object o) { \n"
                + "   //... Your implementation here... \n"
                + "}");
    }

    @Override
    public String getWhat() {
        return "You should implement the equals method!";
    }

}

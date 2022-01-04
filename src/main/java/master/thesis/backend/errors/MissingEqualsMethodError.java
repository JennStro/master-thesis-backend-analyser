package master.thesis.backend.errors;

public class MissingEqualsMethodError extends BaseError {

    public MissingEqualsMethodError(int offset, int length) {
        super(offset, length);
    }
    @Override
    public boolean hasSuggestion() {
        return true;
    }

    @Override
    public String getSuggestion() {
        return "You should add the method \n \n "
                + "@Override \n"
                + "public boolean equals(Object o) { \n"
                + "   //... Your implementation here... \n"
                + "}";
    }

    @Override
    public String getWhat() {
        return "You should implement the equals method!";
    }

}

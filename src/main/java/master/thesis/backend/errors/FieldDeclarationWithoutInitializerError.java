package master.thesis.backend.errors;

import java.util.Optional;

public class FieldDeclarationWithoutInitializerError extends BaseError {

    private String type;
    private String name;

    public void setFieldVariableType(String type) {
        this.type = type;
    }

    public void setFieldVariableName(String name) {
        this.name = name;
    }

    @Override
    public Optional<String> getSuggestion() {
        if (this.containingClass != null && this.name != null && this.type != null) {
           return Optional.of("You could initialize the fieldvariable in the constructor: \n \n"
                    + "public " + this.containingClass + "(" + this.type + " " + this.name + ") { \n "
                    + "	this." + this.name + " = " + this.name + ";\n"
                    + "}");
        }
        return Optional.empty();
    }

    @Override
    public String getWhat() {
        return "You should initialize the field variable!";
    }

    @Override
    public Optional<String> getLink() {
        return Optional.empty();
    }

}

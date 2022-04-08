package master.thesis.backend.errors;

import master.thesis.backend.annotations.AnnotationNames;

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
           return Optional.of("public " + this.containingClass + "(" + this.type + " " + this.name + ") {"
                    + "	this." + this.name + " = " + this.name + ";"
                    + "}");
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getTip() {
        return Optional.of("Tip: You could also initialize the field directly!");
    }

    @Override
    public String getCauseOfError() {
        return "You should initialize the field variable!";
    }

    @Override
    public Optional<String> getMoreInfoLink() {
        return Optional.empty();
    }

    @Override
    public String annotationName() {
        return AnnotationNames.FIELD_DECL_NO_INITIALIZER_ERROR;
    }

}

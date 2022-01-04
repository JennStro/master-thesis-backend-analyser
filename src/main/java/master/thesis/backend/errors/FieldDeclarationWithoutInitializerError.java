package master.thesis.backend.errors;

public class FieldDeclarationWithoutInitializerError extends BaseError {

    private String type;
    private String className;
    private String name;

    public FieldDeclarationWithoutInitializerError(int offset, int length) {
        super(offset, length);
    }

    public void setFieldVariableType(String type) {
        this.type = type;
    }

    public void setClass(String className) {
        this.className = className;
    }

    public void setFieldVariableName(String name) {
        this.name = name;
    }

    @Override
    public boolean hasSuggestion() {
        return this.className != null &&
                this.name != null &&
                this.type != null;
    }

    @Override
    public String getSuggestion() {
        return "You could initialize the fieldvariable in the constructor: \n \n"
                + "public " + this.className + "(" + this.type + " " + this.name + ") { \n "
                + "	this." + this.name + " = " + this.name + ";\n"
                + "}";
    }

    @Override
    public String getWhat() {
        return "You should initialize the field variable!";
    }

}
package visitor;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import errors.BugReport;

public class BugFinderVisitor  extends VoidVisitorAdapter<Void> {

    private BugReport report = new BugReport();

    @Override
    public void visit(MethodDeclaration md, Void arg) {
         super.visit(md, arg);
         System.out.println("Method Name Printed: " + md.getName());
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration decl, Void arg) {
        super.visit(decl, arg);
        System.out.println("Class:" + decl.getName());
    }

    public BugReport getReport() {
        return report;
    }
}

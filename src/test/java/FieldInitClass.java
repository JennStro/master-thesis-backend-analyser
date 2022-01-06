import master.thesis.backend.annotations.NoEqualsMethod;

import java.util.ArrayList;

@NoEqualsMethod
public class FieldInitClass {

    private ArrayList<String> list;
    
    public void setList(ArrayList<String> list) {
        this.list = list;
    }
    
    public void addToList(String word) {
        try {this.list.add(word);
            System.out.println("SUCCESS");}
        catch (NullPointerException e) {
            System.out.println("FAIL");
        }
    }
    
    public static void main(String[] args) {
        FieldInitClass clazz = new FieldInitClass();
        clazz.addToList("word");
    }

}

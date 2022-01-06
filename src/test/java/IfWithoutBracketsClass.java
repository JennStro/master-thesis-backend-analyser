import java.util.ArrayList;

public class IfWithoutBracketsClass {

    public ArrayList<String> method(boolean shouldAddToList) {
        ArrayList<String> list = new ArrayList<>();
        if (shouldAddToList)
            list.add("1");
            list.add("2");
        return list;
    }
    public static void main(String[] args) {
        IfWithoutBracketsClass clazz = new IfWithoutBracketsClass();
        ArrayList<String> list = clazz.method(false);
        if (list.size() != 0) {
            System.out.println("FAIL");
        } else {
            System.out.println("SUCCESS");
        }
    }
}

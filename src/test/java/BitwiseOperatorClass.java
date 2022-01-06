import master.thesis.backend.annotations.NoInitialization;

import java.util.ArrayList;

public class BitwiseOperatorClass {

    public void method(Bar bar) {
        if (bar.drinksHaveBeenInitialized() & bar.barHasMoreThenFiveDrinks()) {
            bar.thisMethodDoesNothing();
        }
        bar.getStatus();
    }

    public static class Bar {

        private boolean hasBeenCalled = false;
        @NoInitialization
        private ArrayList<String> drinks;

        public boolean barHasMoreThenFiveDrinks() {
            hasBeenCalled = true;
            System.out.println("FAIL");
            return drinks.size() > 5;
        }

        public void getStatus() {
            if (hasBeenCalled) {
                System.out.println("FAIL");
            } else {
                System.out.println("SUCCESS");
            }
        }

        public void thisMethodDoesNothing() {}

        public boolean drinksHaveBeenInitialized() {
            return this.drinks != null;
        }
    }

    public static void main(String[] args) {
        BitwiseOperatorClass clazz = new BitwiseOperatorClass();
        Bar bar = new Bar();
        clazz.method(bar);
    }
}

import master.thesis.backend.annotations.NoEqualsMethod;
import master.thesis.backend.annotations.NoInitialization;

@NoEqualsMethod
public class FirstTestClass {
    
    class InnerClass {

        private int number;

        public void setNumber(int number) {
            this.number = number;
        }

    }
}

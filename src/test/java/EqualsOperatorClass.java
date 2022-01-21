import master.thesis.backend.annotations.NoEqualsMethod;

@NoEqualsMethod
public class EqualsOperatorClass {

    public class InnerClass {

        private int number;

        public InnerClass(int number) {
            this.number = number;
        }

        public boolean equals(Object other) {
            if (other instanceof InnerClass) {
                return ((InnerClass) other).number == this.number;
            }
            return false;
        }

    }

    public void method() {
        InnerClass i = new InnerClass(5);
        InnerClass i2 = new InnerClass(5);
        boolean bo = i == i2;
    }

}

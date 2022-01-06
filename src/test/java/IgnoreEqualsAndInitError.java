import master.thesis.backend.annotations.NoEqualsMethod;
import master.thesis.backend.annotations.NoInitialization;

@NoEqualsMethod
public class IgnoreEqualsAndInitError {

    @NoInitialization
    private int a;

    public void setA(int a) {
        this.a = a;
    }

}

public class Utils {
    //cannot be instantiated
    private Utils(){
    }
    public static boolean isNullOrEmpty(String s){
        if(s == null || s.equals("")){
            return true;
        } else {
            return false;
        }
    }
}

public class LoginCredentials {
    public String username = "";
    public String password = "";
    public LoginCredentials(String username, String password){
        if(username == null){
            throw new IllegalArgumentException("Invalid username for login credentials!");
        }
        if(password == null){
            throw new IllegalArgumentException("Invalid password for login credentials!");
        }
        this.username = username;
        this.password = password;
    }    
}

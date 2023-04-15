package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangePasswordData {
    public String tokenID;

    public String username;
    public String loginpwd;
    public String pwd;
    public String pwd2;

    public ChangePasswordData() {

    }

    public ChangePasswordData(String tokenID, String loginpwd, String pwd, String pwd2) {
        this.tokenID= tokenID;
        this.loginpwd=loginpwd;
        this.pwd = pwd;
        this.pwd2 = pwd2;
    }
}

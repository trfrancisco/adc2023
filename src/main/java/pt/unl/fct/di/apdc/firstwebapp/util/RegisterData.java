package pt.unl.fct.di.apdc.firstwebapp.util;

import java.util.Optional;

public class RegisterData {


    public String username;
    public String password;
    public String confirmpwd;
    public String email;
    public String name;

    public String privacy;
    public String landline;


    public String phone;

    public String job;

    public String local;
    public String address;
    public String compaddress;
    public String nif;


    public RegisterData() {

    }

    public RegisterData(String username, String password, String confirmpwd, String email, String name,String privacy, String landline, String phone, String job, String local, String address,String compaddress, String nif) {
        this.username = username;
        this.password = password;
        this.confirmpwd = confirmpwd;
        this.email = email;
        this.name = name;
        this.privacy=privacy;
        this.landline=landline;

        this.phone=phone;

        this.job = job;
        this.local = local;
        this.address = address;
        this.compaddress = compaddress;
        this.nif = nif;


    }


}

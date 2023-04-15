package pt.unl.fct.di.apdc.firstwebapp.resources;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import org.apache.commons.codec.digest.DigestUtils;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData;


@Path("/register")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RegisterResource {

    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

    private final Gson g = new Gson();

    private static final String USER = "USER";

    private static final String INACTIVE = "INACTIVE";

    public RegisterResource() {
    }

    private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();


    @POST
    @Path("/v1")
    @Consumes(MediaType.APPLICATION_JSON)
    /*tenho que adicionar os atributos complementares*/
    public Response registerUser(RegisterData data) {

        String username = data.username;
        String password = data.password;
        String confirmpwd = data.confirmpwd;
        String email = data.email;
        String name = data.name;
        String privacy = data.privacy;
        String landline = data.landline;

        String phone = data.phone;

        String job = data.job;
        String local = data.local;
        String address = data.address;
        String compaddress = data.compaddress;
        String nif = data.nif;


        Key userKey = datastore.newKeyFactory().setKind("User").newKey(username);
        Transaction txn = datastore.newTransaction();
        try {
            Entity user = datastore.get(userKey);

            if (user != null) {
                txn.rollback();
                return Response.status(Status.CONFLICT).entity("User already exists.").build();
            } else if (!checkPassword(password, confirmpwd)) {
                txn.rollback();
                return Response.status(Status.BAD_REQUEST).entity("Invalid password").build();
            } else {

                user = Entity.newBuilder(userKey)
                        .set("password", DigestUtils.sha512Hex(password))
                        .set("username", username)
                        .set("name", name)
                        .set("email", email)
                        .set("role", 1)
                        .set("status", INACTIVE)
                        .set("timestamp", Timestamp.now())
                        .set("privacy", privacy)
                        .set("landline", landline)
                        .set("phone", phone)
                        .set("job", job)
                        .set("local", local)
                        .set("address", address)
                        .set("compaddress", compaddress)
                        .set("nif", nif)
                        .build();

                txn.add(user);
                LOG.info("User registered " + username);
                txn.commit();
                return Response.ok("{}").build();
            }

        } finally {
            if (txn.isActive()) {
                txn.rollback();
                return Response.status(Status.INTERNAL_SERVER_ERROR).build();
            }
        }

    }

    private boolean checkPassword(String password, String confirmpwd) {
        if (password.length() < 8 || !password.matches("(?=.*[a-zA-Z])(?=.*\\d)(?=.*\\p{Punct}).*") || !password.equals(confirmpwd))
            return false;
        return true;
    }


}
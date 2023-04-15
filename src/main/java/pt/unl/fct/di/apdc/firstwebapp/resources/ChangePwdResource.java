package pt.unl.fct.di.apdc.firstwebapp.resources;


import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import org.apache.commons.codec.digest.DigestUtils;
import pt.unl.fct.di.apdc.firstwebapp.util.ChangePasswordData;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.time.Instant;
import java.util.Date;
import java.util.logging.Logger;

@Path("/change")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ChangePwdResource {

    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

    private final Gson g = new Gson();

    public ChangePwdResource() {
    }


    private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    @PUT
    @Path("/pwd/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changePwd( ChangePasswordData change) {


        Key tokenKey = datastore.newKeyFactory().setKind("Token").newKey(change.tokenID);

        Transaction txn = datastore.newTransaction();

        try {
            Entity token = datastore.get(tokenKey);
            if (token == null) {
                txn.rollback();
                return Response.status(Response.Status.CONFLICT).entity("Token expired").build();
            }
            Timestamp date = token.getTimestamp("expirationdata");
            String exp = date.toString();
            Instant timestamp = Instant.parse(exp);
            Instant now = Instant.now();

            if (now.isAfter(timestamp)) {
                txn.rollback();
                return Response.status(Response.Status.CONFLICT).entity("Token expired").build();
            }
            String username = token.getString("username");
            Key userKey = datastore.newKeyFactory().setKind("User").newKey(username);
            Entity user = datastore.get(userKey);

             if (!checkPassword(change.pwd, change.pwd2)) {
                txn.rollback();
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid password").build();
            }
             else if (!user.getString("password").equals(DigestUtils.sha512Hex(change.loginpwd))) {
                txn.rollback();
                return Response.status(Response.Status.UNAUTHORIZED).entity("Wrong password").build();
            } else {
                Entity updateuser = Entity.newBuilder(user)
                        .set("password", DigestUtils.sha512Hex(change.pwd))
                        .build();
                txn.put(updateuser);
                LOG.info("Password changed");
                txn.commit();
                return Response.ok("{}").build();
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();

                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }

    }
// tenho que verificar se é igual à antiga porque nao faz sentido mudar para uma igual
    private boolean checkPassword(String password, String confirmpwd) {
        if (password.length() < 8 || !password.matches("(?=.*[a-zA-Z])(?=.*\\d)(?=.*\\p{Punct}).*") || !password.equals(confirmpwd))
            return false;
        return true;
    }
}

package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.appengine.repackaged.org.joda.time.field.LenientDateTimeField;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import org.apache.commons.codec.digest.DigestUtils;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData;
import pt.unl.fct.di.apdc.firstwebapp.util.Token;

@Path("/remove")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RemoveResource {
    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

    private final Gson g = new Gson();


    public RemoveResource() {
    }

    private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

  /*  @DELETE
    @Path("/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeUser(@PathParam("username") String username, LoginData data) {
        String user = data.username;
        String password = data.password;
        Key userKey = datastore.newKeyFactory().setKind("User").newKey(user);
        Key userKey2 = datastore.newKeyFactory().setKind("User").newKey(username);
        Transaction txn = datastore.newTransaction();
        try {
            Entity u = datastore.get(userKey);
            Entity u2 = datastore.get(userKey2);
            if (u == null || u2 == null) {
                txn.rollback();
                return Response.status(Status.NOT_FOUND).build();
            }
            String user_pwd = u.getString("password");
            if (!DigestUtils.sha512Hex(password).equals(user_pwd)) {
                txn.rollback();
                return Response.status(Status.FORBIDDEN).build();
            }
            long role = u.getLong("role");
            long role2 = u2.getLong("role");
            if (role >= role2) {
                txn.delete(userKey2);
                LOG.info("User removed " + username);
                txn.commit();
                return Response.ok("{}").build();
            }
            else{
                txn.rollback();
                return Response.status(Status.UNAUTHORIZED).build();
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
                return Response.status(Status.INTERNAL_SERVER_ERROR).build();
            }

        }

    }*/

    @DELETE
    @Path("/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeUser(@PathParam("username") String username, Token token) {

        String tokenID = token.tokenID;
        Key tokenKey = datastore.newKeyFactory().setKind("Token").newKey(tokenID);
        Key userKey2 = datastore.newKeyFactory().setKind("User").newKey(username);
        Transaction txn = datastore.newTransaction();
        try {
            Entity t = datastore.get(tokenKey);
            Entity u2 = datastore.get(userKey2);


            if (t == null || u2 == null) {
                txn.rollback();
                return Response.status(Status.NOT_FOUND).build();
            }
            long role = t.getLong("role");
            long role2 = u2.getLong("role");
            if (role >= role2) {
                txn.delete(userKey2);
                LOG.info("User removed " + username);
                txn.commit();
                return Response.ok("{}").build();
            }
            else{
                txn.rollback();
                return Response.status(Status.UNAUTHORIZED).build();
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
                return Response.status(Status.INTERNAL_SERVER_ERROR).build();
            }

        }

    }
}

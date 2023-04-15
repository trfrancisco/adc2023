package pt.unl.fct.di.apdc.firstwebapp.resources;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import org.apache.commons.codec.digest.DigestUtils;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;
import pt.unl.fct.di.apdc.firstwebapp.util.Token;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Path("/user")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")

public class GetUserResource {
    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

    private final Gson g = new Gson();

    public GetUserResource() {
    }

    private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    @POST
    @Path("/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    // como é um get nao pode ter nada no body!!!!!
    public Response getUser(@PathParam("username") String username, Token token) {
        String u = username;
        Key tokenKey = datastore.newKeyFactory().setKind("Token").newKey(token.tokenID);
        Key userKey = datastore.newKeyFactory().setKind("User").newKey(u);
        Transaction txn = datastore.newTransaction();
        try {
            Entity t = datastore.get(tokenKey);
            Entity user = datastore.get(userKey);
            if (t == null || user == null) {
                txn.rollback();
                return Response.status(Response.Status.NOT_FOUND).build();

            } else {
                long role = t.getLong("role");
                if(!t.getString("username").equals(username) && role<= user.getLong("role"))
                    return Response.status(Response.Status.FORBIDDEN).build();

                    LOG.info("Get user sucessful");
                    txn.commit();
                    return Response.ok(g.toJson(user.getProperties())).build();

            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();

                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }

    }
}

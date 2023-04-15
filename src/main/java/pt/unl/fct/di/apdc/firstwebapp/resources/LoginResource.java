package pt.unl.fct.di.apdc.firstwebapp.resources;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {

    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

    private final Gson g = new Gson();

    public LoginResource() {
    }

    private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();


    @POST
    @Path("/v1")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doLoginV1(LoginData data) {
        String u = data.username;
        String password = data.password;
        Key userKey = datastore.newKeyFactory().setKind("User").newKey(u);


        Transaction txn = datastore.newTransaction();
        try {
            Entity user = datastore.get(userKey);
            if (user == null) {
                txn.rollback();
                return Response.status(Response.Status.CONFLICT).entity("User does not exist").build();
            } else if (!user.getString("password").equals(DigestUtils.sha512Hex(password))) {
                txn.rollback();
                return Response.status(Status.FORBIDDEN).entity("Wrong password").build();
            } else {
                long role = user.getLong("role");
                AuthToken token = new AuthToken(u,role);
                Key tokenkey = datastore.newKeyFactory().setKind("Token").newKey(token.tokenID);

                Entity tokenid = Entity.newBuilder(tokenkey)
                        .set("username", u)
                        .set("role", role)
                        .set("creationdata", Timestamp.of(token.creationData))
                        .set("expirationdata", Timestamp.of(token.expirationData))
                        .set("tokenid", DigestUtils.sha512Hex(token.tokenID))
                        .build();
                txn.add(tokenid);
                LOG.info("User " + u + " logged in");
                txn.commit();
                return Response.ok(g.toJson(token)).build();
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }
    }

}

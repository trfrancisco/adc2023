package pt.unl.fct.di.apdc.firstwebapp.resources;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.gson.Gson;

import org.apache.commons.codec.digest.DigestUtils;
import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;
import pt.unl.fct.di.apdc.firstwebapp.util.Token;

@Path("/logout")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LogoutResource {

    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

    private final Gson g = new Gson();

    public LogoutResource() {
    }

    private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();


    @DELETE
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doLoginV1(Token token) {
        String t = token.tokenID;

        Key tokenKey = datastore.newKeyFactory().setKind("Token").newKey(t);


        Transaction txn = datastore.newTransaction();
        try {
            Entity tk = datastore.get(tokenKey);
            if (tk == null) {
                txn.rollback();
                return Response.status(Response.Status.CONFLICT).entity("Token does not exist").build();
            } else {
                txn.delete(tokenKey);
                LOG.info("token deleted");
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

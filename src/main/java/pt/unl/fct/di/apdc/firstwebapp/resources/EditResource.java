package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import org.apache.commons.codec.digest.DigestUtils;
import pt.unl.fct.di.apdc.firstwebapp.util.EditAt;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData;
import pt.unl.fct.di.apdc.firstwebapp.util.Token;

@Path("/edit")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class EditResource {
    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

    private final Gson g = new Gson();


    public EditResource() {
    }

    private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();


    @PUT
    @Path("/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeUser(@PathParam("username") String username, EditAt edit) {

        String u = username;
        String tokenID = edit.tokenID;

        Key userKey = datastore.newKeyFactory().setKind("User").newKey(u);
        Key tokenKey = datastore.newKeyFactory().setKind("Token").newKey(tokenID);

        Transaction txn = datastore.newTransaction();
        try {
            Entity user = datastore.get(userKey);
            Entity tokenEntity = datastore.get(tokenKey);
            if (user == null) {
                txn.rollback();
                return Response.status(Response.Status.CONFLICT).entity("User does not exist" + username).build();

            } else if (tokenEntity == null) {
                txn.rollback();
                return Response.ok().entity("Logged out").build();
            } else {
                long role = user.getLong("role");
                long tokenRole = tokenEntity.getLong("role");
                if (tokenRole < role) {
                    txn.rollback();
                    return Response.status(Status.FORBIDDEN).entity("You do not have permission to edit this user").build();
                } else if (tokenRole == 1) {
                    Entity updateduser = Entity.newBuilder(user)
                            .set("password", DigestUtils.sha512Hex(edit.password))
                            .set("privacy", edit.privacy)
                            .set("landline", edit.landline)
                            .set("phone", edit.phone)
                            .set("job", edit.job)
                            .set("local", edit.local)
                            .set("address", edit.address)
                            .set("compaddress", edit.compaddress)
                            .set("nif", edit.nif)
                            .build();
                    txn.put(updateduser);
                    LOG.info("Attributes changed");
                    txn.commit();
                    return Response.ok().build();
                } else {

                    long nrole=1;
                    if (edit.role.equals("USER")) {
                        nrole = 1;
                    } else if (edit.role.equals("GBO")) {
                        nrole = 2;
                    } else if (edit.role.equals("GS")) {
                        nrole = 3;
                    }

                    Entity updateduser = Entity.newBuilder(user)
                            .set("password", DigestUtils.sha512Hex(edit.password))
                            .set("name", edit.name)
                            .set("email", edit.email)
                            .set("role", nrole)
                            .set("status", edit.status)
                            .set("privacy", edit.privacy)
                            .set("landline", edit.landline)
                            .set("phone", edit.phone)
                            .set("job", edit.job)
                            .set("local", edit.local)
                            .set("address", edit.address)
                            .set("compaddress", edit.compaddress)
                            .set("nif", edit.nif)
                            .build();
                    txn.put(updateduser);
                    LOG.info("Attributes changed");
                    txn.commit();
                    return Response.ok().build();
                }
            }

        } finally {
            if (txn.isActive()) {
                txn.rollback();
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }
    }
}
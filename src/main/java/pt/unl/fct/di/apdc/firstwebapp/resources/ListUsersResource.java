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
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

@Path("/list")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")

public class ListUsersResource {
    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

    private final Gson g = new Gson();

    public ListUsersResource() {
    }

    private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    @GET
    @Path("/v1/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    // como é um get nao pode ter nada no body!!!!!
    public Response listUsers(@PathParam("username") String username, @QueryParam("token") String token) {
        String u = username;
        //String password = pwd;
        Key tokenKey = datastore.newKeyFactory().setKind("Token").newKey(token);
        Key userKey = datastore.newKeyFactory().setKind("User").newKey(u);
        Transaction txn = datastore.newTransaction();
        try {
            Entity t = datastore.get(tokenKey);
            Entity user = datastore.get(userKey);
            if (t == null) {
                txn.rollback();
                return Response.status(Response.Status.CONFLICT).entity("Token expired").build();
            }
            Timestamp date = t.getTimestamp("expirationdata");
            String exp = date.toString();
            Instant timestamp = Instant.parse(exp);
            Instant now = Instant.now();
            if (now.isAfter(timestamp) ) {
                txn.rollback();
                return Response.status(Response.Status.CONFLICT).entity("Token expired").build();
            }
            if (user == null ) {
                txn.rollback();
                return Response.status(Response.Status.CONFLICT).entity("User does not exist" + username).build();

            } else {

                long role = user.getLong("role");
                Query<Entity> query;
                List<Map<String, Value<?>>> users = new ArrayList<>();
                if (role == 1) {
                    query = Query.newEntityQueryBuilder()
                            .setKind("User")
                            .setFilter(
                                    StructuredQuery.CompositeFilter.and(
                                            StructuredQuery.PropertyFilter.le("role", role),
                                            StructuredQuery.PropertyFilter.eq("privacy", "public"),
                                            StructuredQuery.PropertyFilter.eq("status", "ACTIVE")
                                    )
                            )
                            .build();


                    QueryResults<Entity> res = datastore.run(query);
                    res.forEachRemaining(userres -> {
                        Map<String, Value<?>> temp = userres.getProperties();
                        Map<String, Value<?>> temp2 = new HashMap<>(temp); //mutable
                        temp2.remove("password");
                        temp2.remove("timestamp");
                        temp2.remove("role");
                        temp2.remove("status");
                        temp2.remove("privacy");
                        temp2.remove("landilne");
                        temp2.remove("phone");
                        temp2.remove("job");
                        temp2.remove("local");
                        temp2.remove("address");
                        temp2.remove("compaddress");
                        temp2.remove("nif");
                        users.add(temp2);
                    });
                    LOG.info("List of users admins");
                    txn.commit();
                    return Response.ok(g.toJson((users))).build();
                } else {
                    query = Query.newEntityQueryBuilder()
                            .setKind("User")
                            .setFilter(
                                    StructuredQuery.PropertyFilter.le("role", role)
                            )
                            .build();
                    QueryResults<Entity> res = datastore.run(query);
                    res.forEachRemaining(userres -> {
                        Map<String, Value<?>> temp = userres.getProperties();
                        Map<String, Value<?>> temp2 = new HashMap<>(temp); //mutable
                        temp2.remove("password");
                        temp2.remove("timestamp");
                        users.add(temp2);
                    });
                    LOG.info("List of users admins");
                    txn.commit();
                    return Response.ok(g.toJson((users))).build();
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

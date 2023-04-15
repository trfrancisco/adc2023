package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.protobuf.Timestamp;
import com.google.cloud.tasks.v2.*;
import com.google.gson.Gson;

@Path("/utils")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ComputationResource {

	private static final Logger LOG = Logger.getLogger(ComputationResource.class.getName());
	private final Gson g = new Gson();

	private static final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");

	public ComputationResource() {
	} // nothing to be done here @GET

	@GET
	@Path("/hello")
	@Produces(MediaType.TEXT_PLAIN)
	public Response hello() throws IOException {
		throw new IOException("UPS");
	}

	@GET
	@Path("/time")
	public Response getCurrentTime() {

		LOG.fine("Replying to date request.");
		return Response.ok().entity(g.toJson(fmt.format(new Date()))).build();
	}

	@GET
	@Path("/compute")
	public Response triggerExecuteComputeTask() throws IOException {
		String projectId = "peerless-column-379315";
		String queueName = "Default";
		String location = "europe-west6";
		LOG.log(Level.INFO, projectId + " :: " + queueName + " :: " + location);
		try (CloudTasksClient client = CloudTasksClient.create()) {
			String queuePath = QueueName.of(projectId, location, queueName).toString();
			Task.Builder taskBuilder = Task.newBuilder().setAppEngineHttpRequest(AppEngineHttpRequest.newBuilder()
					.setRelativeUri("/rest/utils/compute").setHttpMethod(HttpMethod.POST).build());
			taskBuilder.setScheduleTime(
					Timestamp.newBuilder().setSeconds(Instant.now(Clock.systemUTC()).getEpochSecond()));
			client.createTask(queuePath, taskBuilder.build());
		}

		return Response.ok().build();
	}

	@POST
	@Path("/compute")
	public Response executeComputeTask() {
		LOG.fine("Starting to execute computation taks");
		try {
			Thread.sleep(60 * 1000 * 10);
		} catch (Exception e) {
			LOG.logp(Level.SEVERE, this.getClass().getCanonicalName(), "executeComputeTask", "An exception as ocurred,",
					e);
			return Response.serverError().build();
		} // Simulates 60s execution
		return Response.ok().build();
	}
}
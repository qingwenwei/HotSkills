package edu.carleton.COMP4601.web;


import java.net.UnknownHostException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.mcavallo.opencloud.Cloud;
import org.mcavallo.opencloud.Tag;

import edu.carleton.COMP4601.constants.JobTitles;


@Path("/ep")
public class Main {
	// Allows to insert contextual objects into the class,
	// e.g. ServletContext, Request, Response, UriInfo
	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	private String name;

	public Main() throws UnknownHostException {
		name = "Skill Mama";
	}

	@GET
	public String printName() {
		return name;
	}

	@GET
	@Produces(MediaType.TEXT_XML)
	public String sayXML() {
		return "<?xml version=\"1.0\"?>" + "<bank> " + name + " </bank>";
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public String sayHtml() throws UnknownHostException {
		PageGenerator pg =  new PageGenerator();
		return pg.generateIndexPage();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String sayJSON() {
		return "{" + name + "}";
	}
	
	@GET
	@Path("{job}")
	@Produces(MediaType.TEXT_HTML)
	public String showJobDetails(@PathParam("job") String job) throws NumberFormatException, UnknownHostException {
		PageGenerator pg =  new PageGenerator();
		return pg.generateCloud(job);
	}
	
	
	@GET
	@Path("{job}/{keyword}")
	@Produces(MediaType.TEXT_HTML)
	public String searchJobWithKeyword(@PathParam("job") String job, @PathParam("keyword") String keyword) throws NumberFormatException, UnknownHostException {
		PageGenerator pg =  new PageGenerator();
		return pg.searchJobWithKeyword(job, keyword);
	}
	
	@GET
	@Path("about")
	@Produces(MediaType.TEXT_HTML)
	public String about() throws NumberFormatException, UnknownHostException {
		String html = "This is a very cool web service, yeah~";
		
		return html;
	}
	
}

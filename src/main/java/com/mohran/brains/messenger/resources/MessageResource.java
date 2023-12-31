package com.mohran.brains.messenger.resources;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.function.LongToDoubleFunction;

import com.mohran.brains.messenger.model.Message;
import com.mohran.brains.messenger.resources.beans.MessageFilterBean;
import com.mohran.brains.messenger.service.MessageService;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;

@Path("/messages")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MessageResource {
	
	MessageService messageService = new MessageService();
	@GET
	public List<Message> getMessages(@BeanParam MessageFilterBean filterBean)
	{ 
		if(filterBean.getYear()>0)
		{
			return messageService.getAllMessagesForYear(filterBean.getYear() ); 
		}
   		if(filterBean.getStart() >= 0 && filterBean.getSize()>0)
		{
			return messageService.getAllMessagesPanginated(filterBean.getStart() , filterBean.getSize() );
		}
		return messageService.getAllMessages();
	}
  
	@POST
	public Response addMessage(Message message, @Context UriInfo uriInfo) throws URISyntaxException
	{
		System.out.println(uriInfo.getAbsolutePath());
		Message newMessage = messageService.addMessage(message);
		String newId = String.valueOf(newMessage);
		URI uri = uriInfo.getAbsolutePathBuilder().path(newId).build();
		return Response.created(uri)
					   .entity(newMessage)
					   .build();
//		return messageService.addMessage(message);
		
	}
	@PUT
	@Path("/{messageId}")
	public Message updateMessage(@PathParam("messageId") long id,Message message)
	{
		message.setId(id);
		return messageService.updateMessage(message);	
	}
	@DELETE 
	@Path("/{messageId}")
	public void deleteMessage(@PathParam("messageId")long id)
	{
		messageService.removeMessage(id);
	}
	
	
	
	@GET
	@Path("/{messageId}")
 	public Message getMessage (@PathParam("messageId") long id,@Context UriInfo uriInfo)
	{
		Message message = messageService.getMessage(id);
		
		message.addLink(getUriForSelf(uriInfo, message), "self");
		message.addLink(getUriForProfile(uriInfo, message), "profile");
		message.addLink(getUriForComments(uriInfo, message), "comments");

		
		return message;
	}

	private String getUriForComments(UriInfo uriInfo, Message message) {
		URI uri = uriInfo.getBaseUriBuilder()
				.path(MessageResource.class)
				.path(MessageResource.class,"getCommentResource")
				.path(CommentResource.class)
				.resolveTemplate("messageId",message.getId() )
				.build();
		return uri.toString();
	}

	private String getUriForProfile(UriInfo uriInfo, Message message) {
		URI uri = uriInfo.getBaseUriBuilder()
				.path(ProfileResources.class)
				.path(message.getAuthor())
				.build();
		return uri.toString();
	}

	private String getUriForSelf(UriInfo uriInfo, Message message) {
		String uri = uriInfo.getAbsolutePathBuilder()
		.path(MessageResource.class)
		.path( Long.toString(message.getId()))
		.build()
		.toString();
		return uri;
	}
	
	
	@Path("/{messageId}/comments")
	public CommentResource getCommentResource()
	{
		return new CommentResource();
	}
	
}

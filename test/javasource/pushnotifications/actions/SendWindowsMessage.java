// This file was generated by Mendix Modeler.
//
// WARNING: Only the following code will be retained when actions are regenerated:
// - the import list
// - the code between BEGIN USER CODE and END USER CODE
// - the code between BEGIN EXTRA CODE and END EXTRA CODE
// Other code you write will be lost the next time you deploy the project.
// Special characters, e.g., é, ö, à, etc. are supported in comments.

package pushnotifications.actions;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import pushnotifications.proxies.WindowsMessage;
import pushnotifications.proxies.constants.Constants;
import com.mendix.core.Core;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.webui.CustomJavaAction;
import com.mendix.systemwideinterfaces.core.IMendixObject;

/**
 * Takes windows message input and sends a windows notifcation via a webservice (toast)
 */
public class SendWindowsMessage extends CustomJavaAction<Boolean>
{
	private java.util.List<IMendixObject> __WindowsMessages;
	private java.util.List<pushnotifications.proxies.WindowsMessage> WindowsMessages;

	public SendWindowsMessage(IContext context, java.util.List<IMendixObject> WindowsMessages)
	{
		super(context);
		this.__WindowsMessages = WindowsMessages;
	}

	@Override
	public Boolean executeAction() throws Exception
	{
		this.WindowsMessages = new java.util.ArrayList<pushnotifications.proxies.WindowsMessage>();
		if (__WindowsMessages != null)
			for (IMendixObject __WindowsMessagesElement : __WindowsMessages)
				this.WindowsMessages.add(pushnotifications.proxies.WindowsMessage.initialize(getContext(), __WindowsMessagesElement));

		// BEGIN USER CODE
		ILogNode logger = Core.getLogger(Constants.getLogNode());
		for (WindowsMessage message : WindowsMessages) {
			String title= message.getTitle();
	        String subtitle= message.getMessage();
	        String url= message.getURL();     
	 
	        String message1="<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
	                "<wp:Notification xmlns:wp=\"WPNotification\">" +
	                "<wp:Toast>" +
	                     "<wp:Text1>" + title + "</wp:Text1>" +
	                     "<wp:Text2>" + subtitle + "</wp:Text2>" +
	                     "<wp:Param>/Page2.xaml?NavigatedFrom=Toast Notification</wp:Param>" +
	                "</wp:Toast> " +
	             "</wp:Notification>";
	        URLConnection connection = new URL(url).openConnection();
	        connection.setDoOutput(true); // Triggers POST.
	        connection.setRequestProperty("ContentType", "text/xml");
	        connection.setRequestProperty("ContentLength",message1.getBytes().length+"");
	        connection.setRequestProperty("X-WindowsPhone-Target", "toast");
	        connection.setRequestProperty("X-NotificationClass", "2");
	        OutputStream output = connection.getOutputStream();
	        try {
	             output.write(message1.getBytes(),0,message1.getBytes().length);
	        } finally {
	             try { output.close(); } catch (IOException logOrIgnore) {}
	        }
	        if(connection.getHeaderFields().get("X-NotificationStatus").get(0).equals("Dropped")){
	        	if (message.getFailedCount() > Constants.getMaxFailedCount()) {
	        		logger.error("Toast: Message to " + message.getTo() + " failed: Dropped over nd over");
	        		message.delete();
	        	}
	        	else{
		        	message.setFailed(true);
					message.setFailedReason("Dropped");
					message.setFailedCount(message.getFailedCount() + 1);
					message.setQueued(true);
					message.setNextTry(new Date(
							System.currentTimeMillis() + (60000 * (message.getFailedCount() * 5) )));
					message.commit();
	        	}
	        }
	        if(connection.getHeaderFields().get("X-NotificationStatus").get(0).equals("Received")){
	        	logger.info("GCM: Successfully sent message to: " + message.getTo());
	        	message.delete();
	        }
	       
	        System.out.println("Notification Status="+connection.getHeaderFields().get("X-NotificationStatus"));
	        System.out.println("Subscription Status="+connection.getHeaderFields().get("X-SubscriptionStatus"));
	        System.out.println("Device Connection Status="+connection.getHeaderFields().get("X-DeviceConnectionStatus"));
			
		}
		
		
		
		return true;
		// END USER CODE
	}

	/**
	 * Returns a string representation of this action
	 */
	@Override
	public String toString()
	{
		return "SendWindowsMessage";
	}

	// BEGIN EXTRA CODE
	// END EXTRA CODE
}

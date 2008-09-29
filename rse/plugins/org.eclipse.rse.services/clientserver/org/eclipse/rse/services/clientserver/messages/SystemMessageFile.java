/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * David McKnight   (IBM)  [246406] [performance] Timeout waiting when loading SystemPreferencesManager$ModelChangeListener during startup
 * Martin Oberhuber (Wind River) - [246406] Thread-safe support for Lazy Loading
 *******************************************************************************/

package org.eclipse.rse.services.clientserver.messages;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * Use this class to open, and parse, a RSE-style message file.
 */
public class SystemMessageFile implements ErrorHandler
{

	/**
	 * List<MessageFileInfo> of loaded message files to avoid double loading.
	 * Must be synchronized since queried from multiple Threads.
	 */
	private static final List msgfList = Collections.synchronizedList(new LinkedList());
	private MessageFileInfo msgFile;
	private String defaultMsgFileLocation;
	private InputStream dtdInputStream;
	// the following is an attempt to improve response time, and reduce memory requirements, by
	//  caching SystemMessage objects for previously issued messages. Phil
	private Hashtable messages = new Hashtable();
	// XML TAG AND ELEMENT NAMES...
	private static final String XML_TAG_COMPONENT    = "Component"; //$NON-NLS-1$
	private static final String XML_TAG_SUBCOMPONENT = "Subcomponent"; //$NON-NLS-1$
	private static final String XML_TAG_MESSAGeList  = "MessageList"; //$NON-NLS-1$
	private static final String XML_TAG_MESSAGE      = "Message"; //$NON-NLS-1$
	private static final String XML_TAG_LEVELONE     = "LevelOne"; //$NON-NLS-1$
	private static final String XML_TAG_LEVELTWO     = "LevelTwo"; //$NON-NLS-1$
	private static final String XML_ATTR_ABBR        = "Abbr"; //$NON-NLS-1$
	private static final String XML_ATTR_ID          = "ID"; //$NON-NLS-1$
	private static final String XML_ATTR_INDICATOR   = "Indicator"; //$NON-NLS-1$
	private static final String XML_ATTR_NAME        = "Name"; //$NON-NLS-1$

	// when using lazy loading, this is the thread that loads the message file
	private final Thread fLoadThread;

	// indicates whether the lazy load thread is done
	private boolean fLoadThreadFinished = false;

	/**
	 * File info node in msgfList to avoid duplicate loading.
	 * Thread-safe since immutable.
	 */
	private static class MessageFileInfo
	{
		private final String filename;
		private final String shortName;
		private final Document xmlDocument;

		public MessageFileInfo(String ucFileName, String lcFileName, Document doc)
		{
			filename=ucFileName;
			int idx=lcFileName.lastIndexOf('\\');
			if (idx == -1)
			  idx = lcFileName.lastIndexOf('/');
			if (idx >= 0)
			  shortName = lcFileName.substring(idx+1);
			else
			  shortName = lcFileName;
			xmlDocument=doc;
		}

		public String getMessageFullFileName()
		{
			return filename;
		}
		public String getMessageShortFileName()
		{
			return shortName;
		}

		public Document getXMLDocument()
		{
			return xmlDocument;
		}
	}

	/**
	 * Thread for loading a message file asynchronously.
	 *
	 * Opens Streams, creates an internal message file info object holding the
	 * DTD, and closes Streams. In case of error, the message file info object
	 * is not set (remains <code>null</code>).
	 *
	 * As a result,
	 * <ul>
	 * <li>The {@link #msgFile} variable is set to the loaded message file info
	 * node if successful.</li>
	 * <li>The {@link #fLoadThreadFinished} is guaranteed to be set
	 * <code>true</code>.</li>
	 * <li>All waiting Threads are notified.</li>
	 * </ul>
	 */
	private class LoadThread extends Thread {
		private URL _messageFileURL;
		private URL _dtdURL;
		private String _messageFileName;

		public LoadThread(String messageFileName, URL messageFileURL, URL dtdURL) {
			_messageFileName = messageFileName;
			_messageFileURL = messageFileURL;
			_dtdURL = dtdURL;
		}

		public void run() {
			InputStream messageFile = null;
			InputStream dtdFile = null;
			try {
				messageFile = _messageFileURL.openStream();
				dtdFile = _dtdURL.openStream();
				SystemMessageFile realFile = new SystemMessageFile(_messageFileName, messageFile, dtdFile);
				msgFile = realFile.msgFile;
			} catch (IOException e) {
				// problem loading message file -- msgFile is not set.
			} finally {
				// Notify that work is finished before closing Streams -
				// Avoid not getting this set due to a RuntimeError
				synchronized (this) {
					fLoadThreadFinished = true;
					notifyAll();
				}
				if (messageFile != null) {
					try {
						safeClose(messageFile);
					} finally {
						if (dtdFile != null)
							safeClose(dtdFile);
					}
				}
			}
		}

		private void safeClose(InputStream s) {
			try {
				s.close();
			} catch (IOException e) {
				/* ignore */
			}
		}
	}



	/**
	 * Constructor to use for lazy loading of a system message file.
	 *
	 * The difference between the {@link #SystemMessageFile(String,InputStream,InputStream)}
	 * constructor and this one is that the former loads the message file synchronously while
	 * this one loads the message file in a thread.  The message file and DTD URLs are passed
	 * in here so that the opening of their input streams can be deferred until the time when
	 * the worker thread is started and able to load the message file.
	 *
	 * @param messageFileName the name of the system message file
	 * @param msgFileURL the URL to the message file
	 * @param dtdURL the URL to the DTD for the message file
	 *
	 * @since 3.1
	 */
	public SystemMessageFile(final String messageFileName, final URL msgFileURL, final URL dtdURL)
	{
		// have we already loaded this message file?
		msgFile = getFromCache(messageFileName);
		if (msgFile == null){
			// will set msgFile variable and fLoadThreadFinished when done
			fLoadThread = new LoadThread(messageFileName, msgFileURL, dtdURL);
			fLoadThread.start();
		}
		else { // there's already a cached message file for this
			// no need to load it, just use the msgFile
			fLoadThread = null;
		}
	}


	/**
	 * Constructor
	 * @param messageFileName - a key used to determine if a message file has already been loaded.
	 * Usually the name of the xml file containing the message file.
	 * @param messageFile the stream containing the message file.
	 * @param dtdStream the stream containing the dtd for this message file.
	 */
	public SystemMessageFile (String messageFileName, InputStream messageFile, InputStream dtdStream)
	{
		// have we already loaded this message file?
		msgFile = getFromCache(messageFileName);

		// now, we haven't. Load it now.
		this.dtdInputStream = dtdStream;
		if (msgFile == null)
		{
			Document doc = loadAndParseXMLFile(messageFile);
			msgFile=new MessageFileInfo(messageFileName.toUpperCase(), messageFileName, doc);
			msgfList.add(msgFile);
			//scanForDuplicates(); // don't keep this for production. Too expensive
		}
		fLoadThread = null;
	}

	/**
	 * If the named message file has already been loaded return its
	 * MessageFileInfo
	 * @param messageFileName name of the message file
	 * @return the MessageFileInfo for this message file
	 */
	protected MessageFileInfo getFromCache(String messageFileName)
	{
		for (int i=0; i<msgfList.size(); i++)
		{
			MessageFileInfo msgf=(MessageFileInfo)msgfList.get(i);
			if (msgf.getMessageFullFileName().equals(messageFileName.toUpperCase()))
			{
				return msgf;
			}
		}
		return null;
	}

	/**
	 * Waits until the message file is loaded or canceled due to error
	 */
	private void waitUntilLoaded() {
		// fastpath: returns immediately if constructed synchronously
		if (fLoadThread != null) {
			// asynchronous load pending: wait until loaded or load canceled
			synchronized (fLoadThread) {
				while (!fLoadThreadFinished) {
					try {
						fLoadThread.wait();
					} catch (InterruptedException e) {
						// ignore since the Thread is guaranteed to finish
					}
				}
			}
		}
	}

	/**
	 * Use this method to retrieve a message from the message file. If this
	 * SystemMessageFile loaded from a thread, then the method will wait until
	 * the loading is complete before returning the message.
	 *
	 * @param msgId - the ID of the message to retrieve. This is the
	 *            concatenation of the message's component abbreviation,
	 *            subcomponent abbreviation, and message ID as declared in the
	 *            message XML file.
	 * @return SystemMessage the SysteMessage object that corresponds to the
	 *         message ID
	 */
	public SystemMessage getMessage(String msgId)
	{
		waitUntilLoaded();
		boolean echoErrorsToStandardOut = true;
		// DY Defect 42605
		if (msgFile == null || msgFile.getXMLDocument() == null)
	    {
	    	issueErrorMessage("No XML document for message file", echoErrorsToStandardOut);
            return null;
	    }

	    // caching added by Phil to increase performance, and not to leave a trail of SystemMessage objects for
	    //  the garbage collector. Hopefully, the extra memory for the cache does not defeat these benefits.
		SystemMessage msg = (SystemMessage) messages.get(msgId);
	    if (msg != null)
	    {
	    	//System.out.println("Reusing msg " + msgId);
	        return msg;
	    }

	    // I guess the following line of code implies we only support a single component per message file... phil.
	    // Code tweaked by Phil.
        //String componentAbbr=msgFile.getXMLDocument().getElementsByTagName("Component").item(0).getAttributes().getNamedItem("Abbr").getFirstChild().getNodeValue();
        //NodeList subComponentList=msgFile.getXMLDocument().getElementsByTagName("Subcomponent");

        // parse out the Abbr attr of the first Component element
	    NodeList componentElementList = msgFile.getXMLDocument().getElementsByTagName(XML_TAG_COMPONENT);
	    if ((componentElementList == null) || (componentElementList.getLength() == 0))
	    {
	    	issueErrorMessage("Unable to find any Component elements",echoErrorsToStandardOut);
            return null;
	    }

	    Element componentElement = (Element)componentElementList.item(0);
        String componentAbbr = componentElement.getAttribute(XML_ATTR_ABBR);

        // get list of all Subcomponent elements...
        NodeList subComponentList=msgFile.getXMLDocument().getElementsByTagName(XML_TAG_SUBCOMPONENT);

        if ((subComponentList == null) || (subComponentList.getLength() == 0))
        {
	    	issueErrorMessage("Unable to find any Subcomponent elements",echoErrorsToStandardOut);
            return null;
        }

        // fold given msg ID to uppercase
        msgId = msgId.toUpperCase();

        // search for the right component/subcomponent match
        for (int subComponentIdx=0; subComponentIdx<subComponentList.getLength(); subComponentIdx++)
        {
        	//String subComponentAbbr=subComponentList.item(subComponentIdx).getAttributes().getNamedItem("Abbr").getFirstChild().getNodeValue();
        	Element subComponentElement = (Element)subComponentList.item(subComponentIdx);
        	String subComponentAbbr = subComponentElement.getAttribute(XML_ATTR_ABBR);
        	//String msgPrefix=componentAbbr+subComponentAbbr;
        	String msgPrefix = (componentAbbr + subComponentAbbr).toUpperCase();
        	char msgIndicator=' ';
        	String msgL1=""; //$NON-NLS-1$
        	String msgL2=""; //$NON-NLS-1$

        	// if the message prefix matches, then try to find the message
        	//if (msgPrefix.toUpperCase().equals(msgId.toUpperCase().substring(0,msgPrefix.length())) &&
        	//if (msgPrefix.equals(msgId.substring(0,msgPrefix.length())) &&
        	if (msgId.startsWith(msgPrefix) &&
        		Character.isDigit(msgId.charAt(msgPrefix.length())))
        	{
        		//String msgNumber=msgId.toUpperCase().substring(msgPrefix.length());
        		String msgNumber=msgId.substring(msgPrefix.length());
        		Element messageListNode=null;

        		// search for the message list node
        		/*
        		for (Node node=subComponentList.item(subComponentIdx).getFirstChild();
        		     node!=null; node=node.getNextSibling())
        		{
        		     if (node.getNodeName().equals("MessageList"))
        		     {
        		     	messageListNode=node;
        		     	break;
        		     }
        		}*/
        		NodeList msgListNodes = subComponentElement.getElementsByTagName(XML_TAG_MESSAGeList);
        		if ((msgListNodes!=null) && (msgListNodes.getLength()>0))
        		  messageListNode = (Element)msgListNodes.item(0);
        		else
        		{
	    	        issueWarningMessage("unable to find MessageList nodes for subComponent " + subComponentElement.getAttribute(XML_ATTR_NAME),echoErrorsToStandardOut);
        			continue;
        		}

        		// search for the message node which has the right number
        		//for (Node node=messageListNode.getFirstChild();
        		//     node!=null; node=node.getNextSibling())
        		NodeList msgNodes = messageListNode.getElementsByTagName(XML_TAG_MESSAGE);
        		if ((msgNodes==null) || (msgNodes.getLength()==0))
        		{
	    	        issueWarningMessage("unable to find Message nodes for subComponent " + subComponentElement.getAttribute(XML_ATTR_NAME),echoErrorsToStandardOut);
        			continue;
        		}
        		boolean match = false; // I added this so we stop looping when we find what we are looking for!!! Phil.
                for (int msgIdx = 0; !match && (msgIdx < msgNodes.getLength()); msgIdx++)
        		{
        			 Element node = (Element)msgNodes.item(msgIdx);
        		     // if the message number matches...
        		     //if (node.getNodeName().equals("Message") && node.getAttributes().getNamedItem("ID").getFirstChild().getNodeValue().toUpperCase().equals(msgNumber))
        		     if (node.getAttribute(XML_ATTR_ID).equals(msgNumber))
        		     {
        		     	match = true;
        		     	// save the indicator value
        		     	//msgIndicator=(node.getAttributes().getNamedItem("Indicator").getFirstChild().getNodeValue().toUpperCase().toCharArray())[0];
        		     	msgIndicator=node.getAttribute(XML_ATTR_INDICATOR).toUpperCase().charAt(0);
        		     	// search for the l1 & l2 text
        		     	for (Node msgNode=node.getFirstChild();
        		     	     msgNode!=null; msgNode=msgNode.getNextSibling())
        		     	{
        		     	     // get Level One text
        		     	     if (msgNode.getNodeName().equals(XML_TAG_LEVELONE))
        		     	     	msgL1 = getNodeText(msgNode);
        		     	     // get Level Two text
        		     	     else if (msgNode.getNodeName().equals(XML_TAG_LEVELTWO))
        		     	     	msgL2 = getNodeText(msgNode);
        		     	}
	        		    break;
        		     }
        		}
        		if (!match)
        		{
        			/** TODO - DKM move this somewhere else since system message now needs to be eclipse independent
        			SystemBasePlugin.logError("Requested Message " + msgId + " not found in message file " + msgFile.getMessageShortFileName());
        			*/
        			return null;
        		}

		       	msg = loadSystemMessage(componentAbbr, subComponentAbbr, msgNumber, msgIndicator, msgL1, msgL2);
				messages.put(msgId, msg); // add to cache so we find it immediately next time!!
		       	return msg;
        	}
       	}
		return null;
	}

	/**
	 * Override this to provide different extended SystemMessage implementation
	 * @param componentAbbr a three letter component name
	 * @param subComponentAbbr a one letter subcomponent name
	 * @param msgNumber a four digit message number
	 * @param msgIndicator a single character message type indicator
	 * @param msgL1 the first level text that describes the error
	 * @param msgL2 the second level text that provides details about the error and possible recovery
	 * @return the SystemMessage
	 * @see SystemMessage for message type indicator constants
	 */
	protected SystemMessage loadSystemMessage(String componentAbbr, String subComponentAbbr, String msgNumber, char msgIndicator,
			String msgL1, String msgL2)
	{
		return new SystemMessage(componentAbbr, subComponentAbbr, msgNumber, msgIndicator, msgL1, msgL2);
	}

	/**
	 * Get the level one text
	 */
	private String getNodeText(Node msgNode)
	{
		String nodeText = ""; //$NON-NLS-1$
		for (Node textNode=msgNode.getFirstChild();
        	 textNode!=null; textNode=textNode.getNextSibling())
        {
            if ((textNode.getNodeType()==Node.TEXT_NODE) && (textNode.getNodeValue().trim().length()>0))
        	   nodeText += textNode.getNodeValue();
      	}
		return nodeText.trim();
	}

	/**
	 * Use this method to scan message file for duplicate messages. You typically do this only during development!!
	 * If a duplicate is found, its message id is written to standard out, and to the systems.core
	 * log file.
	 * @return true if duplicates found.
	 */
	public boolean scanForDuplicates()
	{
		boolean echoErrorsToStandardOut = true;
		waitUntilLoaded();
		if (msgFile == null || msgFile.getXMLDocument() == null)
	    	return issueErrorMessage("No XML document for message file", echoErrorsToStandardOut);

        // parse out the Abbr attr of the first Component element
	    NodeList componentElementList = msgFile.getXMLDocument().getElementsByTagName(XML_TAG_COMPONENT);
	    if ((componentElementList == null) || (componentElementList.getLength() == 0))
	    	return issueErrorMessage("Unable to find any Component elements",echoErrorsToStandardOut);

	    Element componentElement = (Element)componentElementList.item(0);
        String componentAbbr = componentElement.getAttribute(XML_ATTR_ABBR);

        // get list of all Subcomponent elements...
        NodeList subComponentList=msgFile.getXMLDocument().getElementsByTagName(XML_TAG_SUBCOMPONENT);

        if ((subComponentList == null) || (subComponentList.getLength() == 0))
	    	return issueErrorMessage("Unable to find any Subcomponent elements",echoErrorsToStandardOut);

        // scan all subcomponents...
        boolean anyDupes = false;
        for (int subComponentIdx=0; subComponentIdx<subComponentList.getLength(); subComponentIdx++)
        {
        	Element subComponentElement = (Element)subComponentList.item(subComponentIdx);
        	String subComponentAbbr = subComponentElement.getAttribute(XML_ATTR_ABBR);
        	String msgPrefix = (componentAbbr + subComponentAbbr).toUpperCase();
        	Vector msgsById = new Vector();

        	// search for the message list node
        	Element messageListNode=null;
        	NodeList msgListNodes = subComponentElement.getElementsByTagName(XML_TAG_MESSAGeList);
        	if ((msgListNodes!=null) && (msgListNodes.getLength()>0))
        	  messageListNode = (Element)msgListNodes.item(0);
        	else
        	{
	    	    issueWarningMessage("unable to find MessageList nodes for subComponent " + subComponentElement.getAttribute(XML_ATTR_NAME),echoErrorsToStandardOut);
        		continue;
        	}

        	NodeList msgNodes = messageListNode.getElementsByTagName(XML_TAG_MESSAGE);
        	if ((msgNodes==null) || (msgNodes.getLength()==0))
        	{
	    	        issueWarningMessage("unable to find Message nodes for subComponent " + subComponentElement.getAttribute(XML_ATTR_NAME),echoErrorsToStandardOut);
        			continue;
        	}
            for (int msgIdx = 0; (msgIdx < msgNodes.getLength()); msgIdx++)
        	{
        		 Element node = (Element)msgNodes.item(msgIdx);
        		 String msgId = msgPrefix + node.getAttribute(XML_ATTR_ID);
        		 if (msgsById.contains(msgId))
        		 {
        		 	anyDupes = true;
	    	        issueWarningMessage("Warning: duplicate message " + msgId + " found", echoErrorsToStandardOut);
        		 }
        		 else
        		   msgsById.addElement(msgId);
        	}
       	}
		return anyDupes;
	}

	/**
	 * Use this method to generate html documentation for the messages in the message file.
	 * This is useful for reference information, or to give to Level 2 for service support.
	 * @param fullFileName - the fully qualified name of the file to write to. Overwrites current contents.
	 * @return true if it went well, false if it failed for some reason, such as given a bad file name. Errors written to standard out.
	 */
	public boolean printHTML(String fullFileName)
	{
		boolean echoErrorsToStandardOut = true;
		waitUntilLoaded();
		if (msgFile == null || msgFile.getXMLDocument() == null)
	    	return issueErrorMessage("No XML document for message file", echoErrorsToStandardOut);

        // parse out the Abbr attr of the first Component element
	    NodeList componentElementList = msgFile.getXMLDocument().getElementsByTagName(XML_TAG_COMPONENT);
	    if ((componentElementList == null) || (componentElementList.getLength() == 0))
	    	return issueErrorMessage("Unable to find any Component elements",echoErrorsToStandardOut);

	    Element componentElement = (Element)componentElementList.item(0);
        String componentAbbr = componentElement.getAttribute(XML_ATTR_ABBR);

        // get list of all Subcomponent elements...
        NodeList subComponentList=msgFile.getXMLDocument().getElementsByTagName(XML_TAG_SUBCOMPONENT);

        if ((subComponentList == null) || (subComponentList.getLength() == 0))
	    	return issueErrorMessage("Unable to find any Subcomponent elements",echoErrorsToStandardOut);

		File outFile = new File(fullFileName);
		PrintWriter outFileStream = null;
		try
		{
			outFileStream = new PrintWriter(new FileOutputStream(outFile));
			outFileStream.println("<HTML> <HEAD> <TITLE> Message File "+msgFile.getMessageShortFileName()+" </TITLE>"); //$NON-NLS-1$ //$NON-NLS-2$
            outFileStream.println("<style type=\"text/css\">"); //$NON-NLS-1$
            outFileStream.println("h2 { background-color: #CCCCFF }"); //$NON-NLS-1$
            outFileStream.println("</style>"); //$NON-NLS-1$
			outFileStream.println("</HEAD> <BODY>"); //$NON-NLS-1$
			outFileStream.println("<H1>"+componentElement.getAttribute(XML_ATTR_NAME)+" Messages</H1>"); //$NON-NLS-1$ //$NON-NLS-2$
			outFileStream.println("<br>"); //$NON-NLS-1$
			outFileStream.println("<TABLE BORDER=\"1\" CELLPADDING=\"3\" CELLSPACING=\"0\" WIDTH=\"100%\">"); //$NON-NLS-1$
			outFileStream.println("<TR BGCOLOR=\"#CCCCFF\">"); //$NON-NLS-1$
			outFileStream.println("<TD COLSPAN=2><FONT SIZE=\"+2\">"); //$NON-NLS-1$
			outFileStream.println("<B>Sub-Component Summary</B></FONT></TD>"); //$NON-NLS-1$
			outFileStream.println("</TR>"); //$NON-NLS-1$
		}
		catch (IOException exc)
		{
			return issueErrorMessage("Unable to open given html file in printHTML: " + exc.getMessage(), echoErrorsToStandardOut);
		}

        // pre-scan all subcomponents...
        for (int subComponentIdx=0; subComponentIdx<subComponentList.getLength(); subComponentIdx++)
        {
        	Element subComponentElement = (Element)subComponentList.item(subComponentIdx);
        	String subComponentAbbr = subComponentElement.getAttribute(XML_ATTR_ABBR);
        	String msgPrefix = (componentAbbr + subComponentAbbr).toUpperCase();
			String scName = subComponentElement.getAttribute(XML_ATTR_NAME);

			outFileStream.println("<TR BGCOLOR=\"white\">");			 //$NON-NLS-1$
			outFileStream.println("<TD ALIGN=\"right\" VALIGN=\"top\" WIDTH=\"1%\">"); //$NON-NLS-1$
			outFileStream.println("<A href=#"+scName+">"+scName+"</A></TD>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			outFileStream.println("<TD><b>Message prefix</b>="+msgPrefix+"</TD></TR>"); //$NON-NLS-1$ //$NON-NLS-2$
       	}
	    outFileStream.println("</TABLE>"); //$NON-NLS-1$

        // scan all subcomponents...
        for (int subComponentIdx=0; subComponentIdx<subComponentList.getLength(); subComponentIdx++)
        {
        	Element subComponentElement = (Element)subComponentList.item(subComponentIdx);
        	String subComponentAbbr = subComponentElement.getAttribute(XML_ATTR_ABBR);
        	String msgPrefix = (componentAbbr + subComponentAbbr).toUpperCase();

        	// search for the message list node
        	Element messageListNode=null;
        	NodeList msgListNodes = subComponentElement.getElementsByTagName(XML_TAG_MESSAGeList);
        	if ((msgListNodes!=null) && (msgListNodes.getLength()>0))
        	  messageListNode = (Element)msgListNodes.item(0);
        	else
        	{
	    	    issueWarningMessage("unable to find MessageList nodes for subComponent " + subComponentElement.getAttribute(XML_ATTR_NAME),echoErrorsToStandardOut);
        		continue;
        	}

        	NodeList msgNodes = messageListNode.getElementsByTagName(XML_TAG_MESSAGE);
        	if ((msgNodes==null) || (msgNodes.getLength()==0))
        	{
	    	        issueWarningMessage("unable to find Message nodes for subComponent " + subComponentElement.getAttribute(XML_ATTR_NAME),echoErrorsToStandardOut);
        			continue;
        	}
        	if (subComponentIdx > 0)
        	  outFileStream.println("<hr>"); //$NON-NLS-1$
			String scName = subComponentElement.getAttribute(XML_ATTR_NAME);
			outFileStream.println("<A NAME=\""+scName+"\"><!-- --></A><H2>"+scName+" Messages</H2>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            for (int msgIdx = 0; (msgIdx < msgNodes.getLength()); msgIdx++)
        	{
        		 Element node = (Element)msgNodes.item(msgIdx);
        		 String msgId = msgPrefix + node.getAttribute(XML_ATTR_ID);
        		 char msgIndicator=node.getAttribute(XML_ATTR_INDICATOR).toUpperCase().charAt(0);
        		 String msgSeverity = "Unknown"; //$NON-NLS-1$
        		 if (msgIndicator == SystemMessage.ERROR)
        		   msgSeverity = "Error";
        		 else if (msgIndicator == SystemMessage.WARNING)
        		   msgSeverity = "Warning";
        		 else if (msgIndicator == SystemMessage.INQUIRY)
        		   msgSeverity = "Question";
        		 else if (msgIndicator == SystemMessage.INFORMATION)
        		   msgSeverity = "Information";
        		 else if (msgIndicator == SystemMessage.COMPLETION)
         		   msgSeverity = "Completion";
        		 else if (msgIndicator == SystemMessage.UNEXPECTED)
        		   msgSeverity = "Unexpected";

        		 String msgL1 = ""; //$NON-NLS-1$
        		 String msgL2 = ""; //$NON-NLS-1$
        		 // search for the l1 & l2 text
        		 for (Node msgNode=node.getFirstChild();
        		      msgNode!=null; msgNode=msgNode.getNextSibling())
        		 {
        		     // get Level One text
        		     if (msgNode.getNodeName().equals(XML_TAG_LEVELONE))
        		       	msgL1 = getNodeText(msgNode);
        		     // get Level Two text
        		     else if (msgNode.getNodeName().equals(XML_TAG_LEVELTWO))
        		      	msgL2 = getNodeText(msgNode);
        		 }
			     outFileStream.println("<H3>"+msgId+"</H3>"); //$NON-NLS-1$ //$NON-NLS-2$
			     outFileStream.println("<b>Severity: </b>"+msgSeverity+"<br>"); //$NON-NLS-1$ //$NON-NLS-2$
			     outFileStream.println("<b>LevelOne: </b>"+msgL1+"<br>"); //$NON-NLS-1$ //$NON-NLS-2$
			     outFileStream.println("<b>LevelTwo: </b>"+msgL2+"<br>"); //$NON-NLS-1$ //$NON-NLS-2$
        	}
        	outFileStream.println("<br>"); //$NON-NLS-1$
       	}
	    outFileStream.println("</BODY></HTML>"); //$NON-NLS-1$
	    outFileStream.close();
		return true;
	}

	/**
	 * Issue an error message
	 */
	private boolean issueErrorMessage(String errormsg, boolean echoStandardOut)
	{

		/** TODO - DKM move this somewhere else since system message now needs to be eclipse independent
		 *
        SystemBasePlugin.logError("MessageFile error for msg file " + msgFile.getMessageShortFileName() + ": " + errormsg);
        **/
        if (echoStandardOut)
          System.out.println("MessageFile error for msg file " + msgFile.getMessageShortFileName() + ": " + errormsg);
        return false;
	}
	/**
	 * Issue a warning message
	 */
	private boolean issueWarningMessage(String errormsg, boolean echoStandardOut)
	{
			/** TODO - DKM move this somewhere else since system message now needs to be eclipse independent
        SystemBasePlugin.logError("MessageFile warning for msg file " + msgFile.getMessageShortFileName() + ": " + errormsg);
        */
        if (echoStandardOut)
          System.out.println("MessageFile warning for msg file " + msgFile.getMessageShortFileName() + ": " + errormsg);
        return false;
	}

	/**
	 * Create the XML parser
	 * Set its entity resolver and error handler.
	 * @return DocumentBuilder
	 */
	private DocumentBuilder createXmlParser()
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(true);
		DocumentBuilder parser;
		try {
			parser = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// the configuration was not valid
			/** TODO -move this elsewhere - can't depend on ui stuff
			SystemBasePlugin.logError("SystemMessageFile: loadAndParseXMLFile, configuration not valid "+e.toString(), e);
			*/
			return null;
		}
//		DOMParser parser=new DOMParser();
//		  try
//		  {
//			 parser.setFeature( "http://xml.org/sax/features/validation", true);
//		  }
//		  catch (SAXNotRecognizedException e)
//		  {
//			// the feature was not recognized
//				/** TODO - DKM move this somewhere else since system message now needs to be eclipse independent
//			SystemBasePlugin.logError("SystemMessageFile: loadAndParseXMLFile, feature not recognized "+e.toString(), e);
//			*/
//			 return null;
//		  }
//		  catch (SAXNotSupportedException e)
//		  {
//			// the feature requested was not supported
//				/** TODO - DKM move this somewhere else since system message now needs to be eclipse independent
//			SystemBasePlugin.logError("SystemMessageFile: loadAndParseXMLFile, feature not recognized "+e.toString(), e);
//			*/
//			return null;
//		  }
		parser.setEntityResolver(new EntityResolver() {
			public InputSource resolveEntity(String publicId, String systemId) throws SAXException, java.io.IOException
			{
				/*
				// This code does not work with fragments
				Path path = new Path(systemId);
				if (systemId.toUpperCase().endsWith("MESSAGEFILE.DTD"))
				{
					if (SystemPlugin.getBaseDefault().getDescriptor().find(path)==null)
					{
						path = new Path("messageFile.dtd");
					}
				}
				return new InputSource(SystemBasePlugin.getBaseDefault().getDescriptor().find(path).toString());
				*/
				// If we have the input stream of the DTD just use it
				if (dtdInputStream != null)
				{
					return new InputSource(dtdInputStream);
				}
				else // if we have the directory containing the DTD use that
				{
					// yantzi:artemis6.2 changed to use URI instead of URL (URIs handle the spaces)
					//URL url = new URL(systemId);
					//File dtdFile = new File(url.getFile());
					try
					{
						URI url = new URI(systemId);
						File dtdFile = new File(url.getPath());
						if (!dtdFile.exists())
						{
							// use default locaiton
							systemId = defaultMsgFileLocation + File.separatorChar + dtdFile.getName();
						}
					}
					catch (URISyntaxException e)
					{
						// ignore and continue
					}

					return new InputSource(systemId);
				}
			}
		});
		parser.setErrorHandler(this);
		return parser;
	}
	/**
	 * loadAndParseXMLFile:
	 * tries to load and parse the specified XML file .
	 * @param String messageFile:	InputStream containing the XML file
	 */
	private Document loadAndParseXMLFile (InputStream messageFile)
	{
        DocumentBuilder parser = createXmlParser();
		try
		{
		   InputSource in = new InputSource(messageFile);

		   // DKM - hack!
		   //   If systemId is null for the InputSource, then
		   //   the current parser hits a fatal exception.
		   //   This hack prevents that exception so that the
		   //   specified EntityResolver will get used.
		   in.setSystemId("foo"); //$NON-NLS-1$
		   Document document = parser.parse(in);

		   //Document document = parser.parse(messageFile);
		   return document;
		}
		catch (SAXException e)
		{
			e.printStackTrace();
			// the parser was unable to parse the file.
			return null;
		}
		catch (IOException e)
		{
			e.printStackTrace();
				/** TODO - DKM move this somewhere else since system message now needs to be eclipse independent
			*/
			return null;
		}
	}

	/**
	 * XML Parser-required method: XML-parser warning.
	 */
   public void warning(SAXParseException ex)
   {
   		/** TODO - DKM move this somewhere else since system message now needs to be eclipse independent
		SystemBasePlugin.logError("SystemMessageFile: warning parsing message file: "+ex.toString());
		*/
   }

   /**
    * XML Parser-required method: XML-parser Error.
    */
   public void error(SAXParseException ex)
   {
   		/** TODO - DKM move this somewhere else since system message now needs to be eclipse independent
		SystemBasePlugin.logError("SystemMessageFile: Error parsing message file: "+ex.toString(), ex);
		Shell shell = SystemPlugin.getActiveWorkbenchShell();
		if (shell.isEnabled() && !shell.isDisposed())
		{
			MessageBox mb = new MessageBox(shell);
			mb.setText("Error loading message file: "+ex.getMessage());
			mb.setMessage("Unable to load message file " + currMsgFile +". Error at line "+ex.getLineNumber()+" and column "+ex.getColumnNumber());
			mb.open();
		}
		*/
   }

	/**
	 * XML Parser-required method: XML-parser Fatal error.
	 */
	public void fatalError(SAXParseException ex) throws SAXException
	{
		/** TODO - DKM move this somewhere else since system message now needs to be eclipse independent
		SystemBasePlugin.logError("SystemMessageFile: Fatal Error parsing message file: "+ex.toString(), ex);
		*/
		throw(ex);
	}

}

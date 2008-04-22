/********************************************************************************
 * Copyright (c) 2008 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 *
 * Contributors:
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * Martin Oberhuber (Wind River) - [219975] Fix SystemMessage#clone()
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 * David McKnight   (IBM)        - [226773] [apidoc] Specify allowed namespaces for SimpleSystemMessage ID in the Javadoc
 ********************************************************************************/
package org.eclipse.rse.services.clientserver.messages;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.core.runtime.IStatus;


public class SimpleSystemMessage extends SystemMessage {

	private String _pluginId;
	private String _messageId;

	/**
	 * Constructor for messages that use explicit Strings and severities.
	 *
	 * This allows using the RSE Messaging Framework based on simple String
	 * messages and IDs, rather than using XML Message files from
	 * {@link SystemMessageFile} along with the
	 * <code>org.eclipse.rse.ui.SystemBasePlugin#loadMessageFile()</code> and
	 * <code>org.eclipse.rse.ui.SystemBasePlugin#getMessage()</code> methods.
	 *
	 * Clients can use either globally unique RSE message IDs or plugin-specific
	 * local IDs. RSE-global message IDs are of the form:
	 * RSE&lt;subcomponent&gt;&lt;number&gt;
	 *
	 * The subcomponent is a single character:
	 * <ul>
	 * <li>"G" for General</li>
	 * <li>"O" for Other</li>
	 * <li>"F" for Files</li>
	 * <li>"C" for Communications</li>
	 * </ul>
	 *
	 * The number is a four digit number.
	 *
	 * Plugin-specific local IDs need only be unique strings within a plugin
	 * that are not prefixed by "RSE". The relative IDs are qualified by the
	 * specified plugin ID.
	 *
	 * @param pluginId the id of the originating plugin
	 * @param messageId the RSE-global unique ID or plugin-specific local ID of
	 *            the message
	 * @param severity using IStatus severities
	 * @param msg the message text
	 */
	public SimpleSystemMessage(String pluginId, String messageId, int severity, String msg) {
		this(pluginId, messageId, severity, msg, (String)null);
	}

	/**
	 * Constructor for messages that use explicit Strings and severities.
	 *
	 * This allows using the RSE Messaging Framework based on simple String
	 * messages and IDs, rather than using XML Message files from
	 * {@link SystemMessageFile} along with the
	 * <code>org.eclipse.rse.ui.SystemBasePlugin#loadMessageFile()</code> and
	 * <code>org.eclipse.rse.ui.SystemBasePlugin#getMessage()</code> methods.
	 *
	 * Clients can use either globally unique RSE message IDs or plugin-specific
	 * local IDs. RSE-global message IDs are of the form:
	 * RSE&lt;subcomponent&gt;&lt;number&gt;
	 *
	 * The subcomponent is a single character:
	 * <ul>
	 * <li>"G" for General</li>
	 * <li>"O" for Other</li>
	 * <li>"F" for Files</li>
	 * <li>"C" for Communications</li>
	 * </ul>
	 *
	 * The number is a four digit number.
	 *
	 * Plugin-specific local IDs need only be unique strings within a plugin
	 * that are not prefixed by "RSE". The relative IDs are qualified by the
	 * specified plugin ID.
	 *
	 * @param pluginId the id of the originating plugin
	 * @param messageId the RSE-global unique ID or plugin-specific local ID of
	 *            the message
	 * @param severity using IStatus severities
	 * @param msg the message text
	 * @param msgDetails the message details
	 */
	public SimpleSystemMessage(String pluginId, String messageId, int severity, String msg, String msgDetails) {
		super("RSE", "G", "-", severityToIndicator(severity), msg, msgDetails);  //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$

		_pluginId = pluginId;
		_messageId = messageId;
	}

	/**
	 * Constructor for messages that use explicit Strings and severities.
	 *
	 * This allows using the RSE Messaging Framework based on simple String
	 * messages and IDs, rather than using XML Message files from
	 * {@link SystemMessageFile} along with the
	 * <code>org.eclipse.rse.ui.SystemBasePlugin#loadMessageFile()</code> and
	 * <code>org.eclipse.rse.ui.SystemBasePlugin#getMessage()</code> methods.
	 *
	 * Clients can use either globally unique RSE message IDs or plugin-specific
	 * local IDs. RSE-global message IDs are of the form:
	 * RSE&lt;subcomponent&gt;&lt;number&gt;
	 *
	 * The subcomponent is a single character:
	 * <ul>
	 * <li>"G" for General</li>
	 * <li>"O" for Other</li>
	 * <li>"F" for Files</li>
	 * <li>"C" for Communications</li>
	 * </ul>
	 *
	 * The number is a four digit number.
	 *
	 * Plugin-specific local IDs need only be unique strings within a plugin
	 * that are not prefixed by "RSE". The relative IDs are qualified by the
	 * specified plugin ID.
	 *
	 *
	 * @param pluginId the id of the originating plugin
	 * @param messageId the RSE-global unique ID or plugin-specific local ID of
	 *            the message
	 * @param severity using IStatus severities
	 * @param msg the message text
	 * @param e an exception to convert into details
	 */
	public SimpleSystemMessage(String pluginId, String messageId, int severity, String msg, Throwable e) {
		super("RSE", "G", "-", severityToIndicator(severity), msg, throwableToDetails(e)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		_pluginId = pluginId;
		_messageId = messageId;
	}

	/**
	 * Constructor for messages that use explicit Strings and severities.
	 *
	 * This allows using the RSE Messaging Framework based on simple String
	 * messages and IDs, rather than using XML Message files from
	 * {@link SystemMessageFile} along with the
	 * <code>org.eclipse.rse.ui.SystemBasePlugin#loadMessageFile()</code> and
	 * <code>org.eclipse.rse.ui.SystemBasePlugin#getMessage()</code> methods.
	 *
	 * This constructor does not supply a message id. It is preferred that a
	 * message id is used since it allows easier identification of a unique
	 * message.
	 *
	 * @param pluginId the id of the originating plugin
	 * @param severity using IStatus severities
	 * @param msg the message text
	 */
	public SimpleSystemMessage(String pluginId, int severity, String msg) {
		this(pluginId, severity, msg, (String)null);
	}

	/**
	 * Constructor for messages that use explicit Strings and severities.
	 *
	 * This allows using the RSE Messaging Framework based on simple String
	 * messages and IDs, rather than using XML Message files from
	 * {@link SystemMessageFile} along with the
	 * <code>org.eclipse.rse.ui.SystemBasePlugin#loadMessageFile()</code> and
	 * <code>org.eclipse.rse.ui.SystemBasePlugin#getMessage()</code> methods.
	 *
	 * This constructor does not supply a message id. It is preferred that a
	 * message id is used since it allows easier identification of a unique
	 * message.
	 *
	 * @param pluginId the id of the originating plugin
	 * @param severity using IStatus severities
	 * @param msg the message text
	 * @param msgDetails the message details
	 */
	public SimpleSystemMessage(String pluginId, int severity, String msg, String msgDetails) {
		super("RSE", "G", "-", severityToIndicator(severity), msg, msgDetails);  //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
		_pluginId = pluginId;

	}

	/**
	 * Constructor for messages that use explicit Strings and severities.
	 *
	 * This allows using the RSE Messaging Framework based on simple String
	 * messages and IDs, rather than using XML Message files from
	 * {@link SystemMessageFile} along with the
	 * <code>org.eclipse.rse.ui.SystemBasePlugin#loadMessageFile()</code> and
	 * <code>org.eclipse.rse.ui.SystemBasePlugin#getMessage()</code> methods.
	 *
	 * This constructor does not supply a message id. It is preferred that a
	 * message id is used since it allows easier identification of a unique
	 * message.
	 *
	 * @param pluginId the id of the originating plugin
	 * @param severity using IStatus severities
	 * @param msg the message text
	 * @param e an exception to convert into details
	 */
	public SimpleSystemMessage(String pluginId, int severity, String msg, Throwable e) {
		super("RSE", "G", "-", severityToIndicator(severity), msg, throwableToDetails(e)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		_pluginId = pluginId;
	}

	private static String throwableToDetails(Throwable e){
		// transform exception stack into a string
		StringWriter excWriter = new StringWriter();
		e.printStackTrace(new PrintWriter(excWriter));
		String dmsg = e.toString();
		if ((dmsg == null) || (e instanceof ClassCastException)) dmsg = e.getClass().getName();
		String msgDetails = dmsg + "\n" + excWriter.toString(); //$NON-NLS-1$
		return msgDetails;
	}

	private static char severityToIndicator(int severity){
		char ind = COMPLETION;
		if ((severity & IStatus.CANCEL) != 0){
			ind = UNEXPECTED;
		}
		else if ((severity & IStatus.ERROR) != 0){
			ind = ERROR;
		}
		else if ((severity & IStatus.INFO) != 0){
			ind = INFORMATION;
		}
		else if ((severity & IStatus.OK) != 0){
			ind = COMPLETION;
		}
		else if ((severity & IStatus.WARNING) != 0){
			ind = WARNING;
		}
		return ind;
	}

	public String getFullMessageID() {
		if (_messageId != null){
			return _messageId;
		}
		else {
			return _pluginId + ":" + getIndicator(); //$NON-NLS-1$
		}
	}

}

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

/**
 * An RSE SystemMessage that can be created from Strings (without XML parsing).
 * 
 * @since 3.0
 */
public class SimpleSystemMessage extends SystemMessage {

	private String _pluginId;
	private String _messageId;

	/**
	 * Creates a String based System Message with severity and ID, but no
	 * message details.
	 *
	 * See {@link #SimpleSystemMessage(String, String, int, String, String)} for
	 * a detailed description.
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
	 * Creates a String based System Message with severity, ID and String
	 * message details.
	 *
	 * This allows using the RSE Messaging Framework based on simple String
	 * messages and IDs, rather than using XML Message files from
	 * {@link SystemMessageFile} along with the
	 * <code>org.eclipse.rse.ui.SystemBasePlugin#loadMessageFile()</code> and
	 * <code>org.eclipse.rse.ui.SystemBasePlugin#getMessage()</code> methods.
	 *
	 * Clients can use either globally unique RSE message IDs or plugin-specific
	 * local IDs. <b>RSE-global message IDs</b> are of the form:
	 * RSE&lt;subcomponent&gt;&lt;number&gt;, where the subcomponent is a single
	 * character:
	 * <ul>
	 * <li>"G" for General</li>
	 * <li>"O" for Other</li>
	 * <li>"F" for Files</li>
	 * <li>"C" for Communications</li>
	 * </ul>
	 * and the number is a four digit number.
	 *
	 * Some RSE-global message IDs are predefined in {@link ICommonMessageIds}.
	 * When used in a SimpleSystemMessage, these common message IDs must be used
	 * along with the matching message Strings from {@link CommonMessages}, in
	 * order to be consistent to the user. For example:
	 *
	 * <pre>
	 * msg = new SimpleSystemMessage(Activator.PLUGIN_ID, ICommonMessageIds.MSG_COMM_AUTH_FAILED, IStatus.ERROR, CommonMessages.MSG_COMM_AUTH_FAILED, NLS.bind(
	 * 		CommonMessages.MSG_COMM_AUTH_FAILED_DETAILS, getHost().getAliasName()));
	 * </pre>
	 *
	 * <b>Plugin-specific local IDs</b> are totally free to be defined by the
	 * plugin that creates a specific message, as long as they are not prefixed
	 * by "RSE". It is recommended that plugins define unique IDs for various
	 * message situations, because this helps problem determination with end
	 * users; but it is not a requirement. Local ID's are specific to the plugin
	 * ID: relative IDs are qualified by the specified plugin ID, so they live
	 * in the plugin ID namespace.
	 *
	 * @param pluginId the id of the originating plugin
	 * @param messageId the RSE-global unique ID or plugin-specific local ID of
	 *            the message
	 * @param severity using IStatus severities
	 * @param msg the message text to be logged or displayed to the user
	 * @param msgDetails the message details with additional information to be
	 *            displayed on request only
	 */
	public SimpleSystemMessage(String pluginId, String messageId, int severity, String msg, String msgDetails) {
		super("RSE", "G", "-", severityToIndicator(severity), msg, msgDetails);  //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$

		_pluginId = pluginId;
		_messageId = messageId;
	}

	/**
	 * Creates a String based System Message with severity and ID, and an
	 * Exception that will be converted into message details.
	 *
	 * See {@link #SimpleSystemMessage(String, String, int, String, String)} for
	 * a detailed description.
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
	 * Creates a String based System Message with a severity and plug-in ID, but
	 * no global or plug-in specific message ID or detail message.
	 *
	 * This constructor does not supply a message id. It is preferred that a
	 * message id is used since it allows easier identification of a unique
	 * message. See
	 * {@link #SimpleSystemMessage(String, String, int, String, String)} for a
	 * detailed description about messages and ID's.
	 *
	 * @param pluginId the id of the originating plugin
	 * @param severity using IStatus severities
	 * @param msg the message text
	 */
	public SimpleSystemMessage(String pluginId, int severity, String msg) {
		this(pluginId, severity, msg, (String)null);
	}

	/**
	 * Creates a String based System Message with a severity and plug-in ID as
	 * well as message details, but no global or plug-in specific message ID.
	 *
	 * This constructor does not supply a message id. It is preferred that a
	 * message id is used since it allows easier identification of a unique
	 * message. See
	 * {@link #SimpleSystemMessage(String, String, int, String, String)} for a
	 * detailed description about messages and ID's.
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
	 * Creates a String based System Message with a severity and plug-in ID, as
	 * well as an exception to convert into message details, but no global or
	 * plug-in specific message ID.
	 *
	 * This constructor does not supply a message id. It is preferred that a
	 * message id is used since it allows easier identification of a unique
	 * message. See
	 * {@link #SimpleSystemMessage(String, String, int, String, String)} for a
	 * detailed description about messages and ID's.
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

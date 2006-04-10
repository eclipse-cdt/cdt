/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.ui.messages;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PropertyPage;


/**
 * @deprecated
 * <p>DO NOT USE THIS CLASS!
 * <p>This class attempts to wrap the message constructs of eclipse provided property
 * and wizard pages with an ISystemMessageLine interface.
 * It fails to do this properly and is extremely fragile since it depends on knowledge
 * of the internal structure of eclipse provided windows.
 * <p>Use SystemMessageLine instead.
 * @link org.eclipse.rse.core.ui.messages.SystemMessageLine
 *
 */
public class SystemDialogPageMessageLine implements ISystemMessageLine, MouseListener {
	// cached inputs
	private Label msgTextLabel;
	private Label msgIconLabel;
	private CLabel msgIconCLabel;
	private DialogPage dlgPage;
	// state
	private SystemMessage sysErrorMessage;
	private SystemMessage sysMessage;
	private boolean stringErrorMessageShowing = false;

	/**
	 * Factory method for wizard pages.
	 * We only need to configure a single message line for all pages in a given wizard,
	 * so this method looks after ensuring there is only one such message line created.
	 * @param wizardPage - the wizard page we are configuring
	 */
	public static SystemDialogPageMessageLine createWizardMsgLine(WizardPage wizardPage) {
		SystemDialogPageMessageLine msgLine = null;
		Composite pageContainer = wizardPage.getControl().getParent();
		Object pageContainerData = null; 
		//Object pageContainerData = pageContainer.getData();
		//System.out.println("pageContainerData = " + pageContainerData);
		if (pageContainerData == null) {
			// wizardPage.getControl() => returns the composite we created in createControl
			//     .getParent() => returns the page container composite created in createPageContainer in WizardDialog, that holds all pages
			//     .getParent() => returns the composite created in createDialogArea in TitleAreaDialog. The "dialog area" of the dialog below the top stuff, and above the button bar.
			//     .getParent() => returns the workarea composite created in createContents in TitleAreaDialog
			//     .getParent() => returns the parent composite passed to createContents in TitleAreaDialog		
			//     .getChildren() => returns the children of this composite, which includes the stuff at the top, which is placed
			//                        there by createTitleArea() in TitleAreaDialog, the parent of WizardDialog
			//                 [0]=> dialog image Label
			//                 [1]=> title Label
			//                 [2]=> message image Label
			//                 [3]=> message Label
			//                 [4]=> filler Label
			Composite dialogAreaComposite = pageContainer.getParent(); // see createDialogArea in WizardDialog
			Composite workAreaComposite = dialogAreaComposite.getParent(); // see createContents in TitleAreaDialog
			Composite mainComposite = workAreaComposite.getParent(); // whatever is passed into createContents in TitleAreaDialog
			Control[] list = mainComposite.getChildren();
			Label msgImageLabel = null;
			Label msgLabel = null;
			if (list[2] instanceof Label) {
				msgImageLabel = (Label) list[2];
			}
			if (list[3] instanceof Label) {
				msgLabel = (Label) list[3];
			} else if (list[4] instanceof Label) {
				msgLabel = (Label) list[4];
			}
			msgLine = new SystemDialogPageMessageLine(wizardPage, msgImageLabel, msgLabel);
			pageContainer.setData(msgLine);
		} else
			msgLine = (SystemDialogPageMessageLine) pageContainerData;
		return msgLine;
	}

	/**
	 * Factory method for property pages.
	 * We only need to configure a single message line for all pages in a properties dialog,
	 * so this method looks after ensuring there is only one such message line created.
	 * @param propertyPage - the property page we are configuring
	 */
	public static SystemDialogPageMessageLine createPropertyPageMsgLine(PropertyPage propertyPage) {
		SystemDialogPageMessageLine msgLine = null;
		Composite pageContainer = propertyPage.getControl().getParent();
		// propertyPage.getControl() => returns the composite we created in createControl
		//     .getParent() => returns the page container composite created in createPageContainer in PreferencesDialog, that holds all pages
		//     .getParent() => returns the composite created in createDialogArea in PreferencesDialog. This holds the tree, title area composite, page container composite and separator	
		//     .getChildren()[1] => returns the title area parent composite, created in createDialogArea in PreferencesDialog
		//     .getChildren()[0] => returns the title area composite, created in createTitleArea in PreferencesDialog
		//     .getChildren() => returns the children of the title area composite
		//                 [0]=> message CLabel
		//                 [1]=> title image
		Composite dialogAreaComposite = pageContainer.getParent(); // see createDialogArea in PreferencesDialog
		Composite titleAreaParentComposite = (Composite) dialogAreaComposite.getChildren()[1];
		Composite titleAreaComposite = (Composite) titleAreaParentComposite.getChildren()[0];
		//Control[] list=titleAreaComposite.getChildren(); 
		// DKM - trying to figure out this mess for 3.0
		Composite listContainer = (Composite) titleAreaComposite.getChildren()[0];
		Control[] list = listContainer.getChildren();
		Label label1 = null;
		Label label2 = null;
		if (list.length > 0) {
			label1 = (Label) list[0];
			label2 = (Label) list[1];
		}
		msgLine = new SystemDialogPageMessageLine(propertyPage, /*(CLabel)list[0]*/label1, label2);
		pageContainer.setData(msgLine);
		return msgLine;
	}

	/**
	 * Private constructor.
	 */
	private SystemDialogPageMessageLine(DialogPage dialogPage, Label msgIconLabel, Label msgTextLabel) {
		this.msgIconLabel = msgIconLabel;
		this.msgTextLabel = msgTextLabel;
		this.dlgPage = dialogPage;
		msgIconLabel.addMouseListener(this);
		msgTextLabel.addMouseListener(this);
	}

	protected SystemMessage getSysErrorMessage() {
		return sysErrorMessage;
	}

	protected SystemMessage getSysMessage() {
		return sysMessage;
	}

	/**
	 * Get the currently displayed error text.
	 * @return The error message. If no error message is displayed <code>null</code> is returned.
	 */
	public SystemMessage getSystemErrorMessage() {
		return sysErrorMessage;
	}

	/**
	 * Clears the currently displayed error message and redisplayes
	 * the message which was active before the error message was set.
	 */
	public void clearErrorMessage() {
		sysErrorMessage = null;
		stringErrorMessageShowing = false;
		dlgPage.setErrorMessage(null);
		setIconToolTipText();
	}

	/**
	 * Clears the currently displayed non-error message.
	 */
	public void clearMessage() {
		dlgPage.setMessage(null);
		sysMessage = null;
		setIconToolTipText();
	}

	/**
	 * Get the currently displayed error text.
	 * @return The error message. If no error message is displayed <code>null</code> is returned.
	 */
	public String getErrorMessage() {
		return dlgPage.getErrorMessage();
	}

	/**
	 * Get the currently displayed message.
	 * @return The message. If no message is displayed <code>null<code> is returned.
	 */
	public String getMessage() {
		return dlgPage.getMessage();
	}

	/**
	 * DO NOT CALL THIS METHOD! IT IS ONLY HERE BECAUSE THE INTERFACE NEEDS IT.
	 * RATHER, CALL THE SAME MSG THAT DIALOGPAGE NOW SUPPORTS, AND THEN CALL
	 * setInternalErrorMessage HERE. WE HAVE TO AVOID INFINITE LOOPS.
	 */
	public void setErrorMessage(String emessage) {
		internalSetErrorMessage(emessage);
		//dlgPage.setErrorMessage(emessage);
	}

	/**
	 * Display the given error message. A currently displayed message
	 * is saved and will be redisplayed when the error message is cleared.
	 */
	public void setErrorMessage(SystemMessage emessage) {
		// I removed @deprecated... I think it was a mistake! What would be the replacement? Phil
		if (emessage == null)
			clearErrorMessage();
		else {
			dlgPage.setErrorMessage(getMessageText(emessage));
			stringErrorMessageShowing = false;
			sysErrorMessage = emessage;
			logMessage(emessage);
		}
		setIconToolTipText();
	}

	/**
	 * Convenience method to set an error message from an exception
	 */
	public void setErrorMessage(Throwable exc) {
		SystemMessage msg = SystemPlugin.getPluginMessage(ISystemMessages.MSG_ERROR_UNEXPECTED);
		msg.makeSubstitution(exc);
		setErrorMessage(msg);
	}

	/**
	 * DO NOT CALL THIS METHOD! IT IS ONLY HERE BECAUSE THE INTERFACE NEEDS IT.
	 * RATHER, CALL THE SAME MSG THAT DIALOGPAGE NOW SUPPORTS, AND THEN CALL
	 * setInternalMessage HERE. WE HAVE TO AVOID INFINITE LOOPS.
	 */
	public void setMessage(String msg) {
		internalSetMessage(msg);
		dlgPage.setMessage(msg);
	}

	/**
	 * Set a non-error message to display. 
	 * If the message line currently displays an error,
	 *  the message is stored and will be shown after a call to clearErrorMessage
	 */
	public void setMessage(SystemMessage smessage) {
		if (smessage == null) {
			clearMessage(); // phil
			return;
		}
		sysMessage = smessage;
		int msgType = IMessageProvider.NONE;
		if ((smessage.getIndicator() == SystemMessage.ERROR) || (smessage.getIndicator() == SystemMessage.UNEXPECTED))
			msgType = IMessageProvider.ERROR;
		else if (smessage.getIndicator() == SystemMessage.WARNING)
			msgType = IMessageProvider.WARNING;
		else if (smessage.getIndicator() == SystemMessage.INFORMATION || (smessage.getIndicator() == SystemMessage.COMPLETION)) msgType = IMessageProvider.INFORMATION;
		dlgPage.setMessage(getMessageText(smessage), msgType);
		logMessage(smessage);
		setIconToolTipText();
	}

	/**
	 * logs the message in the appropriate log
	 */
	private void logMessage(SystemMessage message) {
		Object[] subList = message.getSubVariables();
		for (int i = 0; subList != null && i < subList.length; i++) {
			String msg = message.getFullMessageID() + ": SUB#" + new Integer(i).toString() + ":" + message.getSubValue(subList[i]);
			if (message.getIndicator() == SystemMessage.INFORMATION || message.getIndicator() == SystemMessage.INQUIRY || message.getIndicator() == SystemMessage.COMPLETION)
				SystemBasePlugin.logInfo(msg);
			else if (message.getIndicator() == SystemMessage.WARNING)
				SystemBasePlugin.logWarning(msg);
			else if (message.getIndicator() == SystemMessage.ERROR)
				SystemBasePlugin.logError(msg, null);
			else if (message.getIndicator() == SystemMessage.UNEXPECTED) {
				if (i == subList.length - 1)
					SystemBasePlugin.logError(msg, new Exception());
				else
					SystemBasePlugin.logError(msg, null);
			}
		}
		if (subList == null) {
			String msg = message.getFullMessageID();
			if (message.getIndicator() == SystemMessage.INFORMATION || message.getIndicator() == SystemMessage.INQUIRY || message.getIndicator() == SystemMessage.COMPLETION)
				SystemBasePlugin.logInfo(msg);
			else if (message.getIndicator() == SystemMessage.WARNING)
				SystemBasePlugin.logWarning(msg);
			else if (message.getIndicator() == SystemMessage.ERROR)
				SystemBasePlugin.logError(msg, null);
			else if (message.getIndicator() == SystemMessage.UNEXPECTED) SystemBasePlugin.logError(msg, new Exception());
		}
	}

	// METHODS THAT NEED TO BE CALLED BY DIALOGPAGE IN THEIR OVERRIDE OF SETMESSAGE OR SETERRORMESSAGE
	/**
	 * Someone has called setMessage(String) on the dialog page. It needs to then call this method
	 *  after calling super.setMessage(String) so we can keep track of what is happening.
	 */
	public void internalSetMessage(String msg) {
		sysMessage = null; // overrides it if it was set
		setIconToolTipText();
	}

	/**
	 * Someone has called setErrorMessage(String) on the dialog page. It needs to then call this method
	 *  after calling super.setErrorMessage(String) so we can keep track of what is happening.
	 */
	public void internalSetErrorMessage(String msg) {
		sysErrorMessage = null; // overrides if it was set
		stringErrorMessageShowing = (msg != null);
		setIconToolTipText();
	}

	// MOUSeListener INTERFACE METHODS...
	/**
	 * User double clicked with the mouse
	 */
	public void mouseDoubleClick(MouseEvent event) {
	}

	/**
	 * User pressed the mouse button
	 */
	public void mouseDown(MouseEvent event) {
	}

	/**
	 * User released the mouse button after pressing it
	 */
	public void mouseUp(MouseEvent event) {
		displayMessageDialog();
	}

	/**
	 * Method to return the current system message to display. If error message is set, return it,
	 * else return message.
	 */
	public SystemMessage getCurrentMessage() {
		if (sysErrorMessage != null)
			return sysErrorMessage;
		else if (!stringErrorMessageShowing)
			return sysMessage;
		else
			return null;
	}

	/**
	 * Method to display an error message when the msg button is clicked
	 */
	private void displayMessageDialog() {
		SystemMessage currentMessage = getCurrentMessage();
		if (currentMessage != null) {
			SystemMessageDialog msgDlg = new SystemMessageDialog(dlgPage.getShell(), currentMessage);
			msgDlg.openWithDetails();
		}
	}

	/**
	 * Method to set the tooltip text on the msg icon to tell the user they can press it for more details
	 */
	private void setIconToolTipText() {
		SystemMessage msg = getCurrentMessage();
		String tip = "";
		if (msg != null) {
			//String levelTwo = msg.getLevelTwoText();
			//if ((levelTwo!=null) && (levelTwo.length()>0))
			tip = msg.getFullMessageID() + " " + SystemResources.RESID_MSGLINE_TIP;
		}
		if (msgIconLabel != null) msgIconLabel.setToolTipText(tip);
		if (msgTextLabel != null)
			msgTextLabel.setToolTipText(tip);
		else
			msgIconCLabel.setToolTipText(tip);
	}

	/**
	 * Return the message text to display in the title area, given a system message
	 */
	private String getMessageText(SystemMessage msg) {
		//return msg.getFullMessageID()+" " + msg.getLevelOneText();
		return msg.getLevelOneText();
	}
}
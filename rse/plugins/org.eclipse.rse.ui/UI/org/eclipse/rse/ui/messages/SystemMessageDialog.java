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
 * Martin Oberhuber (Wind River) - [183824] Forward SystemMessageException from IRemoteFileSubsystem
 * Martin Oberhuber (Wind River) - [187115] force SystemMessageDialog always into Display thread
 * Martin Oberhuber (Wind River) - [189272] exception when canceling ssh connect
 * David McKnight   (IBM)        - [216596] determine whether to show yes/no or just okay
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 * Martin Oberhuber (Wind River) - Adding Javadoc
 * David McKnight   (IBM)        - [232670] SystemMessageDialog should not throw IndicatorExceptions
 * David McKnight   (IBM)        - [238585] SystemMessageDialog's details area is always scrolled to the bottom
 * David McKnight   (IBM)        - [253042] SystemMessageDialog.displayHostErrorMessage(shell, string, string) is broken 
 *******************************************************************************/

package org.eclipse.rse.ui.messages;

import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.rse.internal.ui.GenericMessages;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A dialog for showing SystemMessages and optionally asking the user for Yes /
 * No / Ok / Cancel type of response.
 *
 * The client may choose to construct and open the dialog itself, or use one of
 * the provided static factory methods, which construct the dialog and
 * synchronously open it without thread switch (must be called on the UI
 * thread):
 * <ul>
 * <li>{@link #openError(Shell, String, String, IStatus)}</li>
 * <li>{@link #openError(Shell, String, String, IStatus, int)}</li>
 * </ul>
 * or one of the convenience static factory methods which also switch to the UI
 * thread automatically to synchronously open the dialog there if needed:
 * <ul>
 * <li>{@link #show(Shell, SystemMessage)}</li>
 * <li>{@link #showExceptionMessage(Shell, String, Exception)}</li>
 * <li>{@link #displayErrorMessage(Shell, String)}</li>
 * <li>{@link #displayErrorMessage(Shell, SystemMessage)}</li>
 * <li>{@link #displayErrorMessage(Shell, SystemMessage, Throwable)}</li>
 * <li>... and many more displayXXX() static methods.</li>
 * </ul>
 *
 * @noextend This class is not intended to be subclassed by clients, because we
 *           want a uniform appearance of message dialogs.
 */
public class SystemMessageDialog extends ErrorDialog implements Listener {

	/**
	 * Reserve room for this many list items.
	 */
	private static final int LIST_ITEM_COUNT = 5;

	/**
	 * The Details button.
	 */
	private Button detailsButton=null;

	/**
	 * The message to display.
	 */
	private SystemMessage message;

	/**
	 * Exception being reported and logged
	 */
	private Throwable exc;


	/**
	 * The SWT list control that displays the error details.
	 */
	private Text list;


	/**
	 * Indicates whether the error details viewer is currently created.
	 */
	private boolean listCreated = false;


	/**
	 * Filter mask for determining which status items to display.
	 */
	private int displayMask = 0xFFFF;


	/**
	 * The main status object.
	 */
	private IStatus status;


	/**
	 * List of the main error object's detailed errors
	 * (element type: <code>IStatus</code>).
	 */
	private java.util.List statusList;

	/**
	 * the image to use when displaying the message
	 */
	// private String imageName;
	private int imageId;

	/**
	 * show the details panel
	 */
	private boolean showDetails=false;

	/**
	 * buttons for button area
	 */
	private String []buttons=null;

	/**
	 * default button
	 */
	private int defaultIndex=0;

	/**
	 * button id number for the first button in the button bar.<p>
	 * The second button, would have an id of buttonId+1 etc.
	 */
	public static final int BUTTON_ID=1000;

	/**
	 * button pressed to dismiss the dialog
	 */
	private int buttonIdPressed;

	/**
	 *  whether or not to open the dialog with the yes/no buttons
	 */
	private boolean yesNoButtons=false;

	/**
	 *  whether or not to open the dialog with the yes/no/cancel buttons
	 */
	private boolean yesNoCancelButtons=false;

	protected boolean noShowAgainOption;
	protected Button noShowAgainButton;

	// preference stuff for option to not show the dialog again
	protected IPreferenceStore prefStore;
	protected String prefId;
	protected boolean prefValAsSelected;

	/**
	 * Creates an error dialog.
	 * Note that the dialog will have no visual representation (no widgets)
	 * until it is told to open.
	 * @param parentShell the shell under which to create this dialog
	 * @param message the message to display in the dialog
	 */
	public SystemMessageDialog(Shell parentShell, SystemMessage message)
	{
		this(parentShell,
			message.getFullMessageID(),
			message.getLevelOneText(),
			(new MultiStatus(SystemBasePlugin.getBaseDefault().getSymbolicName(), IStatus.OK, "", new Exception(""))), //$NON-NLS-1$ //$NON-NLS-2$
			 		0xFFFFF);
		((MultiStatus)this.status).add(new Status(IStatus.INFO, SystemBasePlugin.getBaseDefault().getSymbolicName(), IStatus.OK, message.getLevelTwoText(),  new Exception(""))); //$NON-NLS-1$
		statusList = Arrays.asList(status.getChildren());
		this.message=message;
	    initImage(message);
	}

	private SystemMessageDialog(Shell parentShell, String dialogTitle, String message,
                                IStatus status, int displayMask)
    {
	    super(parentShell, dialogTitle, message, status, displayMask);
		this.status = status;
		statusList = Arrays.asList(status.getChildren());
		this.displayMask = displayMask;
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	private static String getStatusTitle(SystemMessage message) {
		String title = null;
		// setup image
		if (message.getIndicator()==SystemMessage.INFORMATION ||
				message.getIndicator()==SystemMessage.COMPLETION)
			title = GenericMessages.Information;
		else if (message.getIndicator()==SystemMessage.INQUIRY)
			//imageName=DLG_IMG_QUESTION;
		    title = GenericMessages.Question;
		else if (message.getIndicator()==SystemMessage.ERROR ||
				 message.getIndicator()==SystemMessage.UNEXPECTED)
			title = GenericMessages.Error;
		else if (message.getIndicator()==SystemMessage.WARNING)
			title = GenericMessages.Warning;
		return title;
	}


    private void initImage(SystemMessage message)
    {
		// setup image
		if (message.getIndicator()==SystemMessage.INFORMATION ||
				message.getIndicator()==SystemMessage.COMPLETION)
			//imageName=DLG_IMG_INFO;
		    imageId = SWT.ICON_INFORMATION;
		else if (message.getIndicator()==SystemMessage.INQUIRY)
			//imageName=DLG_IMG_QUESTION;
		    imageId = SWT.ICON_QUESTION;
		else if (message.getIndicator()==SystemMessage.ERROR ||
				 message.getIndicator()==SystemMessage.UNEXPECTED)
			//imageName=DLG_IMG_ERROR;
		    imageId = SWT.ICON_ERROR;
		else if (message.getIndicator()==SystemMessage.WARNING)
			//imageName=DLG_IMG_WARNING;
		    imageId = SWT.ICON_WARNING;
    }


	/* Handles the pressing of the Ok, Details or any button in this dialog.
	 * If the Ok button was pressed then close this dialog.  If the Details
	 * button was pressed then toggle the displaying of the error details area.
	 */
	protected void buttonPressed(int id)
	{
		if (id == IDialogConstants.DETAILS_ID)  // was the details button pressed?
		  toggleDetailsArea();
	    else
	    {
		  super.buttonPressed(id);
		  close();
	    }
		buttonIdPressed=id;
	}

	/*
	 * Creates the buttons for the button bar.
	 * If the message is an inquiry
	 * message or yes/no buttons are explicitly requested then Yes, No, and
	 * perhaps Cancel are the preferred buttons.
	 * Otherwise, if there are buttons supplied by the client use those.
	 * Otherwise if no buttons are supplied, just supply an OK button.
	 * A Details button is suppled if the message indicates that it has any
	 * significant details.  In particular, test to see that the details length is
	 * greater than 2.  This disqualifies using %2 and getting details for some
	 * reason.
	 * d58252 - re-ordered tests to make logic easier to read.  Set initial focus
	 * on the default button since it would normally be on the message which is
	 * now read-only text.
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		if ( yesNoButtons || yesNoCancelButtons || (message.getIndicator()==SystemMessage.INQUIRY) ) {
			boolean yesDefault=(defaultIndex==0);
			boolean noDefault=(defaultIndex==1);
			boolean cancelDefault=(defaultIndex==2);
			createButton(parent, IDialogConstants.YES_ID, IDialogConstants.YES_LABEL, yesDefault);
			createButton(parent, IDialogConstants.NO_ID, IDialogConstants.NO_LABEL, noDefault);
			if (yesNoCancelButtons) {
				createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, cancelDefault);
			}
			if (yesDefault) {
				getButton(IDialogConstants.YES_ID).setFocus();
			} else if (noDefault) {
				getButton(IDialogConstants.NO_ID).setFocus();
			} else if (cancelDefault) {
				getButton(IDialogConstants.CANCEL_ID).setFocus();
			}
		} else if (buttons!=null) {
			for (int i=0; i<buttons.length; i++) {
				boolean defaultButton=(i==defaultIndex);
 				createButton(parent, BUTTON_ID+i, buttons[i], defaultButton);
				if (defaultButton) {
					getButton(BUTTON_ID+i).setFocus();
				}
			}
		} else {
			createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
			getButton(IDialogConstants.OK_ID).setFocus();
		}
		if (status.isMultiStatus() && message != null && message.getLevelTwoText() != null && message.getLevelTwoText().length() > 2) {
			detailsButton = createButton(parent, IDialogConstants.DETAILS_ID, IDialogConstants.SHOW_DETAILS_LABEL, false);
			if (showDetails) {
				toggleDetailsArea2(parent);
			}
		}
	}

	/*
	 * Creates and returns the contents of the upper part
	 * of the dialog (above the button bar).
	 * d58252 - fixed dialog layout to allow resize of message area for long message,
	 * made message area a read-only text for accessibility reasons.
	 */
	protected Control createDialogArea(Composite parent) {
		// create composite
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		((GridLayout)composite.getLayout()).numColumns = 2;

		// create image
		Image image = getShell().getDisplay().getSystemImage(imageId);
		if (image != null) {
			Label label = new Label(composite, 0);
			image.setBackground(label.getBackground());
			label.setImage(image);
			label.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_CENTER |
				GridData.VERTICAL_ALIGN_BEGINNING));
		}

		// create message, this is a read-only text field so it is tab enabled by
		// default for accessibility reasons
		if (message != null) {
			Text messageArea = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
			messageArea.setText(message.getLevelOneText());
			GridData data = new GridData(SWT.FILL, SWT.CENTER, true, true);
			data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
			messageArea.setLayoutData(data);
		}

		// if user wants the option to not show the dialog again
		if (noShowAgainOption) {
			Label l = new Label(composite, SWT.NONE);
			l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER, GridData.VERTICAL_ALIGN_CENTER));
			noShowAgainButton = createNoShowAgainButton(composite);
			GridData data = new GridData(SWT.FILL, SWT.CENTER, true, true);
			noShowAgainButton.setLayoutData(data);
		}

//		composite.pack(true);
		return composite;
	}
	/**
	 * Create this dialog's drop-down list component.
	 *
	 * @param parent the parent composite
	 * @return the drop-down list component
	 */
	protected Text createDropDownList2(Composite parent)
	{
		// create the list
		list = new Text(parent, SWT.READ_ONLY | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL );

		GridData data = new GridData(
			GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL |
			GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_VERTICAL);
		data.heightHint = list.getLineHeight() * LIST_ITEM_COUNT;
		data.widthHint= getDialogArea().getSize().x;
		list.setLayoutData(data);
		listCreated = true;
		// fill the list
		populateList(list);
		list.setTopIndex(0);

		return list;
	}

	/**
	 * When the message involves an exception for which we want to log the stack,
	 *  set that exception here.
	 */
	public void setException(Throwable exc)
	{
		this.exc = exc;
	}

	/**
	 * logs the message in the appropriate log
	 */
	private void logMessage()
	{
		Object[] subList=message.getSubVariables();
		for (int i=0; subList!=null && i<subList.length; i++)
		{
			String msg=message.getFullMessageID()+": SUB#"+new Integer(i).toString()+":"+message.getSubValue(subList[i]); //$NON-NLS-1$ //$NON-NLS-2$
			if (message.getIndicator()==SystemMessage.INFORMATION ||
				message.getIndicator()==SystemMessage.COMPLETION ||
				message.getIndicator()==SystemMessage.INQUIRY)
		    {
				SystemBasePlugin.logInfo(msg);
				if (i==subList.length-1 && message.getIndicator()==SystemMessage.INQUIRY)
					SystemBasePlugin.logInfo(message.getFullMessageID()+" :Button ID Pressed:"+buttonIdPressed); //$NON-NLS-1$
			}
			else if (message.getIndicator()==SystemMessage.WARNING)
				SystemBasePlugin.logWarning(msg);
			else if (message.getIndicator()==SystemMessage.ERROR)
				SystemBasePlugin.logError(msg, null);
			else if (message.getIndicator()==SystemMessage.UNEXPECTED)
			{
				if (i==subList.length-1)
					SystemBasePlugin.logError(msg, (exc!=null) ? exc : new Exception());
				else
					SystemBasePlugin.logError(msg, null);
			}
		}
		if (subList==null)
		{
			String msg=message.getFullMessageID();
			if (message.getIndicator()==SystemMessage.INFORMATION ||
				message.getIndicator()==SystemMessage.COMPLETION ||
				message.getIndicator()==SystemMessage.INQUIRY)
				SystemBasePlugin.logInfo(msg);
			else if (message.getIndicator()==SystemMessage.WARNING)
				SystemBasePlugin.logWarning(msg);
			else if (message.getIndicator()==SystemMessage.ERROR)
				SystemBasePlugin.logError(msg, null);
			else if (message.getIndicator()==SystemMessage.UNEXPECTED)
				SystemBasePlugin.logError(msg, (exc!=null) ? exc : new Exception());
		}
	}

	/**
	 * Opens the message dialog to display the message.
	 */
	public int open()
	{
		if (!showDetails)
			logMessage();
		return super.open();
	}

	/**
	 * Opens the message dialog with the details showing to display the message.
	 */
	public int openWithDetails()
	{
		showDetails=true;
		return open();
	}

	/**
	 * Opens the message dialog with the details showing to display an exception, if that is
	 *  the root cause of the message.
	 * <p>
	 * Also logs the first level text plus exception.
	 */
	public int openWithDetails(Exception exc)
	{
		showDetails=true;
		SystemBasePlugin.logError(message.getLevelOneText(), exc);
		return open();
	}

	/**
	 * Open the dialog with Yes, No, Details button for an Inquiry/Question
	 * message.
	 *
	 * @return <code>true</code> if Yes was pressed, <code>false</code> if
	 *         No was pressed.
	 */
	public boolean openQuestion()
	{
		yesNoButtons=true;
		open();
		return (buttonIdPressed==IDialogConstants.YES_ID);
	}

	/**
	 * Open the dialog with Yes/No, Details button for an Inquiry/Question
	 * message. 
	 *
	 * @return <code>true</code> if Yes was pressed, <code>false</code> if
	 *         No was pressed.
	 */
	public boolean openQuestionNoException()
	{
		return openQuestionNoException(true);
	}

	/**
	 * opens the dialog with an optional Yes/No or OK, Details button for an
	 * Inquiry/Question message. 
	 *
	 * Before RSE 3.0, this method had no arguments. Since RSE 3.0, it is taking
	 * a boolean argument.
	 * 
	 * @since org.eclipse.rse.ui 3.0
	 */
	public boolean openQuestionNoException(boolean showYesNo)
	{
		yesNoButtons=showYesNo;
		open();
		return (buttonIdPressed==IDialogConstants.YES_ID);
	}

	/**
	 * opens the dialog with Yes, No, Cancel Details for an Inquiry/Question message
	 * @return IDialogConstants.YES_ID or NO_ID
  	 */
	public int openYesNoCancel()
	{
		yesNoCancelButtons=true;
		return open();
	}

	/**
	 * returns the id of the button pressed
	 */
	public int getButtonPressedId()
	{
		return buttonIdPressed;
	}

	/**
	 * overrides the default button selection.
	 * @param buttonList  an array of buttons for the button bar
	 */
	public void setButtons(String [] buttonList)
	{
		buttons=buttonList;
	}
	/**
	 * sets the default button for the message.
	 * Use this method if you wan to override the default button (the first one)
	 * @param buttonIndex the 0-based index of the button
	 */
	public void setButtonIndex(int buttonIndex)
	{
		defaultIndex=buttonIndex;
	}




	/**
	 * Populates the list using this error dialog's status object.
	 * This walks the child stati of the status object and
	 * displays them in a list. The format for each entry is
	 *		status_path : status_message
	 * If the status's path was null then it (and the colon)
	 * are omitted.
	 */
	private void populateList(Text list)
	{
		Iterator z = statusList.iterator();
		while (z.hasNext())
		{
			IStatus childStatus = (IStatus) z.next();
			populateList(list, childStatus, 0);
		}
	}



	private void populateList(Text list, IStatus status, int nesting)
	{
		if (!status.matches(displayMask))
		{
			return;
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < nesting; i++)
		{
			sb.append("  "); //$NON-NLS-1$
		}
		sb.append(status.getMessage());
		list.append(sb.toString());
		IStatus[] children = status.getChildren();
		for (int i = 0; i < children.length; i++)
		{
			list.append("\n"); //$NON-NLS-1$
			populateList(list, children[i], nesting + 1);
		}
	}

	/**
	 * Toggles the unfolding of the details area.  This is triggered by
	 * the user pressing the details button.
	 */
	private void toggleDetailsArea()
	{
		Point windowSize = getShell().getSize();
		getContents().computeSize(SWT.DEFAULT, SWT.DEFAULT);

		if (listCreated)
		{
			list.dispose();
			listCreated = false;
			detailsButton.setText(IDialogConstants.SHOW_DETAILS_LABEL);
		}
		else
		{
			list = createDropDownList2((Composite)getContents());
			detailsButton.setText(IDialogConstants.HIDE_DETAILS_LABEL);
		}
		Point newSize = getContents().computeSize(SWT.DEFAULT, SWT.DEFAULT);

		// yantzi:5.1.2 this leaves a gap at the bottom of the dialog when opened with details,
		// why not just set it to the newSize.y?
		//getShell().setSize(new Point(windowSize.x, windowSize.y + (newSize.y - oldSize.y)));
		getShell().setSize(new Point(windowSize.x, newSize.y));
	}

	/**
	 * Toggles the unfolding of the details area.  This is triggered by
	 * open using openWithDetails.
	 */
	private void toggleDetailsArea2(Composite composite)
	{
		Point windowSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
		getDialogArea().computeSize(SWT.DEFAULT, SWT.DEFAULT);
		list = createDropDownList2(composite.getParent());
		detailsButton.setText(IDialogConstants.HIDE_DETAILS_LABEL);
		Point newSize = composite.getParent().computeSize(SWT.DEFAULT, SWT.DEFAULT);
		composite.computeSize(SWT.DEFAULT, SWT.DEFAULT);

		// yantzi:5.1.2 this leaves a gap at the bottom of the dialog when opened with details,
		// why not just set it to the newSize.y?
		//getShell().setSize(new Point(windowSize.x, windowSize.y + (newSize.y - oldSize.y-buttonSize.y)));
		getShell().setSize(new Point(windowSize.x, newSize.y));
	}

	/**
	 * Keeping for posterity. Phil.
	 */
	public static Shell getDefaultShell()
	{
		return SystemBasePlugin.getActiveWorkbenchShell();
	}

	private static void runInUIThread(final Runnable r) {
		Display d = Display.getCurrent();
		if (d!=null) {
			r.run();
		}
		else {
			d = Display.getDefault();
			d.syncExec(r);
		}
	}

	private static int openInUIThread(final Window dlg) {
		Display d = Display.getCurrent();
		if (d!=null) {
			return dlg.open();
		}
		else {
			d = Display.getDefault();
			final int[] rv = new int[1];
			d.syncExec(new Runnable() {
				public void run() {
					rv[0] = dlg.open();
				}
			});
			return rv[0];
		}
	}

	/**
	 * For ease of use for simple messages with no response from user.
	 */
	public static void displayErrorMessage(Shell shell, SystemMessage msg)
	{
		SystemMessageDialog msgDlg = new SystemMessageDialog(shell, msg);
		openInUIThread(msgDlg);
	}

	/**
	 * For ease of use for simple messages which are the result of an exception
	 */
	public static void displayErrorMessage(Shell shell, SystemMessage msg, Throwable exc)
	{
		SystemMessageDialog msgDlg = new SystemMessageDialog(shell, msg);
		msgDlg.setException(exc);
		openInUIThread(msgDlg);
	}

	/**
	 * For ease of use for simple error message text with no response from user.
	 */
	public static void displayErrorMessage(Shell shell, String msgText)
	{
		SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_GENERIC_E);
		msg.makeSubstitution(msgText);
		displayErrorMessage(shell,msg);
	}

	/**
	 * For ease of use for simple message text with no response from user.
	 */
	public static void displayMessage(Shell shell, String msgText)
	{
		SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_GENERIC_I);
		msg.makeSubstitution(msgText);
		displayErrorMessage(shell,msg);
	}

    /**
     * Display this wrapped system message to the user
     */
    public static void displayMessage(Shell shell, SystemMessageException msgEx)
    {
    	if (msgEx != null)
    	  SystemMessageDialog.displayErrorMessage(shell, msgEx.getSystemMessage());
    }

    /**
     * Display this wrapped system message to the user
     */
    public static void displayMessage(SystemMessageException msgEx)
    {
    	if (msgEx != null)
    	  SystemMessageDialog.displayErrorMessage(getDefaultShell(), msgEx.getSystemMessage());
    }

	/**
	 * For ease of use for simple host error messages that are xlated already.
	 */
	public static void displayHostErrorMessage(Shell shell, String hostMsg)
	{
		SystemMessage msg = new SimpleSystemMessage(RSEUIPlugin.PLUGIN_ID, ISystemMessages.MSG_GENERIC_E, IStatus.ERROR, hostMsg);

		SystemMessageDialog msgDlg = new SystemMessageDialog(shell, msg);
		openInUIThread(msgDlg);
	}

	/**
	 * For ease of use for simple host error messages that are xlated already,
	 *   and which have 2nd level text.
	 */
	public static void displayHostErrorMessage(Shell shell, String hostMsg, String levelTwo)
	{
		SystemMessage msg = new SimpleSystemMessage(RSEUIPlugin.PLUGIN_ID, ISystemMessages.MSG_GENERIC_E, IStatus.ERROR, hostMsg, levelTwo);
		
		SystemMessageDialog msgDlg = new SystemMessageDialog(shell, msg);
		openInUIThread(msgDlg);
	}

	/**
	 * For ease of use for simple host warning messages that are xlated already.
	 */
	public static void displayHostWarningMessage(Shell shell, String hostMsg)
	{
		SystemMessage msg = new SimpleSystemMessage(RSEUIPlugin.PLUGIN_ID, ISystemMessages.MSG_GENERIC_W, IStatus.WARNING, hostMsg);
		
		SystemMessageDialog msgDlg = new SystemMessageDialog(shell, msg);
		openInUIThread(msgDlg);
	}

	/**
	 * For ease of use for simple host warning messages that are xlated already,
	 *   and which have 2nd level text.
	 */
	public static void displayHostWarningMessage(Shell shell, String hostMsg, String levelTwo)
	{
		SystemMessage msg = new SimpleSystemMessage(RSEUIPlugin.PLUGIN_ID, ISystemMessages.MSG_GENERIC_W_HELP, IStatus.WARNING, hostMsg, levelTwo);
		SystemMessageDialog msgDlg = new SystemMessageDialog(shell, msg);
		openInUIThread(msgDlg);
	}

	/**
	 * For displaying a generic error message when an unexpected exception happens.
	 */
	public static void displayExceptionMessage(Shell shell, Throwable exc)
	{
		SystemMessage msg = getExceptionMessage(shell, exc);
		if ((shell == null) && (Display.getCurrent()!=null))
		  shell = Display.getCurrent().getActiveShell();
		SystemMessageDialog msgDlg = new SystemMessageDialog(shell, msg);
		msgDlg.setException(exc);
		openInUIThread(msgDlg);
	}


	/**
	 * When an exception occurs and you want to turn it into a SystemMessage,
	 *  call this...
	 */
	public static SystemMessage getExceptionMessage(Shell shell, Throwable exc)
	{
		SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_EXCEPTION_OCCURRED);
		msg.makeSubstitution(exc);
		return msg;
	}

	/**
	 * Put up an error message when the error message framework itself doesn't
	 * work, and also log the error.
	 */
	public static void showExceptionMessage(Shell shell, String msg, Exception exc)
	{
	     final MessageBox mb = new MessageBox(null);
	     //mb.setTitle("Remote Systems Programming Error");
	     mb.setMessage(msg);
	     runInUIThread(new Runnable() {
	    	 public void run() {
	    		 mb.open();
	    	 }
	     });
	     SystemBasePlugin.logError(msg,exc);
	}

   /**
	 * Show this message in the message dialog. We polymorphically show the right message dialog,
	 *  by querying the message type.
	 * @return true if this is a question message, and user presses Yes.
	 */
	public static boolean show(Shell shell, SystemMessage msg)
	{
		boolean yes = false;
		final SystemMessageDialog msgDlg = new SystemMessageDialog(shell, msg);

		if (msg.getIndicator() != SystemMessage.INQUIRY)
			openInUIThread(msgDlg);
		else {
			final boolean[] rv = new boolean[1];
		    runInUIThread(new Runnable() {
		    	 public void run() {
		 			rv[0] = msgDlg.openQuestionNoException();
		    	 }
		    });
		    yes = rv[0];
		}
		return yes;
	}

	/**
	 * Set option to not show this dialog again and specify the preference that should be set
	 * according to whether the user selects to show the dialog again or no. The caller can
	 * query this preference to find out what the user selection is.
	 * @param noShowAgainOption <code>true</code> to show the option in the dialog, <code>false</code> to not show it.
	 * @param prefStore the preference store.
	 * @param prefId the preference id for which a boolean value will be stored according to the user's selection.
	 * @param prefValAsSelected whether to mirror the user selection in the preference. If this is set to
	 * <code>true</code>, then the value stored in the preference is <code>true</code> if the user selects the option,
	 * and <code>false</code> if the user does not select the option. If this is set to <code>false</code>, then the
	 * value stored in the preference will be <code>false</code> if the user selects the option, and <code>true</code>
	 * if the user does not select the option
	 */
	public void setNoShowAgainOption(boolean noShowAgainOption, IPreferenceStore prefStore, String prefId, boolean prefValAsSelected) {
		this.noShowAgainOption = noShowAgainOption;
		this.prefStore = prefStore;
		this.prefId = prefId;
		this.prefValAsSelected = prefValAsSelected;
	}

	/**
	 * Creates a button to allow option to not show this dialog again.
	 * @return the button that allows option to not show this dialog again.
	 */
	protected Button createNoShowAgainButton(Composite c) {
		Button b = new Button(c, SWT.CHECK);
		b.setText(SystemResources.RESID_DO_NOT_SHOW_MESSAGE_AGAIN_LABEL);
		b.setToolTipText(SystemResources.RESID_DO_NOT_SHOW_MESSAGE_AGAIN_TOOLTIP);
		b.addListener(SWT.Selection, this);
		return b;
	}

	/**
	 * Handles events generated by controls on this page.
	 * Should be overridden by child.
	 * Only public because of interface requirement!
	 */
	public void handleEvent(Event e)
	{
	    if (e.type == SWT.Selection) {

	    	if (e.widget == noShowAgainButton) {
	    		boolean isNoShowSelected = noShowAgainButton.getSelection();

	    		if ((prefStore != null) && (prefId != null)) {

	    			if (prefValAsSelected) {
	    				prefStore.setValue(prefId, isNoShowSelected);
	    			}
	    			else {
	    				prefStore.setValue(prefId, !isNoShowSelected);
	    			}
	    		}
	    	}
	    }
	}
}

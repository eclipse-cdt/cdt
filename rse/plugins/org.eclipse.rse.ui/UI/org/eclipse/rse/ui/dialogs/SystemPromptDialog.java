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

package org.eclipse.rse.ui.dialogs;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.Mnemonics;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.messages.SystemMessageLine;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;


/**
 * Base dialog class. Use this whenever more than a simple string
 * prompt is needed (which InputDialog gives you).
 * <p>
 * By default we configure the dialog as modal. If you do not want this,
 * call setBlockOnOpen(false) after instantiating.
 * <p>
 * This base class offers the following ease-of-use features:
 * <ul>
 *  <li>automatic support for typical buttons OK, Browse and Cancel.
 *     <ul>
 *         <li>just override the processOk, processBrowse and processCancel methods to process them
 *         <li>constructor option whether to enable Browse or not
 *         <li>ability to override default labels for all three.
 *     </ul> 
 *   <li>automatic support for a message line
 *     <ul>
 *         <li>can turn it off
 *         <li>methods for writing messages to the message line
 *     </ul>
 *   <li>override just one method (<b>createInner</b>) to populate the client area
 *   <li>support for automatically assigning unique mnemonics to all buttons
 *     <ul>
 *         <li>easier to code, easier to translate
 *         <li>this is always done for you, so do not put your own mnemonics in your button text!
 *     </ul>
 *   <li>support for an input object for change mode vs "new" mode:
 *     <ul>
 *         <li>in change mode, callers pass this in via ctor or setInputObject method
 *         <li>in your child class, get it via inherited getInputObject and cast to what it is you are expecting
 *     </ul>
 *   <li>support for output object:
 *     <ul>
 *         <li>when ok pressed, built up the output object and then call inherited setOutputObject object method
 *         <li>dialog callers can then retrieve it via getOuputObject method.
 *     </ul>
 *   <li>support for {@link #wasCancelled()} method so callers can easily determine how user exited the dialog
 *   <li>special affinity if you use an imbedded WorkWith widget:
 *     <ul>
 *         <li>call WorkWith's setPromptDialog(this) method to tell it the parent dialog is a PromptDialog
 *         <li>WorkWith widget will use PromptDialog's message line vs its own
 *         <li>WorkWith widget will know how to reassign mnemonics for its buttons when style is switched.
 *     </ul>
 *   <li>optional support (mri) for additional buttons (which your code handles):
 *     <ul>
 *         <li>Add
 *         <li>Browse
 *         <li>Test
 *         <li>Details>>
 *     </ul>
 *   <li>optional support for a built-in progress monitor just like wizards have. Call {@link #setNeedsProgressMonitor(boolean)}
 *   <li>a simple {@link #setBusyCursor(boolean)} method to toggle the cursor between busy and normal
 * </ul>
 * 
 * <p>To use this class: </p>
 * <ol>
 *  <li>Subclass it, specifying the dialog title in the constructor. Optionally, also call {@link #setHelp(String)} to set the dialog's help in the constructor.
 *  <li>Override {@link #createInner(Composite)} to populate the contents
 *  <li>Override {@link #processOK()} to process the pressing of the OK button
 * </ol>
 * <p>For error checking, add modify listeners to entry fields and if needed selection listeners to buttons, then in your event handler </p>
 * <ol>
 *  <li>Call {@link #setErrorMessage(SystemMessage)} to display an error if detected. Pass null to clear previous error.
 *  <li>Call {@link #setPageComplete(boolean)} with true if there are no errors and all required data is supplied. This enables/disables OK.
 *  <li>The same two error-reporting strategies described in {@link org.eclipse.rse.ui.wizards.AbstractSystemWizardPage} apply here.
 * </ol>
 */
public abstract class SystemPromptDialog
	   extends org.eclipse.jface.dialogs.Dialog 
	   implements Listener, IDialogConstants, ISystemPromptDialog,
	              ISystemMessageLine, org.eclipse.jface.dialogs.IDialogPage, IRunnableContext, Runnable
{
	
	protected boolean okPressed = false;
	protected boolean showBrowseButton = false;
	protected boolean showTestButton = false;
	protected boolean showAddButton = false;
	protected boolean showDetailsButton = false;
	protected boolean pack = false;
	protected boolean initialOKButtonEnabledState = true;
	protected boolean initialAddButtonEnabledState = false;
	protected boolean initialDetailsButtonEnabledState = true;
	protected boolean detailsButtonHideMode = false;
	protected boolean showOkButton = true;
	protected Shell   overallShell = null;
	protected Composite parentComposite, dialogAreaComposite;
	protected Composite buttonsComposite;
	protected Button  okButton, cancelButton, testButton, browseButton, addButton, detailsButton;
	protected String  title, labelOk, labelBrowse, labelTest, labelCancel, labelAdd, labelDetailsShow, labelDetailsHide;
	protected String  tipOk, tipBrowse, tipTest, tipCancel, tipAdd, tipDetailsShow, tipDetailsHide;
	protected String  detailsShowLabel;
	protected String  detailsHideLabel;
	protected String  helpId;
	//protected Hashtable helpIdPerControl;
	protected Image   titleImage;
	protected Object  inputObject, outputObject; // input and output objects
	protected SystemMessageLine fMessageLine;
	protected SystemMessage pendingMessage, pendingErrorMessage;
	protected int minWidth, minHeight;
    protected int marginWidth = 3;
    protected int marginHeight = 3;
    protected int verticalSpacing = 2;
    protected int horizontalSpacing = 3;

	//protected Composite parent;
	//protected Composite contentsComposite, buttonsComposite;
	protected Mnemonics dialogMnemonics; // list of all unique mnemonics used in this dialog
	protected ISystemValidator outputObjectValidator;
	
	protected long activeRunningOperations = 0;
	protected boolean operationCancelableState;
	protected boolean needsProgressMonitor;
	protected ProgressMonitorPart progressMonitorPart;
	protected Cursor waitCursor;
	protected Cursor arrowCursor;
	protected MessageDialog windowClosingDialog;
	protected SelectionAdapter cancelListener;	
	
	private static final String FOCUS_CONTROL = "focusControl";//$NON-NLS-1$
	
	protected static final int BROWSE_ID = 50;
	protected static final int TEST_ID = 60;	
	protected static final int ADD_ID = 70;	
	protected static final int DETAILS_ID = 80;	
	protected static final boolean BROWSE_BUTTON_YES = true;
	protected static final boolean BROWSE_BUTTON_NO  = false;	
	protected static final boolean TEST_BUTTON_YES = true;
	protected static final boolean TEST_BUTTON_NO  = false;	
	protected static final boolean ADD_BUTTON_YES = true;
	protected static final boolean ADD_BUTTON_NO  = false;	
	protected static final boolean DETAILS_BUTTON_YES = true;
	protected static final boolean DETAILS_BUTTON_NO  = false;	
	
	/**
	 * Constructor one: ok and cancel buttons
	 * @param shell - parent window this dialog is modal to.
	 * @param title - the title for the dialog. Typically translated.
	 * @see #setInputObject(Object)
	 */
	public SystemPromptDialog(Shell shell, String title)
	{
		this(shell, title, null, false);
	}
	/**
	 * Constructor two: ok and cancel buttons and an icon for the dialog title area
	 * @param shell - parent window this dialog is modal to.
	 * @param title - the title for the dialog. Typically translated.
	 * @param titleImage - the icon for the dialog's title area.
	 * @see #setInputObject(Object)
	 */
	public SystemPromptDialog(Shell shell, String title, Image titleImage)
	{
		this(shell, title, null, false, titleImage);
	}
	/**
	 * Constructor three: ok and cancel buttons, plus explicit setting of input object
	 * @param shell - parent window this dialog is modal to.
	 * @param title - the title for the dialog. Typically translated.
	 * @param inputObject - the contextual input data, which can be queried via {@link #getInputObject()}.
	 */
	public SystemPromptDialog(Shell shell, String title, Object inputObject)
	{
		this(shell, title, inputObject, false);
	}
	/**
	 * Constructor four: ok, browse and cancel buttons
	 * @param shell - parent window this dialog is modal to.
	 * @param title - the title for the dialog. Typically translated.
	 * @param browse - true if to show a Browse button, false if no Browse button desired.
	 * @see #setInputObject(Object)
	 */
	public SystemPromptDialog(Shell shell, String title, boolean browse)
	{
		this(shell, title, null, browse);
	}
	/**
	 * Constructor five: ok, browse and cancel buttons, plus explicit setting of input object
	 * @param shell - parent window this dialog is modal to.
	 * @param title - the title for the dialog. Typically translated.
	 * @param inputObject - the contextual input data, which can be queried via {@link #getInputObject()}.
	 * @param browse - true if to show a Browse button, false if no Browse button desired.
	 */
	public SystemPromptDialog(Shell shell, String title, Object inputObject, boolean browse)
	{
		this(shell, title, inputObject, browse, null);
	}
	/**
	 * Constructor six: ok, browse and cancel buttons, plus explicit setting of input object and
	 *  an icon for the dialog title area
	 * @param shell - parent window this dialog is modal to.
	 * @param title - the title for the dialog. Typically translated.
	 * @param inputObject - the contextual input data, which can be queried via {@link #getInputObject()}.
	 * @param browse - true if to show a Browse button, false if no Browse button desired.
	 * @param titleImage - the icon for the dialog's title area.
	 */
	public SystemPromptDialog(Shell shell, String title, Object inputObject, boolean browse,
		                      Image titleImage)
	{
		super(shell);
		setShellStyle(SWT.RESIZE | getShellStyle()); // dwd 
		this.title = title;
		this.titleImage = titleImage;
		this.inputObject = inputObject;
		this.showBrowseButton = browse;
		super.setBlockOnOpen(true);
	}
	/**
	 * Constructor six: an input object. true/false for browse button, true/false for test button, a title image
	 */
	public SystemPromptDialog(Shell shell, String title, Object inputObject, boolean browse, boolean test,
		                      Image titleImage)
	{
		super(shell);
		setShellStyle(SWT.RESIZE | getShellStyle()); // dwd 
		this.title = title;
		this.titleImage = titleImage;
		this.inputObject = inputObject;
		this.showBrowseButton = browse;
		this.showTestButton = test;
		super.setBlockOnOpen(true);
	}


    /* (non-Javadoc)
     * Method declared in Window.
     */
    protected void configureShell(Shell shell) 
    {
  	  super.configureShell(shell);
  	  overallShell = shell;
	  if (title != null)
		shell.setText(title);
	  //if (titleImage != null)
	  //  shell.setImage(titleImage); // ?correct method?
	  //shell.setSize(300,200); // default w,h	   
    }		
    
    /**
     * Specify if a progress monitor is desired in this dialog. Should be called right after instantiation.
     * The default is false. If true is specified, area on the dialog is reserved for the progress monitor,
     * and the monitor can be retrieved via {@link #getProgressMonitor()}. 
     * <p>Support is patterned after WizardDialog in JFace.
     */
    public void setNeedsProgressMonitor(boolean needs)
    {
    	this.needsProgressMonitor = needs;
    }
    
    /**
     * For setting the default overall help for the dialog.
     * This can be overridden per control by calling {@link #setHelp(Control, String)}.
     */
    public void setHelp(String helpId)
    {
    	if (parentComposite != null)
    	{
    	  SystemWidgetHelpers.setHelp(parentComposite, helpId); 
    	  SystemWidgetHelpers.setHelp(buttonsComposite, helpId); 
    	  //SystemWidgetHelpers.setCompositeHelp(parentComposite, helpId, helpIdPerControl);		
    	  //SystemWidgetHelpers.setCompositeHelp(buttonsComposite, helpId, helpIdPerControl);		
    	}
    	this.helpId = helpId;
    }
    /**
     * For retrieving the help Id
     */
    public String getHelpContextId()
    {
    	return helpId;
    }
    /**
     * For setting control-specific help for a control on the wizard page.
     * <p>
     * This overrides the default set in the call to {@link #setHelp(String)}.
     */
    public void setHelp(Control c, String helpId)
    {
    	SystemWidgetHelpers.setHelp(c, helpId);		
    	//if (helpIdPerControl == null)
    	//  helpIdPerControl = new Hashtable();
    	//helpIdPerControl.put(c, helpId);
    }

	/**
	 * For explicitly setting input object. Called by SystemDialogAction
	 */
	public void setInputObject(Object inputObject)
	{
		this.inputObject = inputObject;
	}
	/**
	 * For explicitly getting input object
	 */
	public Object getInputObject()
	{
		return inputObject;
	}
	
	/**
	 * For explicitly getting output object after dialog is dismissed. Set by the
	 * dialog's processOK method.
	 */
	public Object getOutputObject()
	{
		return outputObject;
	}
	
	/**
	 * Allow caller to determine if window was cancelled or not.
	 */
	public boolean wasCancelled()
	{
		return !okPressed;
	}
	
	/** 
	 * If validation of the output object is desired, set the validator here.
	 * It will be used when the child class calls setOutputObject(). 
	 */
	public void setOutputObjectValidator(ISystemValidator outputObjectValidator)
	{
	      this.outputObjectValidator = outputObjectValidator;
	} 
	
	/**
	 * Return the output object validator
	 */
	public ICellEditorValidator getOutputObjectValidator()
	{
		return outputObjectValidator;
	}
	
	/**
	 * Get the ISystemMessageLine control reference.
	 */
	public ISystemMessageLine getMessageLine()
	{
		  return fMessageLine;
	}
		
	/**
	 * For explicitly setting output object. Call this in your processOK method.
	 * If an output object validator has been set via setOutputObjectValidator, then
	 *  this will call its isValid method on the outputObject and will return the error
	 *  message if any that it issues. A return of null always means no errors and
	 *  hence it is ok to dismiss the dialog.
	 */
	protected SystemMessage setOutputObject(Object outputObject)
	{
		this.outputObject = outputObject;
		if ((outputObjectValidator != null) && (outputObject instanceof String))
		  return outputObjectValidator.validate((String)outputObject);
		else
		  return null;
	}
	
	/**
	 * Set the cursor to the wait cursor (true) or restores it to the normal cursor (false).
	 */
	public void setBusyCursor(boolean setBusy)
	{
		if (setBusy)
		{
          // Set the busy cursor to all shells.
    	  Display d = getShell().getDisplay();
    	  waitCursor = new Cursor(d, SWT.CURSOR_WAIT);
    	  setDisplayCursor(waitCursor);		
		}
		else
		{
		  setDisplayCursor(null);
		  if (waitCursor != null)
		    waitCursor.dispose();
		  waitCursor = null;
		}
	}

	// --------------------------
	// OK BUTTON CONFIGURATION...
	// --------------------------
	/**
	 * Disable showing of Ok button
	 */
	public void setShowOkButton(boolean showOk)
	{
		this.showOkButton = showOk;
	}
	/**
	 * For explicitly setting ok button label
	 */
	public void setOkButtonLabel(String label)
	{
		this.labelOk = label;
	}
	/**
	 * For explicitly setting ok button tooltip text
	 */
	public void setOkButtonToolTipText(String tip)
	{
		this.tipOk = tip;
	}
	/**
	 * For explicitly enabling/disabling ok button.
	 */
	public void enableOkButton(boolean enable)
	{
		if (okButton != null)
		 okButton.setEnabled(enable);
	}
	/**
	 * Return ok button widget
	 */
	public Button getOkButton()
	{
		return okButton;
	}
    /**
     * Set initial enabled state of ok button.
     * Call this from createContents, which is called before the ok button is created.
     */
    public void setInitialOKButtonEnabledState(boolean enabled)
    {
    	initialOKButtonEnabledState = enabled;
    }
	/**
	 * To be overridden by children.
	 * Called when user presses OK button. 
	 * Child dialog class should set output object.
	 * Return true to close dialog.
	 * Return false to not close dialog.
	 */
	protected boolean processOK() 
	{
		return true;
	}	
	
	// ------------------------------
	// CANCEL BUTTON CONFIGURATION...
	// ------------------------------
	/**
	 * For explicitly setting cancel button label
	 */
	public void setCancelButtonLabel(String label)
	{
		this.labelCancel = label;
	}
	/**
	 * For explicitly setting cancel button tooltip text
	 */
	public void setCancelButtonToolTipText(String tip)
	{
		this.tipCancel = tip;
	}
	/**
	 * For explicitly enabling/disabling cancel button.
	 */
	public void enableCancelButton(boolean enable)
	{
		if (cancelButton != null)
		  cancelButton.setEnabled(enable);
	}
	/**
	 * Return cancel button widget.
	 * Be careful <i>not</i> to call the deprecated inherited method getCancelButton()!
	 */
	public Button getCancelOrCloseButton()
	{
		return cancelButton;
	}
	/**
	 * To be overridden by children.
	 * Called when user presses CANCEL button. 
	 * Return true to close dialog.
	 * Return false to not close dialog.
	 */
	protected boolean processCancel() 
	{
		return true;
	}		

	// ------------------------------
	// BROWSE BUTTON CONFIGURATION...
	// ------------------------------
	/**
	 * Explicitly specify if Browse Button to be shown
	 */
	public void setShowBrowseButton(boolean show)
	{
		this.showBrowseButton = show;
	}
	/**
	 * For explicitly setting browse button label
	 */
	public void setBrowseButtonLabel(String label)
	{
		this.labelBrowse = label;
	}
	/**
	 * For explicitly setting Browse button tooltip text
	 */
	public void setBrowseButtonToolTipText(String tip)
	{
		this.tipBrowse = tip;
	}
	/**
	 * For explicitly enabling/disabling Browse button.
	 */
	public void enableBrowseButton(boolean enable)
	{
		if (browseButton != null)
		  browseButton.setEnabled(enable);
	}
	/**
	 * Return browse button widget
	 */
	public Button getBrowseButton()
	{
		return browseButton;
	}
	/**
	 * To be overridden by children.
	 * Called when user presses BROWSE button. 
	 * Return false always!
	 */
	protected boolean processBrowse() 
	{
		return false;
	}			

	// ------------------------------
	// TEST BUTTON CONFIGURATION...
	// ------------------------------
	/**
	 * Explicitly specify if Test Button to be shown
	 */
	public void setShowTestButton(boolean show)
	{
		this.showTestButton = show;
	}
	/**
	 * For explicitly setting test button label
	 */
	public void setTestButtonLabel(String label)
	{
		this.labelTest = label;
	}
	/**
	 * For explicitly setting Test button tooltip text
	 */
	public void setTestButtonToolTipText(String tip)
	{
		this.tipTest = tip;
	}
	/**
	 * For explicitly enabling/disabling Test button.
	 */
	public void enableTestButton(boolean enable)
	{
		if (testButton != null)
		  testButton.setEnabled(enable);
	}
	/**
	 * Return test button widget
	 */
	public Button getTestButton()
	{
		return testButton;
	}
	/**
	 * To be overridden by children.
	 * Called when user presses TEST button. 
	 * Return false always!
	 */
	protected boolean processTest() 
	{
		return false;
	}		

	// ------------------------------
	// ADD BUTTON CONFIGURATION...
	// ------------------------------
	/**
	 * Explicitly specify if Add Button to be shown
	 */
	public void setShowAddButton(boolean show)
	{
		this.showAddButton = show;
	}
	/**
	 * For explicitly setting Add button label
	 */
	public void setAddButtonLabel(String label)
	{
		this.labelAdd = label;
	}
	/**
	 * For explicitly setting Add button tooltip text
	 */
	public void setAddButtonToolTipText(String tip)
	{
		this.tipAdd = tip;
	}
	/**
	 * For explicitly enabling/disabling Add button.
	 */
	public void enableAddButton(boolean enable)
	{
		if (addButton != null)
		  addButton.setEnabled(enable);
		else
          initialAddButtonEnabledState = enable;
	}
	/**
	 * Return Add button widget
	 */
	public Button getAddButton()
	{
		return addButton;
	}
	/**
	 * To be overridden by children.
	 * Called when user presses ADD button. 
	 * Return false always!
	 */
	protected boolean processAdd() 
	{
		return false;
	}		

	// ------------------------------
	// DETAILS BUTTON CONFIGURATION...
	// ------------------------------
	/**
	 * Explicitly specify if Details Button to be shown.
	 * There is support to automatically toggle the text.
	 * @param true if the Details button is to be shown
	 * @param true if the button should initially be in "hide mode" versus "hide mode"
	 */
	public void setShowDetailsButton(boolean show, boolean hideMode)
	{
		this.showDetailsButton = show;
		this.detailsButtonHideMode = hideMode;
	}
	/**
	 * For explicitly setting Details button label
	 */
	public void setDetailsButtonLabel(String showLabel, String hideLabel)
	{
		this.labelDetailsShow = showLabel;
		this.labelDetailsHide = hideLabel;
	}
	/**
	 * For explicitly setting Details button tooltip text
	 */
	public void setDetailsButtonToolTipText(String showTip, String hideTip)
	{
		this.tipDetailsShow = showTip;
		this.tipDetailsHide = hideTip;
	}
	/**
	 * For explicitly enabling/disabling Details button.
	 */
	public void enableDetailsButton(boolean enable)
	{
		if (detailsButton != null)
		  detailsButton.setEnabled(enable);
		else
          initialDetailsButtonEnabledState = enable;
	}
	/**
	 * Return Details button widget
	 */
	public Button getDetailsButton()
	{
		return detailsButton;
	}
	/**
	 * To be overridden by children.
	 * Called when user presses DETAILS button. 
	 * <p>
	 * Note the text is automatically toggled for you! You need only
	 * do whatever the functionality is that you desire
	 * 
	 * @param hideMode the current state of the details toggle, prior to this request. If you return true from
	 *   this method, this state and the button text will be toggled.
	 * 
	 * @return true if the details state toggle was successful, false if it failed.
	 */
	protected boolean processDetails(boolean hideMode) 
	{
		return true;
	}		



	/**
	 * Get the list of all unique mnemonics used by buttons on this dialog. This is only
	 * set at the time createButtonBar is called by the parent, and this is after the createContents
	 * method call. It will return null until then. So, it is not available for you at constructor time.
	 * Use setUniqueMnemonic(Button) on the returned object if you want to add a mnemonic to 
	 * button after the fact.
	 */
	public Mnemonics getDialogMnemonics()
	{
		 return dialogMnemonics;
	} 
		
	/**
	 * Create message line. 
	 */
	protected ISystemMessageLine createMessageLine(Composite c)
	{
		//System.out.println("INSIDE CREATEMESSAGELINE");		
		fMessageLine= new SystemMessageLine(c);
		fMessageLine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		Display.getCurrent().asyncExec(this);
		return fMessageLine;
	}
	/**
	 * For asynch exec we defer some operations until other pending events are processed.
	 * For now, this is used to display pending error messages
	 */
	public void run()
	{
		if (pendingErrorMessage != null)
		  setErrorMessage(pendingErrorMessage);
		else if (pendingMessage != null)
		  setMessage(pendingMessage);
		pendingErrorMessage = pendingMessage = null;
	}
	
	/**
	 * Handles events generated by controls on this page.
	 * Should be overridden by child.
	 * Only public because of interface requirement!
	 */
	public void handleEvent(Event e)
	{
	    //Widget source = e.widget;
	}
	
	/**
	 * Swing-like method to auto-set the size of this dialog by 
	 *  looking at the preferred sizes of all constituents.
	 * @deprecated
	 */
    protected void pack()
    {
   	      // pack = true; // defer until controls are all created.
    }                     
    
	/**
	 * Called by createContents method.
	 * Create this dialog's widgets inside a composite.
	 * Child classes must override this.
	 */
	protected abstract Control createInner(Composite parent);
	
	/**
	 * Return the Control to be given initial focus.
	 * Child classes must override this, but can return null.
	 */
	protected abstract Control getInitialFocusControl();
		
	
	
	/**
	 * Override of parent method. 
	 * Called by IDE when button is pressed.
	 */
	protected void buttonPressed(int buttonId) 
	{
		okPressed = false;
		if (buttonId == OK_ID)
		{
		    //setReturnId(buttonId);		  	
			setReturnCode(OK);
			if (processOK())
		    {
			  	okPressed = true;
			    close();
			}
		}
		/* Now handled by the cancelListener
		else if (buttonId == CANCEL_ID)
		{
		  	if (processCancel())
		  	  super.buttonPressed(buttonId);
		}*/
		else if (buttonId == BROWSE_ID)
		{
		  	processBrowse();
		}		  
		else if (buttonId == TEST_ID)
		{
		  	processTest();
		}		  
		else if (buttonId == ADD_ID)
		{
		  	processAdd();
		}		  
		else if (buttonId == DETAILS_ID)
		{			
		  	if (processDetails(detailsButtonHideMode))
		  	{
		  	  detailsButtonHideMode = !detailsButtonHideMode;
		  	  detailsButton.setText(detailsButtonHideMode ? detailsShowLabel : detailsHideLabel);
	          if (detailsButtonHideMode && (tipDetailsShow != null))
	            detailsButton.setToolTipText(tipDetailsShow);
	          else if (!detailsButtonHideMode && (tipDetailsHide != null))
	            detailsButton.setToolTipText(tipDetailsHide);		  	  
		  	}
		}		  

	}
	
	/**
	 * Intercept of parent, so we can create the msg line above the button bar.
	 */
	protected Control createButtonBar(Composite parent) 
	{
		createMessageLine(parent);
		return super.createButtonBar(parent);
	}

	/**
	 * Adjust the width hint of a button to account for the presumed addition of a mnemonic.
	 * @param button the button whose width is to be adjusted.
	 */
	protected void adjustButtonWidth(Button button) {
		String text = button.getText();
		// adjust the width hint to allow for a mnemonic to be added.
		if (text != null) {
			if (text.indexOf('&') < 0) {
				Object layoutData = button.getLayoutData();
				if (layoutData instanceof GridData) {
					GridData gd = (GridData) layoutData;
					if (gd.widthHint != SWT.DEFAULT) {
						gd.widthHint += convertWidthInCharsToPixels(3);
					}
				}
			}
		}
	}

	/**
	 * Add buttons to the dialog's button bar.
	 *
	 * Subclasses may override.
	 *
	 * @param parent the button bar composite
	 */
	protected void createButtonsForButtonBar(Composite parent) 
	{
	   //System.out.println("Inside createButtonsForButtonBar");
       //System.out.println("Vertical spacing="+((GridLayout)parent.getLayout()).verticalSpacing);
       //System.out.println("Margin height="+((GridLayout)parent.getLayout()).marginHeight);
       ((GridLayout)parent.getLayout()).verticalSpacing = verticalSpacing;
       //((GridLayout)parent.getLayout()).horizontalSpacing = horizontalSpacing;
       ((GridLayout)parent.getLayout()).marginWidth = marginWidth;
       ((GridLayout)parent.getLayout()).marginHeight = marginHeight;
		//System.out.println("INSIDE CREATEBUTTONSFORBUTTONBAR");		

	   // create requested buttons...

	   if (showOkButton)
	   {
	     String okLabel = (labelOk!=null)?labelOk: IDialogConstants.OK_LABEL;
	     okButton = createButton(parent, IDialogConstants.OK_ID, okLabel, true);
	     okButton.setEnabled(initialOKButtonEnabledState);
	     if (tipOk != null)
	       okButton.setToolTipText(tipOk);
	   }
	   if (showBrowseButton)
	   {
	     String browseLabel = (labelBrowse!=null)?labelBrowse: SystemResources.BUTTON_BROWSE;
		 browseButton = createButton(parent, BROWSE_ID, browseLabel, false);	   
	     if (tipBrowse != null)
	       browseButton.setToolTipText(tipBrowse);
	   }
	   if (showTestButton)
	   {
	     String testLabel = (labelTest!=null)?labelTest: SystemResources.BUTTON_TEST;
		 testButton = createButton(parent, TEST_ID, testLabel, false);	   
	     if (tipTest != null)
	       testButton.setToolTipText(tipTest);
	   }
	   if (showAddButton)
	   {
	     String addLabel  = (labelAdd!=null)?labelAdd: SystemResources.BUTTON_ADD;
		 addButton = createButton(parent, ADD_ID, addLabel, !showOkButton);	   
	     if (tipAdd != null)
	       addButton.setToolTipText(tipAdd);
         addButton.setEnabled(initialAddButtonEnabledState);
	   }
	   if (showDetailsButton)
	   {
	     detailsShowLabel  = Mnemonics.removeMnemonic((labelDetailsShow!=null)?labelDetailsShow: IDialogConstants.SHOW_DETAILS_LABEL);
	     detailsHideLabel  = Mnemonics.removeMnemonic((labelDetailsHide!=null)?labelDetailsHide: IDialogConstants.HIDE_DETAILS_LABEL);
	     String detailsLabel = detailsButtonHideMode ? detailsShowLabel : detailsHideLabel;
		 detailsButton = createButton(parent, DETAILS_ID, detailsLabel, false);	   
		 adjustButtonWidth(detailsButton);
	     if (detailsButtonHideMode && (tipDetailsShow != null))
	       detailsButton.setToolTipText(tipDetailsShow);
	     else if (!detailsButtonHideMode && (tipDetailsHide != null))
	       detailsButton.setToolTipText(tipDetailsHide);
         detailsButton.setEnabled(initialDetailsButtonEnabledState);
	   }

	   String cancelLabel = (labelCancel!=null)?labelCancel: IDialogConstants.CANCEL_LABEL;
	   cancelButton = createButton(parent, IDialogConstants.CANCEL_ID, cancelLabel, false);
	   if (tipCancel != null)
	     cancelButton.setToolTipText(tipCancel);
	   cancelListener= new SelectionAdapter() 
	   {
		   public void widgetSelected(SelectionEvent e) 
		   {
		   	   if (activeRunningOperations <= 0) 
		   	   {
		  	     if (processCancel())
		  	       doCancel();
		   	   }
		   	   else
		   	     cancelButton.setEnabled(false);
		   }
	   };
	   cancelButton.addSelectionListener(cancelListener);

       buttonsComposite = parent;
	   if (helpId != null)
		 SystemWidgetHelpers.setHelp(buttonsComposite, helpId);	
		 //SystemWidgetHelpers.setCompositeHelp(buttonsComposite, helpId);	
	}	
	
	private void doCancel()
	{
		super.buttonPressed(CANCEL_ID);
	}
	
    /**
     * Set minimum width and height for this dialog. 
     * Pass zero for either to not affect it.
     */
    public void setMinimumSize(int width, int height)
    {
    	minWidth = width;
    	minHeight = height;
    }
	/**
	 * Override of parent.
	 */
	protected Control createContents(Composite parent) 
	{
		//System.out.println("INSIDE SYSTEMPROMPTDIALOG#CREATECONTENTS");
			
		Control c = super.createContents(parent);

        this.parentComposite = (Composite)c;
		if (helpId != null)
		  SystemWidgetHelpers.setHelp(parentComposite, helpId);	 
		  //SystemWidgetHelpers.setCompositeHelp(parentComposite, helpId, helpIdPerControl);	
	
		// OK, parent method created dialog area and button bar.
		// Time now to do our thing...

		// Insert a progress monitor if requested
		if (needsProgressMonitor)
		{
			
			boolean showSeparators = false;
			// Build the first separator line
			Label separator = null;
			if (showSeparators)
			{
			  separator= new Label(parentComposite, SWT.HORIZONTAL | SWT.SEPARATOR);
			  separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			}
			GridLayout pmlayout= new GridLayout();
			pmlayout.numColumns= 1;

			progressMonitorPart= new ProgressMonitorPart(parentComposite, pmlayout, SWT.DEFAULT);
			progressMonitorPart.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			progressMonitorPart.setVisible(false);	
			
			// Build the second separator line
			if (showSeparators)
			{
			  separator= new Label(parentComposite, SWT.HORIZONTAL | SWT.SEPARATOR);
			  separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			}
			if (SystemPlugin.isTheSystemRegistryActive())
			{
			  SystemPlugin.getTheSystemRegistry().setRunnableContext(getShell(),this);
	          // add a dispose listener for the shell
	          getShell().addDisposeListener(new DisposeListener() 
	          {
		        public void widgetDisposed(DisposeEvent e) 
		        {
		          //System.out.println("Inside dispose for SystemPromptDialog");
        	      SystemPlugin.getTheSystemRegistry().clearRunnableContext();		      	
		        }
 	          });
			}
		}
	
		//createMessageLine((Composite)c); now done before buttons are created. d54501		
		
		Control initialFocusControl = getInitialFocusControl();
		if (initialFocusControl != null)
		  initialFocusControl.setFocus();
		  
		//buttonsComposite = buttons; // remember the buttons part of the dialog so we can add mnemonics
		/*
		 * OK now is a good time to add the mnemonics!
		 * This is because both the contents and buttons have been created.
		 */		  
		dialogMnemonics = SystemWidgetHelpers.setMnemonics((Composite)getButtonBar());
		applyMnemonics(dialogMnemonics, (Composite)getDialogArea());
		
		/*
		 * OK, now that mnemonics for the buttons are set, query the mnemonic for the details button and its
		 *  two states... defect 42904
		 */
	    if (showDetailsButton)
	    {
	   	   if (detailsButtonHideMode)
	   	   {
	   	 	  detailsShowLabel = detailsButton.getText();
	   	 	  char m = Mnemonics.getMnemonic(detailsShowLabel);
	   	 	  detailsHideLabel = Mnemonics.applyMnemonic(detailsHideLabel, m);
	   	   }
	   	   else
	   	   {
	   	   	  detailsHideLabel = detailsButton.getText();
	   	 	  char m = Mnemonics.getMnemonic(detailsHideLabel);
	   	 	  detailsShowLabel = Mnemonics.applyMnemonic(detailsShowLabel, m);
	   	   }
	    }
	    if (labelCancel != null)
	      labelCancel = cancelButton.getText(); // reset to include the mnemonic, in case we need to restore it
		
		if (pack)
		{
		   Shell shell = getShell();			
		   shell.setSize(shell.computeSize(SWT.DEFAULT, SWT.DEFAULT, true));
		}
		// return composite created by call to parent's method
		return c;
	}
	
	/**
	 * Apply mnemonic to the composite.
	 * @param c the composite.
	 */
	protected void applyMnemonics(Mnemonics mnemonics, Composite c) {
		SystemWidgetHelpers.setMnemonics(mnemonics, c);		
	}

	/**
	 * Called by parent.
	 * Create overall dialog page layout.
	 */
	protected Control createDialogArea(Composite parent) 
	{
		//System.out.println("INSIDE CREATEDIALOGAREA");		
		Composite c        = new Composite(parent, SWT.NONE);
		this.dialogAreaComposite = c;
		GridLayout layout  = new GridLayout();
		layout.numColumns  = 1;
		layout.marginHeight= marginWidth;
		layout.marginWidth = marginHeight;
		layout.verticalSpacing  = verticalSpacing;
		layout.horizontalSpacing= horizontalSpacing;
		c.setLayout(layout);		
		c.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Control inner = createInner(c); // allows for child classes to override.

		/*
		 * And now is the time to auto-size if so requested...
		 */
		if (minWidth > 0)
		{
		  boolean newData = false;
		  GridData data = (GridData)inner.getLayoutData();
		  if (data == null)
		  {
		  	newData = true;
		    data = new GridData();
		  }
		  data.widthHint = minWidth;
		  data.grabExcessHorizontalSpace = true;
		  data.horizontalAlignment = GridData.FILL;
		  if (newData)
		    inner.setLayoutData(data);
		}
		if (minHeight > 0)
		{
		  boolean newData = false;
		  GridData data = (GridData)inner.getLayoutData();
		  if (data == null)
		  {
		  	newData = true;
		    data = new GridData();
		  }
		  data.heightHint = minHeight;
		  data.grabExcessVerticalSpace = true;
		  data.verticalAlignment = GridData.FILL;
		  if (newData)
		    inner.setLayoutData(data);
		}		
		//this.parent = c;
		//contentsComposite = c; // remember the contents part of the dialog so we can add mnemonics
		return c; 
	}
	
	/**
	 * Call this to disable the Apply button if the input is not complete or not valid.
	 */
	public void setPageComplete(boolean complete)
	{
		if (okButton != null)
		  okButton.setEnabled(complete);
		else
		  initialOKButtonEnabledState = complete;
	}
	
	// -----------------
	// HELPER METHODS...
	// -----------------
    /**
     * Add a separator line. This is a physically visible line.
     */
	protected Label addSeparatorLine(Composite parent, int nbrColumns)
	{
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);	
	    GridData data = new GridData();
	    data.horizontalSpan = nbrColumns;
	    data.horizontalAlignment = GridData.FILL;
	    separator.setLayoutData(data);
	    return separator;		
	}
	/**
	 * Add a spacer line
	 */
	protected Label addFillerLine(Composite parent, int nbrColumns)
	{
		Label filler = new Label(parent, SWT.LEFT);	
	    GridData data = new GridData();
	    data.horizontalSpan = nbrColumns;
	    data.horizontalAlignment = GridData.FILL;
	    filler.setLayoutData(data);	
	    return filler;	
	}
	/**
	 * Add a spacer line that grows in height to absorb extra space
	 */
	protected Label addGrowableFillerLine(Composite parent, int nbrColumns)
	{
		Label filler = new Label(parent, SWT.LEFT);	
	    GridData data = new GridData();
	    data.horizontalSpan = nbrColumns;
	    data.horizontalAlignment = GridData.FILL;
	    data.verticalAlignment = GridData.FILL;
        data.grabExcessVerticalSpace = true;
	    filler.setLayoutData(data);		
	    return filler;	
	}

    /**
     * Expose inherited protected method convertWidthInCharsToPixels as a publicly
     *  excessible method
     */
    public int publicConvertWidthInCharsToPixels(int chars) 
    {
    	return convertWidthInCharsToPixels(chars);
    }
    /**
     * Expose inherited protected method convertHeightInCharsToPixels as a publicly
     *  excessible method
     */
    public int publicConvertHeightInCharsToPixels(int chars) 
    {
    	return convertHeightInCharsToPixels(chars);
    }
    
    // -----------------------------	
	// ISystemMessageLine METHODS...
	// -----------------------------
	/**
	 * Clears the currently displayed error message and redisplayes
	 * the message which was active before the error message was set.
	 */
	public void clearErrorMessage()
	{
		if (fMessageLine != null)
		  fMessageLine.clearErrorMessage();
	}
	/**
	 * Clears the currently displayed message.
	 */
	public void clearMessage()
	{
		if (fMessageLine != null)
		  fMessageLine.clearMessage();
	}
	/**
	 * Get the currently displayed error text.
	 * @return The error message. If no error message is displayed <code>null</code> is returned.
	 */
	public String getErrorMessage()
	{
		if (fMessageLine != null)
		  return fMessageLine.getErrorMessage();
		else
		  return null;
	}
	/**
	 * Get the currently displayed error text.
	 * @return The error message. If no error message is displayed <code>null</code> is returned.
	 */
	public SystemMessage getSystemErrorMessage()
	{
		if (fMessageLine != null)
		  return fMessageLine.getSystemErrorMessage();
		else
		  return null;
	}
	/**
	 * Get the currently displayed message.
	 * @return The message. If no message is displayed <code>null<code> is returned.
	 */
	public String getMessage()
	{
		if (fMessageLine != null)
		  return fMessageLine.getMessage();
		else
		  return null;
	}
	/**
	 * Display the given error message. A currently displayed message
	 * is saved and will be redisplayed when the error message is cleared.
	 */
	public void setErrorMessage(String message)
	{
		if (fMessageLine != null)
		  fMessageLine.setErrorMessage(message);
		else
		  SystemMessageDialog.displayErrorMessage(getShell(),message);
	}
	
	/**
	 * Display the given error message. A currently displayed message
	 * is saved and will be redisplayed when the error message is cleared.
	 */
	public void setErrorMessage(SystemMessage message)
	{
		if (fMessageLine != null)
		{
			if (message != null)
		      fMessageLine.setErrorMessage(message);
		    else
		      fMessageLine.clearErrorMessage();
		}
		else //if (message != null)
		{
		  //(new SystemMessageDialog(getShell(),message)).open();
		  pendingErrorMessage = message;
		}
	}
	/**
	 * Set the message text. If the message line currently displays an error,
	 * the message is stored and will be shown after a call to clearErrorMessage
	 */
	public void setMessage(String message)
	{
		if (fMessageLine != null)
		{
			if (message != null)
			  fMessageLine.setMessage(message);
			else
			  fMessageLine.clearMessage();		
		}
	}	  

	/** 
	 *If the message line currently displays an error,
	 * the message is stored and will be shown after a call to clearErrorMessage
	 */
	public void setMessage(SystemMessage message)
	{
		if (fMessageLine != null)
		  fMessageLine.setMessage(message);
		else if (message != null)
		  //(new SystemMessageDialog(getShell(),message)).open();
		  pendingMessage = message;
	}
	

	/**
	 * Convenience method to set an error message from an exception
	 */
	public void setErrorMessage(Throwable exc)
	{
		if (fMessageLine != null)
		  fMessageLine.setErrorMessage(exc);
		else
		{
	     	SystemMessage msg = SystemPlugin.getPluginMessage(ISystemMessages.MSG_ERROR_UNEXPECTED);
		    msg.makeSubstitution(exc);
		    (new SystemMessageDialog(getShell(),msg)).open();
		}
	}
	
	// -------------------------------------------------------------------------------
	// IDialogPage interface methods, which we only implement to enable dialog help...
	// -------------------------------------------------------------------------------
	public void setDescription(String description) {}
	public String getDescription() {return null;}
	public Image getImage() {return titleImage;}
	public void performHelp() {}
	public void setVisible(boolean visible) {}
	public void dispose() {}
	public Control getControl() {return parentComposite;}
	public void setControl(Control c) {}
	public void createControl(Composite parent) {}
	public void setImageDescriptor(ImageDescriptor id) {}
	/**
	 * Get the dialog's title
	 */
	public String getTitle() 
	{
		return title;
	}
	/**
	 * Set the dialog's title
	 */
	public void setTitle(String title) 
	{
		this.title = title;
		if (overallShell != null)
		  overallShell.setText(title);
    }


    // --------------------------------------------
    // Methods to support a progress monitor...
    //  using WizardDialog as an example.
    // --------------------------------------------

    /**
     * Returns the progress monitor for this dialog (if it has one).
     *
     * @return the progress monitor, or <code>null</code> if
     *   this dialog does not have one
     */
    public IProgressMonitor getProgressMonitor() 
    {
    	return progressMonitorPart;
    }

    /**
     * About to start a long running operation tiggered through
     * the dialog. Shows the progress monitor and disables the dialog's
     * buttons and controls.
     *
     * @param enableCancelButton <code>true</code> if the Cancel button should
     *   be enabled, and <code>false</code> if it should be disabled
     * @return the saved UI state
     */
    protected Object aboutToStart(boolean enableCancelButton) 
    {
    	Map savedState = null;
    	operationCancelableState = enableCancelButton;
    	if ((getShell() != null) && (activeRunningOperations <= 0))
    	{
    		// Save focus control
    		Control focusControl = getShell().getDisplay().getFocusControl();
    		if (focusControl != null && focusControl.getShell() != getShell())
    			focusControl = null;			
    		cancelButton.removeSelectionListener(cancelListener);		
    		// Set the busy cursor to all shells.
    		Display d = getShell().getDisplay();
    		waitCursor = new Cursor(d, SWT.CURSOR_WAIT);
    		setDisplayCursor(waitCursor);
				
    		// Set the arrow cursor to the cancel component.
    		arrowCursor= new Cursor(d, SWT.CURSOR_ARROW);
    		cancelButton.setCursor(arrowCursor);
    		
    		// Set the cancel button label to "Cancel" if it isn't already
    		if (labelCancel != null)
    		  cancelButton.setText("&" + IDialogConstants.CANCEL_LABEL);                		

    		// Deactivate shell
    		savedState = saveUIState(needsProgressMonitor && enableCancelButton);
    		if (focusControl != null)
    			savedState.put(FOCUS_CONTROL, focusControl);
			
    		// Attach the progress monitor part to the cancel button
    		if (needsProgressMonitor) 
    		{
    			progressMonitorPart.attachToCancelComponent(cancelButton);
    			progressMonitorPart.setVisible(true);
    		}
    	}
    	return savedState;
    }    
    
    /**
     * Creates and returns a new wizard closing dialog without opening it.
     */ 
    protected MessageDialog createWizardClosingDialog() 
    {
    	MessageDialog result= new MessageDialog(getShell(),
    		JFaceResources.getString("WizardClosingDialog.title"),//$NON-NLS-1$
    		null,
    		JFaceResources.getString("WizardClosingDialog.message"),//$NON-NLS-1$
    		MessageDialog.QUESTION,
    		new String[] {IDialogConstants.OK_LABEL},		0 ); 
    	return result;
    }
 
    /* (non-Javadoc)
     * Method declared on Dialog.
     */
    public boolean close() 
    {
    	if (okToClose())
    		return hardClose();
    	else
    		return false;
    }
    /**
     * Checks whether it is alright to close this wizard dialog
     * and perform standard cancel processing. If there is a
     * long running operation in progress, this method posts an
     * alert message saying that the wizard cannot be closed.
     *
     * @return <code>true</code> if it is alright to close this dialog, and
     *  <code>false</code> if it is not
     */
    protected boolean okToClose() 
    {
    	if (activeRunningOperations > 0) 
    	{
    		synchronized (this) 
    		{    			
    			windowClosingDialog = createWizardClosingDialog();
    		}	
    		windowClosingDialog.open();
    		synchronized (this) 
    		{
    			windowClosingDialog = null;
    		}
    		return false;
    	}
    	return true;
    }

    /**
     * Closes this window. Really closes it. Calls super.close()
     *
     * @return <code>true</code> if the window is (or was already) closed,
     *   and <code>false</code> if it is still open
     */
    protected boolean hardClose() 
    {
    	return super.close();
    }
    
    /**
     * Restores the enabled/disabled state of the given control.
     *
     * @param w the control
     * @param h the map (key type: <code>String</code>, element type:
     *   <code>Boolean</code>)
     * @param key the key
     * @see #saveEnableStateAndSet
     */
    protected void restoreEnableState(Control w, Map h, String key) 
    {
    	if (w != null) {
    		Boolean b = (Boolean) h.get(key);
    		if (b != null)
    			w.setEnabled(b.booleanValue());
    	}
    }
    /**
     * Restores the enabled/disabled state of the wizard dialog's
     * buttons and the tree of controls for the currently showing page.
     *
     * @param state a map containing the saved state as returned by
     *   <code>saveUIState</code>
     * @see #saveUIState
     */
    protected void restoreUIState(Map state) 
    {
	    //protected Button  okButton, cancelButton, testButton, browseButton, addButton, detailsButton;
    	restoreEnableState(okButton,     state, "ok");
    	restoreEnableState(testButton,   state, "test");
    	restoreEnableState(browseButton, state, "browse");
    	restoreEnableState(cancelButton, state, "cancel");
    	restoreEnableState(addButton,    state, "add");
    	restoreEnableState(detailsButton,state, "details");
    	SystemControlEnableState pageState = (SystemControlEnableState) state.get("page");//$NON-NLS-1$
    	pageState.restore();
    }
   
    /**
     * Captures and returns the enabled/disabled state of the wizard dialog's
     * buttons and the tree of controls for the currently showing page. All
     * these controls are disabled in the process, with the possible excepton of
     * the Cancel button.
     *
     * @param keepCancelEnabled <code>true</code> if the Cancel button should
     *   remain enabled, and <code>false</code> if it should be disabled
     * @return a map containing the saved state suitable for restoring later
     *   with <code>restoreUIState</code>
     * @see #restoreUIState
     */
    protected Map saveUIState(boolean keepCancelEnabled) 
    {
    	Map savedState= new HashMap(10);
    	saveEnableStateAndSet(okButton,     savedState, "ok",     false);
    	saveEnableStateAndSet(testButton,   savedState, "test",   false);
    	saveEnableStateAndSet(browseButton, savedState, "browse", false);
    	saveEnableStateAndSet(cancelButton, savedState, "cancel", keepCancelEnabled);
    	saveEnableStateAndSet(addButton,    savedState, "add",    false);
    	saveEnableStateAndSet(detailsButton,savedState, "details",false);
    	//savedState.put("page", ControlEnableState.disable(getControl()));
    	savedState.put("page", SystemControlEnableState.disable(dialogAreaComposite));    	
    	return savedState;
    }
    
    /**
     * Saves the enabled/disabled state of the given control in the
     * given map, which must be modifiable.
     *
     * @param w the control, or <code>null</code> if none
     * @param h the map (key type: <code>String</code>, element type:
     *   <code>Boolean</code>)
     * @param key the key
     * @param enabled <code>true</code> to enable the control,
     *   and <code>false</code> to disable it
     * @see #restoreEnableState(Control,Map,String)
     */
    protected void saveEnableStateAndSet(Control w, Map h, String key, boolean enabled) 
    {
    	if (w != null) {
    		h.put(key, new Boolean(w.isEnabled()));
    		w.setEnabled(enabled);
    	}
    }
    
    /**
     * Sets the given cursor for all shells currently active
     * for this window's display.
     *
     * @param c the cursor
     */
    protected void setDisplayCursor(Cursor c) 
    {
    	setDisplayCursor(getShell(), c);
    }
    /**
     * Sets the given cursor for all shells currently active for the given shell's display.
     *
     * @param c the cursor
     */
    public static void setDisplayCursor(Shell shell, Cursor c) 
    {
    	if (c == null)
    	{
    		// attempt to fix problem that the busy cursor sometimes stays. Phil
    		// DKM - commenting this out since the attempt to fix problem didn't work
    		//       and it causes accessibility problems when expanding a system via keyboard
    		//	shell.forceActive();
    		//	shell.forceFocus();
    	}
    	if (shell != null && shell.getDisplay() != null)
    	{
    	Shell[] shells = shell.getDisplay().getShells();
    	for (int i = 0; i < shells.length; i++)
    	{
    		shells[i].setCursor(c);
    	}
    	}
    }
    
    
    /**
     * For IRunnableContext. 
     */
    public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) 
           throws InvocationTargetException, InterruptedException 
    {
    	// The operation can only be canceled if it is executed in a separate thread.
    	// Otherwise the UI is blocked anyway.
    	Object state = aboutToStart(fork && cancelable);
    	activeRunningOperations++;
    	if (activeRunningOperations > 1)
    	{
    		//System.out.println("Nested operation!");
    		//(new Exception()).fillInStackTrace().printStackTrace();    		
    	}
    	try {
    		ModalContext.run(runnable, fork, getProgressMonitor(), getShell().getDisplay());
    	} finally {
    		activeRunningOperations--;
    		stopped(state);
    	}
    }    
    /**
     * A long running operation triggered through the wizard
     * was stopped either by user input or by normal end.
     * Hides the progress monitor and restores the enable state
     * wizard's buttons and controls.
     *
     * @param savedState the saved UI state as returned by <code>aboutToStart</code>
     * @see #aboutToStart
     */
    private void stopped(Object savedState) 
    {
    	if ((getShell() != null) && (activeRunningOperations <= 0))
    	{
    		if (needsProgressMonitor) 
    		{
    			progressMonitorPart.setVisible(false);	    			
    			progressMonitorPart.removeFromCancelComponent(cancelButton);
    		}		
    		Map state = (Map)savedState;
    		restoreUIState(state);
    		cancelButton.addSelectionListener(cancelListener);
    		setDisplayCursor(null);	
    		cancelButton.setCursor(null);
    		if (labelCancel != null)
    		  cancelButton.setText(labelCancel);
    		waitCursor.dispose();
    		waitCursor = null;
    		arrowCursor.dispose();
    		arrowCursor = null;
    		Control focusControl = (Control)state.get(FOCUS_CONTROL);
    		if (focusControl != null)
    			focusControl.setFocus();
    	}
    }

}
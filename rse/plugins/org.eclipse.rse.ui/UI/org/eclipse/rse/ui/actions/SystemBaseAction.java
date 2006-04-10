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

package org.eclipse.rse.ui.actions;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.SystemAdapterHelpers;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.ISystemTree;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * A suggested base class for remote systems related actions.
 * <p>
 * What this offers beyond the basic Action class:
 * <ul>
 *   <li>Support for setting label and tooltip and description from a resource bundle and single key prefix,
 *         from which each string is derived by appending ".label", ".tooltip" and ".description" to the prefix.
 *   <li>Support for setting and retrieving the shell from which the action is launched. Can be
 *         set in the parent, or passed as null there and set later via setShell. Retrieve via getShell.
 *   <li>Support actions that are selection-dependent, but need not be informed of every selection change.
 *         For performance reasons, only track selections if the action is displayed in the toolbar where
 *         state change needs to be visible. For popup actions, we only need to be informed of the 
 *         currently selection at the time the popup is built. Popup menu builders call selectionChanged
 *         in this class at popup population time, so you need only subclass this method. However, even
 *         that is made easier. The default implementation of this method in this class converts the 
 *         selection to an IStructuredSelection, and calls an overridable method named updateSelection
 *         passing the structured selection. The action is enabled or disabled based on the returned
 *         boolean value. Just override updateSelection to enable or disable based on selection.
 *         Indeed, the default implementation of the updateSelection method is to return the result of 
 *         AND-ED result of calling checkObjectType on each item in the selection. So the easiest thing
 *         you can do to enable/disable is to override checkObjectType.
 *   <li>Support actions that are selection-dependent and need to be informed of every selection change
 *         as it happens. To enable this, simply call setSelectionProvider to supply the GUI object such
 *         as a viewer which fires the selection changed events we wish to monitor for. For the Remote
 *         System Explorer viewer, we would get call setSelectionProvider(SystemViewPlugin.getSystemView());
 *   <li>Support for actions that are only to be enabled when a single item is selected. To enable this
 *         and save from testing for it yourself, simply call the method allowOnMultipleSelection(false).
 *         The default is true, multiple selections are allowed.
 * 	 <li>Support for disabling actions when the corresponding SystemConnection for the selected object
 *         is offline.  The SystemConnection can be automatically determined for some of the common
 * 		   objects (subsystems, IRemoteFiles), for others you must set the SystemConnection for 
 * 		   this offline support.</li> 
 * </ul>
 * There are many constructors but they can be broken down into permutations of the following info:
 * <ul>
 *   <li>Label, tooltip and description. These can be supplied as strings, or via resource bundle and key.
 *       This requires four flavors of constructor to except varying of these four pieces of information.
 *   <li>Images. There are four flavors of constructors that take an image, and four identical that do not.
 * </ul>
 * 
 * <p>To use this dialog, subclass it and <b>override</b> the following methods</p>:
 * <sl>
 *  <li>{@link #run()}, where you place the code to do the actual work when this action is invoked.
 *  <li>{@link #updateSelection(IStructuredSelection)}, for selection sensitive actions. This is your first
 *   opporunity to enable/disable the action when the selection changes, by looking at the given selection
 *   and returning true or false. The default implementation calls checkObjectType for each selected object.
 *  <li>{@link #checkObjectType(Object)}, for selection sensitive actions. This is your second
 *   opporunity to enable/disable the action when the selection changes, by looking at each individual
 *   selected object, and returning true or false.
 * </sl>
 * <p>In addition to the methods you must override, you can optionally call various methods to configure
 * this action:</p>
 * <sl>
 *  <li>{@link #setInputs(Shell, Viewer,ISelection)} or {@link #setShell(Shell)} and {@link #setViewer(Viewer)} and 
 *    {@link #setSelection(ISelection)}. These methods are called by the RSE viewers for context menu actions, and 
 *    can be called directly for actions used in other contexts.
 *  <li>{@link #setSelectionProvider(ISelectionProvider)}, for those cases when your action monitors for selection
 *    changes (pull) versus being told about them (push) via setSelection. This is less efficient, and should only
 *    be used for selection-dependent actions in toolbars and pull-down menus, versus popup menus.
 *  <li>{@link #setHelp(String)} to set the ID of the F1 context help for this action.
 *  <li>{@link #setContextMenuGroup(String)} to set the menu group in which to place this action, when used in menus.
 *  <li>{@link #allowOnMultipleSelection(boolean)} to specify if this action is to be enabled or disabled when multiple
 *    objects are selected. The default is disabled.
 *  <li>{@link #setSelectionSensitive(boolean)} to specify if this action's enabled state is not sensitive to what
 *    is currently selected.
 * </sl>
 * <p>Further, the code you write can use the properties captured by this action and retrievable by the getter methods
 * supplied by this class.</p>
 * 
 * @see ISystemAction
 * @see SystemBaseDialogAction
 * @see SystemBaseWizardAction 
 */
public class SystemBaseAction extends Action implements ISystemAction
{
	protected Shell   shell;
	protected boolean allowOnMultipleSelection = false;	
	protected IStructuredSelection sSelection = null;
	private   Iterator selectionIterator = null;
	private   String contextMenuGroup = null;
	protected boolean selectionSensitive = true;
	protected boolean traceSelections = false;
	protected String  traceTarget;
	protected Viewer  viewer = null;
	protected String  helpId;
	protected Cursor waitCursor;
	protected Cursor arrowCursor;
	private   ISelectionProvider fSelectionProvider;
	private   Vector  previousShells = new Vector();
	private   Vector  previousViewers = new Vector();
	//private   Vector  previousSelections = new Vector(); hmm, a problem here: can't just test for disposed. 
	// todo: remember previous selections stack, and add a restoreSelection() method that the SystemView 
	//  will call after the context menu is disposed
	
	// yantzi: artemis 6.0:  offline support
	private boolean isAvailableOffline;
	private IHost conn;
		
	/**
	 * Constructor for SystemBaseAction when translated label is known. You must separately
	 *  call setToolTipText and setDescription to enable these if desired.
	 * @param text string to display in menu or toolbar
	 * @param image icon to display in menu or toolbar. Can be null.
	 * @param shell Shell of parent window. Can be null if you don't know it, but call setShell when you do.
	 */
	public SystemBaseAction(String text, ImageDescriptor image, Shell shell) 
	{
		this(text, null, null, image, shell);
	}
	/**
	 * Constructor for SystemBaseAction when translated label and tooltip are known. You must
	 *  separately call setDescription to enable this if desired.
	 * @param text string to display in menu or toolbar
	 * @param tooltip string to display when user hovers mouse over action.
     * @param image icon to display in menu or toolbar. Can be null.
	 * @param parent Shell of parent window. Can be null if you don't know it, but call setShell when you do.
	 */
	public SystemBaseAction(String text, String tooltip, ImageDescriptor image, Shell parent) 
	{
		this(text, tooltip, null, image, parent);
	}
	/**
	 * Constructor for SystemBaseAction when translated label and tooltip and description are
	 *  all known. 
	 * @param text string to display in menu or toolbar
	 * @param tooltip string to display when user hovers mouse over action.
	 * @param description string displayed in status bar of some displays. Longer than tooltip.
     * @param image icon to display in menu or toolbar. Can be null.
	 * @param shell Shell of parent window. Can be null if you don't know it, but call setShell when you do.
	 */
	public SystemBaseAction(String text, String tooltip, String description, ImageDescriptor image, Shell shell) 
	{
		super(text, image);
		this.shell = shell;
		if (tooltip != null)
		  setToolTipText(tooltip);
		if (description != null)
		  setDescription(description);
      //setTracing("SystemFilterPoolReferenceSelectAction");
	}
	
	/**
	 * Constructor for SystemBaseAction when translated label and tooltip and description are
	 *  all known. 
	 * @param text string to display in menu or toolbar
	 * @param tooltip string to display when user hovers mouse over action.
	 * @param description string displayed in status bar of some displays. Longer than tooltip.
     * @param image icon to display in menu or toolbar. Can be null.
     * @param style one of <code>AS_PUSH_BUTTON</code>, <code>AS_CHECK_BOX</code>,
     * 				<code>AS_DROP_DOWN_MENU</code>, <code>AS_RADIO_BUTTON</code>, and <code>AS_UNSPECIFIED</code>.
	 * @param shell Shell of parent window. Can be null if you don't know it, but call setShell when you do.
	 */
	public SystemBaseAction(String text, String tooltip, String description, ImageDescriptor image, int style, Shell shell) 
	{
		super(text, style);
		this.shell = shell;
		if (image != null)
			setImageDescriptor(image);
		if (tooltip != null)
		  setToolTipText(tooltip);
		if (description != null)
		  setDescription(description);
      //setTracing("SystemFilterPoolReferenceSelectAction");
	}

	
	/**
	 * Used for actions with no image icon.
	 * Constructor for SystemBaseAction when translated label is known. You must separately
	 *  call setToolTipText and setDescription to enable these if desired.
	 * @param text string to display in menu or toolbar
	 * @param shell Shell of parent window. Can be null if you don't know it, but call setShell when you do.
	 */
	public SystemBaseAction(String text, Shell shell) 
	{
		this(text, null, null, null, shell);
	}
	/**
	 * Used for actions with no image icon.
	 * Constructor for SystemBaseAction when translated label and tooltip are known. You must
	 *  separately call setDescription to enable this if desired.
	 * @param text string to display in menu or toolbar
	 * @param tooltip string to display when user hovers mouse over action.
	 * @param shell Shell of parent window. Can be null if you don't know it, but call setShell when you do.
	 */
	public SystemBaseAction(String text, String tooltip, Shell shell) 
	{
		this(text, tooltip, null, null, shell);
	}
	/**
	 * Used for actions with no image icon.
	 * Constructor for SystemBaseAction when translated label and tooltip and description are
	 *  all known. 
	 * @param text string to display in menu or toolbar
	 * @param tooltip string to display when user hovers mouse over action.
	 * @param description string displayed in status bar of some displays. Longer than tooltip.
	 * @param shell Shell of parent window. Can be null if you don't know it, but call setShell when you do.
	 */
	public SystemBaseAction(String text, String tooltip, String description, Shell shell) 
	{
		this(text, tooltip, description, null, shell);		
	}	


    // ------------------------
    // HELPER METHODS...
    // ------------------------

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
			shell.forceActive();
			shell.forceFocus();
		}
		Shell[] shells = shell.getDisplay().getShells();
		for (int i = 0; i < shells.length; i++)
		{
			shells[i].setCursor(c);
		}
	}	
    /**
     * Turn on tracing for selections, shell and viewer to watch as it is set
     */
    protected void setTracing(boolean tracing)
    {
    	traceSelections = tracing;
    }
    /**
     * Turn on tracing for selections, shell and viewer to watch as it is set,
     *  scoped to a particular class name (will use indexOf('xxx') to match).
     */
    protected void setTracing(String tracingClassTarget)
    {
    	traceSelections = true;
    	traceTarget = tracingClassTarget;
    }
    /**
     * Issue trace message
     */
    protected void issueTraceMessage(String msg)
    {
    	if (traceSelections)
    	{
    		String className = this.getClass().getName();
    		if ((traceTarget==null) || (className.indexOf(traceTarget)>=0))
              SystemBasePlugin.logInfo(this.getClass().getName()+": "+getText()+": "+msg);
    	}
    }

    /**
     * Returns the implementation of ISystemViewElement for the given
     * object.  Returns null if the adapter is not defined or the
     * object is not adaptable.
     */
    protected ISystemViewElementAdapter getAdapter(Object o) 
    {
    	return SystemAdapterHelpers.getAdapter(o);
    }    
    /**
     * Returns the implementation of ISystemRemoteElement for the given
     * object.  Returns null if this object does not adaptable to this.
     */
    protected ISystemRemoteElementAdapter getRemoteAdapter(Object o) 
    {
    	return SystemAdapterHelpers.getRemoteAdapter(o);
    }

    // -----------------------------------------------------------
    // CONFIGURATION METHODS...
    // -----------------------------------------------------------

    /**
     * An optimization for performance reasons that allows all inputs to be set in one call
     */
    public void setInputs(Shell shell, Viewer v, ISelection selection)
    {
		if (traceSelections)
		  issueTraceMessage(" INSIDE SETINPUTS IN BASE ACTION CLASS");
    	setShell(shell);
    	setViewer(v);
    	setSelection(selection);
    }

	/** 
	 * Sets the parent shell for this action. Usually context dependent.
	 */
	public void setShell(Shell shell)
	{
		// in defect 42399 it was reported the shell for persistent actions gets reset in browse
		//   dialogs, on a right click, overriding the real shell with the browse dialog's shell.
		//   When the browse dialog is closed, we only retain the disposed shell. To solve this
		//   we have to return a stack of shells and on getShell peel back to the last non-disposed
		//   one...
		this.previousShells.add(this.shell); 
		this.shell = shell;
		if (traceSelections)
		  issueTraceMessage(" INSIDE SETSHELL. shell = " + shell);
	}
	/**
	 * Set the Viewer that called this action. It is good practice for viewers to call this
	 *  so actions can directly access them if needed.
	 */
	public void setViewer(Viewer v)
	{
		this.previousViewers.add(this.viewer); // see comment in setShell
		this.viewer = v;
		if (traceSelections)
		  issueTraceMessage(" INSIDE SETVIEWER. viewer = " + viewer);
	}
	/**
	 * This is called when the user selects something in the tree.
	 * This is your opportunity to disable the action based on the current selection.
	 * The default implementation of this method:
	 * <ul>
	 *   <li>Disables the action if the selection is not a structured selection. Should never happen.
	 *   <li>Disables the action if more than one item is selected and allowOnMultipleSelection is false.
	 *   <li>Converts the selection to a structured selection and calls updateSelection. Uses returned
	 *         boolean value to setEnabled() this action.
	 * </ul>
	 */
	public void selectionChanged(SelectionChangedEvent event) 
	{	
		  ISelection selection = event.getSelection();	
		  if (traceSelections)
		    issueTraceMessage("INSIDE SELECTIONCHANGED. Selection null? " + (selection==null));
		  setSelection(selection);			    
	}
	/**
	 * This is called by the UI calling the action, if that UI is not a selection provider.
	 * That is, this is an alternative to calling selectionChanged when there is no SelectionChangedEvent.
	 * @see #selectionChanged(SelectionChangedEvent event)
	 */
	public void setSelection(ISelection selection) 
	{	
		if (traceSelections)
		  issueTraceMessage(" INSIDE SETSELECTION. Selection null? " + (selection==null));
		if ( !(selection instanceof IStructuredSelection) )
		{
		  if (selectionSensitive)
		    setEnabled(false);
		  if (traceSelections)
		  System.out.println(this.getClass().getName() + ". Returning false in setSelection. selection= " + selection);
		  return;
		}
        if (selectionSensitive)
        {
        	// see comment in setShell
        	//this.previousSelections.add(this.sSelection);
        }
		sSelection = (IStructuredSelection)selection;
		if (!selectionSensitive || (selection == null))
		{
		  if (traceSelections)
		  System.out.println(this.getClass().getName() + ". Returning. selectionSensitive = " + selectionSensitive);
		  return;
		}
		boolean multiSelect = (sSelection.size() > 1);
		if (!allowOnMultipleSelection && multiSelect)
		{
          setEnabled(false);
		  if (traceSelections)
		  System.out.println(this.getClass().getName() + ". Returning false in setSelection. #selected = " + sSelection.size());
		}
        else
        {
		  boolean enable = false;
		  /*
		  boolean debug = getText().equals("Copy");		  
		  if (debug)
		    enable = updateSelection(sSelection);
		  else */
		    enable = updateSelection(sSelection);
		  setEnabled(enable);
        }
	}	
	/**
	 * Identify the UI object that will be used to get the selection
	 *  list from. <b>Only call this if your action is displayed in a toolbar
	 *  or non-popup menu<b>, as it will impact performance. It results in your
	 *  action getting called every time the user changes his selection in
	 *  the given provider viewer.
	 */
	public void setSelectionProvider(ISelectionProvider provider) 
	{
		if (fSelectionProvider != null)
	      fSelectionProvider.removeSelectionChangedListener(this);
			
		fSelectionProvider = provider;
		if (traceSelections)
		  issueTraceMessage(" INSIDE SETSELECTIONPROVIDER. fSelectionProvider = " + fSelectionProvider);

		
		if (fSelectionProvider != null)
		  fSelectionProvider.addSelectionChangedListener(this);
	}
    
	
    // ---------------------------------------------------------------------------
    // CONFIGURATION METHODS CHILD CLASSES OR OTHERS CALL TO CONFIGURE THIS ACTION
    // ---------------------------------------------------------------------------
	/**
	 * Set the help id for the action
	 */
	public void setHelp(String id)
	{
		SystemWidgetHelpers.setHelp(this, id);
		this.helpId = id;
	}	

	/**
	 * Set the context menu group this action is to go into, for popup menus. If not set,
	 *  someone else will make this decision.
	 */
	public void setContextMenuGroup(String group)
	{
		contextMenuGroup = group;
	}		
	/**
	 * This method is supplied for actions that are to be enable even when more than
	 * one item is selected. The default is to only enable on single selections.
	 */
	public void allowOnMultipleSelection(boolean allow)
	{
		allowOnMultipleSelection = allow;
	}
	/**
	 * Specify whether this action is selection-sensitive. The default is true.
	 * This means the enabled state is tested and set when the selection is set.
	 */
	public void setSelectionSensitive(boolean sensitive)
	{
		selectionSensitive = sensitive;
	}

    // ---------------------------------------------------------------------------
    // METHODS THAT CAN OR SHOULD BE OVERRIDDEN BY CHILD CLASSES...
    // ---------------------------------------------------------------------------

	/**
	 * First opportunity to decide if the action should be enabled or not based on the
	 * current selection. Called by default implementation of selectionChanged, which
	 * converts the ISelection to an IStructuredSelection, which is all we support. The
	 * return result is used to enable or disable this action.
	 * <p>
	 * The default implementation of this method:
	 * <ul>
	 *   <li>Returns false if calling checkObjectType on any object in the selection list returns false.
	 *   <li>Returns true otherwise.
	 * </ul>
	 * If desired, override this method for a different algorithm to decide enablement.
	 */
	public boolean updateSelection(IStructuredSelection selection)
	{
		boolean enable = true;
		Iterator e= ((IStructuredSelection) selection).iterator();
		while (enable && e.hasNext())
		{
		     enable = checkObjectType(e.next());		  
		}
		return enable;
	}
	
	/**
	 * Second and easiest opportunity to decide if the action should be enabled or not based
	 * on the current selection. Called by default implementation of updateSelection, once for
	 * each item in the selection. If any call to this returns false, the action is disabled.
	 * The default implementation returns true.
	 */
	public boolean checkObjectType(Object selectedObject)
	{
		return true;
	}
		
	     
	/**
	 * This is the method called when the user selects this action.
	 * Child classes need to override this. If you need the parent shell,
	 * call getShell. If you need to know the current selection, call
	 * getSelection(), or getFirstSelection() followed by getNextSelection()
	 * until null is returned.
	 * @see Action#run()
	 */
	public void run() 
	{
		
	}


    // -----------------------------------------------------------
    // GETTER METHODS RETURNING INFORMATION CAPTURED IN BASE CLASS
    // -----------------------------------------------------------
    /**
     * Return if true if this is a dummy action
     */
    public boolean isDummy()
    {
    	String label = getText();
    	if (label == null)
    	  return false;
    	return label.equals("dummy");
    }
    
 	/**
	 * Retrieve the help id for this action
	 */
	public String getHelpContextId()
	{
		return helpId;
	}	

	/**
	 * Retrieves the parent shell for this action. Will be null if setShell has not been called.
	 */
    public Shell getShell()
    {
    	return internalGetShell(true);
    }
	/**
	 * Retrieves the parent shell for this action. Will be null if setShell has not been called.
	 * Method for subclasses that want to call this and not do the test for null.
	 */
    protected Shell getShell(boolean doTest)
    {
    	return internalGetShell(doTest);
    }
	/**
	 * Abstraction
	 */
    private Shell internalGetShell(boolean doTest)
    {
		// in defect 42399 it was reported the shell for persistent actions gets reset in browse
		//   dialogs, on a right click, overriding the real shell with the browse dialog's shell.
		//   When the browse dialog is closed, we only retain the disposed shell. To solve this
		//   we have to return a stack of shells and on getShell peel back to the last non-disposed
		//   one...
        if ((shell!=null) && (shell.isDisposed()))
        {        	
        	boolean found = false;
        	Vector disposedShells = new Vector();
        	for (int idx=previousShells.size()-1; !found && (idx>=0); idx--)
        	{
        		shell = (Shell)previousShells.elementAt(idx);
                if (shell.isDisposed())
                  disposedShells.add(shell);
                else
                  found = true;
        	}
        	if (!found)
        	  shell = null;
        	for (int idx=0; idx<disposedShells.size(); idx++)
        	   previousShells.remove(disposedShells.elementAt(idx));
        }
        if (doTest && (shell == null))
        {
        	System.out.println("Inside getShell for " + this.getClass().getName() + " and the shell is null! This needs to be investigated");
        	SystemBasePlugin.logDebugMessage("SystemBaseAction", "Inside getShell for " + this.getClass().getName() + " and the shell is null! This needs to be investigated");        	
        }
    	return shell;
    }
	/**
	 * Get the Viewer that called this action. Not guaranteed to be set,
	 *  depends if that viewer called setViewer or not. SystemView does.
	 */
	public Viewer getViewer()
	{
		//   see comment in getShell()...
        if ((viewer!=null) && (viewer.getControl().isDisposed()))
        {        	
        	boolean found = false;
        	Vector disposedViewers = new Vector();
        	for (int idx=disposedViewers.size()-1; !found && (idx>=0); idx--)
        	{
        		viewer = (Viewer)previousViewers.elementAt(idx);
                if (viewer.getControl().isDisposed())
                  disposedViewers.add(viewer);
                else
                  found = true;
        	}
        	if (!found)
        	  viewer = null;
        	for (int idx=0; idx<disposedViewers.size(); idx++)
        	   previousViewers.remove(disposedViewers.elementAt(idx));
        }
		return viewer;
	}
	/**
	 * Return the current viewer as an ISystemTree if it is one, or null otherwise
	 */
	protected ISystemTree getCurrentTreeView()
	{
		  Viewer v = getViewer();
		  if (v instanceof ISystemTree)
            return (ISystemTree)v;
          else
            return null;
	}

	/**
	 * Get the context menu group this action is to go into, for popup menus. By default is
	 *  null, meaning there is no recommendation
	 */
	public String getContextMenuGroup()
	{
		return contextMenuGroup;
	}

	/**
	 * Return whether this action is selection-sensitive. The default is true.
	 * This means the enabled state is tested and set when the selection is set.
	 */
	public boolean isSelectionSensitive()
	{
		return selectionSensitive;
	}

	/**
	 * Return value of last call to getSelectionProvider.
	 */
	public ISelectionProvider getSelectionProvider()
	{
		return fSelectionProvider;
	}
	
	/**
	 * Return current selection, as per last call to selectionChanged.
	 */
	public IStructuredSelection getSelection()
	{
		if (traceSelections)
		  issueTraceMessage(" INSIDE GETSELECTION. sSelection null? " + (sSelection==null));
		return sSelection;
	}	
	
	/**
	 * Return first item currently selected, as per last call to selectionChanged.
	 * This is handy for actions where allowOnMultipleSelection is false.
	 * This method also starts a new iterator, and after you can call getNextSelection()
	 * until it returns null. This is handy for actions where allowOnMultipleSelection is true.
	 * @see #getNextSelection()
	 */
	protected Object getFirstSelection()
	{
		if (sSelection != null)
		{
	      selectionIterator = sSelection.iterator();
	      if (selectionIterator.hasNext())
	        return selectionIterator.next();
	      else
	        return null;
		}
		else
		  selectionIterator = null;		
		return null;
	}	
	/**
	 * Return next item currently selection, as per last call to selectionChanged.
	 * YOU MUST HAVE CALLED GETFIRSTSELECTION() PRIOR TO CALLING THIS. 
	 * Your code can loop until this returns null.
	 * @see #getFirstSelection()
	 */
	protected Object getNextSelection()
	{
		if ((sSelection != null) && (selectionIterator != null))
		{
	      if (selectionIterator.hasNext())
	        return selectionIterator.next();
	      else
	        return null;
		}
		else
		  selectionIterator = null;		
		return null;
	}		

	/**
	 * Disables this action if offline mode, otherwise delegates to the super class.
	 * 
	 * @see org.eclipse.jface.action.IAction#isEnabled()
	 */
	public boolean isEnabled() 
	{
		if (!isAvailableOffline)
		{
			if (conn != null && conn.isOffline())
			{
				return false;
			}
			else
			{
				Object selection = getFirstSelection();
				while (selection != null)
				{
					if (selection instanceof ISubSystem && ((ISubSystem)selection).isOffline())
					{
						return false;
					}
				
					// yantzi:  this disables all actions on SystemConnection
					//else if (selection instanceof SystemConnection)
					//{
					//	connection = (SystemConnection)selection;
					//	if (connection.isOffline())
					//		return false;
					//}
					
					selection = getNextSelection();
				}
			}
 
		}

		return super.isEnabled();
	}

	/**
	 * If this is one of those rare actions that is to be made available in offline mode, then call this
	 * during or immediately after constructing to state that. The default is false.
	 */
	public void setAvailableOffline(boolean availableOffline)
	{
		this.isAvailableOffline = availableOffline;
	}

	/**
	 * Set the SystemConnection used by this action for disabling the action
	 * in offline mode, or for whatever reason subclasses may need it.
	 */
	public void setHost(IHost connection)
	{
		conn = connection;
	}
	
	/**
	 * Return the system connection which is the parent of whatever is currently selected.
	 */
	public IHost getSystemConnection()
	{
		return conn;
	}

}
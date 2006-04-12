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

package org.eclipse.rse.ui.widgets;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.ui.SystemBaseForm;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.view.SystemPropertySheetForm;
import org.eclipse.rse.ui.view.SystemViewConnectionSelectionInputProvider;
import org.eclipse.rse.ui.view.SystemViewForm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;



/**
 * A reusable form for prompting for a connection. Unlike {@link org.eclipse.rse.ui.widgets.SystemHostCombo},
 * this form uses a list box to show the available connections. 
 * <p>
 * This form may be used to populate a dialog or a wizard page.
 * <p>
 * To configure the functionality, call these methods:
 * <ul>
 *   <li>{@link #setShowNewConnectionPrompt(boolean)}
 *   <li>{@link #setDefaultConnection(IHost)}
 *   <li>{@link #setSystemTypes(String[])} 
 *   <li>{@link #setShowPropertySheet(boolean)}
 *   <li>{@link #enableAddMode(com.ibm.etools.systems.files.ui.ISystemAddFileListener)}
 *   <li>{@link #setMultipleSelectionMode(boolean)}
 * </ul>
 * <p>
 * To configure the text on the dialog, call these methods:
 * <ul>
 *   <li>{@link #setMessage(String)}
 * </ul>
 * <p>
 * After running, call these methods to get the output:
 * <ul>
 *   <li>{@link #getSelectedConnection()}
 * </ul>
 */
public class SystemSelectConnectionForm extends SystemBaseForm
 	   implements ISelectionChangedListener
{	
	protected static final int PROMPT_WIDTH = 200; // The maximum width of the dialog's prompt, in pixels.
	
	// GUI widgets
    protected Label                   verbageLabel, spacer1, spacer2;
	protected Text                    nameEntryValue;
	protected SystemViewForm          tree;
    protected SystemPropertySheetForm ps;
	//protected ISystemMessageLine      msgLine;	
	protected Composite               outerParent, ps_composite;	
	// inputs
	protected String           verbage = null;
	protected String[]         systemTypes = null;
	protected IHost defaultConn;
	protected boolean          allowNew = true;
	protected boolean          multipleSelectionMode;
	protected boolean          showPropertySheet = false;
	protected Vector           listeners = new Vector();				

	// outputs 
	protected IHost[] outputConnections = null;	
	protected IHost   outputConnection = null;
	// state
	//protected ResourceBundle rb;
	protected boolean      initDone;
	protected boolean      contentsCreated;
	
	//protected String  errorMessage;
	//protected Object  caller;
	//protected boolean callerInstanceOfWizardPage, callerInstanceOfSystemPromptDialog;	
	protected int     autoExpandDepth = 0;

	protected Object previousSelection = null;

   /**
	 * Constructor
	 * @param shell The shell hosting this form
	 * @param msgLine A GUI widget capable of writing error messages to.
	 * 
	 * @see #setShowNewConnectionPrompt(boolean)
	 * @see #setSystemTypes(String[])
	 */
	public SystemSelectConnectionForm(Shell shell, ISystemMessageLine msgLine) 
	{
		super(shell, msgLine);
		//this.caller = caller;
		//callerInstanceOfWizardPage = (caller instanceof WizardPage);
		//callerInstanceOfSystemPromptDialog = (caller instanceof SystemPromptDialog);				

		// set default GUI
		verbage = SystemResources.RESID_SELECTCONNECTION_VERBAGE;        
	}

    // ---------------------------------
    // INPUT OR CONFIGURATION METHODS...
    // ---------------------------------
    /**
     * Set the connection to default the selection to
     */
    public void setDefaultConnection(IHost conn)
    {
        defaultConn = conn;
    }

    /**
     * Set to true if we are to allow users to create a new connection. Default is true.
     */
    public void setShowNewConnectionPrompt(boolean show)
    {
    	allowNew = show;    	
    }
    /**
     * Restrict to certain system types
     * @param systemTypes the system types to restrict what connections are shown and what types of connections
     *  the user can create
     * @see org.eclipse.rse.core.IRSESystemType
     */
    public void setSystemTypes(String[] systemTypes)
    {
    	this.systemTypes = systemTypes;
    }
	/**
	 * Restrict to one system type
	 * @param systemType the system type to restrict what connections are shown and what types of connections
	 *  the user can create
	 * @see org.eclipse.rse.core.IRSESystemType
	 */
	public void setSystemType(String systemType)
	{
		systemTypes = new String[1];
		systemTypes[0] = systemType; 
	}
    /**
     * Set the message shown as the text at the top of the form. Default is "Select a connection"
     */
    public void setMessage(String message)
    {
    	this.verbage = message;
    	if (verbageLabel != null)
    	  verbageLabel.setText(message);
    }
    /**
     * Show the property sheet on the right hand side, to show the properties of the
     * selected connection.
     * <p>
     * Default is false
     */
    public void setShowPropertySheet(boolean show)
    {
    	this.showPropertySheet = show;
    }
 
    /**
     * Set multiple selection mode. Default is single selection mode
     * <p>
     * If you turn on multiple selection mode, you must use the getSelectedConnections()
     *  method to retrieve the list of selected connections.
     * 
     * @see #getSelectedConnections()
     */
    public void setMultipleSelectionMode(boolean multiple)
    {
    	this.multipleSelectionMode = multiple;
    }
    
    /**
     * Add a listener to selection change events in the list
     */
    public void addSelectionChangedListener(ISelectionChangedListener l)
    {
    	if (tree != null)
    	  tree.addSelectionChangedListener(l);
    	else
    	  listeners.addElement(l);
    }
    /**
     * Remove a listener for selection change events in the list
     */
    public void removeSelectionChangedListener(ISelectionChangedListener l)
    {
    	if (tree != null)
    	  tree.removeSelectionChangedListener(l);
    	else
    	  listeners.removeElement(l);
    }
        

    // ---------------------------------
    // OUTPUT METHODS...
    // ---------------------------------
    /**
     * Return all selected connections. 
     * @see #setMultipleSelectionMode(boolean)
     */	
    public IHost[] getSelectedConnections()
    {
    	return outputConnections;
    }
    /**
     * Return selected connection
     */	
    public IHost getSelectedConnection()
    {
    	return outputConnection;
    }

    /**
     * Return the multiple selection mode current setting
     */
    public boolean getMultipleSelectionMode()
    {
    	return multipleSelectionMode;
    }

    // -----------------------------------------------------
    // SEMI-PRIVATE METHODS USED BY CALLING DIALOG/WIZARD...
    // -----------------------------------------------------

	/**
	 * Return control to recieve initial focus
	 */
	public Control getInitialFocusControl()
	{
		return tree.getTreeControl();
	}	
	
	/**
	 * Show or hide the property sheet. This is called after the contents are created when the user
	 *  toggles the Details button.
	 * @param shell Use getShell() in your dialog or wizard page
	 * @param contents Use getContents() in your dialog or wizard page
	 * @return new state -> true if showing, false if hiding
	 */
	public boolean toggleShowPropertySheet(Shell shell, Control contents) 
	{
	    Point windowSize = shell.getSize();
	    Point oldSize = contents.computeSize(SWT.DEFAULT, SWT.DEFAULT);

		if (showPropertySheet) // hiding?
		{
          	ps.dispose();
          	spacer1.dispose();
          	spacer2.dispose();
          	ps_composite.dispose();
          	ps = null; spacer1 = spacer2 = null; ps_composite = null;
          	((GridLayout)outerParent.getLayout()).numColumns = 1;
		}
		else // showing?
		{
		  	//createPropertySheet((Composite)contents, shell);
          	((GridLayout)outerParent.getLayout()).numColumns = 2;
		  	createPropertySheet(outerParent, shell);
		}

	    Point newSize = contents.computeSize(SWT.DEFAULT, SWT.DEFAULT);
	    shell.setSize(new Point(windowSize.x + (newSize.x - oldSize.x), windowSize.y));
	    
		if (ps != null)
		{
		  	ISelection s = tree.getSelection();
		  	if (s != null)
		    	ps.selectionChanged(s);		  
		}
	    
		showPropertySheet = !showPropertySheet;
		return showPropertySheet;
	}
	
	/**
	 * Create the property sheet viewer
	 */
	private void createPropertySheet(Composite outerParent, Shell shell)
	{
		ps_composite = SystemWidgetHelpers.createFlushComposite(outerParent, 1);	
		((GridData)ps_composite.getLayoutData()).grabExcessVerticalSpace = true;
		((GridData)ps_composite.getLayoutData()).verticalAlignment = GridData.FILL;

        // SPACER LINES
        spacer1 = SystemWidgetHelpers.createLabel(ps_composite, "", 1);
        spacer2 = SystemWidgetHelpers.createLabel(ps_composite, "", 1);
        // PROPERTY SHEET VIEWER
        ps = new SystemPropertySheetForm(shell, ps_composite, SWT.BORDER, getMessageLine());			
	}
		
	public void dispose()
	{
		if (tree != null)
		{
			tree.removeSelectionChangedListener(this);	
			for (int i = 0; i < listeners.size(); i++)
			{
				tree.removeSelectionChangedListener((ISelectionChangedListener)listeners.get(i));	
			}
		}	
	}	
	/**
	 * In this method, we populate the given SWT container with widgets and return the container
	 *  to the caller. 
	 * @param parent The parent composite
	 */
	public Control createContents(Composite parent)
	{
		contentsCreated = true;
		
		outerParent = parent;		
		// OUTER COMPOSITE
		//if (showPropertySheet)
        {
        	outerParent = SystemWidgetHelpers.createComposite(parent, showPropertySheet ? 2 : 1);        	
        }

		// INNER COMPOSITE
		int gridColumns = 2;
		Composite composite_prompts = SystemWidgetHelpers.createFlushComposite(outerParent, gridColumns);	

        // PROPERTY SHEET COMPOSITE
		if (showPropertySheet)
		{
			createPropertySheet(outerParent, getShell());
		}
		else
		{
            //((GridLayout)composite_prompts.getLayout()).margin...
		}

        // MESSAGE/VERBAGE TEXT AT TOP
        verbageLabel = (Label) SystemWidgetHelpers.createVerbage(composite_prompts, verbage, gridColumns, false, PROMPT_WIDTH);
        //verbageLabel = SystemWidgetHelpers.createLabel(composite_prompts, verbage, gridColumns);

        // SPACER LINE
        SystemWidgetHelpers.createLabel(composite_prompts, "", gridColumns);
       
        // SELECT OBJECT READONLY TEXT FIELD
        Composite nameComposite = composite_prompts;
        int       nameSpan = gridColumns;
	    nameEntryValue = SystemWidgetHelpers.createReadonlyTextField(nameComposite);
		((GridData)nameEntryValue.getLayoutData()).horizontalSpan = nameSpan;

		// TREE
		SystemViewConnectionSelectionInputProvider inputProvider = new SystemViewConnectionSelectionInputProvider();
		inputProvider.setShowNewConnectionPrompt(allowNew);
		inputProvider.setSystemTypes(systemTypes);        
		tree = new SystemViewForm(getShell(), composite_prompts, SWT.NULL, inputProvider, !multipleSelectionMode, getMessageLine(), gridColumns, 1);
		((GridData)tree.getLayoutData()).widthHint = PROMPT_WIDTH; // normally its 300

        // initialize fields
	    if (!initDone)
	      doInitializeFields();		  		

	    // add selection listeners
		tree.addSelectionChangedListener(this);				  
		if (listeners.size() > 0)
		  for (int idx=0; idx<listeners.size(); idx++)
		     tree.addSelectionChangedListener((ISelectionChangedListener)listeners.elementAt(idx));

		// pre-select default connection...
		if (defaultConn != null)
		{
		  	tree.select(defaultConn, true);
		}		  
        
		return composite_prompts;
	}

	/**
	 * Completes processing of the wizard page or dialog. If this 
	 * method returns true, the wizard/dialog will close; 
	 * otherwise, it will stay active.
	 *
	 * @return true if no errors
	 */
	public boolean verify() 
	{
		getMessageLine().clearErrorMessage();    		
        outputConnection = internalGetConnection();
		return true;
	}


    // -----------------------------------------------------
    // PRIVATE METHODS USED BY US...
    // -----------------------------------------------------
    
    /**
     * Return the current connection
     */
    private IHost internalGetConnection()
    {
    	Object o = tree.getSystemView().getRootParent();
    	if (o instanceof IHost)
    		return ((IHost)o);
    	else
    		return null;
    }
    
    protected void setNameText(String text)
    {
    	nameEntryValue.setText(text);
    }
    private void doInitializeFields()
	{
		  //setPageComplete();
		  initDone = true;
		  return; 
	}
	
    // ---------------------------------------------------
	// METHODS FOR SELECTION CHANGED LISTENER INTERFACE... 
	// ---------------------------------------------------
	/**
	 * User selected something in the tree.
	 */
	public void selectionChanged(SelectionChangedEvent e)
	{
		ISelection selection = e.getSelection();			
		if (ps != null)
		  ps.selectionChanged(selection);
		  				
		outputConnections = null;

		Object[] outputObjects = getSelections(selection);
		if ((outputObjects!=null) && (outputObjects.length>0) && (outputObjects[0] instanceof IHost))
		{
			outputConnections = new IHost[outputObjects.length];
			for (int idx=0; idx<outputConnections.length; idx++)
				outputConnections[idx] = (IHost)outputObjects[idx];
		}
		
		if ((outputConnections != null) && (outputConnections.length>0))
		{
			setNameText(outputConnections[0].getAliasName());
			setPageComplete(true);
		}
		else
		{		
			setNameText("");
			setPageComplete(false);
		}
	}

	/**
	 * Return first item currently selected.
	 */
	protected Object getFirstSelection(ISelection selection)
	{
		IStructuredSelection sSelection = (IStructuredSelection)selection;
		if (sSelection != null)
		{
	      Iterator selectionIterator = sSelection.iterator();
	      if (selectionIterator.hasNext())
	        return selectionIterator.next();
	      else
	        return null;
		}		
		return null;
	}	
	/**
	 * Return all items currently selected.
	 */
	protected Object[] getSelections(ISelection selection)
	{
		IStructuredSelection sSelection = (IStructuredSelection)selection;
		if (sSelection != null)
		{
		  Object[] selectedObjects = new Object[sSelection.size()]; 
	      Iterator selectionIterator = sSelection.iterator();
	      int idx = 0;
	      while (selectionIterator.hasNext())
	      	selectedObjects[idx++] = selectionIterator.next();
	      return selectedObjects;
		}		
		return null;
	}	
	
	
}
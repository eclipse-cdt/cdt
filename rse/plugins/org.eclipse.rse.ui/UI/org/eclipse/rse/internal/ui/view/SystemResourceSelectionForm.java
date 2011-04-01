/********************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others. All rights reserved.
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
 * Kevin Doyle (IBM) - Added getSystemViewForm()
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber (Wind River) - [190442] made SystemActionViewerFilter API
 * Martin Oberhuber (Wind River) - [202866] Fix exceptions in RSE browse dialog when SystemRegistry is not yet fully initialized
 * David McKnight   (IBM)        - [225506] [api][breaking] RSE UI leaks non-API types
 * David Dykstal (IBM) - [232130] meaningless label provided for filter references
 * David McKnight   (IBM)        - [244430] [regression] Incorrect behaviour for SystemSelectRemoteFolderAction
 * David McKnight   (IBM)        - [267061] resource dialog/form to allow custom host combo label
 * David McKnight   (IBM)        - [339282] SystemResourceSelectionForm.selectionChanged() should check for null subsystem
 * David McKnight   (IBM)        - [341616] SystemResourceSelectionForm needs to check whether selected is an IAdaptable
 ********************************************************************************/

package org.eclipse.rse.internal.ui.view;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.SystemActionViewerFilter;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.dialogs.SystemPromptDialog;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.validators.IValidatorRemoteSelection;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.ISystemResourceSelectionInputProvider;
import org.eclipse.rse.ui.view.ISystemTree;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.rse.ui.view.SystemAdapterHelpers;
import org.eclipse.rse.ui.widgets.SystemHostCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class SystemResourceSelectionForm implements ISelectionChangedListener
{	
	private Shell _shell;
	private boolean _multipleSelection = true;
	protected static final int PROMPT_WIDTH = 400; // The maximum width of the dialog's prompt, in pixels.

	private ISystemResourceSelectionInputProvider _inputProvider;
	private SystemHostCombo _connectionCombo;
	private SystemViewForm _systemViewForm;
	private Composite _propertySheetContainer;
    private SystemPropertySheetForm _ps;
	
	private Text _pathText;
	private boolean _isValid;
	private ISystemMessageLine _msgLine;
	protected Object previousSelection = null;
	private IValidatorRemoteSelection _selectionValidator = null;
	private boolean  showPropertySheet = false;

	
	protected Object  caller;
	protected boolean callerInstanceOfWizardPage, callerInstanceOfSystemPromptDialog;	

	protected String    _verbiage = null;
	protected Label     verbiageLabel;
	private Composite _container;
	
	// history
	private HashMap _history;
	
	// outputs 
	protected IHost outputConnection = null;
	protected Object[] outputObjects = null;	
	
	
	public SystemResourceSelectionForm(Shell shell, Composite parent, Object caller,
			ISystemResourceSelectionInputProvider inputProvider, 
			String verbiage,
			boolean multipleSelection, 
			ISystemMessageLine msgLine)
	{
		_msgLine= msgLine;
		_history = new HashMap();
		_inputProvider = inputProvider;
		_multipleSelection = multipleSelection;
		_shell = shell;
		_verbiage = verbiage;
		this.caller = caller;
		callerInstanceOfWizardPage = (caller instanceof WizardPage);
		callerInstanceOfSystemPromptDialog = (caller instanceof SystemPromptDialog);
	        
		createControls(parent);
	}	
	
	public void setMessageLine(ISystemMessageLine msgLine)
	{
		_msgLine = msgLine;
	}
	
	   /**
     * Return first selected object
     */	
    public Object getSelectedObject()
    {
    	if ((outputObjects != null) && (outputObjects.length>=1))
    	  return outputObjects[0];
    	else
    	  return null;
    }
    /**
     * Return all selected objects. 
     */	
    public Object[] getSelectedObjects()
    {
    	return outputObjects;
    }
    
    /**
     * Return the embedded System Tree object.
     * Will be null until createControls is called.
     */
    public ISystemTree getSystemTree()
    {
    	return _systemViewForm.getSystemTree();
    }
    
	public void createControls(Composite parent)
	{
    	_container = SystemWidgetHelpers.createComposite(parent, showPropertySheet ? 2 : 1);  
		//Composite container = new Composite(parent, SWT.NULL);


		// INNER COMPOSITE
		int gridColumns = 2;
		Composite composite_prompts = SystemWidgetHelpers.createFlushComposite(_container, gridColumns);	

        // PROPERTY SHEET COMPOSITE
		if (showPropertySheet)
		{
			createPropertySheet(_container, _shell);
		}


        // MESSAGE/VERBIAGE TEXT AT TOP
        verbiageLabel = SystemWidgetHelpers.createVerbiage(composite_prompts, _verbiage, gridColumns, false, PROMPT_WIDTH);
  
    	
		boolean allowMultipleConnnections = _inputProvider.allowMultipleConnections();
		if (!allowMultipleConnnections)
		{
			//Label connectionLabel = SystemWidgetHelpers.createLabel(composite_prompts, _inputProvider.getSystemConnection().getHostName());
		}
		else
		{
			IRSESystemType[] systemTypes = _inputProvider.getSystemTypes();
			String category = _inputProvider.getCategory();
		
			if (systemTypes != null)
			{
				_connectionCombo = new SystemHostCombo(composite_prompts, SWT.NULL, systemTypes, _inputProvider.getSystemConnection(), _inputProvider.allowNewConnection());	
			}
			else if (category != null)
			{
				_connectionCombo = new SystemHostCombo(composite_prompts, SWT.NULL, _inputProvider.getSystemConnection(), _inputProvider.allowNewConnection(), category);	
			}
			else
			{
				_connectionCombo = new SystemHostCombo(composite_prompts, SWT.NULL, 
						SystemWidgetHelpers.getValidSystemTypes(null),
						_inputProvider.getSystemConnection(),
						_inputProvider.allowNewConnection());	
				
			}
			_connectionCombo.addSelectionListener(new SelectionAdapter() 
					{
				   public void widgetSelected(SelectionEvent evt) 
				   {
					   IHost connection = _connectionCombo.getHost();
					   connectionChanged(connection);
				   }}
					);	
			_connectionCombo.listenToConnectionEvents(true);
		}
		
		_pathText = SystemWidgetHelpers.createReadonlyTextField(composite_prompts);
		_systemViewForm = new SystemViewForm(_shell, composite_prompts, SWT.NULL, _inputProvider, !_multipleSelection, _msgLine);
		_systemViewForm.addSelectionChangedListener(this);		
		
			
		GridLayout layout = new GridLayout();
		GridData gdata = new GridData(GridData.FILL_BOTH);
		composite_prompts.setLayout(layout);
		composite_prompts.setLayoutData(gdata);
			
		doInitializeFields();
	}
	
	private void doInitializeFields()
	{
		  setPageComplete();
		  return; 
	}
	
	/**
	 * Create the property sheet viewer
	 */
	private void createPropertySheet(Composite outerParent, Shell shell)
	{
		_propertySheetContainer = SystemWidgetHelpers.createFlushComposite(outerParent, 1);	
		((GridData)_propertySheetContainer.getLayoutData()).grabExcessVerticalSpace = true;
		((GridData)_propertySheetContainer.getLayoutData()).verticalAlignment = GridData.FILL;

        // PROPERTY SHEET VIEWER
        _ps = new SystemPropertySheetForm(shell,_propertySheetContainer, SWT.BORDER, _msgLine);			
	}
	
	public Control getInitialFocusControl()
	{
		return _systemViewForm.getTreeControl();
	}
	
	public void applyViewerFilter(SystemActionViewerFilter filter)
	{
		if (filter != null)
		{
			_systemViewForm.getSystemTree().addFilter(filter);
		}
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
		if (_isValid)
		{
			if (_msgLine != null)
			{
				_msgLine.clearErrorMessage();    		
			}
			return true;
		}
		else
		{
			return false;
		}
	}
	
	protected ISystemViewElementAdapter getViewAdapter(Object selection)
	{
		if (selection != null && selection instanceof IAdaptable)
		{
			return (ISystemViewElementAdapter)((IAdaptable)selection).getAdapter(ISystemViewElementAdapter.class);
		}
		return null;
	}
	
	protected ISystemRemoteElementAdapter getRemoteAdapter(Object selection)
	{
		if (selection != null && selection instanceof IAdaptable)
		{
			return SystemAdapterHelpers.getRemoteAdapter(selection);
		}
		return null;
	}
	
	protected ISystemRemoteElementAdapter[] getRemoteAdapters(ISelection selection)
	{
		Object[] selectedObjects = getSelections(selection);
		ISystemRemoteElementAdapter[] adapters = new ISystemRemoteElementAdapter[selectedObjects.length];
		for (int idx=0; idx<adapters.length; idx++)
		{
			adapters[idx] = getRemoteAdapter(selectedObjects[idx]);
		}
		return adapters;
	}
	
	
	public void connectionChanged(IHost connection)
	{
		IHost previousConnection = _inputProvider.getSystemConnection();
		 if (previousConnection != connection)
		   {				 	
			
			   _inputProvider.setSystemConnection(connection, false);
			   		
			   _systemViewForm.refresh();
			   Object oldSelection = _history.get(connection);	
			   if (oldSelection != null)
			   {
				   setPreSelection(oldSelection);
			   }
		   }
	}

	public void setVerbiage(String verbiage)
	{
		_verbiage = verbiage;
	}
	
	public boolean setPreSelection(Object selection)
	{
		ISystemViewElementAdapter adapter = getViewAdapter(selection);
		if (adapter != null)
		{
			Object parent = adapter.getParent(selection);
			ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
			ISubSystem ss = adapter.getSubSystem(selection);
			IHost connection = ss.getHost();
			if (_inputProvider.allowMultipleConnections())
			{
				if (_connectionCombo.getHost()!= connection || _inputProvider.getSystemConnection() != connection)
		 		{
		 			_connectionCombo.select(connection);
		 		}
			}
			List filterRefs = registry.findFilterReferencesFor(selection, ss, false);
			
			ISystemTree systemTree = _systemViewForm.getSystemTree();
			if (filterRefs.size() > 0)
			{
				ISystemFilterReference ref = (ISystemFilterReference)filterRefs.get(0);
				systemTree.expandTo(ref, selection);

				return true;
			}
			else
			{
				if (setPreSelection(parent))
				{
					systemTree.expandTo(parent, selection);
					return true;
				}				
			}
		}
		return false;
	}

    
    protected void setPathText(String text)
    {
    	_pathText.setText(text);
    }
  
    
    public Object[] getOutputObjects()
    {
    	return outputObjects;
    }
    
    /**
     * Return selected connection
     */	
    public IHost getSelectedConnection()
    {
    	return outputConnection;
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
	
    
	private void setPathTextFromSelection(Object selection)
	{
		String text = ""; //$NON-NLS-1$
		ISystemViewElementAdapter adapter = getViewAdapter(selection);
		if (selection instanceof ISystemFilterReference) {
			text = adapter.getText(selection);
		} else {
			text = adapter.getAbsoluteName(selection);
		}
		setPathText(text);
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
          _ps.dispose();

          _propertySheetContainer.dispose();
         _ps = null;
         _propertySheetContainer = null;
          ((GridLayout)_container.getLayout()).numColumns = 1;
		}
		else // showing?
		{
		  //createPropertySheet((Composite)contents, shell);
          ((GridLayout)_container.getLayout()).numColumns = 2;
		  createPropertySheet(_container, shell);
		}

	    Point newSize = contents.computeSize(SWT.DEFAULT, SWT.DEFAULT);
	    shell.setSize(new Point(windowSize.x + (newSize.x - oldSize.x), windowSize.y));
	    
		if (_ps != null)
		{
		  ISelection s = _systemViewForm.getSelection();
		  if (s != null)
		    _ps.selectionChanged(s);		  
		}
	    
		showPropertySheet = !showPropertySheet;
		return showPropertySheet;
	}
	
	
//  ---------------------------------------------------
	// METHODS FOR SELECTION CHANGED LISTENER INTERFACE... 
	// ---------------------------------------------------
	/**
	 * User selected something in the _systemViewForm.
	 */
	public void selectionChanged(SelectionChangedEvent e)
	{
		_isValid = true;
		ISelection selection = e.getSelection();
		outputObjects = null;
		int selectionSize = ((IStructuredSelection)selection).size();
		if ((selectionSize > 1) && !_systemViewForm.sameParent())
		{
			clearErrorMessage();
			
			setPathText(""); //$NON-NLS-1$
			setPageComplete();
		    return; // don't enable OK/Add if selections from different parents
		}
		
		if (_ps != null)
			  _ps.selectionChanged(selection);
 
		Object selectedObject = getFirstSelection(selection);
		if (selectedObject == previousSelection && selectionSize == 1)
		{
			// DKM we null set this before, so we need to reset it
			outputObjects = getSelections(selection);
			return;	
		}
		clearErrorMessage();
		setPathText(""); //$NON-NLS-1$
		setPageComplete();

		previousSelection = selectedObject;  
		if (selectedObject != null)
		{

		  ISystemRemoteElementAdapter remoteAdapter = getRemoteAdapter(selectedObject);
		  if (remoteAdapter != null)
		  {
			setPathTextFromSelection(selectedObject);
			
			outputObjects = getSelections(selection);			
			outputConnection = remoteAdapter.getSubSystem(selectedObject).getHost();
			
		 	_history.put(outputConnection, previousSelection);
		  }
		  else if (selectedObject instanceof IAdaptable) 
		  {
			  ISystemViewElementAdapter elementAdapter = (ISystemViewElementAdapter)((IAdaptable)selectedObject).getAdapter(ISystemViewElementAdapter.class);
			  if (elementAdapter != null)
			  {
					setPathTextFromSelection(selectedObject);
					
					outputObjects = getSelections(selection);			
					ISubSystem ss = elementAdapter.getSubSystem(selectedObject);
					if (ss != null){
						outputConnection = ss.getHost();				
						_history.put(outputConnection, previousSelection);
					}
			  }
		  }
		  
		  
		  if (_selectionValidator != null) 
		  {
			  SystemMessage selectionMsg  = _selectionValidator.isValid(outputConnection, getSelections(selection), getRemoteAdapters(selection));

		  	  if (selectionMsg != null)
		  	  {
		  	  	_isValid = false;
		  	    setErrorMessage(selectionMsg);
		  	  }
		  }
		  setPageComplete();
		}
		
	}
	
	/**
	 * This method can be called by the dialog or wizard page host, to decide whether to enable
	 * or disable the next, final or ok buttons. It returns true if the minimal information is
	 * available and is correct.
	 */
	public boolean isPageComplete()
	{
		return ( (_pathText.getText().length() > 0) ) && _isValid;
	}
	
	/**
	 * Inform caller of page-complete status of this form
	 */
	public void setPageComplete()
	{
		if (callerInstanceOfWizardPage)
		{
		  ((WizardPage)caller).setPageComplete(isPageComplete());
		}
		else if (callerInstanceOfSystemPromptDialog)
		{
		  ((SystemPromptDialog)caller).setPageComplete(isPageComplete());
		}		
	}
  
    /**
     * Show the property sheet on the right hand side, to show the properties of the
     * selected object.
     * <p>
     * Default is false
     */
    public void setShowPropertySheet(boolean show)
    {
    	this.showPropertySheet = show;
    }
 
   
	
    /**
     * Specify a validator to use when the user selects a remote file or folder.
     * This allows you to decide if OK should be enabled or not for that remote file or folder.
     */
    public void setSelectionValidator(IValidatorRemoteSelection selectionValidator)
    {
    	_selectionValidator = selectionValidator;
    }

    protected void clearErrorMessage()
    {
    	if (_msgLine != null)
    	  _msgLine.clearErrorMessage();
    }
    protected void setErrorMessage(String msg)
    {
    	if (_msgLine != null)
    	  if (msg != null)
    	    _msgLine.setErrorMessage(msg);
    	  else
    	    _msgLine.clearErrorMessage();
    }
    protected void setErrorMessage(SystemMessage msg)
    {
    	if (_msgLine != null)
    	  if (msg != null)
    	    _msgLine.setErrorMessage(msg);
    	  else
    	    _msgLine.clearErrorMessage();
    }
    

    /**
     * Set the message shown as the text at the top of the form. Eg, "Select a file"
     */
    public void setMessage(String message)
    {
    	this._verbiage = message;
    	if (verbiageLabel != null)
    	  verbiageLabel.setText(message);
    }
    /**
     * Set the tooltip text for the remote systems tree from which an item is selected.
     */
    public void setSelectionTreeToolTipText(String tip)
    {
    	_systemViewForm.setToolTipText(tip);
    }

    
	/**
	 * Sets the label for the connection combo in the form
	 */
    public void setConnectionComboLabel(String label)
    {
    	if (_connectionCombo != null){
    		_connectionCombo.setLabel(label);
    	}
    }

}
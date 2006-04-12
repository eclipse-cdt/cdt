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

package org.eclipse.rse.ui.filters.dialogs;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolManager;
import org.eclipse.rse.filters.ISystemFilterPoolReferenceManager;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.ui.ISystemDeleteTarget;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.ISystemRenameTarget;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.actions.ISystemAction;
import org.eclipse.rse.ui.actions.SystemCommonDeleteAction;
import org.eclipse.rse.ui.actions.SystemCommonRenameAction;
import org.eclipse.rse.ui.dialogs.SystemPromptDialog;
import org.eclipse.rse.ui.dialogs.SystemSimpleContentElement;
import org.eclipse.rse.ui.dialogs.SystemSimpleContentProvider;
import org.eclipse.rse.ui.filters.SystemFilterPoolDialogInterface;
import org.eclipse.rse.ui.filters.SystemFilterPoolDialogOutputs;
import org.eclipse.rse.ui.filters.SystemFilterPoolManagerUIProvider;
import org.eclipse.rse.ui.filters.SystemFilterUIHelpers;
import org.eclipse.rse.ui.filters.SystemFilterWorkWithFilterPoolsTreeViewer;
import org.eclipse.rse.ui.filters.actions.SystemFilterAbstractFilterPoolAction;
import org.eclipse.rse.ui.filters.actions.SystemFilterCopyFilterPoolAction;
import org.eclipse.rse.ui.filters.actions.SystemFilterMoveFilterPoolAction;
import org.eclipse.rse.ui.filters.actions.SystemFilterNewFilterPoolAction;
import org.eclipse.rse.ui.filters.actions.SystemFilterWorkWithFilterPoolsRefreshAllAction;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.validators.ValidatorFilterPoolName;
import org.eclipse.rse.ui.view.ISystemPropertyConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Tree;

/**
 * Dialog for working with filter pools.
 */
public class SystemFilterWorkWithFilterPoolsDialog 
       extends SystemPromptDialog 
       implements ISystemMessages, ISystemPropertyConstants,
                  ISelectionChangedListener, 
                  ISystemDeleteTarget, ISystemRenameTarget,
                  SystemFilterPoolDialogInterface
                  //,ISystemResourceChangeListener
{
	
	private String promptString;
	private Label prompt;
	private SystemFilterWorkWithFilterPoolsTreeViewer tree;
	private ToolBar  toolbar = null;
	private ToolBarManager toolbarMgr = null;
	private SystemSimpleContentProvider provider = new SystemSimpleContentProvider();
    private SystemSimpleContentElement filterPoolContent;
	private SystemSimpleContentElement preSelectedRoot = null;	
	private ISystemFilterPoolManager[] filterPoolManagers;
	private SystemFilterPoolManagerUIProvider caller = null;
	private boolean initializing = false;
	
    //private ActionContributionItem newActionItem, deleteActionItem, renameActionItem;
    private SystemFilterWorkWithFilterPoolsRefreshAllAction refreshAction = null;
    private SystemFilterNewFilterPoolAction  newAction = null;
    //private SystemSimpleDeleteAction         dltAction = null;
    private SystemCommonDeleteAction         dltAction = null;    
    //private SystemSimpleRenameAction         rnmAction = null;
    private SystemCommonRenameAction         rnmAction = null;
    private SystemFilterCopyFilterPoolAction cpyAction = null;
    private SystemFilterMoveFilterPoolAction movAction = null;
    private IAction[]                        contextMenuActions = null;
	
		
	/**
	 * Constructor
	 */
	public SystemFilterWorkWithFilterPoolsDialog(Shell shell, String title, String prompt,
	                                             SystemFilterPoolManagerUIProvider caller)
	                                             //SystemFilterPoolManager[] filterPoolManagers,
	                                             //SystemSimpleContentElement filterPoolContent)
	{
		super(shell, title);				
		this.caller = caller;
		promptString = prompt;		
		this.filterPoolContent = caller.getTreeModel();
		this.filterPoolManagers = caller.getFilterPoolManagers();
        this.preSelectedRoot = caller.getTreeModelPreSelection(filterPoolContent);		
        setCancelButtonLabel(SystemResources.BUTTON_CLOSE);
        setShowOkButton(false);
		//pack();
	}	

	/**
	 * Set the root to preselect
	 */
	public void setRootToPreselect(SystemSimpleContentElement preSelectedRoot)
	{
		this.preSelectedRoot = preSelectedRoot;
	}
	
	/**
	 * Create message line. Intercept so we can set msg line of form.
	 */
	protected ISystemMessageLine createMessageLine(Composite c)
	{
		ISystemMessageLine msgLine = super.createMessageLine(c);
		return fMessageLine;
	}

	/**
	 * @see SystemPromptDialog#getInitialFocusControl()
	 */
	protected Control getInitialFocusControl() 
	{
		return tree.getControl();
	}
	
	/**
	 * Set the pool name validator for the rename action.
	 * The work-with dialog automatically calls setExistingNamesList on it for each selection.
	 */
	public void setFilterPoolNameValidator(ValidatorFilterPoolName pnv)
	{
	}

	/**
	 * @see SystemPromptDialog#createInner(Composite)
	 */
	protected Control createInner(Composite parent) 
	{
		//System.out.println("INSIDE CREATEINNER");    			
		/*
  	    // top level composite
		Composite composite = new Composite(parent,SWT.NONE);        
		composite.setLayout(new GridLayout());
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
	    data.grabExcessHorizontalSpace = true;        
		composite.setLayoutData(data);
		*/
		           
		// Inner composite
		Composite composite_prompts = SystemWidgetHelpers.createComposite(parent, 1);

        // PROMPT
		prompt = SystemWidgetHelpers.createLabel(composite_prompts, promptString);

        // TOOLBAR
        createToolBar(composite_prompts);
                
        // WORK-WITH TREE
        initializing = true;
		tree = new SystemFilterWorkWithFilterPoolsTreeViewer(getShell(), this, new Tree(composite_prompts, SWT.SINGLE | SWT.BORDER));        
	    GridData treeData = new GridData();
	    treeData.horizontalAlignment = GridData.FILL;
	    treeData.grabExcessHorizontalSpace = true;
	    treeData.widthHint = 300;        
	    treeData.heightHint= 300;
	    treeData.verticalAlignment = GridData.CENTER;
	    treeData.grabExcessVerticalSpace = true;
	    tree.getTree().setLayoutData(treeData);  	  	    
	    
	    tree.setContentProvider(provider);
	    tree.setLabelProvider(provider);
			
        // populate tree        
 		if (filterPoolContent != null)
 		{
 		  filterPoolContent.setData(tree); // so actions can refresh our tree
 		  tree.setInput(filterPoolContent);
 		}

		if (preSelectedRoot != null)
		  tree.setSelection(new StructuredSelection(preSelectedRoot), true);

		// expand and pre-check
		tree.expandAll();
	    tree.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);

	    // add selection listener to tree
		tree.addSelectionChangedListener(this);

		// populate toolbar
        populateToolBar(getShell(), tree);
        
        initializing = false;		
        			    			
		return composite_prompts;
	}
	
	/**
	 * Callback from tree when refresh is done
	 */
	public boolean refreshTree()
	{
		if (initializing)
		  return false;
		this.filterPoolContent = caller.getTreeModel();
		this.filterPoolManagers = caller.getFilterPoolManagers();
        this.preSelectedRoot = caller.getTreeModelPreSelection(filterPoolContent);
 		filterPoolContent.setData(tree); // so actions can refresh our tree        
        tree.setInput(filterPoolContent); // hmm, hope we don't go into a loop!        
        //System.out.println("in refreshTree");
        return true;
	}

    /**
     * Create the toolbar displayed at the top of the dialog
     */
	protected void createToolBar(Composite parent)
	{
	    toolbar = new ToolBar(parent, SWT.FLAT | SWT.WRAP);		
	    toolbarMgr = new ToolBarManager(toolbar);
	}
	/**
	 * Populate the toolbar displayed at the top of the dialog
	 */
	protected void populateToolBar(Shell shell, SystemFilterWorkWithFilterPoolsTreeViewer tree)
	{
		newAction = new SystemFilterNewFilterPoolAction(shell,this);
		//dltAction = new SystemSimpleDeleteAction(shell,this);	
		dltAction = new SystemCommonDeleteAction(shell,this);			
		rnmAction = new SystemCommonRenameAction(shell,this);		
		  // undo typical settings...
		  rnmAction.allowOnMultipleSelection(false);
          rnmAction.setProcessAllSelections(false);
		//rnmAction = new SystemSimpleRenameAction(shell,this);		
		  //poolNameValidator = new ValidatorFilterPoolName((Vector)null);
		  //rnmAction.setNameValidator(poolNameValidator);		
    	cpyAction = new SystemFilterCopyFilterPoolAction(shell);
    	  cpyAction.setSelectionProvider(this);
    	movAction = new SystemFilterMoveFilterPoolAction(shell);
    	  movAction.setSelectionProvider(this);
    	refreshAction = new SystemFilterWorkWithFilterPoolsRefreshAllAction(tree, shell);
    	
        contextMenuActions = new IAction[6];
        contextMenuActions[0] = newAction;
        contextMenuActions[1] = rnmAction;
        contextMenuActions[2] = cpyAction;
        contextMenuActions[3] = movAction;
        contextMenuActions[4] = dltAction;                        
        contextMenuActions[5] = refreshAction;        
        
        for (int idx=0; idx<contextMenuActions.length; idx++)
        {
           ((ISystemAction)contextMenuActions[idx]).setSelection(tree.getSelection());
           ((ISystemAction)contextMenuActions[idx]).setViewer(tree);
        }
         
        // populate toolbar...            	    	
		toolbarMgr.add(refreshAction);	
		toolbarMgr.add(newAction);	
		toolbarMgr.add(dltAction);
		toolbarMgr.add(rnmAction);
		toolbarMgr.add(cpyAction);
		toolbarMgr.add(movAction);
		toolbarMgr.update(false);		
		
		// populate tree..
        tree.setContextMenuActions(contextMenuActions);
		
	}
	
	/**
	 * ISelectionChangedListener method. Called when user changes selection in tree
	 */
	public void selectionChanged(SelectionChangedEvent event) 
	{
		IStructuredSelection sel = (IStructuredSelection)event.getSelection();
		SystemSimpleContentElement element = (SystemSimpleContentElement)sel.getFirstElement();		
		if (rnmAction != null)
		  rnmAction.selectionChanged(event);
	}	
	 
	/**
	 * Return current selection
	 */
	public SystemSimpleContentElement getSelectedElement()
	{
		IStructuredSelection sel = (IStructuredSelection)tree.getSelection();				
		return (SystemSimpleContentElement)sel.getFirstElement();
	}	 
	/**
	 * Return true if something selected
	 */
	public boolean isSelectionEmpty()
	{
		IStructuredSelection sel = (IStructuredSelection)tree.getSelection();				
		return sel.isEmpty();
	}	 
    public void clearSelection()
    {
    	tree.setSelection((ISelection)null);
    }
	
	    
	/**
	 * Called when user presses OK button. 
	 * Return true to close dialog.
	 * Return false to not close dialog.
	 */
	protected boolean processOK() 
	{
		boolean closeDialog = verify();
		if (closeDialog)
		{
		  setOutputObject(getInputObject());
		}
		return closeDialog;
	}	
	
	public boolean close()
	{
		//RSEUIPlugin.getDefault().getSystemRegistry().removeSystemResourceChangeListener(this);		
		return super.close();
	}
	/**
	 * Verifies all input.
	 * @return true if there are no errors in the user input
	 */
	public boolean verify() 
	{
		String errMsg = null;
		Control controlInError = null;
		clearErrorMessage();				

		if (errMsg != null)
		  controlInError.setFocus();
		return (errMsg == null);
	}
	
	/**
	 * This method can be called by the dialog or wizard page host, to decide whether to enable
	 * or disable the next, final or ok buttons. It returns true if the minimal information is
	 * available and is correct.
	 */
	public boolean isPageComplete()
	{
		boolean pageComplete = true;
		return pageComplete;
	}
	
	/**
	 * Inform caller of page-complete status of this form
	 */
	public void setPageComplete()
	{
		setPageComplete(isPageComplete());
	}
	
    // ------------------------------
    // ISYSTEMDELETETARGET METHODS...
    // ------------------------------
    
    /**
     * Return true if delete should even be shown in the popup menu
     */
    public boolean showDelete()
    {
    	return true;
    }
    /**
     * Return true if delete should be enabled based on your current selection.
     */
    public boolean canDelete()
    {
    	SystemSimpleContentElement element = getSelectedElement();
    	if (element == null)
    	  return false;
    	Object elementData = element.getData();
    	//System.out.println("In SFWWFPsDlg.canDelete: element data class = " + elementData.getClass().getName());
    	if ((elementData == null) || !(elementData instanceof ISystemFilterPool))
    	  return false;
    	ISystemFilterPool pool = (ISystemFilterPool)elementData;
    	if (pool == null)
    	  return false;
    	else
    	  return (pool.isDeletable() && element.isDeletable());    	     	
    }
    /**
     * Actually do the delete of currently selected items.
     */
    public boolean doDelete(IProgressMonitor monitor)
    {
    	boolean ok = false;
    	SystemSimpleContentElement element = getSelectedElement();
    	Object elementData = element.getData();
    	if ((elementData == null) || !(elementData instanceof ISystemFilterPool))
    	  return ok;
    	ISystemFilterPool pool = (ISystemFilterPool)elementData;
    	ISystemFilterPoolManager mgr = pool.getSystemFilterPoolManager();
        try
        {          
          mgr.deleteSystemFilterPool(pool);
          ok = true;
          SystemSimpleContentElement parent = element.getParent();
   	      clearSelection();
    	  tree.setSelection(new StructuredSelection(parent),true);          
          parent.deleteChild(element);   	  
   	      tree.refresh(parent);             
        } catch (Exception exc) 
        {
			//SystemMessage.displayExceptionMessage(getShell(),RSEUIPlugin.getResourceBundle(),
			//                                      ISystemMessages.MSG_EXCEPTION_DELETING,exc,
			//                                      pool.getName()); 
    	    SystemMessageDialog msgDlg = new SystemMessageDialog(getShell(), 
    	            RSEUIPlugin.getPluginMessage(MSG_EXCEPTION_DELETING).makeSubstitution(pool.getName(),exc));
    	    msgDlg.open(); 
    	    //RSEUIPlugin.logError("Error deleting filter pool in workwith dialog",exc);
        }
        return ok;
    }

    // ------------------------------
    // ISYSTEMRENAMETARGET METHODS...
    // ------------------------------
    
    /**
     * Return true if rename should even be shown in the popup menu
     */
    public boolean showRename()
    {
    	return true;
    }
    /**
     * Return true if rename should be enabled based on your current selection.
     */
    public boolean canRename()
    {
    	SystemSimpleContentElement element = getSelectedElement();
    	if (element == null)
    	  return false;
    	Object elementData = element.getData();
    	//System.out.println("In SFWWFPsDlg.canRename: element data class = " + elementData.getClass().getName());
    	if ((elementData == null) || !(elementData instanceof ISystemFilterPool))
    	  return false;
    	ISystemFilterPool pool = (ISystemFilterPool)elementData;
    	if (pool == null)
    	  return false;
    	else
    	{
    	  boolean renamable = (!pool.isNonRenamable() && element.isRenamable());
    	  if (renamable)
    	  {
            //poolNameValidator.setExistingNamesList(pool.getSystemFilterPoolManager().getSystemFilterPoolNamesVector());    	  	
    	  }
    	  return renamable;    	     	
    	}
    }
    /**
     * Actually do the rename of currently selected items.
     * The array of new names matches the currently selected items.
     */
    public boolean doRename(String[] newNames)
    {
    	boolean ok = false;
    	SystemSimpleContentElement element = getSelectedElement();
    	Object elementData = element.getData();
    	if ((elementData == null) || !(elementData instanceof ISystemFilterPool))
    	  return ok;    	
    	ISystemFilterPool pool = (ISystemFilterPool)elementData;
    	ISystemFilterPoolManager mgr = pool.getSystemFilterPoolManager();
        try
        {          
          mgr.renameSystemFilterPool(pool, newNames[0]);
          ok = true;
          element.setName(newNames[0]);
	      String properties[] = {IBasicPropertyConstants.P_TEXT};
	      tree.update(element, properties); // for refreshing non-structural properties in viewer when model changes   	      
        } 
        catch (SystemMessageException exc)
        {
        	SystemMessageDialog.displayMessage(getShell(), exc);
        }
        catch (Exception exc) 
        {
			//SystemMessage.displayExceptionMessage(getShell(),RSEUIPlugin.getResourceBundle(),
			//                                      ISystemMessages.MSG_EXCEPTION_RENAMING,exc,
			//                                      pool.getName()); 
    	    SystemMessageDialog msgDlg = new SystemMessageDialog(getShell(), 
    	            RSEUIPlugin.getPluginMessage(MSG_EXCEPTION_RENAMING).makeSubstitution(pool.getName(),exc));
    	    msgDlg.open(); 
    	    //RSEUIPlugin.logError("Error renaming filter pool in workwith dialog",exc);
        }
    	return ok;
    }

    // -----------------------------
    // ISELECTIONPROVIDER METHODS...
    // -----------------------------
    /**
     * 
     */
    public void addSelectionChangedListener(ISelectionChangedListener listener) 
    {
    	tree.addSelectionChangedListener(listener);    	
    }
    
    public void removeSelectionChangedListener(ISelectionChangedListener listener) 
    {
    	tree.removeSelectionChangedListener(listener);
    }
    
    public void setSelection(ISelection selection) 
    {
    	tree.setSelection(selection);
    }
    
    public ISelection getSelection()
    {
    	return tree.getSelection();
    }
    
    
    /**
     * Callback from new action when new pool created
     */
    public void addNewFilterPool(Shell shell, ISystemFilterPool pool)
    {
    	String newPoolMgrName = pool.getSystemFilterPoolManager().getName();
    	SystemSimpleContentElement rootElement = filterPoolContent;
		SystemSimpleContentElement[] mgrElements = rootElement.getChildren();
		SystemSimpleContentElement mgrElement = null;
		for (int idx=0; (mgrElement==null) && (idx<mgrElements.length); idx++)
		{
			 String mgrName = mgrElements[idx].getName();
			 if (mgrName.equals(newPoolMgrName))
			   mgrElement = mgrElements[idx];
		}
        SystemSimpleContentElement cElement = 
             new SystemSimpleContentElement(pool.getName(), pool, mgrElement, (Vector)null);
        cElement.setImageDescriptor(SystemFilterUIHelpers.getFilterPoolImage(pool.getProvider(),pool));
    	mgrElement.addChild(cElement, 0);    	    	
    	tree.refresh(mgrElement); // rebuild whole thing    	
   	    tree.setSelection(new StructuredSelection(cElement),true);              	    	
   	    // defect 42503
   	    Object inputObj = getInputObject();
   	    if (inputObj instanceof ISubSystem)
   	    {
   	    	ISubSystem ss = (ISubSystem)inputObj;
   	        SystemMessage msg = RSEUIPlugin.getPluginMessage(MSG_FILTERPOOL_CREATED);
   	        msg.makeSubstitution("'"+pool.getName()+"'", "'"+ss.getName()+"'");
   	        if (shell.isDisposed() || !shell.isVisible())
   	          shell = getShell();
   	        SystemMessageDialog msgdlg = new SystemMessageDialog(shell, msg);
   	        boolean yes = msgdlg.openQuestionNoException();
   	        if (yes)
   	        {
          	   ISystemFilterPoolReferenceManager sfprm = ss.getSystemFilterPoolReferenceManager();
          	   sfprm.addReferenceToSystemFilterPool(pool);   	        	
   	        }
   	    }
    }
    
    
    /**
     * Callback from new action to get array of managers
     */
    public ISystemFilterPoolManager[] getFilterPoolManagers()
    {
        ISystemFilterPoolManager[] mgrs = filterPoolManagers;        
        return mgrs;    	
    }
    /**
     * Callback from new action to get index of initial manager to select
     */
    public int getFilterPoolManagerSelection()
    {
        int selection = 0;
    	SystemSimpleContentElement element = getSelectedElement();
    	Object elementData = element.getData();
    	if (elementData != null)
    	{
    	  if (elementData instanceof ISystemFilterPoolManager)
    	    selection = getManagerIndex((ISystemFilterPoolManager)elementData);
    	  else if (elementData instanceof ISystemFilterPool)
    	    selection = getManagerIndex(((ISystemFilterPool)elementData).getSystemFilterPoolManager());
    	}
    	//System.out.println("In getFilterPoolManagerSelection(). Returning "+selection);
        return selection;    	
    }
    
    private int getManagerIndex(ISystemFilterPoolManager mgr)
    {
    	int pos = -1;
    	ISystemFilterPoolManager[] mgrs = filterPoolManagers;
    	for (int idx=0; (pos==-1)&&(idx<mgrs.length); idx++)
    	{
    	   if (mgr == mgrs[idx])
    	     pos = idx;
    	}
    	if (pos == -1)
    	  pos = 0;
    	return pos;
    }

    // SystemFilterPoolDialogInterface methods...
	/**
	 * Allow base action to pass instance of itself for callback to get info
	 */
    public void setFilterPoolDialogActionCaller(SystemFilterAbstractFilterPoolAction caller)
    {
    }
    /**
     * Return an object containing user-specified information pertinent to filter pool actions
     */
    public SystemFilterPoolDialogOutputs getFilterPoolDialogOutputs()
    {
    	SystemFilterPoolDialogOutputs output = new SystemFilterPoolDialogOutputs();
    	output.filterPoolTreeRoot = filterPoolContent;
    	return output;
    }
    
    /**
     * Set the help context id for this wizard
     */
    public void setHelpContextId(String id)
    {
    	super.setHelp(id);
    }
    
    
}
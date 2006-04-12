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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.actions.ISystemCopyTargetSelectionCallback;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

/**
 * Dialog for selecting a target location on a copy operation.
 */
public class SystemSimpleCopyDialog 
       extends SystemPromptDialog 
       implements ISystemMessages, //ISystemPropertyConstants,
                  ISelectionChangedListener
{
	private String promptString;
	private Label prompt;
	private TreeViewer tree;
	private SystemSimpleContentProvider provider = new SystemSimpleContentProvider();
    private SystemSimpleContentElement copyTreeContent, initialSelection;
	private ISystemCopyTargetSelectionCallback caller = null;
	public static final int MODE_COPY = 0;
	public static final int MODE_MOVE = 1;
	private Object targetContainer = null;
		
	/**
	 * Constructor 
	 */
	public SystemSimpleCopyDialog(Shell shell, String prompt, int mode, ISystemCopyTargetSelectionCallback caller,
	                              SystemSimpleContentElement copyTreeContent, SystemSimpleContentElement selection)
	{
		//super(shell, title);				
		super(shell, (mode==MODE_COPY ? SystemResources.RESID_COPY_TITLE : SystemResources.RESID_MOVE_TITLE));				
		this.caller = caller;
		if (prompt == null)
		  if (mode == MODE_COPY)
		    prompt = SystemResources.RESID_COPY_PROMPT;
		  else
		    prompt = SystemResources.RESID_MOVE_PROMPT;
		promptString = prompt;		
		this.copyTreeContent = copyTreeContent;
		this.initialSelection = selection;	
        //setCancelButtonLabel(RSEUIPlugin.getString(BUTTON_CLOSE));
		//pack();
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
	 * @see SystemPromptDialog#createInner(Composite)
	 */
	protected Control createInner(Composite parent) 
	{
		// Inner composite
		int nbrColumns = 1;
		Composite composite_prompts = SystemWidgetHelpers.createComposite(parent, nbrColumns);

        // PROMPT
		prompt = SystemWidgetHelpers.createLabel(composite_prompts, promptString);
        
        // WORK-WITH TREE
		tree = new TreeViewer(new Tree(composite_prompts, SWT.SINGLE | SWT.BORDER));        
	    GridData treeData = new GridData();
	    treeData.horizontalAlignment = GridData.FILL;
	    treeData.grabExcessHorizontalSpace = true;
	    treeData.widthHint = 300;        
	    treeData.heightHint= 200;
	    treeData.verticalAlignment = GridData.CENTER;
	    treeData.grabExcessVerticalSpace = true;
	    tree.getTree().setLayoutData(treeData);  	  	    
	    
	    tree.setContentProvider(provider);
	    tree.setLabelProvider(provider);
			
        // populate tree        
 		if (copyTreeContent != null)
 		  tree.setInput(copyTreeContent);

		// expand and pre-check
		if (initialSelection != null)
		  tree.setSelection(new StructuredSelection(initialSelection),true);		

        // preset the OK button
        setPageComplete();
        
	    // add selection listener to tree
		tree.addSelectionChangedListener(this);


        //ActionContributionItem[] actionItems = createActionContributionItems();
        //Composite buttons = createButtonBar(composite_prompts, actionItems);

		//RSEUIPlugin.getDefault().getSystemRegistry().addSystemResourceChangeListener(this);
                			    			
		return composite_prompts;
	}
	
	/**
	 * ISelectionChangedListener method. Called when user changes selection in tree
	 */
	public void selectionChanged(SelectionChangedEvent event) 
	{
		IStructuredSelection sel = (IStructuredSelection)event.getSelection();		
		SystemSimpleContentElement element = (SystemSimpleContentElement)sel.getFirstElement();		
		element.setSelected(true);
		setPageComplete();
	}	
	 
	/**
	 * Return current selection
	 */
	public SystemSimpleContentElement getSelectedElement()
	{
		IStructuredSelection sel = (IStructuredSelection)tree.getSelection();				
		if ((sel == null) || sel.isEmpty())
		  return null;
		else
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
		  SystemSimpleContentElement seldObj = getSelectedElement();
		  if (seldObj != null)
		  {
            targetContainer = seldObj.getData();
		    setOutputObject(targetContainer);		  
		  }
		  else
		  {
		    closeDialog = false;
		    setPageComplete(false);
		  }
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
		boolean pageComplete = !isSelectionEmpty();
		if (pageComplete)
		{
			pageComplete = caller.isValidTargetParent(getSelectedElement());
		}
		return pageComplete;
	}
	
	/**
	 * Inform caller of page-complete status of this form
	 */
	public void setPageComplete()
	{
		setPageComplete(isPageComplete());
	}
	
    /**
     * Callback from new action to get index of initial manager to select
     *
    public int getFilterPoolManagerSelection()
    {
        int selection = 0;
    	SystemSimpleContentElement element = getSelectedElement();
    	Object elementData = element.getData();
    	if (elementData != null)
    	{
    	  if (elementData instanceof SystemFilterPoolManager)
    	    selection = getManagerIndex((SystemFilterPoolManager)elementData);
    	  else if (elementData instanceof SystemFilterPool)
    	    selection = getManagerIndex(((SystemFilterPool)elementData).getSystemFilterPoolManager());
    	}
    	//System.out.println("In getFilterPoolManagerSelection(). Returning "+selection);
        return selection;    	
    }
    */
    
    // -----------------
    // OUTPUT METHODS...
    // -----------------
    /**
     * Get the selected target container
     */
    public Object getTargetContainer()
    {
    	return targetContainer;
    }
    
}
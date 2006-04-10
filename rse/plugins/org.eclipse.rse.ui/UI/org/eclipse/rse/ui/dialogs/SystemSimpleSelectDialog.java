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
import java.util.Vector;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.view.ISystemPropertyConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

/**
 * Dialog that uses a checkbox tree viewer to prompt users to select hierarchical items.
 * Works in concert with {@link org.eclipse.rse.ui.dialogs.SystemSimpleContentElement}
 * and {@link org.eclipse.rse.ui.dialogs.SystemSimpleContentProvider}.
 * <p>
 * The {@link #setInputObject} method is used to populate the selection tree:
 * <ul>
 *   <li>The passed object must be of type SystemSimpleContentElement
 *   <li>The method getChildren will be called on that object to get initial visible elements
 *   <li>As user expands each item, getChildren() is progressively called
 *   <li>The initial selection state of each item is determined by calling isSelected on that item
 * </ul>
 * <p>
 * The trick to using this is to first populate a hierarchy of SystemSimpleContentElement elements,
 * each one wrapping one of your own model objects, and then passing to this constructor the root
 * element.
 * <p>
 * Upon successful completion of this dialog (wasCancelled() returns false), the model is 
 * updated to reflect the selections. Call getUpdatedContent() to return the root node, if need be,
 * and then walk the nodes. The selected items are those that return true 
 * to {@link org.eclipse.rse.ui.dialogs.SystemSimpleContentElement#isSelected()}.
 * 
 * @see org.eclipse.rse.ui.dialogs.SystemSimpleContentElement
 * @see org.eclipse.rse.ui.dialogs.SystemSimpleContentProvider
 */
public class SystemSimpleSelectDialog extends SystemPromptDialog 
                                implements ISystemPropertyConstants,
                                            ICheckStateListener
{
	private String promptString;
	private Label prompt;
	private CheckboxTreeViewer tree;
	private SystemSimpleContentProvider provider = new SystemSimpleContentProvider();
	private SystemSimpleContentElement preSelectedRoot = null;
    private boolean initialized = false;
    	
	/**
	 * Constructor
	 */
	public SystemSimpleSelectDialog(Shell shell, String title, String prompt)
	{
		super(shell, title);				
		promptString = prompt;		
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
		//form.setMessageLine(msgLine);
		return fMessageLine;
	}

	/**
	 * @see SystemPromptDialog#getInitialFocusControl()
	 */
	protected Control getInitialFocusControl() 
	{
		//checkNewTreeElements(provider.getElements(getInputObject()));		
		//select the first element in the list
		//Object[] elements = (provider.getElements(getInputObject());
		//Object primary= elements.length > 0 ? elements[0] : null;
		//if (primary != null) 
		//  tree.setSelection(new StructuredSelection(primary));
		  
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
        
        // CHECKBOX SELECT TREE	
		tree = new CheckboxTreeViewer(new Tree(composite_prompts, SWT.CHECK | SWT.BORDER));        
	    GridData treeData = new GridData();
	    treeData.horizontalAlignment = GridData.FILL;
	    treeData.grabExcessHorizontalSpace = true;
	    treeData.widthHint = 300;        
	    treeData.heightHint= 300;
	    treeData.verticalAlignment = GridData.FILL;
	    treeData.grabExcessVerticalSpace = true;
	    tree.getTree().setLayoutData(treeData);  	  	    
	    
	    tree.setContentProvider(provider);
	    tree.setLabelProvider(provider);
			
        // populate tree
		Object inputObject = getInputObject();			
 		if (inputObject != null)
	      initializeInput((SystemSimpleContentElement)inputObject);

		// expand and pre-check
		tree.expandAll();
	    tree.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);

		if (preSelectedRoot != null)
		  tree.reveal(preSelectedRoot);		  

	    // add selection listener to tree
		tree.addCheckStateListener(this);
			    			
		return composite_prompts;
	}
	
	/**
	 * ICheckStateChangedListener method. Called when user changes selection in tree
	 */
	public void checkStateChanged(CheckStateChangedEvent event) 
	{
		SystemSimpleContentElement element = (SystemSimpleContentElement)event.getElement();
		
		if (element.isReadOnly())
		{
			tree.setChecked(element, element.isSelected());
			return;
		} 
		
		boolean checked = event.getChecked();
		element.setSelected(checked);

		SystemSimpleContentElement parent = element.getParent();
		if (parent != null)
		{
		  boolean gray = getShouldBeGrayed(parent);
		  boolean check= getShouldBeChecked(parent);
		  tree.setChecked(parent, check);
		  tree.setGrayed(parent, gray);
		  //System.out.println("...setting parent grayed, checked to " + gray + ", " + check);
		}
			          
        		
		// On check, check all children
		if (checked) 
		{
			tree.setSubtreeChecked(element, true);
			checkSubtreeModel(element, true);
			//System.out.println("...setting setSubtreeChecked true for " + element);
			return;
		}		
		// On uncheck & gray, remain check but ungray
		// and check all its children
		if (tree.getGrayed(element)) 
		{
			tree.setChecked(element, true);
			tree.setGrayed(element, false);
			tree.setSubtreeChecked(element, true);
			checkSubtreeModel(element, true);
			//System.out.println("...setting setChecked(true), setGrayed(false) for " + element);			
			//System.out.println("...setting setSubtreeChecked true for " + element);
			return;
		}
		// On uncheck & not gray, uncheck all its children
	    tree.setSubtreeChecked(element, false);		
	    checkSubtreeModel(element, false);
		//System.out.println("...setting setSubtreeChecked false for " + element);
	}	
	
	private void checkSubtreeModel(SystemSimpleContentElement parent, boolean check)
	{
		parent.setSelected(check);
		SystemSimpleContentElement[] childElements = parent.getChildren();
		if (childElements != null)
		{
		  for (int idx=0; idx<childElements.length; idx++)
		  {
		     //childElements[idx].setSelected(check);
		     checkSubtreeModel(childElements[idx],check);
		  }
	    }		
	}
	
	
	/**
	 * Override of parent. We only support a specific type of input, so we test for it and
	 *  throw it out if we don't get it. This is necessary to defeat some default action.
	 * Called by SystemDialogAction's default run() method after dialog instantiated.
	 */
	public void setInputObject(Object inputObject)
	{
		if (inputObject instanceof SystemSimpleContentElement)
		  setInputObject((SystemSimpleContentElement)inputObject);
		else
		  System.out.println("UNEXPECTED INPUT IN SYSTEMSELECTINPUTDIALOG: "+inputObject);
	}

	/**
	 * Override of parent. Must pass selected object onto the form for initializing fields.
	 * Called by SystemDialogAction's default run() method after dialog instantiated.
	 */
	public void setInputObject(SystemSimpleContentElement inputObject)
	{
		//System.out.println("INSIDE SETINPUTOBJECT: " + inputObject + ", "+inputObject.getClass().getName());
		super.setInputObject(inputObject);
		initializeInput(inputObject);
	}
	
	/**
	 * Set the tree input, initialize checked state
	 */
    private void initializeInput(SystemSimpleContentElement inputObject)
    {
		if ((tree != null) && !initialized)
		{
		  tree.setInput(inputObject);
		  SystemSimpleContentElement[] gray = getPreGrayedElements(inputObject,null);
		  SystemSimpleContentElement[] check= getPreSelectedElements(inputObject,null);
		  SystemSimpleContentElement[] disable= getReadOnlyElements(inputObject,null);
		  tree.setCheckedElements(check); 
		  tree.setGrayedElements(gray);	
		  if ((disable != null) && (disable.length>0))		  	
		  {		 
		  	for (int idx=0; idx<disable.length; idx++)
		  	{
				SystemSimpleContentElement currElement = (SystemSimpleContentElement)disable[idx];
				tree.setGrayed(currElement, true); // so it appears readonly
		  	}
		  }
		  	  		  
		  if (preSelectedRoot != null)
		    tree.setSelection(new StructuredSelection(preSelectedRoot), true);
		    
		  initialized = true;		  		  
		}    	
    }

	/**
	 * Determine, recursively, the tree elements pre-determined to be read-only
	 */
	private SystemSimpleContentElement[] getReadOnlyElements(SystemSimpleContentElement input, Vector oldV)
	{
		Vector v = (oldV==null) ? new Vector() : oldV;
		SystemSimpleContentElement[] children = (SystemSimpleContentElement[])provider.getElements(input);
		if (children != null)
		  for (int idx = 0; idx<children.length; idx++)
		  {
			 if (children[idx].isReadOnly())
			 {
			   	v.addElement(children[idx]);
			   	//System.out.println("Adding readOnly element: " + children[idx]);
			 }
			getReadOnlyElements(children[idx], v);
		  }
    	     
		if (oldV != null)
		  return null;
  
		SystemSimpleContentElement[] readonlyArray = new SystemSimpleContentElement[v.size()];
		for (int idx=0; idx<readonlyArray.length; idx++)
		readonlyArray[idx] = (SystemSimpleContentElement)v.elementAt(idx);    	
		return readonlyArray;
	}
        
    /**
     * Determine, recursively, the tree elements pre-determined for selection
     */
    private SystemSimpleContentElement[] getPreSelectedElements(SystemSimpleContentElement input, Vector oldV)
    {
    	Vector v = (oldV==null) ? new Vector() : oldV;
    	SystemSimpleContentElement[] children = (SystemSimpleContentElement[])provider.getElements(input);
    	if (children != null)
    	  for (int idx = 0; idx<children.length; idx++)
          {
    	     if (children[idx].isSelected())
    	     {
    	       v.addElement(children[idx]);
    	       //System.out.println("Adding checked element: " + children[idx]);
    	     }
    	     getPreSelectedElements(children[idx], v);
          }
    	     
        if (oldV != null)
          return null;
  
        SystemSimpleContentElement[] selected = new SystemSimpleContentElement[v.size()];
        for (int idx=0; idx<selected.length; idx++)
           selected[idx] = (SystemSimpleContentElement)v.elementAt(idx);    	
        return selected;
    }

    /**
     * 
     */
    private SystemSimpleContentElement[] getPreGrayedElements(SystemSimpleContentElement input, Vector oldV)
    {
    	Vector v = (oldV==null) ? new Vector() : oldV;
    	SystemSimpleContentElement[] children = (SystemSimpleContentElement[])provider.getElements(input);
    	boolean allSame = true;
    	boolean currState = false;
    	boolean hasGrayChildren = false;
    	if ((children != null) && (children.length>0))
    	{
    	  currState = children[0].isSelected();
    	  for (int idx = 0; idx<children.length; idx++)
          {
          	 if (allSame && (currState != children[idx].isSelected()))
          	   allSame = false;
    	     //if (children[idx].isSelected())
    	     //  v.addElement(children[idx]);
    	     int oldSize = v.size();
    	     getPreGrayedElements(children[idx], v); // recursively check children's children
    	     if (v.size() != oldSize) // add any new items?
    	       hasGrayChildren = true;    
          }
    	  if (!allSame || hasGrayChildren) // some children checked, others not checked? Or any child grayed out?
    	  {
            v.addElement(input); // gray out this parent
            input.setSelected(true); // select this parent
    	    //System.out.println("Adding grayed element: " + input);
    	  }
    	  else if (allSame && currState)
    	    input.setSelected(true); // select this parent
    	} // if no children, do not gray
    	     
        if (oldV != null)
          return null;
  
        SystemSimpleContentElement[] grayed = new SystemSimpleContentElement[v.size()];
        for (int idx=0; idx<grayed.length; idx++)
           grayed[idx] = (SystemSimpleContentElement)v.elementAt(idx);    	
        return grayed;
    }
    
    /**
     * Dynamically determine grayed state of parent element
     */
    public boolean getShouldBeGrayed(SystemSimpleContentElement parent)
    {
    	SystemSimpleContentElement[] children = parent.getChildren();
    	boolean gray = false;
    	 
    	if ((children == null) || (children.length == 0))
    	  return gray;
    	 
    	boolean allSame = true;
    	boolean currState = children[0].isSelected();
    	boolean hasGrayChildren = false;
     
    	for (int idx=0; idx<children.length; idx++)
    	{
           if (allSame && (children[idx].isSelected() != currState))
             allSame = false;  	 
           if (getShouldBeGrayed(children[idx]))
             hasGrayChildren = true;
    	} 
    	 
    	if (!allSame || hasGrayChildren) // some children checked, others not checked? Or any child grayed out?
    	  gray = true; 
    	
    	return gray;    	
    }
    
    /**
     * Dynamically determine checked state of parent element
     */
    public boolean getShouldBeChecked(SystemSimpleContentElement parent)
    {
        boolean checked = false;

    	SystemSimpleContentElement[] children = parent.getChildren();
    	boolean gray = false;
    	 
    	if ((children == null) || (children.length == 0))
    	  return gray;    	 

        boolean anyChecked = false;
        for (int idx=0; idx<children.length; idx++)
    	{
    	   if (children[idx].isSelected())
    	     anyChecked = true;
		else
		{
		}
    	} 
    	
    	if (anyChecked)
          checked = true;
             
        return checked;
    }
	
	/**
	 * Return updated input model
	 */
	public SystemSimpleContentElement getUpdatedContent()
	{
		return (SystemSimpleContentElement)super.getInputObject();
	}
	/**
	 * Called when user presses OK button. 
	 * Return true to close dialog.
	 * Return false to not close dialog.
	 */
	protected boolean processOK() 
	{
		//newNameString = newName.getText().trim();		
		boolean closeDialog = verify();
		if (closeDialog)
		{
		}
		return closeDialog;
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

}
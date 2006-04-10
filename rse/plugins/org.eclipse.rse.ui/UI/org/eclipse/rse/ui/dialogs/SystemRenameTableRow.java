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
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.validators.ValidatorUniqueString;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;



/**
 * Represents one row in the table in the SystemRenameDialog dialog.
 */
public class SystemRenameTableRow extends SystemDeleteTableRow
{
	private String newName;
	private SystemMessage errorMsg = null;	
	private ISystemValidator inputValidator = null;
	private ValidatorUniqueString inputUniqueNameValidator = null;
	private Object parent;
	private int nameLengthLimit = -1;
	
	/**
	 * Constructor
	 * @param element that is being displayed in the tree, and which the
	 *  user selected the rename action. This represents a single item.
	 */
    public SystemRenameTableRow(Object element, int rowNbr)
    {
    	super(element,rowNbr);
    	this.newName = getName();
    	this.inputValidator = getAdapter(element).getNameValidator(element);    	
		if ((inputValidator != null) && (inputValidator instanceof ISystemValidator))
		  nameLengthLimit = ((ISystemValidator)inputValidator).getMaximumNameLength();
    }
    
    /**
     * Set the new name for this element. Called by the CellModifier
     *  for the rename dialog.
     */
    public void setNewName(String name)
    {
    	//System.out.println("Inside setNewName: from " + newName + ", to " + name);
    	if (name != null)
    	{
    	  if (isRemote()) // defect 43173
    	    newName = trimTrailing(name); // defect 43041
    	  else
    	    newName = name.trim();
    	}
    	else
    	  newName = null;
    }
    
    /**
     * Trim leading blanks
     */
    public static String trimTrailing(String text)
    {
    	return ("."+text).trim().substring(1);
    }
    
    /**
     *  Return the new name for this element, as set by setNewName
     */
    public String getNewName()
    {
    	return newName;
    }
    
    /**
     * Get the parent of this remote object that is being renamed
     */
    public Object getParent()
    {
    	return parent;
    }

    /**
     * Set the parent of this remote object that is being renamed
     */
    public void setParent(Object parent)
    {
    	this.parent = parent;
    }

    /**
     * Get the canonical name of this row. Sometimes, the name alone is not enough to do uniqueness
     *  checking on. For example, two connections or filter pools can have the same name if they are
     *  in different profiles. Two iSeries QSYS objects can have the same name if their object types 
     *  are different. 
     * <p>
     * This method returns a name that can be used for uniqueness checking because it is qualified 
     *  sufficiently to make it unique.
     * <p>
     * Defers to the object's adapter
     */
    public String getCanonicalNewName()
    {
    	// this is all for defect 42145
    	Object element = super.getElement(); 
    	ISystemViewElementAdapter adapter = super.getAdapter();
    	String cName = newName;
    	if (adapter != null)
    	  cName = adapter.getCanonicalNewName(element, newName);
    	else
    	  cName = newName;
    	//System.out.println("Inside getCanonicalNewName: newName: " + newName + ", canonical: " + cName);    	  
    	return cName;
    }
    /**
     * Compares the given new name to this row's current name, taking into consideration case if appropriate.
     * Defers to the object's adapter
     */
    public boolean newNameEqualsOldName()
    {
    	Object element = super.getElement(); 
    	ISystemViewElementAdapter adapter = super.getAdapter();
    	if (adapter != null)
    	  return adapter.namesAreEqual(element, newName);
    	else
    	  return getName().equals(newName);   	    	
    }
    
    /**
     * Return the name length limit, if available via the name validator supplied by the adapter.
     * Returns -1 if not available.
     */
    public int getNameLengthLimit()
    {
    	return nameLengthLimit;
    }

    /**
     * Set the validator for the new name,as supplied by the adaptor for name checking.
     * Overrides the default which is to query it from the object's adapter.
     */
    public void setNameValidator(ISystemValidator nameValidator)
    {
    	inputValidator = nameValidator;
    }

    /**
     * Set the uniqueness validator for the new name,as supplied by the remote adaptor.
     */
    public void setUniqueNameValidator(ValidatorUniqueString uniqueNameValidator)
    {
    	inputUniqueNameValidator = uniqueNameValidator;
    }
    
    /**
     * Return the validator for the new name,as supplied by the adaptor for
     *  this element type.
     * <p>
     * By default queries it from the object's adapter, unless setNameValidator has been
     * called.
     */    
    public ISystemValidator getNameValidator()
    {
    	return inputValidator;
    }

    /**
     * Return the uniqueness validator for the new name,as supplied by the call to setUniqueNameValidator
     */    
    public ValidatorUniqueString getUniqueNameValidator()
    {
    	return inputUniqueNameValidator;
    }

    /**
     * Return true if this row is currently in error
     */
    public boolean getError()
    {
    	return errorMsg != null;
    }
    /**
     * Return text of error if this row is currently in error
     */
    public SystemMessage getErrorMessage()
    {
    	return errorMsg;
    }    
    /**
     * Set error message for this row.
     * Pass null to clear it.
     */
    public void setErrorMessage(SystemMessage errorMsg)
    {
    	this.errorMsg = errorMsg;
    }
    
    public String toString()
    {
    	return getNewName();
    }    
}
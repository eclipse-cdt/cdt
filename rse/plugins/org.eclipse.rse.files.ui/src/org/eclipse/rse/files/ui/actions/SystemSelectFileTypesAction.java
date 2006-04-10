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

package org.eclipse.rse.files.ui.actions;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.rse.subsystems.files.core.SystemFileResources;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileFilterString;
import org.eclipse.rse.ui.actions.SystemBaseDialogAction;
import org.eclipse.rse.ui.dialogs.SystemSelectFileTypesDialog;
import org.eclipse.swt.widgets.Shell;


/**
 * The action for allowing the user to select one or more file types, using the Eclipse
 *  dialog for this. The types are from the file editor registry, as specified in the
 *  Workbench preferences.
 * <p>
 * To set preselected types, use {@link #setTypes(List)} or {@link #setTypes(String[])}.
 * Or, if you have single string of comma-separated types, call {@link #setTypes(String)}.
 * <p>
 * After running, and checking wasCancelled(), you can query the selected types
 * using one of:
 * <ol>
 *   <li>{@link #getTypes()} to retrieve the selected types as a List
 *   <li>{@link #getTypesArray()} to retrieve the selected types as a String array
 *   <li>{@link #getTypesString()} to retrieve the selected types as a single String of comma-delimited selections
 * <p>
 * Note the types are remembered after running, so a subsequent run will result in the previous types
 * being preselected, assuming you re-use the same instance of this class.
 * 
 * @see org.eclipse.rse.ui.dialogs.SystemSelectFileTypesDialog
 */
public class SystemSelectFileTypesAction extends SystemBaseDialogAction 
{
	protected List types = new ArrayList();
	    
	/**
	 * Constructor
     * To set preselected types, use {@link #setTypes(List)} or {@link #setTypes(String[])}.
     * Note the types are remember after running, so a subsequent run will result in the previous types
     * being preselected.
	 */
	public SystemSelectFileTypesAction(Shell shell)
	{
		super(SystemFileResources.ACTION_SELECTFILETYPES_LABEL, SystemFileResources.ACTION_SELECTFILETYPES_TOOLTIP, null, shell);
	}	

    /**
     * Set the current input types as a String array.
     * Each type is a file name extension, without the dot, as in "java" or "class"
     */
    public void setTypes(String[] types)
    {
    	this.types = Arrays.asList(types);
    }
    /**
     * Set the current input types as a java.util List, such as ArrayList
     * Each type is a file name extension, without the dot, as in "java" or "class"
     */
    public void setTypes(List types)
    {
    	this.types = types;
    }
    /**
     * Set the current input types given a comma-separated list as a single String.
     */
    public void setTypes(String typeString)
    {
    	setTypes(RemoteFileFilterString.parseTypes(typeString));
    }

    /**
     * Get the selected file types after running the action. Returns an ArrayList
     */
    public List getTypes()
    {
    	return types;
    }
    /**
     * Get the selected file types after running the action. Returns a String array
     */
    public String[] getTypesArray()
    {
    	String[] typesArray = new String[types.size()];
    	Iterator i = types.iterator();
    	int idx=0;
    	while (i.hasNext())
    	  typesArray[idx++] = (String)i.next();
    	return typesArray;
    }
    /**
     * Get the selected file types as a concatenated list of strings, comma-separated
     */
    public String getTypesString()
    {
    	return RemoteFileFilterString.getTypesString(getTypesArray());
    }
    
    /**
     * Return true if the dialog was cancelled by the user.
     * Only valid after calling run().
     */
    public boolean wasCancelled()
    {
    	return (getValue() == null);
    }


    /**
     * Create and return the dialog
     */
    public Dialog createDialog(Shell parent)
    {
		SystemSelectFileTypesDialog dialog =
			new SystemSelectFileTypesDialog(getShell(), types);
	    return dialog;    	
    }
    
    /**
     * Parent abstract method.
     * Called after dialog runs, to retrieve the value from the dialog.
     * Will return null if dialog cancelled.
     */
    public Object getDialogValue(Dialog dlg)
    {
    	Object[] result = ((SystemSelectFileTypesDialog)dlg).getResult();
    	if (result != null)
    	{
			types = new ArrayList(result.length);
			for (int idx = 0; idx < result.length; idx++)
			   types.add(result[idx]);
    	    return types;
    	}
    	else
    	  return null;
    }
}
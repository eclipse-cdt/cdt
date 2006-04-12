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
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.ISystemRenameTarget;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.dialogs.SystemRenameDialog;
import org.eclipse.rse.ui.dialogs.SystemRenameSingleDialog;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.swt.widgets.Shell;


/**
 * The action that displays the Rename dialog. There are two ways to use this action:
 * <ol>
 *  <li>When invoking from a class that implements ISystemRenameTarget. In this case, that class 
 *      will be called back to determine if this action is to be enabled or not, and to do the actual rename for 
 *      each selected object, after the dialog is dismissed.
 *  <li>When used without an ISystemRenameTarget, in which case you need to call getNewNames() after
 *      running the action, and then use the new names to do the rename. This will return null if the dialog
 *      was cancelled. 
 * </ol>
 * <p>
 * If the input objects do not adapt to {@link org.eclipse.rse.ui.view.ISystemRemoteElementAdapter} or 
 * {@link org.eclipse.rse.ui.view.ISystemViewElementAdapter}, then you
 * should call {@link #setNameValidator(org.eclipse.rse.ui.validators.ISystemValidator)} to 
 * specify a validator that is called to verify the typed new name is valid. Further, to show the type value
 * of the input objects, they should implement {@link org.eclipse.rse.ui.dialogs.ISystemTypedObject}.
 * 
 * @see org.eclipse.rse.ui.dialogs.SystemRenameDialog
 * @see org.eclipse.rse.ui.dialogs.SystemRenameSingleDialog
 */
public class SystemCommonRenameAction extends SystemBaseDialogAction 
                                 
{
	private ISystemRenameTarget renameTarget;
	private boolean            copyCollisionMode = false;
	private String              newNames[];
    private ISystemValidator    nameValidator;
    private String              singleSelectionHelp, multiSelectionHelp, promptLabel, promptTip, verbage;	
    
	/**
	 * Constructor when using a rename target
	 * @param parent The Shell of the parent UI for this dialog
	 * @param target The UI part that has selectable and renamable parts.
	 */
	public SystemCommonRenameAction(Shell parent, ISystemRenameTarget target) 
	{
		super(SystemResources.ACTION_RENAME_LABEL, SystemResources.ACTION_RENAME_TOOLTIP, 
		      RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_RENAME_ID), parent);
		allowOnMultipleSelection(true);
		setProcessAllSelections(true);
		renameTarget = target;
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_REORGANIZE);		
  	    setHelp(RSEUIPlugin.HELPPREFIX+"actn0018");
	}
	
	/**
	 * Constructor when not using a rename target
	 * @param parent The Shell of the parent UI for this dialog
	 */
	public SystemCommonRenameAction(Shell parent) 
	{
		this(parent, null);
	}
	/**
	 * Set the help to use in the dialog when there is a single selection
	 */
	public void setDialogSingleSelectionHelp(String helpID)
	{
		this.singleSelectionHelp = helpID;
	}
	/**
	 * Set the help to use in the dialog when there are multiple selections
	 */
	public void setDialogMultiSelectionHelp(String helpID)
	{
		this.multiSelectionHelp = helpID;
	}
	/**
	 * Set the label and tooltip of the prompt, used when only one thing is selected. The default is "New name:"
	 */
	public void setSingleSelectPromptLabel(String label, String tooltip)
	{
		this.promptLabel = label;
		this.promptTip = tooltip;
	}
	/**
	 * Set the verbage to show at the top of the table, used when multi things are selected. The default is "Enter a new name for each resource"
	 */
	public void setMultiSelectVerbage(String verbage)
	{
		this.verbage = verbage;
	}
	
    /**
     * Set the validator for the new name,as supplied by the adaptor for name checking.
     * Overrides the default which is to query it from the object's adapter.
     */
    public void setNameValidator(ISystemValidator nameValidator)
    {
    	this.nameValidator = nameValidator;
    }

	/**
	 * Indicate this dialog is the result of a copy/move name collision.
	 * Affects the title, verbage at the top of the dialog, and context help.
	 */
	public void setCopyCollisionMode(boolean copyCollisionMode)
	{
		this.copyCollisionMode = copyCollisionMode;
	}
	/**
	 * Query if this dialog is the result of a copy/move name collision.
	 * Affects the title, verbage at the top of the dialog, and context help.
	 */
	public boolean getCopyCollisionMode()
	{
		return copyCollisionMode;
	}
	
    /**
     * Called by SystemBaseAction when selection is set.
     * Our opportunity to verify we are allowed for this selected type.
     * We overload it to call canRename() in the SystemView class.
     */
	public boolean updateSelection(IStructuredSelection selection)
	{
		if (renameTarget == null)
		  return true;
		else
		  return renameTarget.canRename();
	}
	
	/**
	 * If you decide to use the supplied run method as is,
	 *  then you must override this method to create and return
	 *  the dialog that is displayed by the default run method
	 *  implementation.
	 * <p>
	 * If you override run with your own, then
	 *  simply implement this to return null as it won't be used.
	 * @see #run()
	 */
	protected Dialog createDialog(Shell parent)
	{
		// multi-select
		if (getSelection().size() > 1)
		{
		  	SystemRenameDialog dlg = new SystemRenameDialog(parent);
		  	if (nameValidator != null)
		    	dlg.setNameValidator(nameValidator);
			if (multiSelectionHelp != null)
				dlg.setHelp(multiSelectionHelp);
		    if (verbage != null)
		     	dlg.setVerbage(verbage);
		  	return dlg;
		}
		// single-select
		else
		{
		  	SystemRenameSingleDialog dlg = new SystemRenameSingleDialog(parent);
		  	if (copyCollisionMode)
		    	dlg.setCopyCollisionMode(copyCollisionMode);
		  	if (nameValidator != null)
		    	dlg.setNameValidator(nameValidator);
			if (singleSelectionHelp != null)
				dlg.setHelp(singleSelectionHelp);
			if ((promptLabel != null) || (promptTip != null))
				dlg.setPromptLabel(promptLabel, promptTip);
		  	return dlg;
		}
	}
	
	/**
	 * Required by parent. We use it to actually do the rename by calling doRename
	 *  in the supplied ISystemRenameTarget, if we are in that mode.
	 * As a result, we return null from here.
	 * @see #getNewNames()
	 */
	protected Object getDialogValue(Dialog dlg)
	{
		newNames = null;
		if (dlg instanceof SystemRenameDialog)
		{
		  SystemRenameDialog rnmDlg = (SystemRenameDialog)dlg;
		  if (!rnmDlg.wasCancelled())
	 	  {
		    newNames = rnmDlg.getNewNames();
		    if (renameTarget != null)
		      renameTarget.doRename(newNames); // perform the actual renames.
		  }
		}
		else
		{
		  SystemRenameSingleDialog rnmDlg = (SystemRenameSingleDialog)dlg;
		  if (!rnmDlg.wasCancelled())
	 	  {
		    String name = rnmDlg.getNewName();
		    newNames = new String[1];
		    newNames[0] = name;
		    if (renameTarget != null)
		      renameTarget.doRename(newNames); // perform the actual renames.
		  }
		}
		return null;					
	}
	
	/**
	 * Return the new names entered by the user. You only need to call this when you don't supply a 
	 *  rename target. In this case, it is your responsibility to do the actual renames.
	 * @return - array of new names, with the order matching the order of the input selection. Null if dialog cancelled.
	 */
	public String[] getNewNames()
	{
		return newNames;
	}
}
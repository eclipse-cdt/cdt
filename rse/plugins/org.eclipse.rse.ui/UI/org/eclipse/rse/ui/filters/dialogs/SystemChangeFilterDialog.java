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

import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.filters.ISystemFilterPoolManagerProvider;
import org.eclipse.rse.filters.ISystemFilterPoolReferenceManagerProvider;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemPageCompleteListener;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.dialogs.SystemPromptDialog;
import org.eclipse.rse.ui.filters.ISystemChangeFilterPaneEditPaneSupplier;
import org.eclipse.rse.ui.filters.SystemChangeFilterPane;
import org.eclipse.rse.ui.filters.SystemFilterStringEditPane;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;


/**
 * A dialog that allows the user to change a filter. It allows update of the filter strings. <br>
 * We do not typically override this to produce our own change filter dialog ... rather we usually
 *  call the configuration methods to affect it. At a minimum, we usually want to set the {@link #setFilterStringEditPane(SystemFilterStringEditPane) editpane},
 *  which is used to prompt for a new filter string or change an existing one. We usually share the
 *  same edit pane with the {@link SystemNewFilterWizard} wizard.
 */
public class SystemChangeFilterDialog extends SystemPromptDialog 
	implements ISystemPageCompleteListener, ISystemChangeFilterPaneEditPaneSupplier
{
	
	protected SystemChangeFilterPane changeFilterPane; 	
	protected SystemFilterStringEditPane editPane;
	
	/**
	 * Constructor
	 */
	public SystemChangeFilterDialog(Shell shell) 
	{
		this(shell, SystemResources.RESID_CHGFILTER_TITLE);
	}
	/**
	 * Constructor, when unique title desired
	 */
	public SystemChangeFilterDialog(Shell shell, String title)
	{
		super(shell, title);
		changeFilterPane = new SystemChangeFilterPane(shell, this, this);
		changeFilterPane.addPageCompleteListener(this);
		setHelp();
	}		

	/**
	 * Overridable extension point for setting dialog help
	 */
	protected void setHelp()
	{
       setHelp(SystemPlugin.HELPPREFIX+"dufr0000");
	}

	// INPUT/CONFIGURATION
	/**
	 * <i>Configuration method</i><br>
	 * Specify an edit pane that prompts the user for the contents of a filter string.
	 */
	public void setFilterStringEditPane(SystemFilterStringEditPane editPane)
	{
		this.editPane = editPane;
	}
    /**
	 * <i>Configuration method</i><br>
     * Set the contextual system filter pool reference manager provider. Will be non-null if the
     * current selection is a reference to a filter pool or filter, or a reference manager
     * provider.
     * <p>
     * This is passed into the filter and filter string wizards and dialogs in case it is needed
     * for context. 
     */
    public void setSystemFilterPoolReferenceManagerProvider(ISystemFilterPoolReferenceManagerProvider provider)
    {
		changeFilterPane.setSystemFilterPoolReferenceManagerProvider(provider);
    }	
	/**
	 * <i>Configuration method</i><br>
	 * Set the contextual system filter pool manager provider. Will be non-null if the
	 * current selection is a filter pool or filter, or reference to them, or a manager provider.
	 * Generally this is called when the setSystemFilterPoolReferenceManagerProvider can't be called
	 *  for some reason.
	 * <p>
	 * This is passed into the filter and filter string wizards and dialogs in case it is needed
	 * for context. 
	 */
	public void setSystemFilterPoolManagerProvider(ISystemFilterPoolManagerProvider provider)
	{
		changeFilterPane.setSystemFilterPoolManagerProvider(provider);
	}	
		
	/**
	 * <i>Configuration method</i><br>
	 * Set the Parent Filter Pool prompt label and tooltip text.
	 */
	public void setParentPoolPromptLabel(String label, String tip)
	{
		changeFilterPane.setParentPoolPromptLabel(label, tip);
	}
	/**
	 * Return the parent filter pool prompt label, as set by {@link #setParentPoolPromptLabel(String, String)}
	 */
	public String getParentPoolPromptLabel()
	{
		return changeFilterPane.getParentPoolPromptLabel();
	}
	/**
	 * Return the parent filter pool prompt tip, as set by {@link #setParentPoolPromptLabel(String, String)}
	 */
	public String getParentPoolPromptTip()
	{
		return changeFilterPane.getParentPoolPromptTip();
	}

	/**
	 * <i>Configuration method</i><br>
	 * Set the name prompt label and tooltip text.
	 */
	public void setNamePromptLabel(String label, String tip)
	{
		changeFilterPane.setNamePromptLabel(label, tip);
	}
	/**
	 * Return the name prompt label as set by {@link #setNamePromptLabel(String, String)}
	 */
	public String getNamePromptLabel()
	{
		return changeFilterPane.getNamePromptLabel();
	}
	/**
	 * Return the name prompt tip as set by {@link #setNamePromptLabel(String, String)}
	 */
	public String getNamePromptTip()
	{
		return changeFilterPane.getNamePromptTip();
	}

	/**
	 * <i>Configuration method</i><br>
	 * Set the label shown in group box around the filter string list, and the tooltip text for the
	 *  list box.
	 */
	public void setListLabel(String label, String tip)
	{
		changeFilterPane.setListLabel(label, tip);
	}
	/**
	 * Return list label as set by {@link #setListLabel(String, String)}
	 */
	public String getListLabel()
	{
		return changeFilterPane.getListLabel();
	}
	/**
	 * Return list tip as set by {@link #setListLabel(String, String)}
	 */
	public String getListTip()
	{
		return changeFilterPane.getListTip();
	}

	/**
	 * Set the string to show as the first item in the list. 
	 * The default is "New filter string"
	 */
	public void setNewListItemText(String label)
	{
		changeFilterPane.setNewListItemText(label);
	}
	/**
	 * Return the text for the list item, as set by {@link #setNewListItemText(String)},
	 *  or the default if not set.
	 */
	public String getNewListItemText()
	{
		return changeFilterPane.getNewListItemText(); 
	}

	/**
	 * <i>Configuration method</i><br>
	 * Call this to specify a validator for the filter string. It will be called per keystroke.
	 * A default validator is supplied otherwise: ValidatorFilterString.
	 * <p>
	 * Your validator should extend ValidatorFilterString to inherited the uniqueness error checking.
	 * <p>
	 * Alternatively, if all you want is a unique error message for the case when duplicates are found,
	 *  call setDuplicateFilterStringErrorMessage, and it will be used in the default validator.
	 */
	public void setFilterStringValidator(ISystemValidator v)
	{
		changeFilterPane.setFilterStringValidator(v);
	}
	/**
	 * Return the result of {@link #setFilterStringValidator(ISystemValidator)}.
	 */
	public ISystemValidator getFilterStringValidator()
	{
		return changeFilterPane.getFilterStringValidator();
	}
	/**
	 * <i>Configuration method</i><br>
	 * Set the error message to use when the user is editing or creating a filter string, and the 
	 *  Apply processing detects a duplicate filter string in the list.
	 */
	public void setDuplicateFilterStringErrorMessage(SystemMessage msg)
	{
		changeFilterPane.setDuplicateFilterStringErrorMessage(msg);
	}
	/**
	 * Return results of {@link #setDuplicateFilterStringErrorMessage(SystemMessage)}
	 */
	public SystemMessage getDuplicateFilterStringErrorMessage()
	{
		return changeFilterPane.getDuplicateFilterStringErrorMessage();
	}

	/**
	 * <i>Configuration method</i><br>
	 * Specify if you want to include a test button or not. Appears with "Apply" and "Reset"
	 */
	public void setWantTestButton(boolean wantTestButton)
	{
		changeFilterPane.setWantTestButton(wantTestButton);
	}
	/**
	 * Return whether a test button is wanted or not, as set by {@link #setWantTestButton(boolean)}
	 */
	public boolean getWantTestButton()
	{
		return changeFilterPane.getWantTestButton();
	}

	/**
	 * Set if the edit pane is not to be editable
	 */
	public void setEditable(boolean editable)
	{
		changeFilterPane.setEditable(editable);
	}
	/**
	 * Return whether the edit pane is editable, as set by {@link #setEditable(boolean)}
	 */
	public boolean getEditable()
	{
		return changeFilterPane.getEditable();
	}

	/**
	 * Set if the user is to be allowed to create multiple filter strings or not. Default is true
	 */
	public void setSupportsMultipleStrings(boolean multi)
	{
		changeFilterPane.setSupportsMultipleStrings(multi);
	}
	/**
	 * Return whether the user is to be allowed to create multiple filter strings or not. Default is true
	 */
	public boolean getSupportsMultipleStrings()
	{
		return changeFilterPane.getSupportsMultipleStrings();
	}

	// LIFECYCLE
	/**
	 * Intercept of parent so we can set the input filter, and deduce whether
	 *  strings are case sensitive and if duplicates are allowed.<br>
	 * Not typically overridden, but if you do, be sure to call super!
	 */
	public void setInputObject(Object inputObject)
	{
		changeFilterPane.setInputObject(inputObject);
	}
			
	/**
	 * Returns the control (the list view) to recieve initial focus control
	 */
	protected Control getInitialFocusControl()
	{
		return changeFilterPane.getInitialFocusControl();
	}
	/**
	 *  Populates the content area
	 */
	protected Control createInner(Composite parent)
	{
		return changeFilterPane.createContents(parent);
	}
	/**
	 * Intercept of parent so we can reset the default button
	 */
	protected void createButtonsForButtonBar(Composite parent) 
	{
		super.createButtonsForButtonBar(parent);		
		getShell().setDefaultButton(changeFilterPane.getApplyButton()); // defect 46129
	}
	/**
	 * Return our edit pane. Overriding this is an alternative to calling setEditPane.
	 * Method is declared in {@link ISystemChangeFilterPaneEditPaneSupplier}.  
	 */
	public SystemFilterStringEditPane getFilterStringEditPane(Shell shell)
	{
	    if (editPane == null)
	      editPane = new SystemFilterStringEditPane(shell);
	    return editPane;		
	}

	/**
	 * Parent override.
	 * Called when user presses OK button. 
	 * This is when we save all the changes the user made.
	 */
	protected boolean processOK() 
	{
		return changeFilterPane.processOK();
	}	

	/**
	 * Parent override.
	 * Called when user presses CLOSE button. We simply blow away all their changes!
	 */
	protected boolean processCancel() 
	{
		return changeFilterPane.processCancel();
	}	
				

	/**
	 * The comleteness of the page has changed.
	 * This is a callback from SystemChangeFilterPane.
	 */
	public void setPageComplete(boolean complete)
	{
		super.setPageComplete(complete);
	}

	/**
	 * Returns parent shell, under which this window's shell is created.
	 *
	 * @return the parent shell, or <code>null</code> if there is no parent shell
	 */
	public Shell getParentShell() 
	{
		return super.getParentShell();
	}		
}
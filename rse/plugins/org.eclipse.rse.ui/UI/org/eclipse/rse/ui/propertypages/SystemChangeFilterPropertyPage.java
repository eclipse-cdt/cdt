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

package org.eclipse.rse.ui.propertypages;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.SubSystemConfiguration;
import org.eclipse.rse.core.subsystems.SubSystemHelpers;
import org.eclipse.rse.core.subsystems.util.ISubSystemConfigurationAdapter;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterPoolManagerProvider;
import org.eclipse.rse.filters.ISystemFilterPoolReferenceManagerProvider;
import org.eclipse.rse.filters.ISystemFilterReference;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.ISystemPageCompleteListener;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemPropertyResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.filters.ISystemChangeFilterPaneEditPaneSupplier;
import org.eclipse.rse.ui.filters.SystemChangeFilterPane;
import org.eclipse.rse.ui.filters.SystemFilterStringEditPane;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;


/**
 * This is the property page for changing filters. This page used to be the Change dialog.
 * The plugin.xml file registers this for objects of class org.eclipse.rse.internal.filters.SystemFilter or
 *   org.eclipse.rse.filters.SystemFilterReference.
 * <p>
 * If you have your own change filter dialog (versus configuring ours) you must configure this
 *  pane yourself by overriding {@link SubSystemConfiguration#customizeChangeFilterPropertyPage(SystemChangeFilterPropertyPage, ISystemFilter, Shell)}
 *  and configuring the pane as described in that method's javadoc.
 */
public class SystemChangeFilterPropertyPage extends SystemBasePropertyPage
       implements  ISystemMessages, ISystemPageCompleteListener, ISystemChangeFilterPaneEditPaneSupplier
{
	
	protected String errorMessage;
    protected boolean initDone = false;
    
	protected SystemChangeFilterPane changeFilterPane;    
	protected SystemFilterStringEditPane editPane;
	    	
	/**
	 * Constructor for SystemFilterPropertyPage
	 */
	public SystemChangeFilterPropertyPage()
	{
		super();
		RSEUIPlugin sp = RSEUIPlugin.getDefault();
		changeFilterPane = new SystemChangeFilterPane(null, this, this);
		changeFilterPane.addPageCompleteListener(this);
		setHelp(RSEUIPlugin.HELPPREFIX+"dufr0000");	
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
	 * provider itself (eg subsystem)
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
	 * current selection is a filter pool or filter or reference to either, or a manager
	 * provider itself (eg subsystemconfiguration)
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
	 * <i>Configuration method</i><br>
	 * Set the name prompt label and tooltip text.
	 */
	public void setNamePromptLabel(String label, String tip)
	{
		changeFilterPane.setNamePromptLabel(label, tip);
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
	 * Set the string to show as the first item in the list. 
	 * The default is "New filter string"
	 */
	public void setNewListItemText(String label)
	{
		changeFilterPane.setNewListItemText(label);
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
	 * <i>Configuration method</i><br>
	 * Set the error message to use when the user is editing or creating a filter string, and the 
	 *  Apply processing detects a duplicate filter string in the list.
	 */
	public void setDuplicateFilterStringErrorMessage(SystemMessage msg)
	{
		changeFilterPane.setDuplicateFilterStringErrorMessage(msg);
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
	 * Set if the edit pane is not to be editable
	 */
	public void setEditable(boolean editable)
	{
		changeFilterPane.setEditable(editable);
	}

	/**
	 * Set if the user is to be allowed to create multiple filter strings or not. Default is true
	 */
	public void setSupportsMultipleStrings(boolean multi)
	{
		changeFilterPane.setSupportsMultipleStrings(multi);
	}
		
	// OVERRIDABLE METHODS...
	
	/**
	 * Create the page's GUI contents.
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(Composite)
	 */
	protected Control createContentArea(Composite parent)
	{
		Shell shell = getShell();
		if (shell == null)
		{
			System.out.println("Damn, shell is still null!");
			
		}
		changeFilterPane.setShell(shell);
		
		ISystemFilter selectedFilter = getFilter();
		if (selectedFilter.isPromptable())
		{
			int nbrColumns = 1;
			Composite composite_prompts = SystemWidgetHelpers.createComposite(parent, nbrColumns);
			Label test = SystemWidgetHelpers.createLabel(composite_prompts, SystemPropertyResources.RESID_TERM_NOTAPPLICABLE, nbrColumns, false);
			return composite_prompts;			
		}
		
		if (getElement() instanceof ISystemFilterReference)
		{
			ISystemFilterReference filterRef = (ISystemFilterReference)getElement();
			changeFilterPane.setSystemFilterPoolReferenceManagerProvider(filterRef.getProvider());
		}			
		changeFilterPane.setSystemFilterPoolManagerProvider(selectedFilter.getProvider());			
		
		ISubSystemConfiguration ssf = SubSystemHelpers.getParentSubSystemConfiguration(selectedFilter);
		ISubSystemConfigurationAdapter adapter = (ISubSystemConfigurationAdapter)ssf.getAdapter(ISubSystemConfigurationAdapter.class);
		adapter.customizeChangeFilterPropertyPage(ssf, this, selectedFilter, shell);
		
		changeFilterPane.setInputObject(getElement());
		
		/*
		// ensure the page has no special buttons
		noDefaultAndApplyButton();		

		// Inner composite
		int nbrColumns = 2; 
		Composite composite_prompts = SystemWidgetHelpers.createComposite(parent, nbrColumns);	
		
		Label test = SystemWidgetHelpers.createLabel(composite_prompts, "Testing", nbrColumns);


	    if (!initDone)	
	      doInitializeFields();		  
		
		return composite_prompts;
		*/
		return changeFilterPane.createContents(parent);
	}
	/**
	 * Intercept of parent so we can reset the default button
	 */
	protected void contributeButtons(Composite parent) 
	{
		super.contributeButtons(parent);		
		getShell().setDefaultButton(changeFilterPane.getApplyButton()); // defect 46129
	}

	/**
	 * Parent-required method.
	 * Do full page validation.
	 * Return true if ok, false if there is an error.
	 */
	protected boolean verifyPageContents()
	{	
		return true;
	}

	/**
	 * Get the input filter object
	 */
	protected ISystemFilter getFilter()
	{
		Object element = getElement();
		if (element instanceof ISystemFilter)
		  return (ISystemFilter)element;
		else
		  return ((ISystemFilterReference)element).getReferencedFilter();
	}
	
	/**
	 * Called by parent when user presses OK
	 */
	public boolean performOk()
	{
		if (!super.performOk())
			return false;
		else
			return changeFilterPane.processOK();
	}
	/**
	 * Called by parent when user presses Cancel
	 */
	public boolean performCancel()
	{
		return changeFilterPane.processCancel();
	}

	/**
	 * The comleteness of the page has changed.
	 * This is a callback from SystemChangeFilterPane.
	 */
	public void setPageComplete(boolean complete)
	{
		//super.setPageComplete(complete);
		super.setValid(complete); // we'll see if this is the right thing to do
	}
	
	/**
	 * Return our edit pane. Overriding this is an alternative to calling setEditPane.
	 * Method is declared in {@link ISystemChangeFilterPaneEditPaneSupplier}.  
	 */
	public SystemFilterStringEditPane getFilterStringEditPane(Shell shell)
	{
		// this method is called from SystemChangeFilterPane via callback
		if (editPane == null)
		  editPane = new SystemFilterStringEditPane(shell);
		return editPane;		
	}	
}
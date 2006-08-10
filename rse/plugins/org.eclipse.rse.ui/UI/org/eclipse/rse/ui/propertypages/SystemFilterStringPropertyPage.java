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
import java.util.ResourceBundle;
import java.util.Vector;

import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.util.ISubSystemConfigurationAdapter;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolManager;
import org.eclipse.rse.filters.ISystemFilterPoolManagerProvider;
import org.eclipse.rse.filters.ISystemFilterPoolReferenceManagerProvider;
import org.eclipse.rse.filters.ISystemFilterString;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.filters.ISystemFilterStringEditPaneListener;
import org.eclipse.rse.ui.filters.SystemFilterStringEditPane;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.validators.ValidatorFilterString;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;


/**
 * The property page for filter string properties.
 * This is an output-only page.
 * The plugin.xml file registers this for objects of class org.eclipse.rse.internal.filters.SystemFilterString
 */
public class SystemFilterStringPropertyPage extends SystemBasePropertyPage implements ISystemFilterStringEditPaneListener
{
	//gui
	protected Label labelType, labelFilter, labelFilterPool, labelProfile;
	//protected Label labelString;
	//input
	protected SystemFilterStringEditPane editPane;
	protected ISystemValidator filterStringValidator;
	protected SystemMessage dupeFilterStringMessage;
	protected boolean editable = true;
	//state
	protected Composite composite_prompts;
	protected SystemMessage errorMessage;
    protected ResourceBundle rb;	
    protected boolean initDone = false;
    	
	/**
	 * Constructor
	 */
	public SystemFilterStringPropertyPage()
	{
		super();
		RSEUIPlugin sp = RSEUIPlugin.getDefault();
	}
	
	// configuration methods, called by customizeFilterStringPropertyPage in SubSystemConfigurationImpl...
	
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
		editPane.setSystemFilterPoolReferenceManagerProvider(provider);
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
		editPane.setSystemFilterPoolManagerProvider(provider);
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
		filterStringValidator = v;
	}
	/**
	 * <i>Configuration method</i><br>
	 * Set the error message to use when the user is editing or creating a filter string, and the 
	 *  Apply processing detects a duplicate filter string in the list.
	 */
	public void setDuplicateFilterStringErrorMessage(SystemMessage msg)
	{
		dupeFilterStringMessage = msg;
	}
	/**
	 * Set if the edit pane is not to be editable
	 */
	public void setEditable(boolean editable)
	{
		editable = false;
	}
	
	// lifecyle methods...

	/**
	 * Create the page's GUI contents.
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(Composite)
	 */
	protected Control createContentArea(Composite parent)
	{
		// Inner composite
		composite_prompts = SystemWidgetHelpers.createComposite(parent, 2);	

		// Type display
		labelType = createLabeledLabel(composite_prompts, SystemResources.RESID_PP_PROPERTIES_TYPE_LABEL, SystemResources.RESID_PP_PROPERTIES_TYPE_TOOLTIP);
		labelType.setText(SystemResources.RESID_PP_FILTERSTRING_TYPE_VALUE);

		// String display
		//labelString = createLabeledLabel(composite_prompts, SystemResources.RESID_PP_FILTERSTRING_STRING_ROOT);

		// Parent Filter display
		labelFilter = createLabeledLabel(composite_prompts, SystemResources.RESID_PP_FILTERSTRING_FILTER_LABEL, SystemResources.RESID_PP_FILTERSTRING_FILTER_TOOLTIP);
								  		  
		// Parent Filter Pool display
		labelFilterPool = createLabeledLabel(composite_prompts, SystemResources.RESID_PP_FILTERSTRING_FILTERPOOL_LABEL, SystemResources.RESID_PP_FILTERSTRING_FILTERPOOL_TOOLTIP);
			
		// Parent Profile display
		labelProfile = createLabeledLabel(composite_prompts, SystemResources.RESID_PP_FILTERSTRING_PROFILE_LABEL, SystemResources.RESID_PP_FILTERSTRING_PROFILE_TOOLTIP);

	    if (!initDone)	
	      doInitializeFields();		  
		
		return composite_prompts;
	}
	/**
	 * From parent: do full page validation
	 */
	protected boolean verifyPageContents()
	{
		boolean ok = false;
		clearErrorMessage();
		errorMessage = editPane.verify();
		if (errorMessage == null)
		{
		   ok = true;
		   String editedFilterString = editPane.getFilterString();
		   if (filterStringValidator != null)
		   {
			 errorMessage = filterStringValidator.validate(editedFilterString);
		   }
		}	
		if (errorMessage != null)
		{
		   ok = false;
		   setErrorMessage(errorMessage);
		}
		//System.out.println("Inside verifyPageContents. errorMessage = "+errorMessage);
		return ok;
	}
	
	/**
	 * Get the input filter string object
	 */
	protected ISystemFilterString getFilterString()
	{
		Object element = getElement();
		return ((ISystemFilterString)element);
	}

	/**
	 * Initialize values of input fields based on input
	 */
	protected void doInitializeFields()
	{
		initDone = true;
		ISystemFilterString filterstring = getFilterString();
	    ISystemFilter filter = filterstring.getParentSystemFilter();
	    // string
	    //labelString.setText(filterstring.getString());
	    // filter
	    labelFilter.setText(filter.getName());
	    // pool
	    ISystemFilterPool pool = filter.getParentFilterPool();
	    labelFilterPool.setText(pool.getName());
	    // profile
	    ISubSystemConfiguration ssFactory = (ISubSystemConfiguration)(pool.getProvider());
	    String profileName = ssFactory.getSystemProfile(pool).getName();
	    labelProfile.setText( profileName );

	    // edit pane
	    ISubSystemConfiguration factory = (ISubSystemConfiguration)filter.getProvider();
	    ISubSystemConfigurationAdapter adapter = (ISubSystemConfigurationAdapter)factory.getAdapter(ISubSystemConfigurationAdapter.class);
	    adapter.customizeFilterStringPropertyPage(factory, this, filterstring, getShell());
	    if (editPane == null)
	    {
	    	Shell shell = getShell();
	    	//System.out.println("Shell is: "+shell);
	    	editPane = new SystemFilterStringEditPane(shell);	    
	    }
		editPane.setSystemFilterPoolManagerProvider(filter.getProvider());
		editPane.setChangeFilterMode(true);
		editPane.addChangeListener(this);
		Control editPaneComposite = editPane.createContents(composite_prompts);
		((GridData)editPaneComposite.getLayoutData()).horizontalSpan = 2;
		
		editPane.setFilterString(filterstring.getString(), 0);
		if (!editable || filter.isNonChangable())
			editPaneComposite.setEnabled(false);
		else if (filterStringValidator == null)
		{
			Vector existingStrings = filter.getFilterStringsVector();
			existingStrings.remove(filterstring);
			filterStringValidator = new ValidatorFilterString(existingStrings, filter.isStringsCaseSensitive());
			if (dupeFilterStringMessage != null)
				((ValidatorFilterString)filterStringValidator).setDuplicateFilterStringErrorMessage(dupeFilterStringMessage);
		}
	}
	
	/**
	 * Called by parent when user presses OK
	 */
	public boolean performOk()
	{
		boolean ok = super.performOk();
		if (!ok)
			return false;
		ISystemFilterString filterstring = getFilterString();
		ISystemFilter filter = filterstring.getParentSystemFilter();
		ISystemFilterPool pool = filter.getParentFilterPool(); // recurses for nested filter
		ISystemFilterPoolManager mgr = pool.getSystemFilterPoolManager();
		try
		{
			mgr.updateSystemFilterString(filterstring, editPane.getFilterString());
		}
		catch (SystemMessageException e)
		{
			SystemBasePlugin.logError("Error updating filter string from property page", e);
			e.printStackTrace();
			SystemMessageDialog.displayMessage(getShell(), e);
			ok = false;
		}
		catch (Exception e)
		{
			SystemBasePlugin.logError("Error updating filter string from property page", e);
			e.printStackTrace();
			SystemMessageDialog.displayExceptionMessage(getShell(), e);
			ok = false;
		}
		  
		/*
		String[] listItems = listView.getItems();
		String[] filterStrings = new String[listItems.length - 1];
		for (int idx=0; idx<filterStrings.length; idx++)
		   filterStrings[idx] = listItems[idx+1];
		try {
			mgr.updateSystemFilter(inputFilter, inputFilter.getName(), filterStrings);
		} 
		catch (SystemMessageException exc) 
		{
			getMessageLine().setErrorMessage(exc.getSystemMessage());
			return false;
		}
		catch (Exception exc) 
		{
			//displayErrorMessage("Error updating filter: " + exc.getMessage());
			SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_UPDATEFILTER_FAILED);
			String excText = exc.getMessage();
			if (excText == null)
			  excText = exc.getClass().getName();
			msg.makeSubstitution(excText,exc);
			getMessageLine().setErrorMessage(msg);
			return false;
		}*/					
		return ok;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.filters.ISystemFilterStringEditPaneListener#filterStringChanged(org.eclipse.rse.core.ui.messages.SystemMessage)
	 */
	public void filterStringChanged(SystemMessage message)
	{		
		if (message == null)
			clearErrorMessage();
		else
			setErrorMessage(message);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.filters.ISystemFilterStringEditPaneListener#backupChangedState()
	 */
	public void backupChangedState()
	{
	}
	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.filters.ISystemFilterStringEditPaneListener#restoreChangedState()
	 */
	public void restoreChangedState()
	{
	}

}
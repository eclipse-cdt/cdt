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
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolReference;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemPropertyResources;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;


/**
 * The property page for filter pool properties.
 * This is an output-only page.
 * The plugin.xml file registers this for objects of class com.ibm.etools.systems.filters.SystemFilterPool
 */
public class SystemFilterPoolPropertyPage extends SystemBasePropertyPage
{

	protected Label labelType, labelName, labelProfile, labelReferenceCount, labelRelatedConnection;
	protected String errorMessage;
    protected boolean initDone = false;
    	
	/**
	 * Constructor
	 */
	public SystemFilterPoolPropertyPage()
	{
		super();
		RSEUIPlugin sp = RSEUIPlugin.getDefault();
	}
	/**
	 * Create the page's GUI contents.
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(Composite)
	 */
	protected Control createContentArea(Composite parent)
	{
		// ensure the page has no special buttons
		noDefaultAndApplyButton();		

		// Inner composite
		Composite composite_prompts = SystemWidgetHelpers.createComposite(parent, 2);	

		// Type display
		labelType = createLabeledLabel(composite_prompts, SystemResources.RESID_PP_PROPERTIES_TYPE_LABEL, SystemResources.RESID_PP_PROPERTIES_TYPE_TOOLTIP);
		labelType.setText(SystemResources.RESID_FILTERPOOL_TYPE_VALUE);
		
		// Name display
		labelName = createLabeledLabel(composite_prompts, SystemResources.RESID_FILTERPOOL_NAME_LABEL, SystemResources.RESID_FILTERPOOL_NAME_TOOLTIP);
		
		// Profile display
		labelProfile = createLabeledLabel(composite_prompts, SystemResources.RESID_FILTERPOOL_PROFILE_LABEL, SystemResources.RESID_FILTERPOOL_PROFILE_TOOLTIP);
		
		// Reference count display
		labelReferenceCount = createLabeledLabel(composite_prompts, SystemResources.RESID_FILTERPOOL_REFERENCECOUNT_LABEL,  SystemResources.RESID_FILTERPOOL_REFERENCECOUNT_TOOLTIP);

		// Related connection display
		labelRelatedConnection = createLabeledLabel(composite_prompts, SystemResources.RESID_FILTERPOOL_RELATEDCONNECTION_LABEL, SystemResources.RESID_FILTERPOOL_RELATEDCONNECTION_TOOLTIP);
					  		  
	    if (!initDone)	
	      doInitializeFields();		  
		
		return composite_prompts;
	}
	/**
	 * From parent: do full page validation
	 */
	protected boolean verifyPageContents()
	{
		return true;
	}
	
	/**
	 * Get the input filterpool object
	 */
	protected ISystemFilterPool getFilterPool()
	{
		Object element = getElement();
		if (element instanceof ISystemFilterPool)
		  return (ISystemFilterPool)element;
		else
		  return ((ISystemFilterPoolReference)element).getReferencedFilterPool();
	}

	/**
	 * Initialize values of input fields based on input
	 */
	protected void doInitializeFields()
	{
		initDone = true;
	    ISystemFilterPool pool = getFilterPool();
	    // name    
	    labelName.setText(pool.getName());
	    // profile
	    ISubSystemConfiguration ssFactory = (ISubSystemConfiguration)(pool.getProvider());
	    String profileName = ssFactory.getSystemProfile(pool).getName();
	    labelProfile.setText( profileName );
	    // reference count
	    labelReferenceCount.setText(Integer.toString(pool.getReferenceCount()));
		// related connection
		if (pool.getOwningParentName() == null)
			labelRelatedConnection.setText(SystemPropertyResources.RESID_TERM_NOTAPPLICABLE);
		else
			labelRelatedConnection.setText(pool.getOwningParentName());
	}
	

}
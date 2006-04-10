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
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterReference;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;


/**
 * The property page for filter properties.
 * This is an output-only page.
 * The plugin.xml file registers this for objects of class com.ibm.etools.systems.filters.SystemFilter
 */
public class SystemFilterPropertyPage extends SystemBasePropertyPage
{
	
	protected Label labelType, labelName, labelFilterPool, labelStringCount, labelIsConnectionPrivate, labelProfile;
	protected String errorMessage;	
    protected boolean initDone = false;
    	
	/**
	 * Constructor
	 */
	public SystemFilterPropertyPage()
	{
		super();
		SystemPlugin sp = SystemPlugin.getDefault();
	}
	/**
	 * Create the page's GUI contents.
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(Composite)
	 */
	protected Control createContentArea(Composite parent)
	{
		// Inner composite
		Composite composite_prompts = SystemWidgetHelpers.createComposite(parent, 2);	

		// Type display
		labelType = createLabeledLabel(composite_prompts, SystemResources.RESID_PP_PROPERTIES_TYPE_LABEL,  SystemResources.RESID_PP_PROPERTIES_TYPE_TOOLTIP);
		labelType.setText(SystemResources.RESID_PP_FILTER_TYPE_VALUE);

		// Name display
		labelName = createLabeledLabel(composite_prompts, SystemResources.RESID_PP_FILTER_NAME_LABEL, SystemResources.RESID_PP_FILTER_NAME_TOOLTIP);

		// String count display
		labelStringCount = createLabeledLabel(composite_prompts, SystemResources.RESID_PP_FILTER_STRINGCOUNT_LABEL,  SystemResources.RESID_PP_FILTER_STRINGCOUNT_TOOLTIP);

		// Is connection-private display
		labelIsConnectionPrivate = createLabeledLabel(composite_prompts, SystemResources.RESID_PP_FILTER_ISCONNECTIONPRIVATE_LABEL, SystemResources.RESID_PP_FILTER_ISCONNECTIONPRIVATE_TOOLTIP);
					  		  
		// Parent Filter Pool display
		labelFilterPool = createLabeledLabel(composite_prompts, SystemResources.RESID_PP_FILTER_FILTERPOOL_LABEL, SystemResources.RESID_PP_FILTER_FILTERPOOL_TOOLTIP);
			
		// Parent Profile display
		labelProfile = createLabeledLabel(composite_prompts, SystemResources.RESID_PP_FILTER_PROFILE_LABEL, SystemResources.RESID_PP_FILTER_PROFILE_TOOLTIP);

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
	 * Initialize values of input fields based on input
	 */
	protected void doInitializeFields()
	{
		initDone = true;
	    ISystemFilter filter = getFilter();
	    boolean isTransient = filter.isTransient();
	    // name    
	    labelName.setText(filter.getName());
	    // type
	    if (filter.isPromptable())
	      labelType.setText(SystemResources.RESID_PP_FILTER_TYPE_PROMPTABLE_VALUE);
	    if (!isTransient)
	    {
	      // pool
	      ISystemFilterPool pool = filter.getParentFilterPool();
	      labelFilterPool.setText(pool.getName());	    
	      // profile
	      ISubSystemConfiguration ssFactory = (ISubSystemConfiguration)(pool.getProvider());
	      String profileName = ssFactory.getSystemProfile(pool).getName();
	      labelProfile.setText( profileName );
	      // string count
	      labelStringCount.setText(Integer.toString(filter.getFilterStringCount()));
		  // is connection-private
		  if (pool.getOwningParentName() == null)
			  labelIsConnectionPrivate.setText(SystemResources.TERM_NO);
		  else
		  	  labelIsConnectionPrivate.setText(SystemResources.TERM_YES);
	    }
	}
	
}
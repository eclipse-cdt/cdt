/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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
 * Martin Oberhuber (Wind River) - Adapted original tutorial code to Open RSE.
 ********************************************************************************/

package samples.subsystems;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.ui.filters.actions.SystemChangeFilterAction;
import org.eclipse.rse.ui.filters.actions.SystemNewFilterAction;
import org.eclipse.rse.ui.view.SubsystemConfigurationAdapter;
import org.eclipse.swt.widgets.Shell;

import samples.RSESamplesPlugin;

/**
 * Adds functionality to the basic SubSystemConfiguration.
 */
public class DeveloperSubSystemConfigurationAdapter extends
		SubsystemConfigurationAdapter
{

	/**
	 * Constructor for DeveloperSubSystemConfigurationAdapter.
	 */
	public DeveloperSubSystemConfigurationAdapter()
	{
		super();
	}

	/**
	 * Override of parent method, to affect what is returned for the New Filter-> actions.
	 * We intercept here, versus getNewFilterPoolFilterAction, so that we can return multiple
	 *  actions versus just one.
	 */
	protected IAction[] getNewFilterPoolFilterActions(ISubSystemConfiguration factory, ISystemFilterPool selectedPool, Shell shell)
	{
	  	SystemNewFilterAction teamAction = (SystemNewFilterAction)super.getNewFilterPoolFilterAction(factory, selectedPool, shell);
	  	teamAction.setWizardPageTitle(RSESamplesPlugin.getResourceString("filter.team.pagetitle")); //$NON-NLS-1$
	  	teamAction.setPage1Description(RSESamplesPlugin.getResourceString("filter.team.pagetext")); //$NON-NLS-1$
	  	teamAction.setType("team"); //$NON-NLS-1$
	  	teamAction.setText(RSESamplesPlugin.getResourceString("filter.team.pagetitle") + "..."); //$NON-NLS-1$ //$NON-NLS-2$
	  	
	  	SystemNewFilterAction devrAction = (SystemNewFilterAction)super.getNewFilterPoolFilterAction(factory, selectedPool, shell);    	
	  	devrAction.setWizardPageTitle(RSESamplesPlugin.getResourceString("filter.devr.pagetitle")); //$NON-NLS-1$
	  	devrAction.setPage1Description(RSESamplesPlugin.getResourceString("filter.devr.pagetext")); //$NON-NLS-1$
	  	devrAction.setType("devr"); //$NON-NLS-1$
	  	devrAction.setText(RSESamplesPlugin.getResourceString("filter.devr.pagetitle") + "..."); //$NON-NLS-1$ //$NON-NLS-2$
	  	devrAction.setFilterStringEditPane(new DeveloperFilterStringEditPane(shell));    	
	  	
	  	IAction[] actions = new IAction[2];
	  	actions[0] = teamAction;
	  	actions[1] = devrAction;
	  	return actions;
	 }
	  
	/**
	 * Override of parent method for returning the change-filter action, so we can affect it.
	 */
	protected IAction getChangeFilterAction(ISubSystemConfiguration factory, ISystemFilter selectedFilter, Shell shell)
	{
		SystemChangeFilterAction action = (SystemChangeFilterAction)super.getChangeFilterAction(factory, selectedFilter, shell);
	  	String type = selectedFilter.getType();
	  	if (type == null)
	  	  type = "team"; //$NON-NLS-1$ 
	  	if (type.equals("team")) //$NON-NLS-1$
	  	{
	  		action.setDialogTitle(RSESamplesPlugin.getResourceString("filter.team.dlgtitle")); //$NON-NLS-1$
	  	}
	  	else
	  	{
	  		action.setDialogTitle(RSESamplesPlugin.getResourceString("filter.devr.dlgtitle")); //$NON-NLS-1$
	  		action.setFilterStringEditPane(new DeveloperFilterStringEditPane(shell));
	  	}    
	  	return action;
	}    	

	/**
	 * Override of parent method for returning the image for filters in our subsystem.
	 */
	public ImageDescriptor getSystemFilterImage(ISystemFilter filter)
	{
	  	String type = filter.getType();
	  	if (type == null)
	  	  type = "team"; //$NON-NLS-1$
	  	if (type.equals("team")) //$NON-NLS-1$
	   	  return RSESamplesPlugin.getDefault().getImageDescriptor("ICON_ID_TEAMFILTER"); //$NON-NLS-1$
	   	else
	   	  return RSESamplesPlugin.getDefault().getImageDescriptor("ICON_ID_DEVELOPERFILTER"); //$NON-NLS-1$
	}

}

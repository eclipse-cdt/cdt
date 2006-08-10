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
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.core.subsystems.util;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolReference;
import org.eclipse.rse.filters.ISystemFilterReference;
import org.eclipse.rse.filters.ISystemFilterString;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.filters.actions.SystemNewFilterAction;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.propertypages.ISystemSubSystemPropertyPageCoreForm;
import org.eclipse.rse.ui.propertypages.SystemChangeFilterPropertyPage;
import org.eclipse.rse.ui.propertypages.SystemFilterStringPropertyPage;
import org.eclipse.rse.ui.widgets.IServerLauncherForm;
import org.eclipse.rse.ui.wizards.ISystemNewConnectionWizardPage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

/**
 * Defines the interface that must be implemented for adapters of for subsystem configurations.
 * This adapter is used when creating wizard pages for new connections.
 */
public interface ISubSystemConfigurationAdapter
{	
	
	/**
	 * There is a reasonable amount of processing needed to configure filter wizards. To aid
	 *  in performance and memory usage, we extract that processing into this method, and then
	 *  use a callback contract with the filter wizard to call us back to do this processing 
	 *  only at the time the action is actually selected to be run.
	 * <p>
	 * The processing we do here is to specify the filter pools to prompt the user for, in the
	 *  second page of the New Filter wizards.
	 * <p>
	 * This method is from the ISystemNewFilterActionConfigurator interface
	 */
	public void configureNewFilterAction(ISubSystemConfiguration factory, SystemNewFilterAction newFilterAction, Object callerData);
	

	// -----------------------------------
	// WIZARD PAGE CONTRIBUTION METHODS... 
	// -----------------------------------
	/**
	 * Optionally return one or more wizard pages to append to the New Connection Wizard if
	 *  the user selects a system type that this subsystem factory supports.
	 * <p>
	 * Some details:
	 * <ul>
	 *   <li>The wizard pages must implement ISystemNewConnectionWizardPage, so as to fit into the wizard's framework
	 *   <li>When the user successfully presses Finish, the createConnection method in the SystemRegistry will call 
	 *        your {@link #createSubSystem(IHost,boolean, ISystemNewConnectionWizardPage[])} method to create the 
	 *        your subsystem for the connection. The same pages you return here are passed back to you so you can 
	 *        interrogate them for the user-entered data and use it when creating the default subsystem instance.
	 * </ul>
	 * Tip: consider extending {@link org.eclipse.rse.ui.wizards.AbstractSystemNewConnectionWizardPage} for your wizard page class.
	 */
	public ISystemNewConnectionWizardPage[] getNewConnectionWizardPages(ISubSystemConfiguration factory, IWizard wizard);

	/**
	 * Returns any framework-supplied actions remote objects that should be contributed to the popup menu
	 * for the given selection list. This does nothing if this adapter does not implement ISystemViewRemoteElementAdapter,
	 * else it potentially adds menu items for "User Actions" and Compile", for example. It queries the subsystem
	 * factory of the selected objects to determine if these actions are appropriate to add.
	 * 
	 * @param menu The menu to contribute actions to
	 * @param selection The window's current selection.
	 * @param shell of viewer calling this. Most dialogs require a shell.
	 * @param menuGroup recommended menu group to add actions to. If added to another group, you must be sure to create that group first.
	 * @param subsystem the subsystem of the selection
	 */
	public void addCommonRemoteActions(ISubSystemConfiguration factory, SystemMenuManager menu, IStructuredSelection selection, Shell shell, String menuGroup, ISubSystem subsystem);


	// ---------------------------------
	// FILTER POOL METHODS...
	// ---------------------------------

    /**
     * Returns a list of actions for the popup menu when user right clicks on a
     *  filter pool object within a subsystem of this factory.
     * Only supported by subsystems that support filters.
     */
    public IAction[] getFilterPoolActions(ISubSystemConfiguration factory, ISystemFilterPool selectedPool, Shell shell);
 
	// ---------------------------------
	// FILTER POOL REFERENCE METHODS...
	// ---------------------------------
    /**
     * Returns a list of actions for the popup menu when user right clicks on a
     *  filter pool reference object within a subsystem of this factory. Note,
     *  these are added to the list returned by getFilterPoolActions().
     * <p>
     * Only supported by subsystems that support filters.
     */
    public IAction[] getFilterPoolReferenceActions(ISubSystemConfiguration factory, ISystemFilterPoolReference selectedPoolReference, Shell shell);

    
    // ---------------------------------
    // FILTER METHODS
    // ---------------------------------
      /**
     * Returns a list of actions for the popup menu when user right clicks on a
     *  filter object.
     * <p>
     * Only supported and used by subsystems that support filters.
     * <p>
     * Most actions are handled in this base, except if you have your own action for
     * creating a new nested filter. In this case, <b>override getNewFilterAction()</b>
     */
    public IAction[] getFilterActions(ISubSystemConfiguration factory, ISystemFilter selectedFilter, Shell shell);

	/**
	 * In addition to a change filter action, we now also support the same functionality
	 *  via a Properties page for filters. When this page is activated, this method is called
	 *  to enable customization of the page, given the selected filter.
	 */
	public void customizeChangeFilterPropertyPage(ISubSystemConfiguration factory, SystemChangeFilterPropertyPage page, ISystemFilter selectedFilter, Shell shell);
	/**
	 * In addition to a change filter action, we now also support the same functionality
	 *  via a Properties page for filter strings, in the Team View. When this page is activated, 
	 *  this method is called to enable customization of the page, given the selected filter string.
	 */
	public void customizeFilterStringPropertyPage(ISubSystemConfiguration factory, SystemFilterStringPropertyPage page, ISystemFilterString selectedFilterString, Shell shell);
	

    /**
     * Prompt the user to create a new filter as a result of the user expanding a promptable
     * filter.
     * @return the filter created by the user or null if they cancelled the prompting
     */
    public ISystemFilter createFilterByPrompting(ISubSystemConfiguration factory, ISystemFilterReference referenceToPromptableFilter, Shell shell)
        throws Exception;


    // ---------------------------------
    // FILTER REFERENCE METHODS
    // ---------------------------------

    /**
     * Returns a list of actions for the popup menu when user right clicks on a
     *  filter reference object within a subsystem of this factory.
     * Only supported and used by subsystems that support filters.
     * <p>
     * Most actions are handled in this base, except if you have your own action for
     * creating a new filter. In this case, <b>override getNewFilterAction()</b>
     * To add additional actions, override {@link #getFilterReferenceActions(ISystemFilterReference, Shell)}.
     *
     * @param selectedFilterRef the currently selected filter reference
     * @param shell parent shell of viewer where the popup menu is being constructed
     */
    public IAction[] getFilterReferenceActions(ISubSystemConfiguration factory, ISystemFilterReference selectedFilterRef, Shell shell);

    /**
     * Returns a list of actions for the popup menu when user right clicks on a subsystem object from this factory.
     */
    public IAction[] getSubSystemActions(ISubSystemConfiguration factory, ISubSystem selectedSubSystem, Shell shell);


    
    // --------------------------
    // SERVER LAUNCHER METHODS...
    // --------------------------
	/**
	 * Return the form used in the property page, etc for this server launcher.
	 * Only called if {@link #supportsServerLaunchProperties()} returns true.
	 */
	public IServerLauncherForm getServerLauncherForm(ISubSystemConfiguration factory, Shell shell, ISystemMessageLine msgLine);	

    // --------------------------
    // SUBSYSTEM PROPERTY PAGE METHOD...
    // --------------------------
	/*
	 * Return the form used in the subsyste property page
	 */
	public ISystemSubSystemPropertyPageCoreForm getSubSystemPropertyPageCoreFrom(ISubSystemConfiguration factory, ISystemMessageLine msgLine, Object caller);
	
	  /**
     * Return image descriptor for subsystems created by this factory. Comes from icon attribute in extension point xml
     */
    public ImageDescriptor getImage(ISubSystemConfiguration factory);
    /**
     * Return actual graphics Image of this factory.
     * This is the same as calling getImage().createImage() but the resulting image is cached
     */
    public Image getGraphicsImage(ISubSystemConfiguration factory);
    /**
     * Return image to use when this susystem is connection. Comes from icon attribute in extension point xml
     */
    public ImageDescriptor getLiveImage(ISubSystemConfiguration factory);
    /**
     * Return actual graphics LiveImage of this factory.
     * This is the same as calling getLiveImage().createImage() but the resulting image is cached
     */
    public Image getGraphicsLiveImage(ISubSystemConfiguration factory);

	/**
	 * Supply the image to be used for filter pool managers, within actions.
	 * REQUIRED BY SYSTEMFILTERPOOLMANAGERPROVIDER INTERFACE
	 */
	public ImageDescriptor getSystemFilterPoolManagerImage();
	/**
	 * Supply the image to be used for filter pools, within actions.
	 * REQUIRED BY SYSTEMFILTERPOOLMANAGERPROVIDER INTERFACE
	 */
	public ImageDescriptor getSystemFilterPoolImage(ISystemFilterPool filterPool);
	/**
	 * Supply the image to be used for filters, within actions.
	 * REQUIRED BY SYSTEMFILTERPOOLMANAGERPROVIDER INTERFACE
	 */
	public ImageDescriptor getSystemFilterImage(ISystemFilter filter);
	/*
	 * Supply the image to be used for the given filter string, within actions.
	 * REQUIRED BY SYSTEMFILTERPOOLMANAGERPROVIDER INTERFACE
	 */
	public ImageDescriptor getSystemFilterStringImage(ISystemFilterString filterString);
	
	/*
	 * Supply the image to be used for the given filter string string, within actions.
	 * REQUIRED BY SYSTEMFILTERPOOLMANAGERPROVIDER INTERFACE
	 */
	public ImageDescriptor getSystemFilterStringImage(String filterStringString);
	
	public void renameSubSystemProfile(ISubSystemConfiguration factory, String oldProfileName, String newProfileName);
}
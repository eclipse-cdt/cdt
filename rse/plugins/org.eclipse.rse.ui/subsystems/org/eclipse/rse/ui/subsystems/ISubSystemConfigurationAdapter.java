/********************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [186748] Move from UI/org.eclipse.rse.core.subsystems.util
 * Martin Oberhuber (Wind River) - [189123] Move renameSubSystemProfile() from UI to Core
 * David Dykstal (IBM) - [168976][api] move ISystemNewConnectionWizardPage from core to UI
 ********************************************************************************/

package org.eclipse.rse.ui.subsystems;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolReference;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.filters.ISystemFilterString;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISubSystemConfigurator;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.filters.actions.SystemNewFilterAction;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.propertypages.ISystemSubSystemPropertyPageCoreForm;
import org.eclipse.rse.ui.propertypages.SystemChangeFilterPropertyPage;
import org.eclipse.rse.ui.propertypages.SystemFilterStringPropertyPage;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.view.IContextObject;
import org.eclipse.rse.ui.widgets.IServerLauncherForm;
import org.eclipse.rse.ui.wizards.newconnection.ISystemNewConnectionWizardPage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PropertyPage;

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
	public void configureNewFilterAction(ISubSystemConfiguration config, SystemNewFilterAction newFilterAction, Object callerData);
	

	// -----------------------------------
	// WIZARD PAGE CONTRIBUTION METHODS... 
	// -----------------------------------
	/**
	 * Optionally return one or more wizard pages to append to the New Connection Wizard if
	 * the user selects a system type that this subsystem configuration supports.
	 * <p>
	 * Some details:
	 * <ul>
	 * <li>The wizard pages must implement ISystemNewConnectionWizardPage, so as to fit into the wizard's framework
	 * <li>When the user successfully presses Finish, the createConnection method in the SystemRegistry will call 
	 * your {@link ISubSystemConfiguration#createSubSystem(IHost,boolean, ISubSystemConfigurator[])} method to create the 
	 * your subsystem for the connection. The same pages you return here are passed back to you so you can 
	 * interrogate them for the user-entered data and use it when creating the default subsystem instance.
	 * </ul>
	 * Tip: consider extending {@link org.eclipse.rse.ui.wizards.AbstractSystemNewConnectionWizardPage} for your wizard page class.
	 * @since 3.0
	 */
	public ISystemNewConnectionWizardPage[] getNewConnectionWizardPages(ISubSystemConfiguration config, IWizard wizard);

	/**
	 * Returns any framework-supplied actions remote objects that should be contributed to the popup menu
	 * for the given selection list. This does nothing if this adapter does not implement ISystemViewRemoteElementAdapter,
	 * else it potentially adds menu items for "User Actions" and Compile", for example. It queries the subsystem
	 * configuration of the selected objects to determine if these actions are appropriate to add.
	 * 
	 * @param config The subsystem configuration to work on
	 * @param menu The menu to contribute actions to
	 * @param selection The window's current selection.
	 * @param shell of viewer calling this. Most dialogs require a shell.
	 * @param menuGroup recommended menu group to add actions to. If added to another group, you must be sure to create that group first.
	 * @param subsystem the subsystem of the selection
	 */
	public void addCommonRemoteActions(ISubSystemConfiguration config, SystemMenuManager menu, IStructuredSelection selection, Shell shell, String menuGroup, ISubSystem subsystem);


	// ---------------------------------
	// FILTER POOL METHODS...
	// ---------------------------------

    /**
     * Returns a list of actions for the popup menu when user right clicks on a
     *  filter pool object within a subsystem of this subsystem configuration.
     * Only supported by subsystems that support filters.
     */
    public IAction[] getFilterPoolActions(SystemMenuManager menu, IStructuredSelection selection, Shell shell, String menuGroup, ISubSystemConfiguration config, ISystemFilterPool selectedPool);

    
	// ---------------------------------
	// FILTER POOL REFERENCE METHODS...
	// ---------------------------------
    /**
     * Returns a list of actions for the popup menu when user right clicks on a
     * filter pool reference object within a subsystem of this subsystem
     * configuration. Note, these are added to the list returned by 
     * getFilterPoolActions().
     * <p>
     * Only supported by subsystems that support filters.
     */
    public IAction[] getFilterPoolReferenceActions(SystemMenuManager menu, IStructuredSelection selection, Shell shell, String menuGroup, ISubSystemConfiguration config, ISystemFilterPoolReference selectedPoolReference);

    
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
    public IAction[] getFilterActions(SystemMenuManager menu, IStructuredSelection selection, Shell shell, String menuGroup, ISubSystemConfiguration config, ISystemFilter selectedFilter);

	/**
	 * In addition to a change filter action, we now also support the same functionality
	 *  via a Properties page for filters. When this page is activated, this method is called
	 *  to enable customization of the page, given the selected filter.
	 */
	public void customizeChangeFilterPropertyPage(ISubSystemConfiguration config, SystemChangeFilterPropertyPage page, ISystemFilter selectedFilter, Shell shell);

	/**
	 * In addition to a change filter action, we now also support the same functionality
	 *  via a Properties page for filter strings, in the Team View. When this page is activated, 
	 *  this method is called to enable customization of the page, given the selected filter string.
	 */
	public void customizeFilterStringPropertyPage(ISubSystemConfiguration config, SystemFilterStringPropertyPage page, ISystemFilterString selectedFilterString, Shell shell);
	
    /**
     * Prompt the user to create a new filter as a result of the user expanding a promptable
     * filter.
     * @return the filter created by the user or null if they cancelled the prompting
     */
    public ISystemFilter createFilterByPrompting(ISubSystemConfiguration config, ISystemFilterReference referenceToPromptableFilter, Shell shell)
        throws Exception;

    
    // ---------------------------------
    // FILTER REFERENCE METHODS
    // ---------------------------------

    /**
     * Returns a list of actions for the popup menu when user right clicks on a
     *  filter reference object within a subsystem of this subsystem configuration.
     * Only supported and used by subsystems that support filters.
     * <p>
     * Most actions are handled in this base, except if you have your own action for
     * creating a new filter. In this case, <b>override getNewFilterAction()</b>
     * To add additional actions, override {@link #getFilterReferenceActions(SystemMenuManager, IStructuredSelection, Shell, String, ISubSystemConfiguration, ISystemFilterReference)}.
     *
     * @param selectedFilterRef the currently selected filter reference
     * @param shell parent shell of viewer where the popup menu is being constructed
     */
    public IAction[] getFilterReferenceActions(SystemMenuManager menu, IStructuredSelection selection, Shell shell, String menuGroup, ISubSystemConfiguration config, ISystemFilterReference selectedFilterRef);

    /**
     * Returns a list of actions for the popup menu when user right clicks on a
     * subsystem object from this subsystem configuration.
     */
    public IAction[] getSubSystemActions(SystemMenuManager menu, IStructuredSelection selection, Shell shell, String menuGroup, ISubSystemConfiguration config, ISubSystem selectedSubSystem);

    
    // --------------------------
    // SERVER LAUNCHER METHODS...
    // --------------------------

    /**
	 * Return the form used in the property page, etc for this server launcher.
	 * Only called if {@link ISubSystemConfiguration#supportsServerLaunchProperties(IHost)} returns true.
	 */
	public IServerLauncherForm getServerLauncherForm(ISubSystemConfiguration config, Shell shell, ISystemMessageLine msgLine);	

	
    // --------------------------
    // SUBSYSTEM PROPERTY PAGE METHOD...
    // --------------------------
	/**
	 * Return the form used in the subsystem property page.
	 * @return the form used in the subsystem property page.
	 */
	public ISystemSubSystemPropertyPageCoreForm getSubSystemPropertyPageCoreFrom(ISubSystemConfiguration config, ISystemMessageLine msgLine, Object caller);
	
	/**
     * Return image descriptor for subsystems created by this 
     * subsystem configuration.
     * Comes from icon attribute in extension point xml.
     * @param config the subsystem configuration
     * @return the image descriptor for the given subsystem configuration.
     */
    public ImageDescriptor getImage(ISubSystemConfiguration config);

    /**
     * Return actual graphics Image of this subsystem configuration.
     * This is the same as calling getImage().createImage(),
     * but the resulting image is cached.
     * @param config the subsystem configuration
     * @return the cached image for the given subsystem configuration.
     */
    public Image getGraphicsImage(ISubSystemConfiguration config);

    /**
     * Return image to use when this subsystem is connected.
     * Comes from icon attribute in extension point xml.
     * @param config the subsystem configuration
     * @return the image descriptor for the given subsystem configuration.
     */
    public ImageDescriptor getLiveImage(ISubSystemConfiguration config);

    /**
     * Return actual graphics LiveImage of this subsystem configuration.
     * This is the same as calling getLiveImage().createImage(),
     * but the resulting image is cached.
     * @param config the subsystem configuration
     * @return the cached image for the given subsystem configuration.
     */
    public Image getGraphicsLiveImage(ISubSystemConfiguration config);

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
	
	/**
	 * Supply the image to be used for filter pool references
	 */
	public ImageDescriptor getSystemFilterPoolImage(ISystemFilterPoolReference filterPool);

	/**
	 * Supply the image to be used for filter references
	 */
	public ImageDescriptor getSystemFilterImage(ISystemFilterReference filter);
	
	/**
	 * Supply the image to be used for the given filter string, within actions.
	 * REQUIRED BY SYSTEMFILTERPOOLMANAGERPROVIDER INTERFACE
	 */
	public ImageDescriptor getSystemFilterStringImage(ISystemFilterString filterString);
	
	/**
	 * Supply the image to be used for the given filter string string, within actions.
	 * REQUIRED BY SYSTEMFILTERPOOLMANAGERPROVIDER INTERFACE
	 */
	public ImageDescriptor getSystemFilterStringImage(String filterStringString);
	
	/**
	 * Return the single property page to show in the tabbed notebook for the
	 *  for SubSystem property of the parent Connection. Return null if no 
	 *  page is to be contributed for this. You are limited to a single page,
	 *  so you may have to compress. It is recommended you prompt for the port
	 *  if applicable since the common base subsystem property page is not shown
	 *  To help with this you can use the SystemPortPrompt widget.
	 */
   public PropertyPage getPropertyPage(ISubSystem subsystem, Composite parent);
   
	/**
	 * Return the validator for the userId.
	 * A default is supplied.
	 * Note this is only used for the subsystem's properties, so will not
	 * be used by the connection's default. Thus, is only of limited value.
	 * <p>
	 * This must be castable to ICellEditorValidator for the property sheet support.
	 */
	public ISystemValidator getUserIdValidator(ISubSystemConfiguration config);

	/**
	 * Return the validator for the password which is prompted for at runtime.
	 * No default is supplied.
	 */
	public ISystemValidator getPasswordValidator(ISubSystemConfiguration confi);

	/**
	 * Return the validator for the port.
	 * A default is supplied.
	 * This must be castable to ICellEditorValidator for the property sheet support.
	 */
	public ISystemValidator getPortValidator(ISubSystemConfiguration confi);
	
	/**
	 * Filters an array of children and returns the results. The default implementation does not filter
	 * and simply returns the children passed in. Subclasses should override if they want to filter objects.
	 * @param parent the parent context.
	 * @param children the children to filter.
	 * @return the children after filtering.
	 */
	public Object[] applyViewFilters(IContextObject parent, Object[] children);
}
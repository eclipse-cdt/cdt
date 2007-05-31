/********************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - Replace SystemRegistry by ISystemRegistry
 * Martin Oberhuber (Wind River) - [190231] Remove UI-only code from SubSystemConfiguration
 ********************************************************************************/

package org.eclipse.rse.core.servicesubsystem;

import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.model.ISystemNewConnectionWizardPage;
import org.eclipse.rse.core.subsystems.AbstractConnectorService;
import org.eclipse.rse.core.subsystems.AbstractConnectorServiceManager;
import org.eclipse.rse.core.subsystems.IServiceSubSystemConfiguration;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.SubSystemConfiguration;

/**
 * This class is to be used by subsystem-providers that do not desire to use MOF/EMF. It is 
 * therefore recommended starting base class for subsystem providers. 
 * <br>
 * To use this class, simply subclass it and override the appropriate methods in it, such as:</p>
 * <ul>
 *    <li>any of the supportsXXX() configuration methods you wish to change.
 *    <li>{@link #createSubSystemInternal(org.eclipse.rse.core.model.IHost)}, to instantiate your subsystem class.
 * </ul>
 * <p>
 * For additional customization of the subsystem, you may supply a {@link org.eclipse.rse.ui.view.SubSystemConfigurationAdapter},
 * which allows you to
 *    <li>supply your own New->Filter popup menu action via {@link org.eclipse.rse.ui.view.SubSystemConfigurationAdapter#getNewFilterPoolFilterAction(ISubSystemConfiguration, ISystemFilterPool, org.eclipse.swt.widgets.Shell)}, and 
 *    <li>supply your own Change Filter popup menu action via {@link org.eclipse.rse.ui.view.SubSystemConfigurationAdapter#getChangeFilterAction(ISubSystemConfiguration, ISystemFilter, org.eclipse.swt.widgets.Shell)}.
 * </ul>
 * <p>
 * This class is typically used together with:</p>
 * <ul>
 *   <li>{@link org.eclipse.rse.core.servicesubsystem.ServiceSubSystem} for the subsystem
 *   <li>{@link AbstractConnectorService} for the connector service
 *   <li>{@link AbstractConnectorServiceManager} for the connector service manager
 *   <li>{@link org.eclipse.rse.core.subsystems.AbstractResource} for the individual remote resources
 * </ul>
 * <p>
 * In general, for what methods to override, only worry about the non-generated methods in
 * this class, and ignore the hundreds in {@link org.eclipse.rse.core.subsystems.SubSystemConfiguration}
 * 
 * @see org.eclipse.rse.core.servicesubsystem.ServiceSubSystem
 * @see AbstractConnectorService
 * @see AbstractConnectorServiceManager
 */

public abstract class ServiceSubSystemConfiguration extends SubSystemConfiguration implements IServiceSubSystemConfiguration  
{

	protected ServiceSubSystemConfiguration()
	{
		super();
	}

	// ------------------------------------------------------
	// CONFIGURATION METHODS THAT ARE OVERRIDDEN FROM PARENT
	// WE ASSUME TYPICAL DEFAULTS, BUT CHILDREN CAN OVERRIDE
	// ------------------------------------------------------
	/**
	 * <i>Overridable configuration method. Default is <b>false</b></i><br>
	 * Return true if instance of this subsystem configuration's subsystems support connect and disconnect actions.
	 */
	public boolean supportsSubSystemConnect()
	{
		return true;
	}

    /**
	 * <i>Overridable configuration method. Default is <b>true</b></i><br>
     * Return true (default) or false to indicate if subsystems of this subsystem configuration support user-editable
     *  port numbers.
     */
    public boolean isPortEditable()
    {
    	return true;
    }
    /**
	 * <i>Overridable configuration method. Default is <b>true</b></i><br>
     * Required method for subsystem configuration child classes. Return true if you support filters, false otherwise.
     * If you support filters, then some housekeeping will be done for you automatically. Specifically, they
     * will be saved and restored for you automatically.
     */
    public boolean supportsFilters()
    {
    	return true;
    }
    /**
	 * <i>Overridable configuration method. Default is <b>false</b></i><br>
     * Do we allow filters within filters?
     */
    public boolean supportsNestedFilters()
    {
    	return false;
    }
	/**
	 * <i>COverridable configuration method. Default is <b>false</b></i><br>
	 * Return true if you support user-defined actions for the remote system objects returned from expansion of
	 *  subsystems created by this subsystem configuration
	 */
	public boolean supportsUserDefinedActions()
	{
		return false;
	}
	/**
	 * <i>Overridable configuration method. Default is <b>false</b></i><br>
	 * Return true if you support user-defined/managed named file types
	 */
	public boolean supportsFileTypes()
	{
		return false;
	}
	/**
	 * <i>Overridable configuration method. Default is <b>false</b></i><br>
	 * Tell us if filter strings are case sensitive. 
	 */
	public boolean isCaseSensitive()
	{
		return false;
	}
	/**
	 * <i>Overridable configuration method. Default is <b>false</b></i><br>
	 * Tell us if duplicate filter strings are supported per filter.
	 */
	public boolean supportsDuplicateFilterStrings()
	{
		return false;
	}
	
	// ------------------------------------------------
	// FRAMEWORKD METHODS TO BE OVERRIDDEN IF APPROPRIATE.
	// THESE ARE CALLED BY OUR OWN PARENT
	// ------------------------------------------------

    /**
	 * <i>Overridable lifecycle method. Not typically overridden.</i><br>
     * After a new subsystem instance is created, the framework calls this method
     * to initialize it. This is your opportunity to set default attribute values.
     * 
     * <p>The reason for the connect wizard pages parameter is in case your subsystem configuration contributes a page to that wizard,
     * whose values are needed to set the subsystem's initial state. For example, you might decide to add a 
     * page to the connection wizard to prompt for a JDBC Driver name. If so, when this method is called at 
     * the time a new connection is created after the wizard, your page will have the user's value. You can
     * thus use it here to initialize that subsystem property. Be use to use instanceof to find your particular
     * page. 
     * </p>
     * 
     * <p>
     * If you override this, <i>PLEASE CALL SUPER TO DO DEFAULT INITIALIZATION!</i>
     * 
     * @param subsys - The subsystem that was created via createSubSystemInternal
     * @param yourNewConnectionWizardPages - The wizard pages you supplied to the New Connection wizard, via the
     *            {@link org.eclipse.rse.ui.view.SubSystemConfigurationAdapter#getNewConnectionWizardPages(org.eclipse.rse.core.subsystems.ISubSystemConfiguration, org.eclipse.jface.wizard.IWizard)} 
     *            method or null if you didn't override this method.
     *            Note there may be more pages than you originally supplied, as you are passed all pages contributed
     *            by this subsystem configuration object, including subclasses. Null on a clone operation.
     * 
     * @see org.eclipse.rse.ui.view.SubSystemConfigurationAdapter#getNewConnectionWizardPages(org.eclipse.rse.core.subsystems.ISubSystemConfiguration, org.eclipse.jface.wizard.IWizard)
     */
    protected void initializeSubSystem(ISubSystem subsys,ISystemNewConnectionWizardPage[] yourNewConnectionWizardPages)
    {
    	super.initializeSubSystem(subsys, yourNewConnectionWizardPages);
    }
    
	// --------------------------------
	// METHODS FOR SUPPLYING ACTIONS...
	// --------------------------------

    /**
     * <i>Optionally overridable method affecting the visual display of objects within subsystems created by this subsystem configuration.</i><br>
     * Return the translated string to show in the property sheet for the "type" property, for the selected
     *  filter. This method is only called for filters within subsystems created by this subsystem configuration.
     * <p>
     * Returns a default string, override if appropriate.
     */
    public String getTranslatedFilterTypeProperty(ISystemFilter selectedFilter)
    {
    	return super.getTranslatedFilterTypeProperty(selectedFilter);
    }

} 
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
 * Martin Oberhuber (Wind River) - [186748] Move ISubSystemConfigurationAdapter from UI/rse.core.subsystems.util
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber (Wind River) - [189123] Move renameSubSystemProfile() from UI to Core
 * Martin Oberhuber (Wind River) - [190231] Remove UI-only code from SubSystemConfiguration
 * Martin Oberhuber (Wind River) - [174789] [performance] Don't contribute Property Pages to Wizard automatically
 * David McKnight   (IBM)        - [197129] Removing obsolete  ISystemConnectionWizardPropertyPage and SystemSubSystemsPropertiesWizardPage
 * David Dykstal    (IBM)        - [217556] remove service subsystem types
 * David McKnight   (IBM)        - [225506] [api][breaking] RSE UI leaks non-API types
 * David Dykstal (IBM) - [168976][api] move ISystemNewConnectionWizardPage from core to UI
 ********************************************************************************/

package org.eclipse.rse.ui.view;

import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolManager;
import org.eclipse.rse.core.filters.ISystemFilterPoolReference;
import org.eclipse.rse.core.filters.ISystemFilterPoolReferenceManager;
import org.eclipse.rse.core.filters.ISystemFilterPoolWrapperInformation;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.filters.ISystemFilterString;
import org.eclipse.rse.core.model.ISubSystemConfigurator;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.ISubSystemConfigurationProxy;
import org.eclipse.rse.core.subsystems.SubSystem;
import org.eclipse.rse.core.subsystems.SubSystemConfiguration;
import org.eclipse.rse.internal.core.filters.SystemFilterPoolWrapperInformation;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.internal.ui.actions.SystemClearPasswordAction;
import org.eclipse.rse.internal.ui.actions.SystemConnectAction;
import org.eclipse.rse.internal.ui.actions.SystemDisconnectAction;
import org.eclipse.rse.internal.ui.actions.SystemFilterCascadingNewFilterPoolReferenceAction;
import org.eclipse.rse.internal.ui.actions.SystemFilterCopyFilterAction;
import org.eclipse.rse.internal.ui.actions.SystemFilterCopyFilterPoolAction;
import org.eclipse.rse.internal.ui.actions.SystemFilterMoveDownFilterAction;
import org.eclipse.rse.internal.ui.actions.SystemFilterMoveDownFilterPoolReferenceAction;
import org.eclipse.rse.internal.ui.actions.SystemFilterMoveFilterAction;
import org.eclipse.rse.internal.ui.actions.SystemFilterMoveFilterPoolAction;
import org.eclipse.rse.internal.ui.actions.SystemFilterMoveUpFilterAction;
import org.eclipse.rse.internal.ui.actions.SystemFilterMoveUpFilterPoolReferenceAction;
import org.eclipse.rse.internal.ui.actions.SystemFilterNewFilterPoolAction;
import org.eclipse.rse.internal.ui.actions.SystemFilterRemoveFilterPoolReferenceAction;
import org.eclipse.rse.internal.ui.actions.SystemFilterSelectFilterPoolsAction;
import org.eclipse.rse.internal.ui.actions.SystemFilterWorkWithFilterPoolsAction;
import org.eclipse.rse.internal.ui.subsystems.SubSystemConfigurationProxyAdapter;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.actions.ISystemAction;
import org.eclipse.rse.ui.actions.SystemPasteFromClipboardAction;
import org.eclipse.rse.ui.filters.actions.ISystemNewFilterActionConfigurator;
import org.eclipse.rse.ui.filters.actions.SystemChangeFilterAction;
import org.eclipse.rse.ui.filters.actions.SystemFilterAbstractFilterPoolAction;
import org.eclipse.rse.ui.filters.actions.SystemNewFilterAction;
import org.eclipse.rse.ui.filters.dialogs.SystemChangeFilterDialog;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.propertypages.ISystemSubSystemPropertyPageCoreForm;
import org.eclipse.rse.ui.propertypages.SystemChangeFilterPropertyPage;
import org.eclipse.rse.ui.propertypages.SystemFilterStringPropertyPage;
import org.eclipse.rse.ui.propertypages.SystemSubSystemPropertyPageCoreForm;
import org.eclipse.rse.ui.subsystems.ISubSystemConfigurationAdapter;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.validators.ValidatorPortInput;
import org.eclipse.rse.ui.validators.ValidatorSpecialChar;
import org.eclipse.rse.ui.widgets.IServerLauncherForm;
import org.eclipse.rse.ui.widgets.RemoteServerLauncherForm;
import org.eclipse.rse.ui.wizards.SubSystemServiceWizardPage;
import org.eclipse.rse.ui.wizards.newconnection.ISystemNewConnectionWizardPage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PropertyPage;


public class SubSystemConfigurationAdapter implements ISubSystemConfigurationAdapter, ISystemNewFilterActionConfigurator
{
	protected Hashtable imageTable = null;

	// actions stuff...
	private IAction[] subSystemActions = null;
	private IAction[] filterPoolActions = null;
	private IAction[] filterPoolReferenceActions = null;
	private IAction[] filterActions = null;
	public SubSystemConfigurationAdapter()
	{
	}


		/**
		 * Returns any framework-supplied actions remote objects that should be contributed to the popup menu
		 * for the given selection list. This does nothing if this adapter does not implement ISystemRemoteElementAdapter,
		 * else it potentially adds menu items for "User Actions" and Compile", for example. It queries the subsystem
		 * configuration of the selected objects to determine if these actions are appropriate to add.
		 *
		 * <p>
		 * No need to override.
		 *
		 * @param menu The menu to contribute actions to
		 * @param selection The window's current selection.
		 * @param shell of viewer calling this. Most dialogs require a shell.
		 * @param menuGroup recommended menu group to add actions to. If added to another group, you must be sure to create that group first.
		 * @param subsystem the subsystem of the selection
		 */
		public void addCommonRemoteActions(ISubSystemConfiguration config, SystemMenuManager menu, IStructuredSelection selection, Shell shell, String menuGroup, ISubSystem subsystem)
		{
			/** FIXME - UDAs should not be coupled to subsystem configuration adapter
			SystemCompileManager mgr = config.getCompileManager();

			if (config.supportsCompileActions() && (mgr != null))
			{
				int size = selection.size();

				// for single selections, we try to avoid iterator, to hopefully make it a bit faster
				if (size == 1)
				{
					if (mgr.isCompilable(selection.getFirstElement()))
					{ // check that selection is compilable
						mgr.addCompileActions(shell, selection, menu, menuGroup);
					}
				}
				else if (size > 1)
				{
					Iterator iter = selection.iterator();

					boolean allCompilable = true;

					// check that all selections are compilable
					while (iter.hasNext())
					{
						Object element = iter.next();
						allCompilable = mgr.isCompilable(element);

						if (!allCompilable)
						{
							break;
						}
					}

					if (allCompilable)
					{
						mgr.addCompileActions(shell, selection, menu, menuGroup);
					}
				}
			}

			if (config.supportsUserDefinedActions() && config.supportsUserDefinedActions(selection))
			{
				addUserDefinedActions(config, shell, selection, menu, menuGroup, getActionSubSystem(config, subsystem));
			}
			**/
		}

		// -----------------------------------
		// WIZARD PAGE CONTRIBUTION METHODS... (UCD defect 43194)
		// -----------------------------------
		/**
		 * Optionally return one or more wizard pages to append to the New Wizard connection if
		 *  the user selects a system type that this subsystem configuration supports.
		 * <p>
		 * Some details:
		 * <ul>
		 *   <li>The wizard pages must implement ISystemNewConnectionWizardPage, so as to fit into the wizard's framework
		 *   <li>When the user successfully presses Finish, the createConnection method in the SystemRegistry will call 
		 *        your {@link SubSystemConfiguration#createSubSystem(org.eclipse.rse.core.model.IHost,boolean, ISubSystemConfigurator[])} method to create the 
		 *        your subsystem for the connection. The same pages you return here are passed back to you so you can 
		 *        interrogate them for the user-entered data and use it when creating the default subsystem instance.
		 * </ul>
		 * Tip: consider extending {@link org.eclipse.rse.ui.wizards.AbstractSystemNewConnectionWizardPage} for your wizard page class.
		 * 
		 * @since 3.0 ISystemNewConnectionWizardPage moved from Core to UI
		 */
		public ISystemNewConnectionWizardPage[] getNewConnectionWizardPages(ISubSystemConfiguration config, IWizard wizard)
		{
			if (config.getServiceType() != null)
			{
				SubSystemServiceWizardPage page = new SubSystemServiceWizardPage(wizard, config);
				return new ISystemNewConnectionWizardPage[] {page};
			}
			//MOB Removed due to performance issue -- see Eclipse Bugzilla bug 174789
//			else
//			{
//				List pages = getSubSystemPropertyPages(config);
//				if (pages != null && pages.size() > 0)
//				{
//					SystemSubSystemsPropertiesWizardPage page = new SystemSubSystemsPropertiesWizardPage(wizard, config, pages);
//					return new ISystemNewConnectionWizardPage[] {page};
//				}
//			}
			return new ISystemNewConnectionWizardPage[0];
		}

		/*
		 * Return the form used in the subsystem property page.  This default implementation returns Syste
		 */
		public ISystemSubSystemPropertyPageCoreForm getSubSystemPropertyPageCoreFrom(ISubSystemConfiguration config, ISystemMessageLine msgLine, Object caller)
		{
		    return new SystemSubSystemPropertyPageCoreForm(msgLine, caller);
		}



		// FIXME - UDAs no longer coupled with config in core
//		// ---------------------------------
//		// USER-DEFINED ACTIONS METHODS...
//		// ---------------------------------
//
//		/**
//		 * Get the action subsystem object for this subsystemconfiguration,
//		 * and set its current subsystem to the given subsystem instance.
//		 * Will ensure the user action subsystem is only ever instantiated once.
//		 * <p>
//		 * Called in the Work With User Actions and the User Actions cascading action.
//		 * <p>
//		 * Do not override this, as the implementation is complete. However,
//		 *  you must override createActionSubSystem.
//		 *
//		 * @see #supportsUserDefinedActions()
//		 * @see #createActionSubSystem()
//		 */
//		public SystemUDActionSubsystem getActionSubSystem(ISubSystemConfiguration config, ISubSystem subsystem)
//		{
//			if (udas == null)
//				udas = createActionSubSystem(config);
//			if (udas != null)
//			{
//				udas.setSubsystem(subsystem);
//				udas.setSubSystemConfiguration(config);
//			}
//			return udas;
//		}
//
//		/**
//		 * Overridable method to instantiate the SystemUDActionSubsystem.
//		 * You must override this if you return true to supportsUserActions.
//		 *
//		 * @see #supportsUserDefinedActions()
//		 * @see #getActionSubSystem(ISubSystem)
//		 */
//		protected SystemUDActionSubsystem createActionSubSystem(ISubSystemConfiguration config)
//		{
//			return null;
//		}
//
//		/**
//		 * Populate main context menu with a "User Actions->"  submenu cascade,
//		 *  which will only be populated when the submenu is selected.
//		 * <p>
//		 * This is called by the addCommonRemoteObjectsActions method, if this subsystem
//		 *  supports user defined actions.
//		 */
//		public static void addUserDefinedActions(ISubSystemConfiguration config, Shell shell, IStructuredSelection selection, SystemMenuManager menu, String menuGroup, SystemUDActionSubsystem userActionSubSystem)
//		{
//			SystemUDACascadeAction act = new SystemUDACascadeAction(userActionSubSystem, selection);
//			menu.add(menuGroup, act);
//		}


		// ---------------------------------
		// COMPILE ACTIONS METHODS...
		// ---------------------------------

		// ---------------------------------
		// USER-PREFERENCE METHODS...
		// ---------------------------------


		// ---------------------------------
		// PROXY METHODS. USED INTERNALLY...
		// ---------------------------------


		// ---------------------------------
		// FACTORY ATTRIBUTE METHODS...
		// ---------------------------------

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.rse.ui.subsystems.ISubSystemConfigurationAdapter#getImage(org.eclipse.rse.core.subsystems.ISubSystemConfiguration)
		 */
		public ImageDescriptor getImage(ISubSystemConfiguration config)
		{
			ISubSystemConfigurationProxy proxy = config.getSubSystemConfigurationProxy();
			Object adapterCandidate = Platform.getAdapterManager().getAdapter(proxy, SubSystemConfigurationProxyAdapter.class);
			SubSystemConfigurationProxyAdapter adapter = (SubSystemConfigurationProxyAdapter) adapterCandidate;
			ImageDescriptor result = adapter.getImageDescriptor();
			return result;
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.rse.ui.subsystems.ISubSystemConfigurationAdapter#getGraphicsImage(org.eclipse.rse.core.subsystems.ISubSystemConfiguration)
		 */
		public Image getGraphicsImage(ISubSystemConfiguration config)
		{
			ImageDescriptor id = getImage(config);
			if (id != null)
			{
				Image image = null;
				if (imageTable == null)
					imageTable = new Hashtable();
				else
					image = (Image) imageTable.get(id);
				if (image == null)
				{
					image = id.createImage();
					imageTable.put(id, image);
				}
				return image;
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.rse.ui.subsystems.ISubSystemConfigurationAdapter#getLiveImage(org.eclipse.rse.core.subsystems.ISubSystemConfiguration)
		 */
		public ImageDescriptor getLiveImage(ISubSystemConfiguration config)
		{
			ISubSystemConfigurationProxy proxy = config.getSubSystemConfigurationProxy();
			Object adapterCandidate = Platform.getAdapterManager().getAdapter(proxy, SubSystemConfigurationProxyAdapter.class);
			SubSystemConfigurationProxyAdapter adapter = (SubSystemConfigurationProxyAdapter) adapterCandidate;
			ImageDescriptor result = adapter.getLiveImageDescriptor();
			return result;
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.rse.ui.subsystems.ISubSystemConfigurationAdapter#getGraphicsLiveImage(org.eclipse.rse.core.subsystems.ISubSystemConfiguration)
		 */
		public Image getGraphicsLiveImage(ISubSystemConfiguration config)
		{
			ImageDescriptor id = getLiveImage(config);
			if (id != null)
			{
				Image image = null;
				if (imageTable == null)
					imageTable = new Hashtable();
				else
					image = (Image) imageTable.get(id);
				if (image == null)
				{
					image = id.createImage();
					imageTable.put(id, image);
				}
				return image;
			}
			return null;
		}


		// ---------------------------------
		// PROFILE METHODS...
		// ---------------------------------

		// private methods...


		// ---------------------------------
		// SUBSYSTEM METHODS...
		// ---------------------------------


		/**
		 * Returns a list of actions for the popup menu when user right clicks on a subsystem object from this subsystem configuration.
		 * <p>
		 * Override if additional actions needs to be contributed.
		 * <p>
		 * @see #getSubSystemNewFilterPoolActions(SystemMenuManager, IStructuredSelection, Shell, String, ISubSystemConfiguration, ISubSystem)
		 *
		 * @param selectedSubSystem the currently selected subsystem
		 * @param shell The Shell of the view where this action was launched from
		 * @return array of IAction objects to contribute to the popup menu
		 */
    public IAction[] getSubSystemActions(SystemMenuManager menu, IStructuredSelection selection, Shell shell, String menuGroup, ISubSystemConfiguration config, ISubSystem selectedSubSystem)
		{
			Vector childActions = new Vector();
			if (config.supportsFilters())
			{
				boolean showFilterPools = config.showFilterPools();
				// if showing filter pools, we have to add a "new filter pool" action here...
				if (showFilterPools)
				{
					IAction[] newFPActions = getSubSystemNewFilterPoolActions(menu, selection, shell, menuGroup, config, selectedSubSystem);
					if (newFPActions != null)
					{
						for (int idx = 0; idx < newFPActions.length; idx++)
						{
							// special case handling...
							// set input subsystem for new filter pool actions...
							if (newFPActions[idx] instanceof SystemFilterAbstractFilterPoolAction)
							{
								SystemFilterAbstractFilterPoolAction fpAction = (SystemFilterAbstractFilterPoolAction) newFPActions[idx];
								fpAction.setFilterPoolManagerNamePreSelection(selectedSubSystem.getSystemProfile().getName());
								fpAction.setFilterPoolManagerProvider(config);
							}
							childActions.addElement(newFPActions[idx]);
						} // end for loop
					} // end if newFPActions != null
				} // and if showFilterPools
				// if showing filter pools, we have to add a "select filter pool and work-with filter pools" actions here...
				if (showFilterPools)
				{
					childActions.addElement(new SystemFilterSelectFilterPoolsAction(shell));
					childActions.addElement(new SystemFilterWorkWithFilterPoolsAction(shell));
				} // end if showFilterPools
				// if not showing filter pools, we have to add a "new filter" action here...
				if (!showFilterPools)
				{
					IAction[] newFilterActions = getNewFilterPoolFilterActions(menu, selection, shell, menuGroup, config, null);
					if ((newFilterActions != null) && (newFilterActions.length > 0))
					{
						// pre-scan for legacy
						for (int idx = 0; idx < newFilterActions.length; idx++)
						{
							if (newFilterActions[idx] instanceof SystemNewFilterAction)
								 ((SystemNewFilterAction) newFilterActions[idx]).setCallBackConfigurator(this, selectedSubSystem);
							else
							{
							}
						}
						// now add the actions
						for (int idx = 0; idx < newFilterActions.length; idx++)
							childActions.addElement(newFilterActions[idx]);
					} // end if newFilterActions != null
				} // end if !showFilterPools
			} // end if supportsFilters()

// FIXME - UDAs moved out of here
//			// if user defined actions are supported, add an action to work with them...
//			if (config.supportsUserDefinedActions())
//				childActions.addElement(new SystemWorkWithUDAsAction(shell, (ISubSystem)null));
//			// if named file types are supported, add an action to work with them...
//			if (config.supportsFileTypes())
//				childActions.addElement(new SystemWorkWithFileTypesAction(shell, null));
//			// if compile actions are supported, add an action to work with them...
//			if (config.supportsCompileActions())
//				childActions.addElement(new SystemWorkWithCompileCommandsAction(shell, false));



			if (config.supportsSubSystemConnect())
			{
			    // MJB: RE defect 40854
				addConnectOrDisconnectAction(childActions, shell, selectedSubSystem);
			}

			if (subSystemActions == null)
			{
				// premise: none of these actions' constructor need the current selection as input
				int nbrBaseActions = 0;
				if (config.supportsSubSystemConnect())
				{
					//nbrBaseActions += 2; // 4; MJB: RE defect 50854
					if (selectedSubSystem.getConnectorService().supportsUserId())
						nbrBaseActions += 1;
				}
				//if (supportsFilters())
				//  nbrBaseActions += 2;
				subSystemActions = new IAction[nbrBaseActions];
				int ssIdx = 0;
				if (config.supportsSubSystemConnect())
				{
				    // MJB: RE defect 40854
					//subSystemActions[ssIdx++] = new SystemConnectAction(shell);
					//subSystemActions[ssIdx++] = new SystemDisconnectAction(shell);

					if (selectedSubSystem.getConnectorService().supportsUserId())
						subSystemActions[ssIdx++] = new SystemClearPasswordAction(shell);
				}
			} // end if subsystemActons == null

			IAction[] allActions = new IAction[childActions.size() + subSystemActions.length];
			int allIndex = 0;
			for (int idx = 0; idx < childActions.size(); idx++)
				allActions[allIndex++] = (IAction) childActions.elementAt(idx);
			for (int idx = 0; idx < subSystemActions.length; idx++)
				allActions[allIndex++] = subSystemActions[idx];

			return allActions;
		}

    /**
     * Overridable method to add the connect or disconnect action.
     *
     * @param actions The list of child actions. Add the connect/disconnect action to this vector is applicable.
     * @param shell The shell.
     * @param selectedSS The selected subsystem.
     */
		protected void addConnectOrDisconnectAction(Vector actions, Shell shell, ISubSystem selectedSS)
		{
			boolean connected = selectedSS.isConnected();
			if (connected)
			{
				actions.addElement(new SystemDisconnectAction(shell));
			}
			else
			{
				actions.addElement(new SystemConnectAction(shell));
			}
		}

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
		public void configureNewFilterAction(ISubSystemConfiguration config, SystemNewFilterAction newFilterAction, Object callerData)
		{
			//System.out.println("Inside configureNewFilterAction! It worked!");
			newFilterAction.setFromRSE(true);
			boolean showFilterPools = config.showFilterPools();

			// It does not make sense, when invoked from a filterPool, to ask the user
			//  for the parent filter pool, or to ask the user whether the filter is connection
			//  specific, as they user has explicitly chosen their pool...
			//if (!showFilterPools || (callerData instanceof SubSystem))
			if (!showFilterPools)
			{
				ISubSystem selectedSubSystem = (ISubSystem) callerData;
				// When not showing filter pools, we need to distinquish between an advanced user and a new user.
				// For a new user we simply want to ask them whether this filter is to be team sharable or private,
				//  and based on that, we will place the filter in the default filter pool for the appropriate profile.
				// For an advanced user who has simply turned show filter pools back off, we want to let them choose
				//  explicitly which filter pool they want to place the filter in.
				// To approximate the decision, we will define an advanced user as someone who already has a reference
				//  to a filter pool other than the default pools in the active profiles.
				boolean advancedUser = false;
				ISystemFilterPoolReferenceManager refMgr = selectedSubSystem.getSystemFilterPoolReferenceManager();
				ISystemFilterPool[] refdPools = refMgr.getReferencedSystemFilterPools();
				if (refdPools.length == 0)
					SystemBasePlugin.logInfo("SubSystemConfigurationImpl::getSubSystemActions - getReferencedSystemFilterPools returned array of length zero."); //$NON-NLS-1$
				// so there already exists references to more than one filter pool, but it might simply be a reference
				//  to the default filter pool in the user's profile and another to reference to the default filter pool in
				//  the team profile... let's see...
				else if (refdPools.length > 1)
				{
					for (int idx = 0; !advancedUser && (idx < refdPools.length); idx++)
					{
						if (!refdPools[idx].isDefault() && (refdPools[idx].getOwningParentName()==null))
							advancedUser = true;
					}
				}
				if (advancedUser)
				{
					newFilterAction.setAllowFilterPoolSelection(refdPools); // show all pools referenced in this subsystem, and let them choose one
				}
				else
				{
					boolean anyAdded = false;
					ISystemFilterPoolWrapperInformation poolWrapperInfo = getNewFilterWizardPoolWrapperInformation();
					ISystemProfile[] activeProfiles = RSECorePlugin.getTheSystemRegistry().getActiveSystemProfiles();
					ISystemProfile activeProfile = selectedSubSystem.getHost().getSystemProfile();
					for (int idx = 0; idx < activeProfiles.length; idx++)
					{
						ISystemFilterPool defaultPool = getDefaultSystemFilterPool(config, activeProfiles[idx]);

						if (defaultPool != null)
						{
							poolWrapperInfo.addWrapper(activeProfiles[idx].getName(), defaultPool, (activeProfiles[idx] == activeProfile)); // display name, pool to wrap, whether to preselect
							anyAdded = true;
						}
					}
					if (anyAdded)
						newFilterAction.setAllowFilterPoolSelection(poolWrapperInfo);
				}
			}
		}

		/**
		 * Given a profile, return the first (hopefully only) default pool for this
		 * profile.
		 */
		public ISystemFilterPool getDefaultSystemFilterPool(ISubSystemConfiguration config, ISystemProfile profile)
		{
			ISystemFilterPool pool = null;
			ISystemFilterPoolManager mgr = config.getFilterPoolManager(profile);
			pool = mgr.getFirstDefaultSystemFilterPool(); // RETURN FIRST
			return pool;
		}

		/**
		 * Overridable entry for child classes to supply their own flavour of ISystemFilterPoolWrapperInformation for
		 *  the new filter wizards.
		 * @since 3.0 replaced SystemFilterPoolWrapperInformation by ISystemFilterPoolWrapperInformation
		 */
		protected ISystemFilterPoolWrapperInformation getNewFilterWizardPoolWrapperInformation()
		{
			return new SystemFilterPoolWrapperInformation(SystemResources.RESID_NEWFILTER_PAGE2_PROFILE_LABEL, SystemResources.RESID_NEWFILTER_PAGE2_PROFILE_TOOLTIP,
					SystemResources.RESID_NEWFILTER_PAGE2_PROFILE_VERBIAGE);
		}
		/**
		 * Overridable entry for child classes to supply their own "new" action(s) for creating a
		 *  filter pool.
		 * By default, this creates an action for creating a new filter pool and a new filter pool reference.
		 * @param selectedSubSystem the currently selected subsystem
		 * @param shell The Shell of the view where this action was launched from
		 * @return array of IAction objects to contribute to the popup menu
		 */
		protected IAction[] getSubSystemNewFilterPoolActions(SystemMenuManager menu, IStructuredSelection selection, Shell shell, String menuGroup, ISubSystemConfiguration config, ISubSystem selectedSubSystem)
		{
			IAction[] actions = new IAction[2];
			actions[0] = new SystemFilterNewFilterPoolAction(shell);
			((ISystemAction) actions[0]).setHelp(RSEUIPlugin.HELPPREFIX + "actn0040"); //$NON-NLS-1$
			((SystemFilterNewFilterPoolAction) actions[0]).setDialogHelp(RSEUIPlugin.HELPPREFIX + "wnfp0000"); //$NON-NLS-1$
			actions[1] = new SystemFilterCascadingNewFilterPoolReferenceAction(shell, selectedSubSystem.getSystemFilterPoolReferenceManager());
			((ISystemAction) actions[1]).setHelp(RSEUIPlugin.HELPPREFIX + "actn0041"); //$NON-NLS-1$
			return actions;
		}

		/**
		 * Supply the image to be used for filter pool managers, within actions.
		 * REQUIRED BY SYSTEMFILTERPOOLMANAGERPROVIDER INTERFACE
		 */
		public ImageDescriptor getSystemFilterPoolManagerImage()
		{
			return RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_PROFILE_ID);
		}
		/**
		 * Supply the image to be used for filter pools, within actions.
		 * REQUIRED BY SYSTEMFILTERPOOLMANAGERPROVIDER INTERFACE
		 */
		public ImageDescriptor getSystemFilterPoolImage(ISystemFilterPool filterPool)
		{
			return RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_FILTERPOOL_ID);
		}
		/**
		 * Supply the image to be used for filters, within actions.
		 * REQUIRED BY SYSTEMFILTERPOOLMANAGERPROVIDER INTERFACE
		 */
		public ImageDescriptor getSystemFilterImage(ISystemFilter filter)
		{
			return RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_FILTER_ID);
		}

		/**
		 * Supply the image to be used for filter pool references.  This implementation
		 * just gets the referenced filter pool and calls the method
		 * getSystemFilterPoolImage(ISystemFilterPool) on it.
		 * Override this method to provide custom images for filter pool references.
		 */
		public ImageDescriptor getSystemFilterPoolImage(ISystemFilterPoolReference filterPoolRef)
		{
			return getSystemFilterPoolImage(filterPoolRef.getReferencedFilterPool());
		}

		/**
		 * Supply the image to be used for filter references.  This implementation
		 * just gets the referenced filter and calls the method
		 * getSystemFilterImage(ISystemFile) on it.
		 * Override this method to provide custom images for filter references.
		 */
		public ImageDescriptor getSystemFilterImage(ISystemFilterReference filterRef)
		{
			return getSystemFilterImage(filterRef.getReferencedFilter());
		}


		/*
		 * Supply the image to be used for the given filter string, within actions.
		 * REQUIRED BY SYSTEMFILTERPOOLMANAGERPROVIDER INTERFACE
		 */
		public ImageDescriptor getSystemFilterStringImage(ISystemFilterString filterString)
		{
			return getSystemFilterStringImage(filterString.getString());
		}

		/*
		 * Supply the image to be used for the given filter string string, within actions.
		 * REQUIRED BY SYSTEMFILTERPOOLMANAGERPROVIDER INTERFACE
		 */
		public ImageDescriptor getSystemFilterStringImage(String filterStringString)
		{
			return RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_FILTERSTRING_ID);
		}


		// ------------------------------------------------
		// HELPER METHODS TO SIMPLY EVENT FIRING...
		// ------------------------------------------------



		// ------------------------------------------------
		// FILTER POOL MANAGER PROVIDER CALLBACK METHODS...
		// ------------------------------------------------

		// ---------------------
		// FILTER POOL EVENTS...
		// ---------------------

		/**
		 * Returns a list of actions for the popup menu when user right clicks on a
		 *  filter pool object within a subsystem of this subsystem configuration.
		 * Only supported and used by subsystems that support filters.
		 * <p>
		 * YOU DO NOT NEED TO OVERRIDE THIS METHOD.
		 * <p>
		 * Most actions are handled in this base, except if you have your own action for
		 * creating a new filter. In this case, <b>override getNewFilterAction()</b>
		 *
		 * @param selectedPool the currently selected pool
		 * @param shell parent shell of viewer where the popup menu is being constructed
		 */
    public IAction[] getFilterPoolActions(SystemMenuManager menu, IStructuredSelection selection, Shell shell, String menuGroup, ISubSystemConfiguration config, ISystemFilterPool selectedPool)
		{
			Vector childActions = new Vector();
			IAction[] newActions = getNewFilterPoolFilterActions(menu, selection, shell, menuGroup, config, selectedPool);
			if (newActions != null)
			{
				for (int idx = 0; idx < newActions.length; idx++)
				{
					childActions.addElement(newActions[idx]);
					//if (newActions[idx] instanceof SystemNewFilterAction)
					//	 ((SystemNewFilterAction) newActions[idx]).setCallBackConfigurator(this, null);
				}
			}
			if (filterPoolActions == null)
			{
				int nbr = 2;
				filterPoolActions = new IAction[nbr];
				SystemFilterCopyFilterPoolAction copyAction = new SystemFilterCopyFilterPoolAction(shell);
				copyAction.setPromptString(SystemResources.RESID_COPY_TARGET_PROFILE_PROMPT);
				copyAction.setHelp(RSEUIPlugin.HELPPREFIX + "actn0060"); //$NON-NLS-1$
				copyAction.setDialogHelp(RSEUIPlugin.HELPPREFIX + "dcfp0000"); //$NON-NLS-1$
				SystemFilterMoveFilterPoolAction moveAction = new SystemFilterMoveFilterPoolAction(shell);
				moveAction.setPromptString(SystemResources.RESID_MOVE_TARGET_PROFILE_PROMPT);
				moveAction.setHelp(RSEUIPlugin.HELPPREFIX + "actn0061"); //$NON-NLS-1$
				moveAction.setDialogHelp(RSEUIPlugin.HELPPREFIX + "dmfp0000"); //$NON-NLS-1$
				filterPoolActions[0] = copyAction;
				filterPoolActions[1] = moveAction;
			}
			for (int idx = 0; idx < filterPoolActions.length; idx++)
			{
				childActions.addElement(filterPoolActions[idx]);
			}

			IAction[] allFilterPoolActions = new IAction[childActions.size()];
			for (int idx = 0; idx < childActions.size(); idx++)
				allFilterPoolActions[idx] = (IAction) childActions.elementAt(idx);

			return allFilterPoolActions;
		}

    /**
		 * Overridable method to return the actions for creating a new filter in a filter pool.
		 * By default returns one action created by calling {@link #getNewFilterPoolFilterAction(ISubSystemConfiguration, ISystemFilterPool, Shell)}.
		 * <p>
		 * If you have multiple actions for creating new filters, override this.
		 * <p>
		 * If you have only a single action for creating new filters, override getNewFilterPoolFilterAction (without the 's').
		 * <p>
		 * @param selectedPool the currently selected pool
		 * @param shell parent shell of viewer where the popup menu is being constructed
		 */
		protected IAction[] getNewFilterPoolFilterActions(SystemMenuManager menu, IStructuredSelection selection, Shell shell, String menuGroup, ISubSystemConfiguration config, ISystemFilterPool selectedPool)
		{
			IAction[] actions = new IAction[1];
			actions[0] = getNewFilterPoolFilterAction(config, selectedPool, shell);
			return actions;
		}
		/**
		 * Overridable method to return the single action for creating a new filter in a filter pool.
		 * By default returns a default supplied action for this.
		 * <p>
		 * If you have multiple actions for creating new filters, override getNewFilterPoolFilterActions (note the 's').
		 * <p>
		 * If you have only a single action for creating new filters, override this.
		 * <p>
		 * @param selectedPool the currently selected pool
		 * @param shell parent shell of viewer where the popup menu is being constructed
		 */
		protected IAction getNewFilterPoolFilterAction(ISubSystemConfiguration config, ISystemFilterPool selectedPool, Shell shell)
		{
			SystemNewFilterAction action = new SystemNewFilterAction(shell, selectedPool);
			action.setHelp(RSEUIPlugin.HELPPREFIX + "actn0042"); //$NON-NLS-1$
			action.setDialogHelp(RSEUIPlugin.HELPPREFIX + "wnfr0000"); //$NON-NLS-1$
			return action;
		}
		/**
		 * Overridable method to return the action for creating a new nested filter inside another filter.
		 * By default returns getNewFilterPoolFilterAction(selectedFilter.getParentFilterPool(),shell).
		 * @param selectedFilter the currently selected filter
		 * @param shell parent shell of viewer where the popup menu is being constructed
		 */
		protected IAction getNewNestedFilterAction(ISubSystemConfiguration config, ISystemFilter selectedFilter, Shell shell)
		{
			return getNewFilterPoolFilterAction(config, selectedFilter.getParentFilterPool(), shell);
		}
		/**
		 * Overridable method to return the action for changing an existing filter.
		 * By default returns new SystemChangeFilterAction, unless the filter's isSingleFilterStringOnly()
		 *  returns true, in which case null is returned.
		 *
		 * @param selectedFilter the currently selected filter
		 * @param shell parent shell of viewer where the popup menu is being constructed
		 */
		protected IAction getChangeFilterAction(ISubSystemConfiguration config, ISystemFilter selectedFilter, Shell shell)
		{
			/* We don't do this here now as this is overridable. Now done in SystemChangeFilterAction.
			 * Also, single filter string doesn't mean non-editable.
			 *
			if (selectedFilter.isSingleFilterStringOnly())
			{
				//System.out.println("filter " + selectedFilter + " is single filter string only");
				return null;
			}*/
			SystemChangeFilterAction action = new SystemChangeFilterAction(shell);
			action.setHelp(RSEUIPlugin.HELPPREFIX + "actn0081"); //$NON-NLS-1$
			action.setDialogHelp(RSEUIPlugin.HELPPREFIX + "dufr0000"); //$NON-NLS-1$
			return action;
		}
		/**
		 * In addition to a change filter action, we now also support the same functionality
		 *  via a Properties page for filters. When this page is activated, this method is called
		 *  to enable customization of the page, given the selected filter.
		 * <p>
		 * By default, this method will call {@link #getChangeFilterAction(ISubSystemConfiguration, ISystemFilter, Shell)} to get
		 * your change filter action, and will configure the given page from the dialog created by your
		 * change filter action.
		 * <p>
		 * If your filter uses its own Change Filter dialog, versus subclassing or configuring
		 * {@link org.eclipse.rse.ui.filters.dialogs.SystemChangeFilterDialog} you will have to override this method
		 * and specify the following information for the supplied page (via its setters):
		 * <ul>
		 *   <li>{@link org.eclipse.rse.ui.propertypages.SystemChangeFilterPropertyPage#setDuplicateFilterStringErrorMessage(org.eclipse.rse.services.clientserver.messages.SystemMessage)}
		 *   <li>{@link org.eclipse.rse.ui.propertypages.SystemChangeFilterPropertyPage#setFilterStringEditPane(org.eclipse.rse.ui.filters.SystemFilterStringEditPane)}
		 *   <li>{@link org.eclipse.rse.ui.propertypages.SystemChangeFilterPropertyPage#setFilterStringValidator(org.eclipse.rse.ui.validators.ISystemValidator)}
		 *   <li>{@link org.eclipse.rse.ui.propertypages.SystemChangeFilterPropertyPage#setListLabel(String, String)}
		 *   <li>{@link org.eclipse.rse.ui.propertypages.SystemChangeFilterPropertyPage#setParentPoolPromptLabel(String, String)}
		 *   <li>{@link org.eclipse.rse.ui.propertypages.SystemChangeFilterPropertyPage#setNamePromptLabel(String, String)}
		 *   <li>{@link org.eclipse.rse.ui.propertypages.SystemChangeFilterPropertyPage#setNewListItemText(String)}
		 *   <li>{@link org.eclipse.rse.ui.propertypages.SystemChangeFilterPropertyPage#setDuplicateFilterStringErrorMessage(org.eclipse.rse.services.clientserver.messages.SystemMessage)}
		 *   <li>{@link org.eclipse.rse.ui.propertypages.SystemChangeFilterPropertyPage#setEditable(boolean)}
		 * 	 <li>{@link org.eclipse.rse.ui.propertypages.SystemChangeFilterPropertyPage#setSupportsMultipleStrings(boolean)}
		 *   <li>{@link org.eclipse.rse.ui.propertypages.SystemChangeFilterPropertyPage#setDescription(String)}
		 * 	</ul>
		 */
		public void customizeChangeFilterPropertyPage(ISubSystemConfiguration config, SystemChangeFilterPropertyPage page, ISystemFilter selectedFilter, Shell shell)
		{
			// default behaviour is a total hack! We want to preserve all the configuration done on the
			// Change dialog, so we instantiate it merely so that we can copy the configuration information...
			IAction changeAction = getChangeFilterAction(config, selectedFilter, shell);
		  	if (changeAction instanceof SystemChangeFilterAction)
		  	{
				SystemChangeFilterAction changeFilterAction = (SystemChangeFilterAction)changeAction;
				changeFilterAction.setSelection(new StructuredSelection(selectedFilter));
				org.eclipse.jface.dialogs.Dialog dlg = changeFilterAction.createDialog(shell);
				if (dlg instanceof SystemChangeFilterDialog)
				{
					SystemChangeFilterDialog changeFilterDlg = (SystemChangeFilterDialog)dlg;
					//changeFilterAction.callConfigureFilterDialog(changeFilterDlg); createDialog calls this already!
					page.setDuplicateFilterStringErrorMessage(changeFilterDlg.getDuplicateFilterStringErrorMessage());
					page.setFilterStringEditPane(changeFilterDlg.getFilterStringEditPane(shell));
					page.setFilterStringValidator(changeFilterDlg.getFilterStringValidator());
					page.setListLabel(changeFilterDlg.getListLabel(), changeFilterDlg.getListTip());
					page.setParentPoolPromptLabel(changeFilterDlg.getParentPoolPromptLabel(), changeFilterDlg.getParentPoolPromptTip());
					page.setNamePromptLabel(changeFilterDlg.getNamePromptLabel(), changeFilterDlg.getNamePromptTip());
					page.setNewListItemText(changeFilterDlg.getNewListItemText());

					page.setDescription(changeFilterDlg.getTitle());
				}
		  	}
			if (selectedFilter.isNonChangable())
				page.setEditable(false);
			//System.out.println("Selected filter: "+selectedFilter.getName()+", isSingleFilterStringOnly: "+selectedFilter.isSetSingleFilterStringOnly());
			boolean singleFilterString = selectedFilter.isSingleFilterStringOnly() || (selectedFilter.isNonChangable() && (selectedFilter.getFilterStringCount() == 1));
			if (singleFilterString)
				page.setSupportsMultipleStrings(false);
		}

		/**
		 * In addition to a change filter action, we now also support the same functionality
		 *  via a Properties page for filter strings, in the Team View. When this page is activated,
		 *  this method is called to enable customization of the page, given the selected filter string.
		 *
		 * <p>
		 * By default, this method will call {@link #getChangeFilterAction(ISubSystemConfiguration, ISystemFilter, Shell)} to get
		 * your change filter action, and will configure the given page from the dialog created by your
		 * change filter action.
		 * <p>
		 * If your filter uses its own Change Filter dialog, versus subclassing or configuring
		 * {@link org.eclipse.rse.ui.filters.dialogs.SystemChangeFilterDialog} you will have to
		 * override this method and specify the following information for the supplied page (via its setters):
		 * <ul>
		 *   <li>{@link org.eclipse.rse.ui.propertypages.SystemFilterStringPropertyPage#setDuplicateFilterStringErrorMessage(org.eclipse.rse.services.clientserver.messages.SystemMessage)}
		 *   <li>{@link org.eclipse.rse.ui.propertypages.SystemFilterStringPropertyPage#setFilterStringEditPane(org.eclipse.rse.ui.filters.SystemFilterStringEditPane)}
		 *   <li>{@link org.eclipse.rse.ui.propertypages.SystemFilterStringPropertyPage#setFilterStringValidator(org.eclipse.rse.ui.validators.ISystemValidator)}
		 *   <li>{@link org.eclipse.rse.ui.propertypages.SystemFilterStringPropertyPage#setDuplicateFilterStringErrorMessage(org.eclipse.rse.services.clientserver.messages.SystemMessage)}
		 *   <li>{@link org.eclipse.rse.ui.propertypages.SystemFilterStringPropertyPage#setEditable(boolean)}
		 *   <li>{@link org.eclipse.rse.ui.propertypages.SystemFilterStringPropertyPage#setDescription(String)}
		 * </ul>
		 */
		public void customizeFilterStringPropertyPage(ISubSystemConfiguration config, SystemFilterStringPropertyPage page, ISystemFilterString selectedFilterString, Shell shell)
		{
			// default behaviour is a total hack! We want to preserve all the configuration done on the
			// Change dialog, so we instantiate it merely so that we can copy the configuration information...
			ISystemFilter selectedFilter = selectedFilterString.getParentSystemFilter();
			IAction changeAction = getChangeFilterAction(config, selectedFilter, shell);
			if (changeAction instanceof SystemChangeFilterAction)
			{
				SystemChangeFilterAction changeFilterAction = (SystemChangeFilterAction)changeAction;
				changeFilterAction.setSelection(new StructuredSelection(selectedFilter));
				org.eclipse.jface.dialogs.Dialog dlg = changeFilterAction.createDialog(shell);
				if (dlg instanceof SystemChangeFilterDialog)
				{
					SystemChangeFilterDialog changeFilterDlg = (SystemChangeFilterDialog)dlg;
					//changeFilterAction.callConfigureFilterDialog(changeFilterDlg); createDialog calls this!
					page.setDuplicateFilterStringErrorMessage(changeFilterDlg.getDuplicateFilterStringErrorMessage());
					page.setFilterStringEditPane(changeFilterDlg.getFilterStringEditPane(shell));
					page.setFilterStringValidator(changeFilterDlg.getFilterStringValidator());
					page.setDescription(changeFilterDlg.getTitle());
				}
			}
			if (selectedFilter.isNonChangable())
				page.setEditable(false);
		}

		// ---------------------------------
		// FILTER POOL REFERENCE METHODS...
		// ---------------------------------


		/**
		 * Returns a list of actions for the popup menu when user right clicks on a
		 *  filter pool reference object within a subsystem of this subsystem configuration. Note,
		 *  these are added to the list returned by getFilterPoolActions().
		 * Only supported by subsystems that support filters.
		 * @param selectedPoolReference the currently selected pool reference
		 * @param shell parent shell of viewer where the popup menu is being constructed
		 */
    public IAction[] getFilterPoolReferenceActions(SystemMenuManager menu, IStructuredSelection selection, Shell shell, String menuGroup, ISubSystemConfiguration config, ISystemFilterPoolReference selectedPoolReference)
		{
			ISystemFilterPool selectedPool = selectedPoolReference.getReferencedFilterPool();
			if (filterPoolReferenceActions == null)
			{
				filterPoolReferenceActions = new IAction[3];
				filterPoolReferenceActions[0] = getRemoveFilterPoolReferenceAction(config, selectedPool, shell);
				filterPoolReferenceActions[1] = new SystemFilterMoveUpFilterPoolReferenceAction(shell);
				((ISystemAction) filterPoolReferenceActions[1]).setHelp(RSEUIPlugin.HELPPREFIX + "actn0063"); //$NON-NLS-1$
				filterPoolReferenceActions[2] = new SystemFilterMoveDownFilterPoolReferenceAction(shell);
				((ISystemAction) filterPoolReferenceActions[2]).setHelp(RSEUIPlugin.HELPPREFIX + "actn0064"); //$NON-NLS-1$
			}

			return filterPoolReferenceActions;
		}

    /**
		 * Overridable method to return the action for removing a filter pool reference.
		 * By default returns new SystemRemoveFilterPoolReferenceAction.
		 * @param selectedPool the currently selected pool
		 * @param shell parent shell of viewer where the popup menu is being constructed
		 */
		protected IAction getRemoveFilterPoolReferenceAction(ISubSystemConfiguration config, ISystemFilterPool selectedPool, Shell shell)
		{
			ISystemAction action = new SystemFilterRemoveFilterPoolReferenceAction(shell);
			action.setHelp(RSEUIPlugin.HELPPREFIX + "actn0062"); //$NON-NLS-1$
			return action;
		}

		// ---------------------------------
		// FILTER METHODS
		// ---------------------------------



		/**
		 * Prompt the user to create a new filter as a result of the user expanding a promptable
		 * filter.
		 * <p>
		 * This base implementation prompts using the generic filter prompt. You should override this but
		 * copy this code to use as a base/example how to do this.
		 *
		 * @return the filter created by the user or null if they cancelled the prompting
		 */
		public ISystemFilter createFilterByPrompting(ISubSystemConfiguration config, ISystemFilterReference referenceToPromptableFilter, Shell shell) throws Exception
		{
			ISystemFilter filterPrompt = referenceToPromptableFilter.getReferencedFilter();
			ISystemFilterPool selectedPool = filterPrompt.getParentFilterPool();

			SystemNewFilterAction action = new SystemNewFilterAction(shell, selectedPool);
			Object simulatedSelectedParent = null;
			if (!config.showFilterPools()) // if we are not showing filter pools, the parent will be the subsystem itself
			{
				simulatedSelectedParent = referenceToPromptableFilter.getProvider(); // this is the subsystem
				action.setCallBackConfigurator(this, simulatedSelectedParent);
			}
			else // if we are showing filter pools, the parent will be the selected filter pool reference
			{
				simulatedSelectedParent = referenceToPromptableFilter.getParentSystemFilterReferencePool();
				action.setCallBackConfigurator(this, referenceToPromptableFilter.getProvider());
			}
			action.setSelection(new StructuredSelection(simulatedSelectedParent)); // pretend parent node was selected

			action.run();
			ISystemFilter newFilter = action.getNewFilter();
			return newFilter;
		}

		/**
		 * Returns a list of actions for the popup menu when user right clicks on a
		 *  filter object.
		 * <p>
		 * Only supported and used by subsystems that support filters.
		 * <p>
		 * YOU DO NOT NEED TO OVERRIDE THIS METHOD.
		 * <p>
		 * Most actions are handled in this base, except if you have your own action for
		 * creating a new nested filter. In this case, <b>override getNewFilterAction()</b>
		 */
    public IAction[] getFilterActions(SystemMenuManager menu, IStructuredSelection selection, Shell shell, String menuGroup, ISubSystemConfiguration config, ISystemFilter selectedFilter)
		{
			Vector childActions = new Vector();
			Vector ourChildActions = getAdditionalFilterActions(config, selectedFilter, shell);
			int pasteIndex = -1;
			if (ourChildActions != null)
				for (int idx = 0; idx < ourChildActions.size(); idx++)
				{
					// we want to make sure the order is kept consistent at
					// Copy, Paste, Move, Delete Rename
					if (ourChildActions.elementAt(idx) instanceof SystemPasteFromClipboardAction) pasteIndex = idx;
					else childActions.addElement(ourChildActions.elementAt(idx));
				}

			// Add our static default-supplied actions
			if (filterActions == null)
			{
				int additionalActions = 4;
				if (pasteIndex > -1) additionalActions++;
				int fsIdx = 0;
				filterActions = new IAction[additionalActions];
				SystemFilterCopyFilterAction copyAction = new SystemFilterCopyFilterAction(shell);
				copyAction.setPromptString(SystemResources.RESID_COPY_TARGET_FILTERPOOL_PROMPT);
				copyAction.setHelp(RSEUIPlugin.HELPPREFIX + "actn0082"); //$NON-NLS-1$
				copyAction.setDialogHelp(RSEUIPlugin.HELPPREFIX + "dcfr0000"); //$NON-NLS-1$
				filterActions[fsIdx++] = copyAction;

				// we want to make sure the order is kept consistent at
				// Copy, Paste, Move, Delete Rename
				if (pasteIndex > -1 && ourChildActions != null)
				{
					filterActions[fsIdx++] = (IAction) ourChildActions.elementAt(pasteIndex);
				}

				SystemFilterMoveFilterAction moveAction = new SystemFilterMoveFilterAction(shell);
				moveAction.setPromptString(SystemResources.RESID_MOVE_TARGET_FILTERPOOL_PROMPT);
				moveAction.setHelp(RSEUIPlugin.HELPPREFIX + "actn0083"); //$NON-NLS-1$
				moveAction.setDialogHelp(RSEUIPlugin.HELPPREFIX + "dmfr0000"); //$NON-NLS-1$
				filterActions[fsIdx++] = moveAction;

				filterActions[fsIdx] = new SystemFilterMoveUpFilterAction(shell);
				((SystemFilterMoveUpFilterAction) filterActions[fsIdx++]).setHelp(RSEUIPlugin.HELPPREFIX + "actn0084"); //$NON-NLS-1$
				filterActions[fsIdx] = new SystemFilterMoveDownFilterAction(shell);
				((SystemFilterMoveDownFilterAction) filterActions[fsIdx++]).setHelp(RSEUIPlugin.HELPPREFIX + "actn0085"); //$NON-NLS-1$
			}
			// add overridable dynamic actions
			if (config.supportsNestedFilters())
			{
				IAction newNestedFilterAction = getNewNestedFilterAction(config, selectedFilter, shell);
				if (newNestedFilterAction != null)
					childActions.addElement(newNestedFilterAction);
			}

			// IAction chgFilterAction = getChangeFilterAction(config, selectedFilter, shell);
			// if (chgFilterAction != null)
			//	childActions.addElement(chgFilterAction);
			/*
			if (showFilterStrings())
			{
			  IAction[] newStringActions = getNewFilterStringActions(selectedFilter, shell);
			  if (newStringActions != null)
			    for (int idx=0; idx<newStringActions.length; idx++)
			       childActions.addElement(newStringActions[idx]);
			}
			*/
			IAction[] allFilterActions = new IAction[childActions.size() + filterActions.length];
			int allIdx = 0;

				for (int idx = 0; idx < childActions.size(); idx++)
					allFilterActions[allIdx++] = (IAction) childActions.elementAt(idx);
			for (int idx = 0; idx < filterActions.length; idx++)
				allFilterActions[allIdx++] = filterActions[idx];

			return allFilterActions;
		}


		/**
		 * Overridable entry for child classes to contribute filter actions beyond the
		 * default supplied actions.
		 * <p>
		 * By default, this returns null.
		 * @return Vector of IAction objects.
		 * @see #getFilterActions(SystemMenuManager, IStructuredSelection, Shell, String, ISubSystemConfiguration, ISystemFilter)
		 */
		protected Vector getAdditionalFilterActions(ISubSystemConfiguration config, ISystemFilter selectedFilter, Shell shell)
		{
			return null;
		}

		// ---------------------------------
		// FILTER REFERENCE METHODS
		// ---------------------------------

		/**
		 * Returns a list of actions for the popup menu when user right clicks on a
		 *  filter reference object within a subsystem of this subsystem configuration.
		 * Only supported and used by subsystems that support filters.
		 * <p>
		 * YOU DO NOT NEED TO OVERRIDE THIS METHOD.
		 * <p>
		 * Most actions are handled in this base, except if you have your own action for
		 * creating a new filter. In this case, <b>override getNewFilterAction()</b>
		 *
		 * @param selectedFilterRef the currently selected filter reference
		 * @param shell parent shell of viewer where the popup menu is being constructed
		 */
    public IAction[] getFilterReferenceActions(SystemMenuManager menu, IStructuredSelection selection, Shell shell, String menuGroup, ISubSystemConfiguration config, ISystemFilterReference selectedFilterRef)
		{
			return null;
		}

		// -------------------------
		// SERVER LAUNCH SUPPORT ...
		// -------------------------

		/**
		 * Return the form used in the property page, etc for this server launcher.
		 * Only called if {@link ISubSystemConfiguration#supportsServerLaunchProperties(org.eclipse.rse.core.model.IHost)} returns true.
		 * <p>
		 * Override if appropriate.
		 * @return the UI form for the server launcher.
		 */
		public IServerLauncherForm getServerLauncherForm(ISubSystemConfiguration config, Shell shell, ISystemMessageLine msgLine)
		{
			return new RemoteServerLauncherForm(shell, msgLine);
		}

		/**
		 * Return the validator for the password which is prompted for at runtime.
		 * Returns null by default.
		 */
		public ISystemValidator getPasswordValidator(ISubSystemConfiguration configuration)
		{
			return null;
		}
		/**
		 * Return the validator for the port.
		 * A default is supplied.
		 * This must be castable to ICellEditorValidator for the property sheet support.
		 */
		public ISystemValidator getPortValidator(ISubSystemConfiguration configuration)
		{
			ISystemValidator portValidator = new ValidatorPortInput();
			return portValidator;
		}



		public PropertyPage getPropertyPage(ISubSystem subsystem, Composite parent) {
			return ((SubSystem)subsystem).getPropertyPage(parent);
		}


		public ISystemValidator getUserIdValidator(ISubSystemConfiguration config) {
			ISystemValidator userIdValidator =
				new ValidatorSpecialChar(
					"=;", //$NON-NLS-1$
					false,
					RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_VALIDATE_USERID_NOTVALID),
					RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_VALIDATE_USERID_EMPTY));
			// false => allow empty? No.
			return userIdValidator;
		}

		/**
		 * The default implementation does not filter and simply returns the children passed in.
		 * Subclasses should override if they want to filter objects.
		 * @param parent the parent context.
		 * @param children the children to filter.
		 * @return the children after filtering.
		 */
		public Object[] applyViewFilters(IContextObject parent, Object[] children) {
			return children;
		}
}
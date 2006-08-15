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

package org.eclipse.rse.ui.view;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.servicesubsystem.IServiceSubSystemConfiguration;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.SubSystemConfiguration;
import org.eclipse.rse.core.subsystems.util.ISubSystemConfigurationAdapter;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolManager;
import org.eclipse.rse.filters.ISystemFilterPoolReference;
import org.eclipse.rse.filters.ISystemFilterPoolReferenceManager;
import org.eclipse.rse.filters.ISystemFilterReference;
import org.eclipse.rse.filters.ISystemFilterString;
import org.eclipse.rse.filters.SystemFilterPoolWrapperInformation;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemProfile;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.actions.ISystemAction;
import org.eclipse.rse.ui.actions.SystemClearPasswordAction;
import org.eclipse.rse.ui.actions.SystemConnectAction;
import org.eclipse.rse.ui.actions.SystemDisconnectAction;
import org.eclipse.rse.ui.actions.SystemPasteFromClipboardAction;
import org.eclipse.rse.ui.filters.SystemFilterStringEditPane;
import org.eclipse.rse.ui.filters.actions.ISystemNewFilterActionConfigurator;
import org.eclipse.rse.ui.filters.actions.SystemChangeFilterAction;
import org.eclipse.rse.ui.filters.actions.SystemFilterAbstractFilterPoolAction;
import org.eclipse.rse.ui.filters.actions.SystemFilterCascadingNewFilterPoolReferenceAction;
import org.eclipse.rse.ui.filters.actions.SystemFilterCopyFilterAction;
import org.eclipse.rse.ui.filters.actions.SystemFilterCopyFilterPoolAction;
import org.eclipse.rse.ui.filters.actions.SystemFilterMoveDownFilterAction;
import org.eclipse.rse.ui.filters.actions.SystemFilterMoveDownFilterPoolReferenceAction;
import org.eclipse.rse.ui.filters.actions.SystemFilterMoveFilterAction;
import org.eclipse.rse.ui.filters.actions.SystemFilterMoveFilterPoolAction;
import org.eclipse.rse.ui.filters.actions.SystemFilterMoveUpFilterAction;
import org.eclipse.rse.ui.filters.actions.SystemFilterMoveUpFilterPoolReferenceAction;
import org.eclipse.rse.ui.filters.actions.SystemFilterNewFilterPoolAction;
import org.eclipse.rse.ui.filters.actions.SystemFilterRemoveFilterPoolReferenceAction;
import org.eclipse.rse.ui.filters.actions.SystemFilterSelectFilterPoolsAction;
import org.eclipse.rse.ui.filters.actions.SystemFilterWorkWithFilterPoolsAction;
import org.eclipse.rse.ui.filters.actions.SystemNewFilterAction;
import org.eclipse.rse.ui.filters.dialogs.SystemChangeFilterDialog;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.propertypages.ISystemSubSystemPropertyPageCoreForm;
import org.eclipse.rse.ui.propertypages.SystemChangeFilterPropertyPage;
import org.eclipse.rse.ui.propertypages.SystemFilterStringPropertyPage;
import org.eclipse.rse.ui.propertypages.SystemSubSystemPropertyPageCoreForm;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.widgets.RemoteServerLauncherForm;
import org.eclipse.rse.ui.widgets.IServerLauncherForm;
import org.eclipse.rse.ui.wizards.ISystemNewConnectionWizardPage;
import org.eclipse.rse.ui.wizards.SubSystemServiceWizardPage;
import org.eclipse.rse.ui.wizards.SystemSubSystemsPropertiesWizardPage;
import org.eclipse.swt.graphics.Image;
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
		 * factory of the selected objects to determine if these actions are appropriate to add.
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
		public void addCommonRemoteActions(ISubSystemConfiguration factory, SystemMenuManager menu, IStructuredSelection selection, Shell shell, String menuGroup, ISubSystem subsystem)
		{
			/** FIXME - UDAs should not be coupled to factory adapter
			SystemCompileManager mgr = factory.getCompileManager();

			if (factory.supportsCompileActions() && (mgr != null))
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

			if (factory.supportsUserDefinedActions() && factory.supportsUserDefinedActions(selection))
			{
				addUserDefinedActions(factory, shell, selection, menu, menuGroup, getActionSubSystem(factory, subsystem));
			}
			**/
		}

		// -----------------------------------
		// WIZARD PAGE CONTRIBUTION METHODS... (UCD defect 43194)
		// -----------------------------------
		/**
		 * Optionally return one or more wizard pages to append to the New Wizard connection if
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
		public ISystemNewConnectionWizardPage[] getNewConnectionWizardPages(ISubSystemConfiguration factory, IWizard wizard)
		{
			if (factory instanceof IServiceSubSystemConfiguration)
			{
				SubSystemServiceWizardPage page = new SubSystemServiceWizardPage(wizard, factory);
				return new ISystemNewConnectionWizardPage[] {page};
			}
			else
			{
				List pages = getSubSystemPropertyPages(factory);
				if (pages != null && pages.size() > 0)
				{								
					SystemSubSystemsPropertiesWizardPage page = new SystemSubSystemsPropertiesWizardPage(wizard, factory, pages);
					return new ISystemNewConnectionWizardPage[] {page};				
				}
			}
			return new ISystemNewConnectionWizardPage[0];
		}
		
	
	
		
		/*
		 * Return the form used in the subsyste property page.  This default implementation returns Syste
		 */
		public ISystemSubSystemPropertyPageCoreForm getSubSystemPropertyPageCoreFrom(ISubSystemConfiguration factory, ISystemMessageLine msgLine, Object caller)
		{
		    return new SystemSubSystemPropertyPageCoreForm(msgLine, caller);
		}
		
		/**
		 * Gets the list of property pages applicable for a subsystem associated with this factory
		 * @return the list of subsystem property pages
		 */
		protected List getSubSystemPropertyPages(ISubSystemConfiguration factory)
		{
			List propertyPages= new ArrayList();
			// Get reference to the plug-in registry
			IExtensionRegistry registry = Platform.getExtensionRegistry();

			// Get configured property page extenders
			IConfigurationElement[] propertyPageExtensions =
				registry.getConfigurationElementsFor("org.eclipse.ui", "propertyPages");
				
			for (int i = 0; i < propertyPageExtensions.length; i++)
			{
				IConfigurationElement configurationElement = propertyPageExtensions[i];
				String objectClass = configurationElement.getAttribute("objectClass");
				String name = configurationElement.getAttribute("name");
				Class objCls = null;
				try
				{
				    ClassLoader loader = getClass().getClassLoader();
					objCls = Class.forName(objectClass, false, loader);
				}
				catch (Exception e)
				{
				}
				
				
				if (objCls != null && ISubSystem.class.isAssignableFrom(objCls) && factory.isFactoryFor(objCls))			
				{
					try
					{
						PropertyPage page = (PropertyPage) configurationElement.createExecutableExtension("class");
						page.setTitle(name);
						propertyPages.add(page);
					}
					catch (Exception e)
					{
					}
				}
			}
			return propertyPages;
		}

		// FIXME - UDAs no longer coupled with factory in core
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
//		public SystemUDActionSubsystem getActionSubSystem(ISubSystemConfiguration factory, ISubSystem subsystem)
//		{
//			if (udas == null)
//				udas = createActionSubSystem(factory);
//			if (udas != null)
//			{
//				udas.setSubsystem(subsystem);
//				udas.setSubSystemConfiguration(factory);
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
//		protected SystemUDActionSubsystem createActionSubSystem(ISubSystemConfiguration factory)
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
//		public static void addUserDefinedActions(ISubSystemConfiguration factory, Shell shell, IStructuredSelection selection, SystemMenuManager menu, String menuGroup, SystemUDActionSubsystem userActionSubSystem)
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

	
		/**
		 * Return image descriptor of this factory.
		 * This comes from the xml "icon" attribute of the extension point.
		 */
		public ImageDescriptor getImage(ISubSystemConfiguration factory)
		{
			return factory.getImage();
		}
		/**
		 * Return actual graphics Image of this factory.
		 * This is the same as calling getImage().createImage() but the resulting
		 *  image is cached.
		 */
		public Image getGraphicsImage(ISubSystemConfiguration factory)
		{
			ImageDescriptor id = getImage(factory);
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

		/**
		 * Return image to use when this susystem is connection.
		 * This comes from the xml "iconlive" attribute of the extension point.
		 */
		public ImageDescriptor getLiveImage(ISubSystemConfiguration factory)
		{
			return factory.getLiveImage();
		}

		/**
		 * Return actual graphics LiveImage of this factory.
		 * This is the same as calling getLiveImage().createImage() but the resulting
		 *  image is cached.
		 */
		public Image getGraphicsLiveImage(ISubSystemConfiguration factory)
		{
			ImageDescriptor id = getLiveImage(factory);
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
		 * Returns a list of actions for the popup menu when user right clicks on a subsystem object from this factory.
		 * By default returns a single item array with a SystemNewFilterPoolAction object and
		 * calls overridable method getAdditionalSubSystemActions.
		 * <p>
		 * If you wish to support more actions, override getAdditionalSubSystemActions to return a Vector
		 * of IAction objects.
		 * @see #getSubSystemNewFilterPoolActions(ISubSystem, Shell)
		 * @see #getAdditionalSubSystemActions(ISubSystem, Shell)
		 * @param selectedSubSystem the currently selected subsystem
		 * @param shell The Shell of the view where this action was launched from
		 * @return array of IAction objects to contribute to the popup menu
		 */
		public IAction[] getSubSystemActions(ISubSystemConfiguration factory, ISubSystem selectedSubSystem, Shell shell)
		{
			Vector ourChildActions = getAdditionalSubSystemActions(factory, selectedSubSystem, shell);
			// we need to start with a fresh vector each time not build up on what our child
			//  class gives us, since that may be cached and hence will grow if we keep adding to i.
			Vector childActions = new Vector();
			if (ourChildActions != null)
				for (int idx = 0; idx < ourChildActions.size(); idx++)
					childActions.addElement(ourChildActions.elementAt(idx));
			if (factory.supportsFilters())
			{
				boolean showFilterPools = factory.showFilterPools();
				// if showing filter pools, we have to add a "new filter pool" action here...
				if (showFilterPools)
				{
					IAction[] newFPActions = getSubSystemNewFilterPoolActions(factory, selectedSubSystem, shell);
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
								fpAction.setFilterPoolManagerProvider(factory);
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
					IAction[] newFilterActions = getNewFilterPoolFilterActions(factory, null, shell);
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
						/*
						if (anyLegacy)
						{
						   SystemFilterPoolReferenceManager refMgr = selectedSubSystem.getSystemFilterPoolReferenceManager();
						   SystemFilterPool[] refdPools = refMgr.getReferencedSystemFilterPools();
						   if ( refdPools.length == 0 )
						      RSEUIPlugin.logInfo("SubSystemConfigurationImpl::getSubSystemActions - getReferencedSystemFilterPools returned array of lenght zero.");
						   for (int idx=0; idx<newFilterActions.length; idx++)
						   {
						         if (newFilterActions[idx] instanceof SystemFilterBaseNewFilterAction && refdPools.length > 0 )
							          ((SystemFilterBaseNewFilterAction)newFilterActions[idx]).setAllowFilterPoolSelection(refdPools);
						   } // end for loop
						}
						*/
						// now add the actions
						for (int idx = 0; idx < newFilterActions.length; idx++)
							childActions.addElement(newFilterActions[idx]);
					} // end if newFilterActions != null
				} // end if !showFilterPools
			} // end if supportsFilters()
			
// FIXME - UDAs moved out of here
//			// if user defined actions are supported, add an action to work with them...
//			if (factory.supportsUserDefinedActions())
//				childActions.addElement(new SystemWorkWithUDAsAction(shell, (ISubSystem)null));
//			// if named file types are supported, add an action to work with them...
//			if (factory.supportsFileTypes())
//				childActions.addElement(new SystemWorkWithFileTypesAction(shell, null));
//			// if compile actions are supported, add an action to work with them...
//			if (factory.supportsCompileActions())
//				childActions.addElement(new SystemWorkWithCompileCommandsAction(shell, false));

	
			
			if (factory.supportsSubSystemConnect())
			{
			    // MJB: RE defect 40854 
				addConnectOrDisconnectAction(childActions, shell, selectedSubSystem);
			}

			if (subSystemActions == null)
			{
				// premise: none of these actions' constructor need the current selection as input
				int nbrBaseActions = 0;
				if (factory.supportsSubSystemConnect())
				{
					//nbrBaseActions += 2; // 4; MJB: RE defect 50854    	
					if (selectedSubSystem.getConnectorService().supportsUserId())
						nbrBaseActions += 1;
				}
				//if (supportsFilters())
				//  nbrBaseActions += 2;
				subSystemActions = new IAction[nbrBaseActions];
				int ssIdx = 0;
				if (factory.supportsSubSystemConnect())
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
		
		private void addConnectOrDisconnectAction(Vector actions, Shell shell, ISubSystem selectedSS)
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
		public void configureNewFilterAction(ISubSystemConfiguration factory, SystemNewFilterAction newFilterAction, Object callerData)
		{
			//System.out.println("Inside configureNewFilterAction! It worked!");
			newFilterAction.setFromRSE(true);
			boolean showFilterPools = factory.showFilterPools();
			
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
					SystemBasePlugin.logInfo("SubSystemConfigurationImpl::getSubSystemActions - getReferencedSystemFilterPools returned array of length zero.");
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
					SystemFilterPoolWrapperInformation poolWrapperInfo = getNewFilterWizardPoolWrapperInformation();
					ISystemProfile[] activeProfiles = RSEUIPlugin.getTheSystemRegistry().getActiveSystemProfiles();
					ISystemProfile activeProfile = selectedSubSystem.getHost().getSystemProfile();
					for (int idx = 0; idx < activeProfiles.length; idx++)
					{
						ISystemFilterPool defaultPool = getDefaultSystemFilterPool(factory, (ISystemProfile)activeProfiles[idx]);
						
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
		public ISystemFilterPool getDefaultSystemFilterPool(ISubSystemConfiguration factory, ISystemProfile profile)
		{
			ISystemFilterPool pool = null;
			ISystemFilterPoolManager mgr = factory.getFilterPoolManager(profile);
			pool = mgr.getFirstDefaultSystemFilterPool(); // RETURN FIRST
			return pool;
		}
		
		/**
		 * Overridable entry for child classes to supply their own flavour of ISystemFilterPoolWrapperInformation for
		 *  the new filter wizards.
		 */
		protected SystemFilterPoolWrapperInformation getNewFilterWizardPoolWrapperInformation()
		{
			return new SystemFilterPoolWrapperInformation(SystemResources.RESID_NEWFILTER_PAGE2_PROFILE_LABEL, SystemResources.RESID_NEWFILTER_PAGE2_PROFILE_TOOLTIP, 
					SystemResources.RESID_NEWFILTER_PAGE2_PROFILE_VERBAGE);
		}
		/**
		 * Overridable entry for child classes to supply their own "new" action(s) for creating a
		 *  filter pool.
		 * By default, this creates an action for creating a new filter pool and a new filter pool reference.
		 * @param selectedSubSystem the currently selected subsystem
		 * @param shell The Shell of the view where this action was launched from
		 * @return array of IAction objects to contribute to the popup menu
		 */
		protected IAction[] getSubSystemNewFilterPoolActions(ISubSystemConfiguration factory, ISubSystem selectedSubSystem, Shell shell)
		{
			IAction[] actions = new IAction[2];
			actions[0] = new SystemFilterNewFilterPoolAction(shell);
			((ISystemAction) actions[0]).setHelp(RSEUIPlugin.HELPPREFIX + "actn0040");
			((SystemFilterNewFilterPoolAction) actions[0]).setDialogHelp(RSEUIPlugin.HELPPREFIX + "wnfp0000");
			actions[1] = new SystemFilterCascadingNewFilterPoolReferenceAction(shell, selectedSubSystem.getSystemFilterPoolReferenceManager());
			((ISystemAction) actions[1]).setHelp(RSEUIPlugin.HELPPREFIX + "actn0041");
			return actions;
		}
		/**
		 * Overridable entry for child classes to contribute subsystem actions
		 * beyond the default supplied actions.
		 * <p>
		 * By default, returns null.
		 * @return Vector of IAction objects.
		 * @see #getSubSystemActions(ISubSystem,Shell)
		 */
		protected Vector getAdditionalSubSystemActions(ISubSystemConfiguration factory, ISubSystem selectedSubSystem, Shell shell)
		{
			return null;
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
		 *  filter pool object within a subsystem of this factory.
		 * Only supported and used by subsystems that support filters.
		 * <p>
		 * YOU DO NOT NEED TO OVERRIDE THIS METHOD.
		 * <p>
		 * Most actions are handled in this base, except if you have your own action for
		 * creating a new filter. In this case, <b>override getNewFilterAction()</b>
		 * To add additional actions, override {@link #getAdditionalFilterPoolActions(ISystemFilterPool selectedPool, Shell shell)}.
		 *
		 * @param pool the currently selected pool
		 * @param shell parent shell of viewer where the popup menu is being constructed
		 */
		public IAction[] getFilterPoolActions(ISubSystemConfiguration factory, ISystemFilterPool selectedPool, Shell shell)
		{
			Vector childActions = new Vector();
			Vector ourChildActions = getAdditionalFilterPoolActions(factory, selectedPool, shell);
			if (ourChildActions != null)
				for (int idx = 0; idx < ourChildActions.size(); idx++)
					childActions.addElement(ourChildActions.elementAt(idx));
			IAction[] newActions = getNewFilterPoolFilterActions(factory, selectedPool, shell);
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
				copyAction.setHelp(RSEUIPlugin.HELPPREFIX + "actn0060");
				copyAction.setDialogHelp(RSEUIPlugin.HELPPREFIX + "dcfp0000");
				SystemFilterMoveFilterPoolAction moveAction = new SystemFilterMoveFilterPoolAction(shell);
				moveAction.setPromptString(SystemResources.RESID_MOVE_TARGET_PROFILE_PROMPT);
				moveAction.setHelp(RSEUIPlugin.HELPPREFIX + "actn0061");
				moveAction.setDialogHelp(RSEUIPlugin.HELPPREFIX + "dmfp0000");
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
		 * Overridable entry for child classes to contribute filter pool actions beyond the
		 * default supplied actions.
		 * <p>
		 * By default, this returns null.
		 * @param pool the currently selected pool
		 * @param shell parent shell of viewer where the popup menu is being constructed
		 * @return Vector of IAction objects.
		 * @see #getFilterPoolActions(ISystemFilterPool,Shell)
		 */
		protected Vector getAdditionalFilterPoolActions(ISubSystemConfiguration factory, ISystemFilterPool selectedPool, Shell shell)
		{
			return null;
		}
		/**
		 * Overridable method to return the actions for creating a new filter in a filter pool.
		 * By default returns one action created by calling {@link #getNewFilterPoolFilterAction(ISystemFilterPool, Shell)}.
		 * <p>
		 * If you have multiple actions for creating new filters, override this.
		 * <p>
		 * If you have only a single action for creating new filters, override getNewFilterPoolFilterAction (without the 's').
		 * <p>
		 * @param pool the currently selected pool
		 * @param shell parent shell of viewer where the popup menu is being constructed
		 */
		protected IAction[] getNewFilterPoolFilterActions(ISubSystemConfiguration factory, ISystemFilterPool selectedPool, Shell shell)
		{
			IAction[] actions = new IAction[1];
			actions[0] = getNewFilterPoolFilterAction(factory, selectedPool, shell);
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
		 * @param pool the currently selected pool
		 * @param shell parent shell of viewer where the popup menu is being constructed
		 */
		protected IAction getNewFilterPoolFilterAction(ISubSystemConfiguration factory, ISystemFilterPool selectedPool, Shell shell)
		{
			SystemNewFilterAction action = new SystemNewFilterAction(shell, selectedPool);
			action.setHelp(RSEUIPlugin.HELPPREFIX + "actn0042");
			action.setDialogHelp(RSEUIPlugin.HELPPREFIX + "wnfr0000");
			return action;
		}
		/**
		 * Overridable method to return the action for creating a new nested filter inside another filter.
		 * By default returns getNewFilterPoolFilterAction(selectedFilter.getParentFilterPool(),shell).
		 * @param pool the currently selected pool
		 * @param shell parent shell of viewer where the popup menu is being constructed
		 */
		protected IAction getNewNestedFilterAction(ISubSystemConfiguration factory, ISystemFilter selectedFilter, Shell shell)
		{
			return getNewFilterPoolFilterAction(factory, selectedFilter.getParentFilterPool(), shell);
		}
		/**
		 * Overridable method to return the action for changing an existing filter.
		 * By default returns new SystemChangeFilterAction, unless the filter's isSingleFilterStringOnly()
		 *  returns true, in which case null is returned.
		 * 
		 * @param selectedFilter the currently selected filter
		 * @param shell parent shell of viewer where the popup menu is being constructed
		 */
		protected IAction getChangeFilterAction(ISubSystemConfiguration factory, ISystemFilter selectedFilter, Shell shell)
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
			action.setHelp(RSEUIPlugin.HELPPREFIX + "actn0081");
			action.setDialogHelp(RSEUIPlugin.HELPPREFIX + "dufr0000");
			return action;
		}
		/**
		 * In addition to a change filter action, we now also support the same functionality
		 *  via a Properties page for filters. When this page is activated, this method is called
		 *  to enable customization of the page, given the selected filter.
		 * <p>
		 * By default, this method will call {@link #getChangeFilterAction(ISystemFilter, Shell)} to get
		 * your change filter action, and will configure the given page from the dialog created by your
		 * change filter action.  
		 * <p>
		 * If your filter uses its own Change Filter dialog, versus subclassing or configuring
		 * {@link org.eclipse.rse.ui.filters.dialogs.SystemChangeFilterDialog} you will have to override this method
		 * and specify the following information for the supplied page (via its setters):
		 * <ul>
		 *   <li>{@link org.eclipse.rse.ui.propertypages.SystemChangeFilterPropertyPage#setDuplicateFilterStringErrorMessage(SystemMessage)}
		 *   <li>{@link org.eclipse.rse.ui.propertypages.SystemChangeFilterPropertyPage#setFilterStringEditPane(SystemFilterStringEditPane)}
		 *   <li>{@link org.eclipse.rse.ui.propertypages.SystemChangeFilterPropertyPage#setFilterStringValidator(ISystemValidator)}
		 *   <li>{@link org.eclipse.rse.ui.propertypages.SystemChangeFilterPropertyPage#setListLabel(String, String)}
		 *   <li>{@link org.eclipse.rse.ui.propertypages.SystemChangeFilterPropertyPage#setParentPoolPromptLabel(String, String)}
		 *   <li>{@link org.eclipse.rse.ui.propertypages.SystemChangeFilterPropertyPage#setNamePromptLabel(String, String)}
		 *   <li>{@link org.eclipse.rse.ui.propertypages.SystemChangeFilterPropertyPage#setNewListItemText(String)}
		 *   <li>{@link org.eclipse.rse.ui.propertypages.SystemChangeFilterPropertyPage#setDuplicateFilterStringErrorMessage(SystemMessage)}
		 *   <li>{@link org.eclipse.rse.ui.propertypages.SystemChangeFilterPropertyPage#setEditable(boolean)}
		 * 	 <li>{@link org.eclipse.rse.ui.propertypages.SystemChangeFilterPropertyPage#setSupportsMultipleStrings(boolean)}
		 *   <li>{@link org.eclipse.rse.ui.propertypages.SystemChangeFilterPropertyPage#setDescription(String)}
		 * 	</ul>
		 */
		public void customizeChangeFilterPropertyPage(ISubSystemConfiguration factory, SystemChangeFilterPropertyPage page, ISystemFilter selectedFilter, Shell shell)
		{
			// default behaviour is a total hack! We want to preserve all the configuration done on the
			// Change dialog, so we instantiate it merely so that we can copy the configuration information...
			IAction changeAction = getChangeFilterAction(factory, selectedFilter, shell);
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
		 * By default, this method will call {@link #getChangeFilterAction(ISystemFilter, Shell)} to get
		 * your change filter action, and will configure the given page from the dialog created by your
		 * change filter action. 
		 * <p>
		 * If your filter uses its own Change Filter dialog, versus subclassing or configuring
		 * {@link org.eclipse.rse.ui.filters.dialogs.SystemChangeFilterDialog} you will have to 
		 * override this method and specify the following information for the supplied page (via its setters):
		 * <ul>
		 *   <li>{@link org.eclipse.rse.ui.propertypages.SystemFilterStringPropertyPage#setDuplicateFilterStringErrorMessage(SystemMessage)}
		 *   <li>{@link org.eclipse.rse.ui.propertypages.SystemFilterStringPropertyPage#setFilterStringEditPane(SystemFilterStringEditPane)}
		 *   <li>{@link org.eclipse.rse.ui.propertypages.SystemFilterStringPropertyPage#setFilterStringValidator(ISystemValidator)}
		 *   <li>{@link org.eclipse.rse.ui.propertypages.SystemFilterStringPropertyPage#setDuplicateFilterStringErrorMessage(SystemMessage)}
		 *   <li>{@link org.eclipse.rse.ui.propertypages.SystemFilterStringPropertyPage#setEditable(boolean)}
		 *   <li>{@link org.eclipse.rse.ui.propertypages.SystemFilterStringPropertyPage#setDescription(String)}
		 * </ul>
		 */
		public void customizeFilterStringPropertyPage(ISubSystemConfiguration factory, SystemFilterStringPropertyPage page, ISystemFilterString selectedFilterString, Shell shell)
		{
			// default behaviour is a total hack! We want to preserve all the configuration done on the
			// Change dialog, so we instantiate it merely so that we can copy the configuration information...
			ISystemFilter selectedFilter = selectedFilterString.getParentSystemFilter();
			IAction changeAction = getChangeFilterAction(factory, selectedFilter, shell);
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
		 *  filter pool reference object within a subsystem of this factory. Note,
		 *  these are added to the list returned by getFilterPoolActions().
		 * Only supported by subsystems that support filters.
		 * @param selectedPoolRef the currently selected pool reference
		 * @param shell parent shell of viewer where the popup menu is being constructed
		 */
		public IAction[] getFilterPoolReferenceActions(ISubSystemConfiguration factory, ISystemFilterPoolReference selectedPoolRef, Shell shell)
		{
			ISystemFilterPool selectedPool = selectedPoolRef.getReferencedFilterPool();
			Vector childActions = getAdditionalFilterPoolReferenceActions(factory, selectedPool, shell);
			int nbrChildActions = 0;
			if (childActions != null)
				nbrChildActions = childActions.size();
			int fpIdx = 0;
			if (filterPoolReferenceActions == null)
			{
				int nbr = 3;
				filterPoolReferenceActions = new IAction[nbr + nbrChildActions];
				filterPoolReferenceActions[fpIdx++] = getRemoveFilterPoolReferenceAction(factory, selectedPool, shell);
				filterPoolReferenceActions[fpIdx] = new SystemFilterMoveUpFilterPoolReferenceAction(shell);
				((ISystemAction) filterPoolReferenceActions[fpIdx++]).setHelp(RSEUIPlugin.HELPPREFIX + "actn0063");
				filterPoolReferenceActions[fpIdx] = new SystemFilterMoveDownFilterPoolReferenceAction(shell);
				((ISystemAction) filterPoolReferenceActions[fpIdx++]).setHelp(RSEUIPlugin.HELPPREFIX + "actn0064");
			}

			if (childActions != null)
				for (int idx = 0; idx < nbrChildActions; idx++)
					filterPoolReferenceActions[fpIdx++] = (IAction) childActions.elementAt(idx);

			return filterPoolReferenceActions;
		}
		/**
		 * Overridable entry for child classes to contribute filter pool reference actions beyond the
		 * default supplied actions.
		 * <p>
		 * By default, this returns null.
		 * @param pool the currently selected pool
		 * @param shell parent shell of viewer where the popup menu is being constructed
		 * @return Vector of IAction objects.
		 * @see #getFilterPoolReferenceActions(ISystemFilterPoolReference,Shell)
		 */
		protected Vector getAdditionalFilterPoolReferenceActions(ISubSystemConfiguration factory, ISystemFilterPool selectedPool, Shell shell)
		{
			return null;
		}
		/**
		 * Overridable method to return the action for removing a filter pool reference.
		 * By default returns new SystemRemoveFilterPoolReferenceAction.
		 * @param pool the currently selected pool
		 * @param shell parent shell of viewer where the popup menu is being constructed
		 */
		protected IAction getRemoveFilterPoolReferenceAction(ISubSystemConfiguration factory, ISystemFilterPool selectedPool, Shell shell)
		{
			ISystemAction action = new SystemFilterRemoveFilterPoolReferenceAction(shell);
			action.setHelp(RSEUIPlugin.HELPPREFIX + "actn0062");
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
		public ISystemFilter createFilterByPrompting(ISubSystemConfiguration factory, ISystemFilterReference referenceToPromptableFilter, Shell shell) throws Exception
		{
			ISystemFilter filterPrompt = referenceToPromptableFilter.getReferencedFilter();
			ISystemFilterPool selectedPool = filterPrompt.getParentFilterPool();

			SystemNewFilterAction action = new SystemNewFilterAction(shell, selectedPool);
			Object simulatedSelectedParent = null;
			if (!factory.showFilterPools()) // if we are not showing filter pools, the parent will be the subsystem itself
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
		public IAction[] getFilterActions(ISubSystemConfiguration factory, ISystemFilter selectedFilter, Shell shell)
		{
			Vector childActions = new Vector();
			Vector ourChildActions = getAdditionalFilterActions(factory, selectedFilter, shell);
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
				copyAction.setHelp(RSEUIPlugin.HELPPREFIX + "actn0082");
				copyAction.setDialogHelp(RSEUIPlugin.HELPPREFIX + "dcfr0000");
				filterActions[fsIdx++] = copyAction;

				// we want to make sure the order is kept consistent at
				// Copy, Paste, Move, Delete Rename
				if (pasteIndex > -1)
				{
					filterActions[fsIdx++] = (IAction) ourChildActions.elementAt(pasteIndex);
				}
				
				SystemFilterMoveFilterAction moveAction = new SystemFilterMoveFilterAction(shell);
				moveAction.setPromptString(SystemResources.RESID_MOVE_TARGET_FILTERPOOL_PROMPT);
				moveAction.setHelp(RSEUIPlugin.HELPPREFIX + "actn0083");
				moveAction.setDialogHelp(RSEUIPlugin.HELPPREFIX + "dmfr0000");
				filterActions[fsIdx++] = moveAction;

				filterActions[fsIdx] = new SystemFilterMoveUpFilterAction(shell);
				((SystemFilterMoveUpFilterAction) filterActions[fsIdx++]).setHelp(RSEUIPlugin.HELPPREFIX + "actn0084");
				filterActions[fsIdx] = new SystemFilterMoveDownFilterAction(shell);
				((SystemFilterMoveDownFilterAction) filterActions[fsIdx++]).setHelp(RSEUIPlugin.HELPPREFIX + "actn0085");
			}
			// add overridable dynamic actions
			if (factory.supportsNestedFilters())
			{
				IAction newNestedFilterAction = getNewNestedFilterAction(factory, selectedFilter, shell);
				if (newNestedFilterAction != null)
					childActions.addElement(newNestedFilterAction);
			}
			IAction chgFilterAction = getChangeFilterAction(factory, selectedFilter, shell);
			if (chgFilterAction != null)
				childActions.addElement(chgFilterAction);
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
			if (childActions != null)
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
		 * @see #getFilterActions(ISystemFilter,Shell)
		 */
		protected Vector getAdditionalFilterActions(ISubSystemConfiguration factory, ISystemFilter selectedFilter, Shell shell)
		{
			return null;
		}

		// ---------------------------------
		// FILTER REFERENCE METHODS
		// ---------------------------------

		/**
		 * Returns a list of actions for the popup menu when user right clicks on a
		 *  filter reference object within a subsystem of this factory.
		 * Only supported and used by subsystems that support filters.
		 * <p>
		 * YOU DO NOT NEED TO OVERRIDE THIS METHOD.
		 * <p>
		 * Most actions are handled in this base, except if you have your own action for
		 * creating a new filter. In this case, <b>override getNewFilterAction()</b>
		 * To add additional actions, override {@link #getAdditionalFilterReferenceActions(ISystemFilterReference, Shell)}.
		 *
		 * @param selectedFilterRef the currently selected filter reference
		 * @param shell parent shell of viewer where the popup menu is being constructed
		 */
		public IAction[] getFilterReferenceActions(ISubSystemConfiguration factory, ISystemFilterReference selectedFilterRef, Shell shell)
		{
			Vector childActions = getAdditionalFilterReferenceActions(factory, selectedFilterRef, shell);
			int nbrChildActions = 0;
			if (childActions != null)
				nbrChildActions = childActions.size();
			else
				childActions = new Vector();
			/*
			if (filterReferenceActions == null)
			{
			 int nbr = 2;
			 filterReferenceActions = new IAction[nbr];
			}
			for (int idx=0; idx<filterReferenceActions.length; idx++)
			{
				childActions.addElement(filterReferenceActions[idx]);
				++nbrChildActions;
			}
			*/
			IAction[] allFilterRefActions = new IAction[nbrChildActions];
			for (int idx = 0; idx < nbrChildActions; idx++)
				allFilterRefActions[idx] = (IAction) childActions.elementAt(idx);

			return allFilterRefActions;
		}
		/**
		 * Overridable entry for child classes to contribute filter reference actions beyond the
		 * default supplied actions.
		 * <p>
		 * By default, this returns null.
		 * @param selectedFilterRef the currently selected filter reference
		 * @param shell parent shell of viewer where the popup menu is being constructed
		 * @return Vector of IAction objects.
		 */
		protected Vector getAdditionalFilterReferenceActions(ISubSystemConfiguration factory, ISystemFilterReference selectedFilterRef, Shell shell)
		{
			return null;
		}

		// ---------------------------------
		// FILTER STRING METHODS
		// ---------------------------------

		/*
		 * Returns a list of actions for the popup menu when user right clicks on a
		 *  filter string object (and has set the preferences to see them).
		 * Only supported and used by subsystems that support filters.
		 * <p>
		 * YOU DO NOT NEED TO OVERRIDE THIS METHOD.
		 * <p>
		 * Most actions are handled in this base, only override if you have something unique.
		 *
		public IAction[] getFilterStringActions(SystemFilterString selectedFilterString, Shell shell)
		{
			Vector childActions = new Vector();
			Vector ourChildActions = getAdditionalFilterStringActions(selectedFilterString, shell);
			if (ourChildActions != null)
			  for (int idx=0; idx<ourChildActions.size(); idx++)
			     childActions.addElement(ourChildActions.elementAt(idx));
			
			if (filterStringActions == null)
			{
				filterStringActions = new IAction[4];
				filterStringActions[0] = new SystemFilterMoveUpFilterStringAction(shell);
		        ((ISystemAction)filterStringActions[0]).setHelp(RSEUIPlugin.HELPPREFIX+"actn0093");
				filterStringActions[1] = new SystemFilterMoveDownFilterStringAction(shell);
		        ((ISystemAction)filterStringActions[1]).setHelp(RSEUIPlugin.HELPPREFIX+"actn0094");
		
			    SystemFilterCopyFilterStringAction copyAction = new SystemFilterCopyFilterStringAction(shell);
			    copyAction.setPromptString(SystemResources.RESID_COPY_TARGET_FILTER_PROMPT));
			    copyAction.setHelp(RSEUIPlugin.HELPPREFIX+"actn0091");
			    copyAction.setDialogHelp(RSEUIPlugin.HELPPREFIX+"dcfs0000");
				filterStringActions[2] = copyAction;
			    SystemFilterMoveFilterStringAction moveAction = new SystemFilterMoveFilterStringAction(shell);
			    moveAction.setPromptString(SystemResources.RESID_MOVE_TARGET_FILTER_PROMPT));
			    moveAction.setHelp(RSEUIPlugin.HELPPREFIX+"actn0093");
			    moveAction.setDialogHelp(RSEUIPlugin.HELPPREFIX+"dmfs0000");
				filterStringActions[3] = moveAction;
			}
		    IAction chgAction = getChangeFilterStringAction(selectedFilterString, shell);
		    if (chgAction != null)
		      childActions.addElement(chgAction);
			for (int idx=0; idx<filterStringActions.length; idx++)
		    {
		    	childActions.addElement(filterStringActions[idx]);
		    }
		
			IAction[] allFilterStringActions = new IAction[childActions.size()];
			for (int idx=0; idx<childActions.size(); idx++)
			   allFilterStringActions[idx] = (IAction)childActions.elementAt(idx);
			
			return allFilterStringActions;
		}*/
		/*
		 * Overridable entry for child classes to contribute filter string actions beyond the
		 * default supplied actions.
		 * <p>
		 * By default, this returns null.
		 * @param selectedFilterString the currently selected filter string
		 * @param shell parent shell of viewer where the popup menu is being constructed
		 * @return Vector of IAction objects.
		 *
		protected Vector getAdditionalFilterStringActions(SystemFilterString selectedFilterString, Shell shell)
		{
			return null;
		}*/
		/*
		 * Overridable entry for child classes to contribute their own change filter string action.
		 * <p>
		 * By default, this returns the default change filter string action.
		 * @param selectedFilterString the currently selected filter string
		 * @param shell parent shell of viewer where the popup menu is being constructed
		 * @return change action.
		 *
		protected IAction getChangeFilterStringAction(SystemFilterString selectedFilterString, Shell shell)
		{
			//IAction chgAction = new SystemFilterDefaultUpdateFilterStringAction(shell);
			//return chgAction;
			return null;
		}*/

		/*
		 * Returns a list of actions for the popup menu when user right clicks on a
		 *  filter string reference object (and has set the preferences to see them).
		 * <p>
		 * Only supported and used by subsystems that support filters.
		 * <p>
		 * Most actions are handled in this base, only override if you have something unique.
		 *
		public IAction[] getFilterStringReferenceActions(SystemFilterStringReference selectedFilterStringRef, Shell shell)
		{
			Vector childActions = new Vector();
			Vector ourChildActions = getAdditionalFilterStringReferenceActions(selectedFilterStringRef, shell);
			if (ourChildActions != null)
			  for (int idx=0; idx<ourChildActions.size(); idx++)
			     childActions.addElement(ourChildActions.elementAt(idx));
		
			if (filterStringReferenceActions == null)
			{
			}
			if (filterStringReferenceActions != null)
			{
		      for (int idx=0; idx<filterStringReferenceActions.length; idx++)
		      {
		    	childActions.addElement(filterStringReferenceActions[idx]);
		      }
			}
			IAction[] allFilterStringRefActions = new IAction[childActions.size()];
			for (int idx=0; idx<childActions.size(); idx++)
			   allFilterStringRefActions[idx] = (IAction)childActions.elementAt(idx);
			
			return allFilterStringRefActions;    	
		}*/

		/*
		 * Overridable entry for child classes to contribute filter string refernce actions beyond the
		 * default supplied actions.
		 * <p>
		 * By default, this returns null.
		 * @param selectedFilterStringRef the currently selected filter string reference
		 * @param shell parent shell of viewer where the popup menu is being constructed
		 * @return Vector of IAction objects.
		 *
		protected Vector getAdditionalFilterStringReferenceActions(SystemFilterStringReference selectedFilterStringRef, Shell shell)
		{
			return null;
		}*/

		// -------------------------
		// SERVER LAUNCH SUPPORT ...
		// -------------------------

		/**
		 * Return the form used in the property page, etc for this server launcher.
		 * Only called if {@link #supportsServerLaunchProperties()} returns true. 
		 * <p>
		 * We return {@link org.eclipse.rse.ui.widgets.ServerLauncherForm}.
		 * Override if appropriate.
		 */
		public IServerLauncherForm getServerLauncherForm(ISubSystemConfiguration factory, Shell shell, ISystemMessageLine msgLine)
		{
			return new RemoteServerLauncherForm(shell, msgLine);
		}

		/**
		 * Called by SystemRegistry's renameSystemProfile method to ensure we update our
		 *  filter pool manager names (and their folders)
		 * <p>
		 * Must be called AFTER changing the profile's name!!
		 */
		public void renameSubSystemProfile(ISubSystemConfiguration factory, String oldProfileName, String newProfileName)
		{
			//RSEUIPlugin.logDebugMessage(this.getClass().getName(), "Inside renameSubSystemProfile. newProfileName = "+newProfileName);
			ISystemProfile profile = factory.getSystemProfile(newProfileName);
			factory.renameFilterPoolManager(profile); // update filter pool manager name
			//if (profile.isDefaultPrivate()) // I don't remember why this was here, but it caused bad things, Phil.
			{
				// Rename the default filter pool for this profile, as it's name is derived from the profile.
				ISystemFilterPool defaultPoolForThisProfile = factory.getDefaultFilterPool(profile, oldProfileName);
				if (defaultPoolForThisProfile != null)
					try
					{
						factory.getFilterPoolManager(profile).renameSystemFilterPool(defaultPoolForThisProfile, SubSystemConfiguration.getDefaultFilterPoolName(newProfileName, factory.getId()));
					}
					catch (Exception exc)
					{
						SystemBasePlugin.logError("Unexpected error renaming default filter pool " + SubSystemConfiguration.getDefaultFilterPoolName(newProfileName, factory.getId()), exc);
						System.out.println("Unexpected error renaming default filter pool " + SubSystemConfiguration.getDefaultFilterPoolName(newProfileName, factory.getId()) + ": " + exc);
					}
			}					
		}

}
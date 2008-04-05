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
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [190271] Move ISystemViewInputProvider to Core
 * Xuan Chen        (IBM)        - [222263] Need to provide a PropertySet Adapter for System Team View
 * David McKnight   (IBM)        - [225506] [api][breaking] RSE UI leaks non-API types
 ********************************************************************************/

package org.eclipse.rse.internal.ui.view;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolReference;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.filters.ISystemFilterString;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemMessageObject;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemViewInputProvider;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISystemDragDropAdapter;
import org.eclipse.rse.internal.ui.view.team.SystemTeamViewCategoryAdapter;
import org.eclipse.rse.internal.ui.view.team.SystemTeamViewCategoryNode;
import org.eclipse.rse.internal.ui.view.team.SystemTeamViewProfileAdapter;
import org.eclipse.rse.internal.ui.view.team.SystemTeamViewPropertySetAdapter;
import org.eclipse.rse.internal.ui.view.team.SystemTeamViewPropertySetNode;
import org.eclipse.rse.internal.ui.view.team.SystemTeamViewSubSystemConfigurationAdapter;
import org.eclipse.rse.internal.ui.view.team.SystemTeamViewSubSystemConfigurationNode;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.internal.model.SystemNewConnectionPromptObject;
import org.eclipse.rse.ui.model.ISystemPromptableObject;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.ui.IActionFilter;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * This factory maps requests for an adapter object from a given
 *  element object.
 */
public class SystemViewAdapterFactory implements IAdapterFactory {

	private SystemViewRootInputAdapter rootAdapter = new SystemViewRootInputAdapter();
	private SystemViewConnectionAdapter connectionAdapter = new SystemViewConnectionAdapter();
	private SystemViewSubSystemAdapter subsystemAdapter = new SystemViewSubSystemAdapter();
	private SystemViewFilterPoolAdapter filterPoolAdapter = new SystemViewFilterPoolAdapter();
	private SystemViewFilterAdapter filterAdapter = new SystemViewFilterAdapter();
	private SystemViewFilterPoolReferenceAdapter filterPoolReferenceAdapter = new SystemViewFilterPoolReferenceAdapter();
	private SystemViewFilterReferenceAdapter filterReferenceAdapter = new SystemViewFilterReferenceAdapter();
	private SystemViewMessageAdapter msgAdapter = new SystemViewMessageAdapter();
	private SystemViewPromptableAdapter promptAdapter = new SystemViewPromptableAdapter();
	private SystemViewNewConnectionPromptAdapter newConnPromptAdapter = new SystemViewNewConnectionPromptAdapter();
	private SystemTeamViewProfileAdapter profileAdapter = new SystemTeamViewProfileAdapter();
	private SystemTeamViewCategoryAdapter categoryAdapter = new SystemTeamViewCategoryAdapter();
	private SystemTeamViewSubSystemConfigurationAdapter subsysFactoryAdapter = new SystemTeamViewSubSystemConfigurationAdapter();
	private SystemTeamViewPropertySetAdapter propertySetAdapter = new SystemTeamViewPropertySetAdapter();

	private SystemViewFilterStringAdapter filterStringAdapter = new SystemViewFilterStringAdapter();

	/**
	 * @see IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return new Class[] { ISystemViewElementAdapter.class, ISystemDragDropAdapter.class, IPropertySource.class, IWorkbenchAdapter.class,
													IActionFilter.class, IDeferredWorkbenchAdapter.class };
	}

	/**
	 * Called by our plugin's startup method to register our adaptable object types 
	 * with the platform. We prefer to do it here to isolate/encapsulate all factory
	 * logic in this one place.
	 * @param manager the adapter manager controlling this factory
	 */
	public void registerWithManager(IAdapterManager manager) {
		manager.registerAdapters(this, ISystemViewInputProvider.class);
		manager.registerAdapters(this, ISystemProfile.class);
		manager.registerAdapters(this, IHost.class);
		manager.registerAdapters(this, ISubSystem.class);
		manager.registerAdapters(this, ISystemFilter.class);
		manager.registerAdapters(this, ISystemFilterPool.class);
		manager.registerAdapters(this, ISystemFilterPoolReference.class);
		manager.registerAdapters(this, ISystemFilterReference.class);
		manager.registerAdapters(this, ISystemFilterString.class);
		manager.registerAdapters(this, ISystemMessageObject.class);
		manager.registerAdapters(this, ISystemPromptableObject.class);
		manager.registerAdapters(this, SystemTeamViewCategoryNode.class);
		manager.registerAdapters(this, SystemTeamViewSubSystemConfigurationNode.class);
		manager.registerAdapters(this, SystemTeamViewPropertySetNode.class);

		// FIXME - UDAs no longer in core
		//manager.registerAdapters(this, SystemTeamViewCompileTypeNode.class);
		//manager.registerAdapters(this, SystemTeamViewCompileCommandNode.class);	
		//manager.registerAdapters(this, SystemUDActionElement.class);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		Object adapter = null;
		if (adaptableObject instanceof ISystemViewElementAdapter)
			adapter = adaptableObject;
		else if (adaptableObject instanceof ISystemDragDropAdapter)
			adapter = adaptableObject;
		else if (adaptableObject instanceof ISystemViewInputProvider)
			adapter = rootAdapter;
		else if (adaptableObject instanceof ISystemProfile)
			adapter = profileAdapter;
		else if (adaptableObject instanceof IHost)
			adapter = connectionAdapter;
		else if (adaptableObject instanceof ISubSystem)
			adapter = subsystemAdapter;
		else if (adaptableObject instanceof ISystemFilterPoolReference)
			adapter = filterPoolReferenceAdapter;
		else if (adaptableObject instanceof ISystemFilterPool)
			adapter = filterPoolAdapter;
		else if (adaptableObject instanceof ISystemFilterReference)
			adapter = filterReferenceAdapter;
		else if (adaptableObject instanceof ISystemFilterString)
			adapter = filterStringAdapter;
		else if (adaptableObject instanceof ISystemFilter)
			adapter = filterAdapter;
		else if (adaptableObject instanceof ISystemMessageObject)
			adapter = msgAdapter;
		else if (adaptableObject instanceof ISystemPromptableObject) {

			if (adaptableObject instanceof SystemNewConnectionPromptObject) {
				adapter = newConnPromptAdapter;
			} else {
				adapter = promptAdapter;
			}
		} else if (adaptableObject instanceof SystemTeamViewCategoryNode)
			adapter = categoryAdapter;
		else if (adaptableObject instanceof SystemTeamViewSubSystemConfigurationNode) adapter = subsysFactoryAdapter;
		else if (adaptableObject instanceof SystemTeamViewPropertySetNode) adapter = propertySetAdapter;

		/** FIXME - UDAs no longer in core
		 else if (adaptableObject instanceof SystemTeamViewCompileTypeNode)
		 adapter = getCompileTypeAdapter();
		 else if (adaptableObject instanceof SystemTeamViewCompileCommandNode)
		 adapter = getCompileCommandAdapter();	      	      
		 else if (adaptableObject instanceof SystemUDActionElement)
		 adapter = getUserActionAdapter();
		 */

		if ((adapter != null) && (adapterType == IPropertySource.class)) {
			((ISystemViewElementAdapter) adapter).setPropertySourceInput(adaptableObject);
		} else if (adapter == null) {
			SystemBasePlugin.logWarning("No adapter found for object of type: " + adaptableObject.getClass().getName()); //$NON-NLS-1$
		}
		return adapter;
	}


}
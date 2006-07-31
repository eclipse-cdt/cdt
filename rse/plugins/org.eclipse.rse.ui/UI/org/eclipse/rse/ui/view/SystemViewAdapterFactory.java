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

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolReference;
import org.eclipse.rse.filters.ISystemFilterReference;
import org.eclipse.rse.filters.ISystemFilterString;
import org.eclipse.rse.internal.model.SystemNewConnectionPromptObject;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemMessageObject;
import org.eclipse.rse.model.ISystemProfile;
import org.eclipse.rse.model.ISystemPromptableObject;
import org.eclipse.rse.ui.view.team.SystemTeamViewCategoryAdapter;
import org.eclipse.rse.ui.view.team.SystemTeamViewCategoryNode;
import org.eclipse.rse.ui.view.team.SystemTeamViewProfileAdapter;
import org.eclipse.rse.ui.view.team.SystemTeamViewSubSystemConfigurationAdapter;
import org.eclipse.rse.ui.view.team.SystemTeamViewSubSystemConfigurationNode;
import org.eclipse.ui.IActionFilter;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.views.properties.IPropertySource;


/**
 * This factory maps requests for an adapter object from a given
 *  element object.
 */
public class SystemViewAdapterFactory implements IAdapterFactory 
{

	private SystemViewRootInputAdapter     rootAdapter  = new SystemViewRootInputAdapter();
	private SystemViewConnectionAdapter    connectionAdapter= new SystemViewConnectionAdapter();
	private SystemViewSubSystemAdapter     subsystemAdapter = new SystemViewSubSystemAdapter();	
	private SystemViewFilterPoolAdapter    filterPoolAdapter= new SystemViewFilterPoolAdapter();			
	private SystemViewFilterAdapter        filterAdapter    = new SystemViewFilterAdapter();		
	private SystemViewFilterPoolReferenceAdapter   filterPoolReferenceAdapter= new SystemViewFilterPoolReferenceAdapter();		
	private SystemViewFilterReferenceAdapter       filterReferenceAdapter    = new SystemViewFilterReferenceAdapter();	
	private SystemViewMessageAdapter               msgAdapter = new SystemViewMessageAdapter();
	private SystemViewPromptableAdapter            promptAdapter = new SystemViewPromptableAdapter();
	private SystemViewNewConnectionPromptAdapter   newConnPromptAdapter = new SystemViewNewConnectionPromptAdapter();
	private SystemTeamViewProfileAdapter           profileAdapter= new SystemTeamViewProfileAdapter();
	private SystemTeamViewCategoryAdapter          categoryAdapter;
	private SystemTeamViewSubSystemConfigurationAdapter  subsysFactoryAdapter;
	
	private SystemViewFilterStringAdapter          filterStringAdapter       = new SystemViewFilterStringAdapter();	

	/**
	 * @see IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() 
	{		
	    return new Class[] {
	            ISystemViewElementAdapter.class, 
	            ISystemDragDropAdapter.class, 
	            IPropertySource.class, 
	            IWorkbenchAdapter.class, 
	            IActionFilter.class,
	            IDeferredWorkbenchAdapter.class
	            };		
	}
	/**
	 * Called by our plugin's startup method to register our adaptable object types 
	 * with the platform. We prefer to do it here to isolate/encapsulate all factory
	 * logic in this one place.
	 */
	public void registerWithManager(IAdapterManager manager)
	{
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
		
		// FIXME - UDAs no longer in core
		//manager.registerAdapters(this, SystemTeamViewCompileTypeNode.class);
		//manager.registerAdapters(this, SystemTeamViewCompileCommandNode.class);	
		//manager.registerAdapters(this, SystemUDActionElement.class);
	}
	/**
	 * @see IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) 
	{
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
	      	}
	      	else {
	      		adapter = promptAdapter;
	      	}
	      }
		  else if (adaptableObject instanceof SystemTeamViewCategoryNode)
		  	adapter = getCategoryAdapter();
		  else if (adaptableObject instanceof SystemTeamViewSubSystemConfigurationNode)
		    adapter = getSubSystemConfigurationAdapter();
	      
	      /** FIXME - UDAs no longer in core
		  else if (adaptableObject instanceof SystemTeamViewCompileTypeNode)
		    adapter = getCompileTypeAdapter();
		  else if (adaptableObject instanceof SystemTeamViewCompileCommandNode)
		    adapter = getCompileCommandAdapter();	      	      
		  else if (adaptableObject instanceof SystemUDActionElement)
		    adapter = getUserActionAdapter();
		    */
		  	        	        
	      if ((adapter != null) && (adapterType == IPropertySource.class))
	      {	
	        ((ISystemViewElementAdapter)adapter).setPropertySourceInput(adaptableObject);
	      }		
	      else if (adapter == null)
	      {
	      	SystemBasePlugin.logWarning("No adapter found for object of type: " + adaptableObject.getClass().getName());
	      }	     	    
		return adapter;
	}

    /**
     * Because we use singletons for our adapters, it is possible to speed up 
     * access to them by simply returning them from here.
     * <p>
     * This method returns the RSE adapter for root inputs to the RSE
	 * @return SystemViewRootInputAdapter
     */
    public SystemViewRootInputAdapter getRootInputAdapter()
    {
    	return rootAdapter;
    }
    
	/**
     * Because we use singletons for our adapters, it is possible to speed up 
     * access to them by simply returning them from here.
     * <p>
     * This method returns the RSE adapter for connection objects
	 * @return SystemViewConnectionAdapter
	 */
	public SystemViewConnectionAdapter getConnectionAdapter()
	{
		return connectionAdapter;
	}

	/**
	 * Because we use singletons for our adapters, it is possible to speed up 
	 * access to them by simply returning them from here.
	 * <p>
	 * This method returns the RSE adapter for profile objects
	 * @return SystemViewProfileAdapter
	 */
	public SystemTeamViewProfileAdapter getProfileAdapter()
	{
		return profileAdapter;
	}
	
	/**
     * Because we use singletons for our adapters, it is possible to speed up 
     * access to them by simply returning them from here.
     * <p>
     * This method returns the RSE adapter for filters
	 * @return SystemViewFilterAdapter
	 */
	public SystemViewFilterAdapter getFilterAdapter()
	{
		return filterAdapter;
	}

	/**
     * Because we use singletons for our adapters, it is possible to speed up 
     * access to them by simply returning them from here.
     * <p>
     * This method returns the RSE adapter for filter pools
	 * @return SystemViewFilterPoolAdapter
	 */
	public SystemViewFilterPoolAdapter getFilterPoolAdapter()
	{
		return filterPoolAdapter;
	}

	/**
     * Because we use singletons for our adapters, it is possible to speed up 
     * access to them by simply returning them from here.
     * <p>
     * This method returns the RSE adapter for filter pool references, which
     * are what we actually see in the RSE.
	 * @return SystemViewFilterPoolReferenceAdapter
	 */
	public SystemViewFilterPoolReferenceAdapter getFilterPoolReferenceAdapter()
	{
		return filterPoolReferenceAdapter;
	}

	/**
     * Because we use singletons for our adapters, it is possible to speed up 
     * access to them by simply returning them from here.
     * <p>
     * This method returns the RSE adapter for filter references, which are
     *  what we actually see in the RSE
	 * @return SystemViewFilterReferenceAdapter
	 */
	public SystemViewFilterReferenceAdapter getFilterReferenceAdapter()
	{
		return filterReferenceAdapter;
	}

	/**
     * Because we use singletons for our adapters, it is possible to speed up 
     * access to them by simply returning them from here.
     * <p>
     * This method returns the RSE adapter for messages shown in the RSE as child objects
	 * @return SystemViewMessageAdapter
	 */
	public SystemViewMessageAdapter getMsgAdapter()
	{
		return msgAdapter;
	}

	/**
     * Because we use singletons for our adapters, it is possible to speed up 
     * access to them by simply returning them from here.
     * <p>
     * This method returns the RSE adapter for promptable objects the run an action when expanded
	 * @return SystemViewPromptableAdapter
	 */
	public SystemViewPromptableAdapter getPromptAdapter()
	{
		return promptAdapter;
	}

	/**
     * Because we use singletons for our adapters, it is possible to speed up 
     * access to them by simply returning them from here.
     * <p>
     * This method returns the RSE adapter for subsystems
	 * @return SystemViewSubSystemAdapter
	 */
	public SystemViewSubSystemAdapter getSubsystemAdapter()
	{
		return subsystemAdapter;
	}
	
	/**
	 * Return adapter for category nodes in team view
	 */	
	public SystemTeamViewCategoryAdapter getCategoryAdapter()
	{
		if (categoryAdapter == null)
			categoryAdapter = new SystemTeamViewCategoryAdapter();
		return categoryAdapter;
	}
	/**
	 * Return adapter for subsystem factory nodes in team view
	 */	
	public SystemTeamViewSubSystemConfigurationAdapter getSubSystemConfigurationAdapter()
	{
		if (subsysFactoryAdapter == null)
			subsysFactoryAdapter = new SystemTeamViewSubSystemConfigurationAdapter();
		return subsysFactoryAdapter;
	}
	
// FIXME user actions and compile commands no longer coupled with core	
//	/**
//	 * Return adapter for user actions nodes in team view
//	 */	
//	public SystemTeamViewUserActionAdapter getUserActionAdapter()
//	{
//		if (userActionAdapter == null)
//			userActionAdapter = new SystemTeamViewUserActionAdapter();
//		return userActionAdapter;
//	}
//
//	/**
//	 * Return adapter for compile type nodes in team view
//	 */	
//	public SystemTeamViewCompileTypeAdapter getCompileTypeAdapter()
//	{
//		if (compileTypeAdapter == null)
//			compileTypeAdapter = new SystemTeamViewCompileTypeAdapter();
//		return compileTypeAdapter;
//	}
//	/**
//	 * Return adapter for compile command nodes in team view
//	 */	
//	public SystemTeamViewCompileCommandAdapter getCompileCommandAdapter()
//	{
//		if (compileCmdAdapter == null)
//			compileCmdAdapter = new SystemTeamViewCompileCommandAdapter();
//		return compileCmdAdapter;
//	}
	
	/**
     * Because we use singletons for our adapters, it is possible to speed up 
     * access to them by simply returning them from here.
     * <p>
     * This method returns the RSE adapter for filter strings
	 * @return SystemViewFilterStringAdapter
	 */
	public SystemViewFilterStringAdapter getFilterStringAdapter()
	{
		return filterStringAdapter;
	}
}
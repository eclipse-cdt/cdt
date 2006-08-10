package org.eclipse.rse.examples.daytime.ui;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.rse.core.subsystems.util.ISubsystemConfigurationAdapter;
import org.eclipse.rse.examples.daytime.subsystems.DaytimeSubsystemConfiguration;

public class DaytimeSubSystemConfigurationAdapterFactory implements IAdapterFactory {

	private ISubsystemConfigurationAdapter ssFactoryAdapter = new DaytimeSubSystemConfigurationAdapter();
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() 
	{
	    return new Class[] {ISubsystemConfigurationAdapter.class};		
	}
	
	/**
	 * Called by our plugin's startup method to register our adaptable object types 
	 * with the platform. We prefer to do it here to isolate/encapsulate all factory
	 * logic in this one place.
	 * 
	 * @param manager Platform adapter manager to register with
	 */
	public void registerWithManager(IAdapterManager manager)
	{
		manager.registerAdapters(this, DaytimeSubsystemConfiguration.class);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) 
	{
	    Object adapter = null;
	    if (adaptableObject instanceof DaytimeSubsystemConfiguration)
	    	adapter = ssFactoryAdapter;
	      	    
		return adapter;
	}

}

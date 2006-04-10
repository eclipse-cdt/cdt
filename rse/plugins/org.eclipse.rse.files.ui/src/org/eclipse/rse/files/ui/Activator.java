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

package org.eclipse.rse.files.ui;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.files.ui.resources.SystemRemoteEditManager;
import org.eclipse.rse.files.ui.resources.SystemUniversalTempFileListener;
import org.eclipse.rse.files.ui.view.RemoteFileSubsystemFactoryAdapterFactory;
import org.eclipse.rse.files.ui.view.SystemViewFileAdapterFactory;
import org.eclipse.rse.files.ui.view.SystemViewSearchResultAdapterFactory;
import org.eclipse.rse.files.ui.view.SystemViewSearchResultSetAdapterFactory;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


/**
 * The main plugin class to be used in the desktop.
 */
public class Activator extends AbstractUIPlugin {

	//The shared instance.
	private static Activator plugin;
	
	private static SystemUniversalTempFileListener _tempFileListener;
	
	private SystemViewFileAdapterFactory svfaf; // for fastpath
    private SystemViewSearchResultSetAdapterFactory svsaf; // for fastpath
	private SystemViewSearchResultAdapterFactory svsraf; // for fastpath
	
	/**
	 * The constructor.
	 */
	public Activator() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);		

	    // refresh the remote edit project at plugin startup, to ensure
	    // it's never closed
		SystemRemoteEditManager.getDefault().refreshRemoteEditProject();
		

    	int eventMask = IResourceChangeEvent.POST_CHANGE;	
    	IWorkspace ws = SystemBasePlugin.getWorkspace();
  
    	
    	

	    IAdapterManager manager = Platform.getAdapterManager();
	    
	    svfaf = new SystemViewFileAdapterFactory();	
	    svfaf.registerWithManager(manager);	
	
	
	
	    svsaf = new SystemViewSearchResultSetAdapterFactory();	
	    svsaf.registerWithManager(manager);	
	    
		svsraf = new SystemViewSearchResultAdapterFactory();	
		svsraf.registerWithManager(manager);	
		
	    RemoteFileSubsystemFactoryAdapterFactory rfssfaf = new RemoteFileSubsystemFactoryAdapterFactory();
	    rfssfaf.registerWithManager(manager);
	    
	    // universal temp file listener
	    _tempFileListener = SystemUniversalTempFileListener.getListener();	
	  	// add listener for temp files
    	ws.addResourceChangeListener(_tempFileListener, eventMask);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception 
	{
		super.stop(context);
		
		IWorkspace ws = SystemBasePlugin.getWorkspace();
		ws.removeResourceChangeListener(_tempFileListener);
  	  	_tempFileListener = null;
		plugin = null;
		
		
		
	}

	/**
	 * Returns the shared instance.
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.rse.files.ui", path);
	}
	
	  /**
     * For pathpath access to our adapters for remote universal file objects. Exploits the knowledge we use singleton adapters.
     */
    public SystemViewFileAdapterFactory getSystemViewFileAdapterFactory()
    {
    	return svfaf;
    }

    /**
     * For pathpath access to our adapters for searchable result output objects. Exploits the knowledge we use singleton adapters.
     */
    public SystemViewSearchResultSetAdapterFactory getSystemViewSearchResultSetAdapterFactory()
    {
    	return svsaf;
    }
    
	/**
	  * For pathpath access to our adapters for searchable result output objects. Exploits the knowledge we use singleton adapters.
	  */
	 public SystemViewSearchResultAdapterFactory getSystemViewSearchResultAdapterFactory()
	 {
		 return svsraf;
	 }
}
package org.eclipse.cdt.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.cdt.core.index.IndexModel;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.internal.core.CProjectDescriptor;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;


public class CCorePlugin extends Plugin {
	
	public static final int STATUS_CDTPROJECT_EXISTS = 1;
	
	public static final String PLUGIN_ID= "org.eclipse.cdt.core";
	public static final String BUILDER_ID= PLUGIN_ID + ".cbuilder";
		
	private static CCorePlugin fgCPlugin;
	private static ResourceBundle fgResourceBundle;

	// -------- static methods --------
	
	static {
		try {
			fgResourceBundle= ResourceBundle.getBundle("org.eclipse.cdt.internal.CCorePluginResources");
		} catch (MissingResourceException x) {
			fgResourceBundle= null;
		}
	}

	public static String getResourceString(String key) {
		try {
			return fgResourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!";
		} catch (NullPointerException e) {
			return "#" + key + "#";
		}
	}
	
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}	
	
	public static String getFormattedString(String key, String arg) {
		return MessageFormat.format(getResourceString(key), new String[] { arg });
	}
	
	public static String getFormattedString(String key, String[] args) {
		return MessageFormat.format(getResourceString(key), args);
	}
	
	public static ResourceBundle getResourceBundle() {
		return fgResourceBundle;
	}
		
	public static CCorePlugin getDefault() {
		return fgCPlugin;
	}
	
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, "Error", e));
	}
	
	public static void log(IStatus status) {
		((Plugin)getDefault()).getLog().log(status);
	}	
		
	// ------ CPlugin

	public CCorePlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		fgCPlugin= this;
	}
		
	/**
	 * @see Plugin#shutdown
	 */
	public void shutdown() throws CoreException {
		super.shutdown();
	}		
	
	/**
	 * @see Plugin#startup
	 */
	public void startup() throws CoreException {
		super.startup();

		// Fired up the model.
		getCoreModel();
		// Fired up the indexer. It should delay itself for 10 seconds
		getIndexModel();
	}
	
	public IConsole getConsole() throws CoreException {
		IConsole consoleDocument = null;

		IExtensionPoint extension = getDescriptor().getExtensionPoint("CBuildConsole");
		if (extension != null) {
			IExtension[] extensions =  extension.getExtensions();
			for(int i = 0; i < extensions.length; i++){
				IConfigurationElement [] configElements = extensions[i].getConfigurationElements();
				consoleDocument = (IConsole)configElements[0].createExecutableExtension("class");
			}
		}		
		if ( consoleDocument == null ) {
			return new IConsole() {
				public void clear() {
				}
				public void start(IProject project) {
				}
				public ConsoleOutputStream getOutputStream() {
					return new ConsoleOutputStream();
				}
			};
		}
		return consoleDocument;
	}
	
	public CoreModel getCoreModel() {
		return CoreModel.getDefault();
	}

	public IndexModel getIndexModel() {
		return IndexModel.getDefault();
	}	
	
	public ICProjectDescriptor getCProjectDescription(IProject project) throws CoreException {
		return CProjectDescriptor.getDescription(project);
	}
	
	public void mapCProjectOwner(IProject project, String id) throws CoreException {
		CProjectDescriptor.configure(project, id);
	}
        
    /**
     * Creates a C project resource given the project handle and description.
     *
     * @param description the project description to create a project resource for
     * @param projectHandle the project handle to create a project resource for
     * @param monitor the progress monitor to show visual progress with
     * @param projectID required for mapping the project to an owner
     *
     * @exception CoreException if the operation fails
     * @exception OperationCanceledException if the operation is canceled
     */
    public IProject createCProject(IProjectDescription description, IProject projectHandle,
        IProgressMonitor monitor, String projectID) throws CoreException, OperationCanceledException {
        try {
            if (monitor == null) {
                monitor = new NullProgressMonitor();
            }
            monitor.beginTask("Creating C Project", 3);//$NON-NLS-1$
            if (!projectHandle.exists()){
                projectHandle.create(description, monitor);               
            }

            if (monitor.isCanceled()){
                throw new OperationCanceledException();
            }
            
            // Open first.
            projectHandle.open(monitor);
           
            // Add C Nature ... does not add duplicates
            CProjectNature.addCNature(projectHandle, new SubProgressMonitor(monitor, 1));
            mapCProjectOwner(projectHandle, projectID);
        } finally {
            //monitor.done();
        }
        return projectHandle;
    }

    /**
     * Method convertProjectFromCtoCC converts
     * a C Project to a C++ Project
     * The newProject MUST, not be null, already have a C Nature 
     * && must NOT already have a C++ Nature
     * 
     * @param projectHandle
     * @param monitor
     * @param projectID
     * @throws CoreException
     */
    
    public void convertProjectFromCtoCC(IProject projectHandle, IProgressMonitor monitor, String projectID)
    throws CoreException{
        if ((projectHandle != null) 
                && projectHandle.hasNature(CCProjectNature.C_NATURE_ID)
                && !projectHandle.hasNature(CCProjectNature.CC_NATURE_ID)) {
            // Add C++ Nature ... does not add duplicates        
            CCProjectNature.addCCNature(projectHandle, monitor);
            
            if(projectID != null){
                mapCProjectOwner(projectHandle, projectID);
            }
         } 
    }
    /**
     * Method convertProjectFromCtoCC converts
     * a C Project to a C++ Project
     * The newProject MUST, not be null, already have a C Nature 
     * && must NOT already have a C++ Nature<br>
     * This method does not map the project to an owner and should only
     * be used when this mapping has already taken place.
     * 
     * @param projectHandle
     * @param monitor
     * @throws CoreException
     */
    
    public void convertProjectFromCtoCC(IProject projectHandle, IProgressMonitor monitor)
    throws CoreException{
                
        convertProjectFromCtoCC(projectHandle, monitor, null);
    }
	/**
	 * Method addDefaultCBuilder adds the default C make builder
	 * @param projectHandle
	 * @param monitor
     * @exception CoreException
	 */
    public void addDefaultCBuilder( IProject projectHandle, IProgressMonitor monitor) 
        throws CoreException{
        // Set the Default C Builder.
        CProjectNature.addCBuildSpec(projectHandle, monitor);
    }
    
    /**
     * Method to convert a project to a C nature 
     * & default make builder
     * All checks should have been done externally
     * (as in the Conversion Wizards). 
     * This method blindly does the conversion.
     * 
     * @param project
     * @param String targetNature
     * @param monitor
     * @param projectID
     * @exception CoreException
     */
    
    public void convertProjectToC(IProject projectHandle, IProgressMonitor monitor, String projectID)
    throws CoreException{
        if ((projectHandle == null) || (monitor == null) || (projectID == null)){
            return;
        }
        IWorkspace workspace = ResourcesPlugin.getWorkspace();        
        IProjectDescription description = workspace.newProjectDescription(projectHandle.getName());
        description.setLocation(projectHandle.getFullPath());
        createCProject(description, projectHandle, monitor, projectID);
        addDefaultCBuilder(projectHandle, monitor);
    }
    /**
     * Method to convert a project to a C++ nature 
     * & default make builder, if it does not have one already
     * 
     * @param project
     * @param String targetNature
     * @param monitor
     * @param projectID
     * @exception CoreException
     */
    
    public void convertProjectToCC(IProject projectHandle, IProgressMonitor monitor, String projectID)
    throws CoreException{
        if ((projectHandle == null) || (monitor == null) || (projectID == null)){
            return;
        }
        createCProject(projectHandle.getDescription(), projectHandle, monitor, projectID);
        // now add C++ nature
        convertProjectFromCtoCC(projectHandle, monitor);
        addDefaultCBuilder(projectHandle, monitor);
    }
}

package org.eclipse.cdt.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.cdt.core.index.IndexModel;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.internal.core.BinaryParserConfiguration;
import org.eclipse.cdt.internal.core.CDescriptorManager;
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
	public static final int STATUS_CDTPROJECT_MISMATCH = 2;
	public static final int CDT_PROJECT_NATURE_ID_MISMATCH = 3;
	
	public static final String PLUGIN_ID= "org.eclipse.cdt.core";

	public static final String BUILDER_MODEL_ID= PLUGIN_ID + ".CBuildModel";

	private static CCorePlugin fgCPlugin;
	private static ResourceBundle fgResourceBundle;

	private CDescriptorManager fDescriptorManager;

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
		fDescriptorManager.shutdown();
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
		fDescriptorManager = new CDescriptorManager();
		fDescriptorManager.startup();
	}

	public IConsole getConsole(String id) {
		try {
			IExtensionPoint extension = getDescriptor().getExtensionPoint("CBuildConsole");
			if (extension != null) {
				IExtension[] extensions =  extension.getExtensions();
				for(int i = 0; i < extensions.length; i++){
					IConfigurationElement [] configElements = extensions[i].getConfigurationElements();
					for( int j = 0; j < configElements.length; j++ ) {
						String builderID = configElements[j].getAttribute("builderID");
						if ( (id == null && builderID == null) || 
							 ( id != null && builderID.equals(id))) {
							return (IConsole)configElements[j].createExecutableExtension("class");
						 }
					}
				}
			}	
		} catch (CoreException e) {
		} 
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

	public IConsole getConsole() throws CoreException {
		return getConsole(null);
	}

	public IBinaryParserConfiguration[] getBinaryParserConfigurations() {
		ArrayList list = new ArrayList();
		IExtensionPoint extensionPoint = getDescriptor().getExtensionPoint("BinaryParser");
		if (extensionPoint != null) {
			IExtension[] extensions =  extensionPoint.getExtensions();
			for(int i = 0; i < extensions.length; i++){
				IConfigurationElement [] configElements = extensions[i].getConfigurationElements();
				for( int j = 0; j < configElements.length; j++ ) {
					String format = configElements[j].getAttribute("format");
					String name = configElements[j].getAttribute("name");
					list.add(new BinaryParserConfiguration(format, name));
				}
			}
		}	
		return (IBinaryParserConfiguration[])list.toArray(new IBinaryParserConfiguration[0]);
	}
	
	public IBinaryParser getBinaryParser(String format) {
		try {
			IExtensionPoint extensionPoint = getDescriptor().getExtensionPoint("BinaryParser");
			if (extensionPoint != null) {
				IExtension[] extensions =  extensionPoint.getExtensions();
				for(int i = 0; i < extensions.length; i++){
					IConfigurationElement [] configElements = extensions[i].getConfigurationElements();
					for( int j = 0; j < configElements.length; j++ ) {
						String attr = configElements[j].getAttribute("format");
						if (attr != null && attr.equalsIgnoreCase(format)) {
							return (IBinaryParser)configElements[j].createExecutableExtension("class");
						}
					}
				}
			}	
		} catch (CoreException e) {
		} 
		return null;
	}

	public CoreModel getCoreModel() {
		return CoreModel.getDefault();
	}

	public IndexModel getIndexModel() {
		return IndexModel.getDefault();
	}	
	
	public ICDescriptor getCProjectDescription(IProject project) throws CoreException {
		return fDescriptorManager.getDescriptor(project);
	}
	
	public void mapCProjectOwner(IProject project, String id, boolean override) throws CoreException {
		if ( !override ) {
			fDescriptorManager.configure(project, id);
		} else {
			fDescriptorManager.convert(project, id);
		}
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
            mapCProjectOwner(projectHandle, projectID, false);
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
     * @throws CoreException
     */
    
    public void convertProjectFromCtoCC(IProject projectHandle, IProgressMonitor monitor)
    throws CoreException{
        if ((projectHandle != null) 
                && projectHandle.hasNature(CCProjectNature.C_NATURE_ID)
                && !projectHandle.hasNature(CCProjectNature.CC_NATURE_ID)) {
            // Add C++ Nature ... does not add duplicates        
            CCProjectNature.addCCNature(projectHandle, monitor);
         } 
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
     * & default make builder (Will always add a default builder)
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
    	this.convertProjectToC(projectHandle, monitor, projectID, true);

    }   
    /**
     * Method to convert a project to a C nature 
     * & default make builder (if indicated)
     * All checks should have been done externally
     * (as in the Conversion Wizards). 
     * This method blindly does the conversion.
     * 
     * @param project
     * @param String targetNature
     * @param monitor
     * @param projectID
     * @param addMakeBuilder
     * @exception CoreException
     */
    
    public void convertProjectToC(IProject projectHandle, IProgressMonitor monitor, String projectID, boolean addMakeBuilder)
    throws CoreException{
        if ((projectHandle == null) || (monitor == null) || (projectID == null)){
            return;
        }
        IWorkspace workspace = ResourcesPlugin.getWorkspace();        
        IProjectDescription description = workspace.newProjectDescription(projectHandle.getName());
        description.setLocation(projectHandle.getFullPath());
        createCProject(description, projectHandle, monitor, projectID);
        if (addMakeBuilder) {
        	addDefaultCBuilder(projectHandle, monitor);
        }
    }
    /**
     * Method to convert a project to a C++ nature 
     * & default make builder(if indicated), if it does not have one already
     * 
     * @param project
     * @param String targetNature
     * @param monitor
     * @param projectID
     * @param addMakeBuilder
     * @exception CoreException
     */
    
    public void convertProjectToCC(IProject projectHandle, IProgressMonitor monitor, String projectID, boolean addMakeBuilder)
    throws CoreException{
        if ((projectHandle == null) || (monitor == null) || (projectID == null)){
            return;
        }
        createCProject(projectHandle.getDescription(), projectHandle, monitor, projectID);
        // now add C++ nature
        convertProjectFromCtoCC(projectHandle, monitor);
        if (addMakeBuilder){
        	addDefaultCBuilder(projectHandle, monitor);
        }
    }
        /**
     * Method to convert a project to a C++ nature 
     * & default make builder,
     * Note: Always adds the default Make builder
     * 
     * @param project
     * @param String targetNature
     * @param monitor
     * @param projectID
     * @exception CoreException
     */
    
    public void convertProjectToCC(IProject projectHandle, IProgressMonitor monitor, String projectID)
    throws CoreException{
    	this.convertProjectToCC(projectHandle, monitor, projectID, true);
    }
 
// Extract the builder from the .cdtproject.  
//	public ICBuilder[] getBuilders(IProject project) throws CoreException {
//		ICExtension extensions[] = fDescriptorManager.createExtensions(BUILDER_MODEL_ID, project);
//		ICBuilder builders[] = new ICBuilder[extensions.length];
//		System.arraycopy(extensions, 0, builders, 0, extensions.length);
//		return builders;
//	}
	
	public IProcessList getProcessList() {
		IExtensionPoint extension = getDescriptor().getExtensionPoint("ProcessList");
		if (extension != null) {
			IExtension[] extensions =  extension.getExtensions();
			IConfigurationElement [] configElements = extensions[0].getConfigurationElements();
			if ( configElements.length != 0 ) {
				try {
					return (IProcessList) configElements[0].createExecutableExtension("class");
				}
				catch (CoreException e) {
				}
			}
		}
		return null;
	}
}

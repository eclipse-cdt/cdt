/**********************************************************************
 * Copyright (c) 2004 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ManagedProject extends BuildObject implements IManagedProject {
	
	private static final String EMPTY_STRING = new String();
	private static final IConfiguration[] emptyConfigs = new IConfiguration[0];
	
	//  Parent and children
	private IProjectType projectType;
	private String projectTypeId;
	private IResource owner;
	private List configList;	//  Configurations of this project type
	private Map configMap;
	//  Miscellaneous
	private boolean isDirty = false;
	private boolean resolved = true;

	/*
	 *  C O N S T R U C T O R S
	 */

	/* (non-Javadoc)
	 * Sets the Eclipse project that owns the Managed Project
	 * 
	 * @param owner
	 */
	protected ManagedProject(IResource owner) {
		this.owner = owner;
	}
	
	/**
	 * Create a project instance from the project-type specified in the argument, 
	 * that is owned by the specified Eclipse project.
	 * 
	 * @param owner  the Eclipse project that owns the Managed Project
	 * @param projectType
	 */
	public ManagedProject(IResource owner, IProjectType projectType) {
		// Make the owner of the ProjectType the project resource
		this(owner);
		
		// Copy the parent's identity
		this.projectType = projectType;
		int id = ManagedBuildManager.getRandomNumber();
		setId(owner.getName() + "." + projectType.getId() + "." + id);		 //$NON-NLS-1$ //$NON-NLS-2$
		setName(projectType.getName());

		// Hook me up
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(owner);
		buildInfo.setManagedProject(this);
		setDirty(true);
	}

	/**
	 * Create the project instance from project file.
	 * 
	 * @param buildInfo
	 * @param element
	 */
	public ManagedProject(ManagedBuildInfo buildInfo, Element element) {
		this(buildInfo.getOwner());
		
		// Initialize from the XML attributes
		loadFromProject(element);

		// Load children
		NodeList configElements = element.getChildNodes();
		for (int i = 0; i < configElements.getLength(); ++i) {
			Node configElement = configElements.item(i);
			if (configElement.getNodeName().equals(IConfiguration.CONFIGURATION_ELEMENT_NAME)) {
				Configuration config = new Configuration(this, (Element)configElement);
			}
		}
		
		// hook me up
		buildInfo.setManagedProject(this);
	}

	/*
	 *  E L E M E N T   A T T R I B U T E   R E A D E R S   A N D   W R I T E R S
	 */
	
	/* (non-Javadoc)
	 * Initialize the project information from the XML element 
	 * specified in the argument
	 * 
	 * @param element An XML element containing the project information 
	 */
	protected void loadFromProject(Element element) {
		
		// id
		setId(element.getAttribute(IBuildObject.ID));

		// name
		if (element.hasAttribute(IBuildObject.NAME)) {
			setName(element.getAttribute(IBuildObject.NAME));
		}
		
		// projectType
		projectTypeId = element.getAttribute(PROJECTTYPE);
		if (projectTypeId != null && projectTypeId.length() > 0) {
			projectType = ManagedBuildManager.getExtensionProjectType(projectTypeId);
			if (projectType == null) {
				// TODO:  Report error
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedProject#serialize()
	 */
	public void serialize(Document doc, Element element) {
		element.setAttribute(IBuildObject.ID, id);
		
		if (name != null) {
			element.setAttribute(IBuildObject.NAME, name);
		}

		if (projectType != null) {
			element.setAttribute(PROJECTTYPE, projectType.getId());
		}
		
		// Serialize my children
		List configElements = getConfigurationList();
		Iterator iter = configElements.listIterator();
		while (iter.hasNext()) {
			Configuration config = (Configuration) iter.next();
			Element configElement = doc.createElement(IConfiguration.CONFIGURATION_ELEMENT_NAME);
			element.appendChild(configElement);
			config.serialize(doc, configElement);
		}
		
		// I am clean now
		isDirty = false;
	}

	/*
	 *  P A R E N T   A N D   C H I L D   H A N D L I N G
	 */

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedProject#getOwner()
	 */
	public IResource getOwner() {
		return owner;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedProject#updateOwner(org.eclipse.core.resources.IResource)
	 */
	public void updateOwner(IResource resource) {
		if (!resource.equals(owner)) {
			// Set the owner correctly
			owner = resource;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedProject#getProjectType()
	 */
	public IProjectType getProjectType() {
		return projectType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedProject#createConfiguration(org.eclipse.cdt.core.build.managed.IConfiguration)
	 */
	public IConfiguration createConfiguration(IConfiguration parent, String id) {
		Configuration config = new Configuration(this, (Configuration)parent, id, false);
		return (IConfiguration)config;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedProject#createConfigurationClone(org.eclipse.cdt.core.build.managed.IConfiguration)
	 */
	public IConfiguration createConfigurationClone(IConfiguration parent, String id) {
		Configuration config = new Configuration(this, (Configuration)parent, id, true);
		return (IConfiguration)config;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedProject#getConfiguration()
	 */
	public IConfiguration getConfiguration(String id) {
		return (IConfiguration)getConfigurationMap().get(id);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedProject#getConfigurations()
	 */
	public IConfiguration[] getConfigurations() {
		IConfiguration[] configs = new IConfiguration[getConfigurationList().size()];
		Iterator iter = getConfigurationList().listIterator();
		int i = 0;
		while (iter.hasNext()) {
			Configuration config = (Configuration)iter.next();
			configs[i++] = (IConfiguration)config; 
		}
		return configs;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedProject#removeConfiguration(java.lang.String)
	 */
	public void removeConfiguration(String id) {
		final String removeId = id;
		IWorkspaceRunnable remover = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				// Remove the specified configuration from the list and map
				Iterator iter = getConfigurationList().listIterator();
				while (iter.hasNext()) {
					 IConfiguration config = (IConfiguration)iter.next();
					 if (config.getId().equals(removeId)) {
						// TODO:  For now we clean the entire project.  This may be overkill, but
						//        it avoids a problem with leaving the configuration output directory
					 	//        around and having the outputs try to be used by the makefile generator code.
					 	IResource proj = config.getOwner();
						IManagedBuildInfo info = null;
					 	if (proj instanceof IProject) {
							info = ManagedBuildManager.getBuildInfo(proj);
					 	}
						IConfiguration currentConfig = null;
						boolean isCurrent = true;
			 			if (info != null) {
			 				currentConfig = info.getDefaultConfiguration();
			 				if (!currentConfig.getId().equals(removeId)) {
			 					info.setDefaultConfiguration(config);
			 					isCurrent = false;
			 				}
			 			}
			 			((IProject)proj).build(IncrementalProjectBuilder.CLEAN_BUILD, monitor);
					 	
					 	getConfigurationList().remove(config);
						getConfigurationMap().remove(removeId);

						if (info != null) {
							if (!isCurrent) {
			 					info.setDefaultConfiguration(currentConfig);								
							} else {
								// If the current default config is the one being removed, reset the default config
								String[] configs = info.getConfigurationNames();
								if (configs.length > 0) {
									info.setDefaultConfiguration(configs[0]);
								}
							}
			 			}
						break;
					}
				}
			}
		};
		try {
			ResourcesPlugin.getWorkspace().run( remover, null );
		}
		catch( CoreException e ) {}
		setDirty(true);
	}

	/* (non-Javadoc)
	 * Adds the Configuration to the Configuration list and map
	 * 
	 * @param Tool
	 */
	public void addConfiguration(Configuration configuration) {
		getConfigurationList().add(configuration);
		getConfigurationMap().put(configuration.getId(), configuration);
	}
	
	/* (non-Javadoc)
	 * Safe accessor for the list of configurations.
	 * 
	 * @return List containing the configurations
	 */
	private List getConfigurationList() {
		if (configList == null) {
			configList = new ArrayList();
		}
		return configList;
	}
	
	/* (non-Javadoc)
	 * Safe accessor for the map of configuration ids to configurations
	 * 
	 * @return
	 */
	private Map getConfigurationMap() {
		if (configMap == null) {
			configMap = new HashMap();
		}
		return configMap;
	}

	/*
	 *  M O D E L   A T T R I B U T E   A C C E S S O R S
	 */

	/*
	 *  O B J E C T   S T A T E   M A I N T E N A N C E
	 */

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedProject#(getDefaultArtifactName)
	 */
	public String getDefaultArtifactName(){
		String name = new String();
		// Check for spaces
		String[] tokens = getOwner().getName().split("\\s");	//$NON-NLS-1$
		for (int index = 0; index < tokens.length; ++index) {
			name += tokens[index];
		}
		return name;
	}
	
	/* (non-Javadoc)
	 *  Resolve the element IDs to interface references
	 */
	public void resolveReferences() {
		if (!resolved) {
			resolved = true;
			// Resolve project-type
			if (projectTypeId != null && projectTypeId.length() > 0) {
				projectType = ManagedBuildManager.getExtensionProjectType(projectTypeId);
				if (projectType == null) {
					// TODO:  Report error
				}
			}
			
			// call resolve references on any children
			Iterator configIter = getConfigurationList().iterator();
			while (configIter.hasNext()) {
				Configuration current = (Configuration)configIter.next();
				current.resolveReferences();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedProject#isDirty()
	 */
	public boolean isDirty() {
		// If I need saving, just say yes
		if (isDirty) return true;
		
		// Otherwise see if any configurations need saving
		Iterator iter = getConfigurationList().listIterator();
		while (iter.hasNext()) {
			Configuration current = (Configuration) iter.next();
			if (current.isDirty()) return true;
		}
		
		return isDirty;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedProject#setDirty(boolean)
	 */
	public void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
		// Propagate "false" to the children
		if (!isDirty) {
			Iterator iter = getConfigurationList().listIterator();
			while (iter.hasNext()) {
				Configuration current = (Configuration) iter.next();
				current.setDirty(false);
			}		    
		}
	}

}

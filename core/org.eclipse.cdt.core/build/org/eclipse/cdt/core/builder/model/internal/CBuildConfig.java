/**********************************************************************
 * Copyright (c) 2002,2003 Timesys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Timesys - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.core.builder.model.internal;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xerces.dom.DocumentImpl;
import org.eclipse.cdt.core.builder.BuilderPlugin;
import org.eclipse.cdt.core.builder.model.ICBuildConfig;
import org.eclipse.cdt.core.builder.model.ICBuildConfigWorkingCopy;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.internal.core.DebugCoreMessages;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author sam.robb
 */
public class CBuildConfig extends PlatformObject implements ICBuildConfig {

	/**
	 * Location this configuration is stored in. This 
	 * is the key for a build configuration handle.
	 */
	private IPath fLocation;
	
	/**
	 * Constructs a build configuration in the given location.
	 * 
	 * @param location path to where this build configuration's
	 *  underlying file is located
	 */
	protected CBuildConfig(IPath location) {
		setLocation(location);
	}

	/**
	 * Constructs a launch configuration from the given
	 * memento.
	 * 
	 * @param memento configuration memento
	 * @exception CoreException if the memento is invalid or
	 * 	an exception occurrs reading the memento
	 */
	protected CBuildConfig(String memento) throws CoreException {
		Exception ex = null;
		try {
			Element root = null;
			DocumentBuilder parser =
				DocumentBuilderFactory.newInstance().newDocumentBuilder();
			StringReader reader = new StringReader(memento);
			InputSource source = new InputSource(reader);
			root = parser.parse(source).getDocumentElement();
			
			String localString = root.getAttribute("local"); //$NON-NLS-1$
			String path = root.getAttribute("path"); //$NON-NLS-1$

			String message = null;				
			if (path == null) {
				message = DebugCoreMessages.getString("LaunchConfiguration.Invalid_build_configuration_memento__missing_path_attribute_3"); //$NON-NLS-1$
			} else if (localString == null) {
				message = DebugCoreMessages.getString("LaunchConfiguration.Invalid_build_configuration_memento__missing_local_attribute_4"); //$NON-NLS-1$
			}
			if (message != null) {
				IStatus s = newStatus(message, DebugException.INTERNAL_ERROR, null);
				throw new CoreException(s);
			}
			
			IPath location = ResourcesPlugin.getWorkspace().getRoot().getLocation().append(path);
			
			setLocation(location);
			return;
		} catch (ParserConfigurationException e) {
			ex = e;			
		} catch (SAXException e) {
			ex = e;
		} catch (IOException e) {
			ex = e;
		}
		IStatus s = newStatus(DebugCoreMessages.getString("LaunchConfiguration.Exception_occurred_parsing_memento_5"), DebugException.INTERNAL_ERROR, ex); //$NON-NLS-1$
		throw new CoreException(s);
	}
	
	/**
	 * Creates and returns a new error status based on 
	 * the given mesasge, code, and exception.
	 * 
	 * @param message error message
	 * @param code error code
	 * @param e exception or <code>null</code>
	 * @return status
	 */
	protected IStatus newStatus(String message, int code, Throwable e) {
		return new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), code, message, e);
	}
	
	/**
	 * @see ICBuildConfig#build(IProgressMonitor)
	 */
	public void build(IProgressMonitor monitor) throws CoreException {
	}
	
	/**
	 * A configuration's name is that of the last segment
	 * in it's location (subtract the ".build" extension).
	 * 
	 * @see ICBuildConfig#getName()
	 */
	public String getName() {
		return getLastLocationSegment();
	}
	
	private String getLastLocationSegment() {
		String name = getLocation().lastSegment();
		name = name.substring(0, name.length() - (BUILD_CONFIGURATION_FILE_EXTENSION.length() + 1));
		return name;
	}

	/**
	 * @see ICBuildConfig#getLocation()
	 */
	public IPath getLocation() {
		return fLocation;
	}

	/**
	 * Sets the location of this configuration's underlying
	 * file.
	 * 
	 * @param location the location of this configuration's underlying
	 *  file
	 */
	private void setLocation(IPath location) {
		fLocation = location;
	}

	/**
	 * @see ICBuildConfig#exists()
	 */
	public boolean exists() {
		IFile file = getFile();
		if (file == null) {
			return getLocation().toFile().exists();
		} else {
			return file.exists();
		}
	}

	/**
	 * @see ICBuildConfig#getAttribute(String, int)
	 */
	public int getAttribute(String attributeName, int defaultValue) throws CoreException {
		return getInfo().getIntAttribute(attributeName, defaultValue);
	}

	/**
	 * @see ICBuildConfig#getAttribute(String, String)
	 */
	public String getAttribute(String attributeName, String defaultValue) throws CoreException {
		return getInfo().getStringAttribute(attributeName, defaultValue);
	}

	/**
	 * @see ICBuildConfig#getAttribute(String, boolean)
	 */
	public boolean getAttribute(String attributeName, boolean defaultValue) throws CoreException {
		return getInfo().getBooleanAttribute(attributeName, defaultValue);
	}

	/**
	 * @see ICBuildConfig#getAttribute(String, List)
	 */
	public List getAttribute(String attributeName, List defaultValue) throws CoreException {
		return getInfo().getListAttribute(attributeName, defaultValue);
	}

	/**
	 * @see ICBuildConfig#getAttribute(String, Map)
	 */
	public Map getAttribute(String attributeName, Map defaultValue) throws CoreException {
		return getInfo().getMapAttribute(attributeName, defaultValue);
	}

	/**
	 * @see ICBuildConfig#isLocal()
	 */
	public boolean isLocal() {
		return  getFile() == null;
	}

	/**
	 * @see ICBuildConfig#getWorkingCopy()
	 */
	public ICBuildConfigWorkingCopy getWorkingCopy() throws CoreException {
		return new CBuildConfigWorkingCopy(this);
	}
	
	/**
	 * @see ICBuildConfig#copy(String name)
	 */
	public ICBuildConfigWorkingCopy copy(String name) throws CoreException {
		ICBuildConfigWorkingCopy copy = new CBuildConfigWorkingCopy(this, name);
		return copy;
	}	

	/**
	 * @see ICBuildConfig#isWorkingCopy()
	 */
	public boolean isWorkingCopy() {
		return false;
	}

	/**
	 * @see ICBuildConfig#delete()
	 */
	public void delete() throws CoreException {
		if (exists()) {
			if (isLocal()) {
				if (!(getLocation().toFile().delete())) {
					throw new DebugException(
						new Status(Status.ERROR, DebugPlugin.getUniqueIdentifier(),
						 DebugException.REQUEST_FAILED, DebugCoreMessages.getString("LaunchConfiguration.Failed_to_delete_build_configuration._1"), null) //$NON-NLS-1$
					);
				}
				// manually update the build manager cache since there
				// will be no resource delta
				getBuildConfigurationManager().configurationDeleted(this);
			} else {
				// delete the resource using IFile API such that
				// resource deltas are fired.
				IResource file = getFile();
				if (file != null) {
					file.delete(true, null);
				} else {
					// Error - the exists test passed, but could not locate file 
				}
			}
		}
	}
	
	/**
	 * Returns the info object containing the attributes
	 * of this configuration
	 * 
	 * @return info for this handle
	 * @exception CoreException if unable to retrieve the
	 *  info object
	 */
	protected CBuildConfigInfo getInfo() throws CoreException {
		return getBuildConfigurationManager().getInfo(this);
	}
	
	/**
	 * Returns the build manager
	 * 
	 * @return build manager
	 */
	protected CBuildConfigManager getBuildConfigurationManager() {
		return BuilderPlugin.getDefault().getBuildConfigurationManager();
	}

	/**
	 * @see ICBuildConfig#getMemento()
	 */
	public String getMemento() throws CoreException {
		IPath relativePath = getFile().getFullPath();
		relativePath = relativePath.setDevice(null);
		
		Document doc = new DocumentImpl();
		Element node = doc.createElement("buildConfiguration"); //$NON-NLS-1$
		doc.appendChild(node);
		node.setAttribute("local", (new Boolean(isLocal())).toString()); //$NON-NLS-1$
		node.setAttribute("path", relativePath.toString()); //$NON-NLS-1$
		
		try {
			return CBuildConfigManager.serializeDocument(doc);
		} catch (IOException e) {
			IStatus status = newStatus(DebugCoreMessages.getString("LaunchConfiguration.Exception_occurred_creating_build_configuration_memento_9"), DebugException.INTERNAL_ERROR,  e); //$NON-NLS-1$
			throw new CoreException(status);
		}
	}

	/**
	 * @see ICBuildConfig#getFile()
	 */	
	public IFile getFile() {
		return ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(getLocation());
	}

	/**
	 * @see ICBuildConfig#getProject()
	 */	
	public IProject getProject() {
		return getFile().getProject();
	}

	/**
	 * @see ICBuildConfig#contentsEqual(ICBuildConfig)
	 */
	public boolean contentsEqual(ICBuildConfig object) {
		try {
			if (object instanceof CBuildConfig) {
				CBuildConfig otherConfig = (CBuildConfig) object;
				return getName().equals(otherConfig.getName())
				 	 && getLocation().equals(otherConfig.getLocation())
					 && getInfo().equals(otherConfig.getInfo());
			}
			return false;
		} catch (CoreException ce) {
			return false;
		}
	}

	/**
	 * Returns whether this configuration is equal to the
	 * given configuration. Two configurations are equal if
	 * they are stored in the same location (and neither one
	 * is a working copy).
	 * 
	 * @return whether this configuration is equal to the
	 *  given configuration
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (object instanceof ICBuildConfig) {
			if (isWorkingCopy()) {
				return this == object;
			} 
			ICBuildConfig config = (ICBuildConfig) object;
			if (!config.isWorkingCopy()) {
				return config.getLocation().equals(getLocation());
			}
		}
		return false;
	}
	
	/**
	 * @see Object#hashCode()
	 */
	public int hashCode() {
		return getLocation().hashCode();
	}
	
	/**
	 * Returns the container this build configuration is 
	 * stored in, or <code>null</code> if this build configuration
	 * is stored locally.
	 * 
	 * @return the container this build configuration is 
	 * stored in, or <code>null</code> if this build configuration
	 * is stored locally
	 */
	protected IContainer getContainer() {
		IFile file = getFile();
		if (file != null) {
			return file.getParent();
		}
		return null;
	}

}

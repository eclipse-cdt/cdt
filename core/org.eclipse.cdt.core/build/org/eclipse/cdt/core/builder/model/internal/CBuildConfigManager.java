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

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.Serializer;
import org.apache.xml.serialize.SerializerFactory;
import org.eclipse.cdt.core.builder.model.ICBuildConfig;
import org.eclipse.cdt.core.builder.model.ICBuildConfigListener;
import org.eclipse.cdt.core.builder.model.ICBuildConfigManager;
import org.eclipse.cdt.core.builder.model.ICBuildConfigWorkingCopy;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.internal.core.DebugCoreMessages;
import org.eclipse.debug.internal.core.ListenerList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Manages build configurations.
 *
 * @see ICBuildConfigManager
 */
public class CBuildConfigManager implements ICBuildConfigManager {

	/**
	 * Collection of listeners
	 */
	private ListenerList fListeners = new ListenerList(5);

	/**
	 * Types of notifications
	 */
	public static final int ADDED   = 0;
	public static final int REMOVED = 1;
	public static final int CHANGED = 2;

	/**
	 * Serializes a XML document into a string - encoded in UTF8 format,
	 * with platform line separators.
	 * 
	 * @param doc document to serialize
	 * @return the document as a string
	 */
	public static String serializeDocument(Document doc) throws IOException {
		ByteArrayOutputStream s= new ByteArrayOutputStream();
		OutputFormat format = new OutputFormat();
		format.setIndenting(true);
		format.setLineSeparator(System.getProperty("line.separator"));  //$NON-NLS-1$
		
		Serializer serializer =
			SerializerFactory.getSerializerFactory(Method.XML).makeSerializer(
				new OutputStreamWriter(s, "UTF8"), //$NON-NLS-1$
				format);
		serializer.asDOMSerializer().serialize(doc);
		return s.toString("UTF8"); //$NON-NLS-1$		
	}	

	/**
	 * @see ICBuildConfigManager#addListener(ICBuildConfigListener)
	 */
	public void addListener(ICBuildConfigListener listener) {
		fListeners.add(listener);
	}

	/**
	 * @see ICBuildConfigManager#removeListener(ICBuildConfigListener)
	 */
	public void removeListener(ICBuildConfigListener listener) {
		fListeners.remove(listener);
	}

	/**
	 * @see ICBuildConfigManager#addConfiguration(ICBuildConfig)
	 */
	public void addConfiguration(ICBuildConfig configuration) {
		fireUpdate(configuration, ADDED);
	}

	/**
	 * @see ICBuildConfigManager#removeConfiguration(ICBuildConfig)
	 */
	public void removeConfiguration(ICBuildConfig configuration) {
		fireUpdate(configuration, REMOVED);
	}

	/**
	 * @see ICBuildConfigManager#generateUniqueConfigurationNameFrom(IProject, String)
	 */
	public String generateUniqueConfigurationNameFrom(IProject project, String baseName) {
		int index = 1;
		int length= baseName.length();
		int copyIndex = baseName.lastIndexOf(" ("); //$NON-NLS-1$
		if (copyIndex > -1 && length > copyIndex + 2 && baseName.charAt(length - 1) == ')') {
			String trailer = baseName.substring(copyIndex + 2, length -1);
			if (isNumber(trailer)) {
				try {
					index = Integer.parseInt(trailer);
					baseName = baseName.substring(0, copyIndex);
				} catch (NumberFormatException nfe) {
				}
			}
		} 
		String newName = baseName;
		try {
			StringBuffer buffer= null;
			while (isExistingConfigurationName(project, newName)) {
				buffer = new StringBuffer(baseName);
				buffer.append(" ("); //$NON-NLS-1$
				buffer.append(String.valueOf(index));
				index++;
				buffer.append(')');
				newName = buffer.toString();		
			}		
		} catch (CoreException e) {
			DebugPlugin.log(e);
		}
		return newName;
	}

	/**
	 * @see ICBuildConfigManager#getConfiguration(IFile)
	 */
	public ICBuildConfig getConfiguration(IFile file) {
		return new CBuildConfig(file.getLocation());
	}

	/**
	 * @see ICBuildConfigManager#getConfiguration(String)
	 */
	public ICBuildConfig getConfiguration(String memento) throws CoreException {
		return new CBuildConfig(memento);
	}

	public ICBuildConfigWorkingCopy getConfiguration(IProject project, String name) {
		if ((name == null) || (name.length() < 1)) {
			name = "New Configuration";
		}
		name = generateUniqueConfigurationNameFrom(project, name);
		return new CBuildConfigWorkingCopy(project, name);
	}

	/**
	 * @see ICBuildConfigManager#getConfigurations(IProject)
	 */
	public ICBuildConfig[] getConfigurations(IProject project) throws CoreException {
		List configs = findConfigurations(project, ICBuildConfig.BUILD_CONFIGURATION_FILE_EXTENSION);
		return (ICBuildConfig[]) configs.toArray(new ICBuildConfig[configs.size()]);
	}

	/**
	 * @see ICBuildConfigManager#isExistingConfigurationName(IProject, String)
	 */
	public boolean isExistingConfigurationName(IProject project, String name) throws CoreException {
		List configFiles;
		int count = 0;
		
		configFiles = findConfigurations(project, ICBuildConfig.BUILD_CONFIGURATION_FILE_EXTENSION);
		count = configFiles.size();
		
		if (count > 0) {
			for (Iterator iter = configFiles.iterator(); iter.hasNext();) {
				ICBuildConfig element = (ICBuildConfig) iter.next();
				if (name.equals(element.getName())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Fires notification to the listeners that a configuration
	 * has been added, removed, updated, or deleted.
	 */
	protected void fireUpdate(ICBuildConfig configuration, int update) {
		Object[] copiedListeners = fListeners.getListeners();
		for (int i= 0; i < copiedListeners.length; i++) {
			ICBuildConfigListener listener = (ICBuildConfigListener) copiedListeners[i];
			switch (update) {
				case ADDED:
					listener.configurationAdded(configuration);
					break;
				case REMOVED:
					listener.configurationRemoved(configuration);
					break;
				case CHANGED:
					listener.configurationChanged(configuration);
					break;
			}
		}
	}

	/**
	 * Notifies the manager that a configuration has been deleted.
	 * 
	 * @param project the project containing the configuration
	 * @param config the configuration that was deleted
	 */
	protected void configurationDeleted(ICBuildConfig configuration) throws CoreException {
		fireUpdate(configuration, REMOVED);
	}
	
	/**
	 * Notifies the manager that a configuration has been added.
	 * 
	 * @param project the project containing the configuration
	 * @param config the configuration that was added
	 */
	protected void configurationAdded(ICBuildConfig configuration) throws CoreException {
		if (isValid(configuration)) {
			fireUpdate(configuration, ADDED);
		} else {
			fireUpdate(configuration, ADDED);
		}
	}

	/**
	 * Notifies the manager that a configuration has been added.
	 * 
	 * @param project the project containing the configuration
	 * @param config the launch configuration that was changed
	 */
	protected void configurationChanged(ICBuildConfig configuration) {
		if (isValid(configuration)) {
			fireUpdate(configuration, CHANGED);
		} else {
			fireUpdate(configuration, REMOVED);
		}								
	}
	
	/**
	 * Returns the info object for the specified launch configuration.
	 * If the configuration exists, but is not yet in the cache,
	 * an info object is built and added to the cache.
	 * 
	 * @exception CoreException if an exception occurs building
	 *  the info object
	 * @exception DebugException if the config does not exist
	 */
	protected CBuildConfigInfo getInfo(ICBuildConfig config) throws CoreException {
		CBuildConfigInfo info = null;

		if (config.exists()) {
			InputStream stream = null;
			
			try {
				IFile file = ((CBuildConfig) config).getFile();
				stream = file.getContents();
				info = createInfoFromXML(stream);
			} catch (FileNotFoundException e) {
				throw createDebugException(MessageFormat.format(DebugCoreMessages.getString("LaunchManager.{0}_occurred_while_reading_launch_configuration_file._1"), new String[]{e.toString()}), e); //$NON-NLS-1$					
			} catch (SAXException e) {
				throw createDebugException(MessageFormat.format(DebugCoreMessages.getString("LaunchManager.{0}_occurred_while_reading_launch_configuration_file._1"), new String[]{e.toString()}), e); //$NON-NLS-1$					
			} catch (ParserConfigurationException e) {
				throw createDebugException(MessageFormat.format(DebugCoreMessages.getString("LaunchManager.{0}_occurred_while_reading_launch_configuration_file._1"), new String[]{e.toString()}), e); //$NON-NLS-1$					
			} catch (IOException e) {
				throw createDebugException(MessageFormat.format(DebugCoreMessages.getString("LaunchManager.{0}_occurred_while_reading_launch_configuration_file._1"), new String[]{e.toString()}), e); //$NON-NLS-1$					
			} finally {
				if (stream != null) {
					try {
						stream.close();
					} catch (IOException e) {
						throw createDebugException(MessageFormat.format(DebugCoreMessages.getString("LaunchManager.{0}_occurred_while_reading_launch_configuration_file._1"), new String[]{e.toString()}), e); //$NON-NLS-1$					
					}
				}
			}
	
		} else {
			throw createDebugException(DebugCoreMessages.getString("LaunchManager.Launch_configuration_does_not_exist._6"), null); //$NON-NLS-1$
		}

		return info;
	}	

	/**
	 * Return a LaunchConfigurationInfo object initialized from XML contained in
	 * the specified stream.  Simply pass out any exceptions encountered so that
	 * caller can deal with them.  This is important since caller may need access to the
	 * actual exception.
	 */
	protected CBuildConfigInfo createInfoFromXML(InputStream stream)
		throws CoreException, ParserConfigurationException, IOException, SAXException {
		Element root = null;
		DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		root = parser.parse(new InputSource(stream)).getDocumentElement();
		CBuildConfigInfo info = new CBuildConfigInfo();
		info.initializeFromXML(root);
		return info;
	}

	/**
	 * Return an instance of DebugException containing the specified message and Throwable.
	 */
	protected DebugException createDebugException(String message, Throwable throwable) {
		return new DebugException(
					new Status(
					 Status.ERROR, DebugPlugin.getUniqueIdentifier(),
					 DebugException.REQUEST_FAILED, message, throwable 
					)
				);
	}

	/**
	 * Finds and returns all configurations in the given
	 * container (and subcontainers)
	 * 
	 * @param container the container to search
	 * @exception CoreException an exception occurs traversing
	 *  the container.
	 * @return all configurations in the given container
	 */
	protected List findConfigurations(IContainer container, String extension) throws CoreException {
		List list = new ArrayList(10);
		if (container instanceof IProject && !((IProject)container).isOpen()) {
			return list;
		}
		searchForFiles(container, extension, list);
		Iterator iter = list.iterator();
		List configs = new ArrayList(list.size());
		while (iter.hasNext()) {
			IFile file = (IFile)iter.next();
			configs.add(getConfiguration(file));
		}
		return configs;
	}
	
	/**
	 * Recursively searches the given container for files with the given
	 * extension.
	 * 
	 * @param container the container to search in
	 * @param extension the file extension being searched for
	 * @param list the list to add the matching files to
	 * @exception CoreException if an exception occurs traversing
	 *  the container
	 */
	protected void searchForFiles(IContainer container, String extension, List list) throws CoreException {
		IResource[] members = container.members();
		for (int i = 0; i < members.length; i++) {
			if (members[i] instanceof IContainer) {
				if (members[i] instanceof IProject && !((IProject)members[i]) .isOpen()) {
					continue;
				}
				searchForFiles((IContainer)members[i], extension, list);
			} else if (members[i] instanceof IFile) {
				IFile file = (IFile)members[i];
				if (extension.equalsIgnoreCase(file.getFileExtension())) {
					list.add(file);
				}
			}
		}
	}

	/**
	 * Returns whether the given String is composed solely of digits
	 */
	private boolean isNumber(String string) {
		int numChars= string.length();
		if (numChars == 0) {
			return false;
		}
		for (int i= 0; i < numChars; i++) {
			if (!Character.isDigit(string.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns whether the given configuration passes a basic
	 * integritiy test.
	 * 
	 * @param config the configuration to verify
	 * @return whether the config meets basic integrity constraints
	 */
	protected boolean isValid(ICBuildConfig config) {
		// TODO: Tests?
		return (null != config);
	}
}


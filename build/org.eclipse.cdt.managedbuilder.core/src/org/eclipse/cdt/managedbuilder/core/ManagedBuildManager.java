/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.cdt.core.AbstractCExtension;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.DefaultManagedConfigElement;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.internal.core.Target;
import org.eclipse.cdt.managedbuilder.internal.core.Tool;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.QualifiedName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This is the main entry point for getting at the build information
 * for the managed build system. 
 */
public class ManagedBuildManager extends AbstractCExtension implements IScannerInfoProvider {

	private static final QualifiedName buildInfoProperty = new QualifiedName(ManagedBuilderCorePlugin.getUniqueIdentifier(), "managedBuildInfo");	//$NON-NLS-1$
	private static final String ROOT_ELEM_NAME = "ManagedProjectBuildInfo";	//$NON-NLS-1$
	private static final String FILE_NAME = ".cdtbuild";	//$NON-NLS-1$
	private static final ITarget[] emptyTargets = new ITarget[0];
	public static final String INTERFACE_IDENTITY = ManagedBuilderCorePlugin.getUniqueIdentifier() + "." + "ManagedBuildManager";	//$NON-NLS-1$ //$NON-NLS-2$
	public static final String EXTENSION_POINT_ID = "ManagedBuildInfo";		//$NON-NLS-1$
	
	// This is the version of the manifest and project files that
	private static final String buildInfoVersion = "2.0.0"; //$NON-NLS-1$
	private static boolean extensionTargetsLoaded = false;
	private static Map extensionTargetMap;
	private static List extensionTargets;
	private static Map extensionToolMap;
	private static Map configElementMap;

	// Listeners interested in build model changes
	private static Map buildModelListeners;
	
	/**
	 * Returns the list of targets that are defined by this project,
	 * projects referenced by this project, and by the extensions. 
	 * 
	 * @param project
	 * @return
	 */
	public static ITarget[] getDefinedTargets(IProject project) {
		// Make sure the extensions are loaded
		loadExtensions();

		// Get the targets for this project and all referenced projects
		List definedTargets = null;
		// To Do

		// Create the array and copy the elements over
		int size = extensionTargets != null ? 
				extensionTargets.size()	+ (definedTargets != null ? definedTargets.size() : 0) :
				0;

		ITarget[] targets = new ITarget[size];
		
		int n = 0;
		for (int i = 0; i < extensionTargets.size(); ++i)
			targets[n++] = (ITarget)extensionTargets.get(i);
		
		if (definedTargets != null)
			for (int i = 0; i < definedTargets.size(); ++i)
				targets[n++] = (ITarget)definedTargets.get(i);
				
		return targets;
	}

	/* (non-Javadoc)
	 * Safe accessor for the map of IDs to Targets
	 * 
	 * @return
	 */
	protected static Map getExtensionTargetMap() {
		if (extensionTargetMap == null) {
			extensionTargetMap = new HashMap();
		}
		return extensionTargetMap;
	}

	/* (non-Javadoc)
	 * Safe accessor for the map of IDs to Tools
	 * 
	 * @return
	 */
	protected static Map getExtensionToolMap() {
		if (extensionToolMap == null) {
			extensionToolMap = new HashMap();
		}
		return extensionToolMap;
	}

	/**
	 * Returns the targets owned by this project.  If none are owned,
	 * an empty array is returned.
	 * 
	 * @param project
	 * @return
	 */
	public static ITarget[] getTargets(IResource resource) {
		IManagedBuildInfo buildInfo = getBuildInfo(resource);
		
		if (buildInfo != null) {
			List targets = buildInfo.getTargets();
			return (ITarget[])targets.toArray(new ITarget[targets.size()]);
		} else {
			return emptyTargets;
		}
	}

	/**
	 * Answers the tool with the ID specified in the argument or <code>null</code>.
	 * 
	 * @param id
	 * @return
	 */
	public static ITool getTool(String id) {
		return (ITool) getExtensionToolMap().get(id);
	}
	
	/**
	 * Answers the result of a best-effort search to find a target with the 
	 * specified ID, or <code>null</code> if one is not found.
	 * 
	 * @param resource
	 * @param id
	 * @return
	 */
	public static ITarget getTarget(IResource resource, String id) {
		ITarget target = null;
		// Check if the target is spec'd in the build info for the resource
		if (resource != null) {
			IManagedBuildInfo buildInfo = getBuildInfo(resource);
			if (buildInfo != null)
				target = buildInfo.getTarget(id);
		}
		// OK, check the extension map
		if (target == null) {
			target = (ITarget)getExtensionTargetMap().get(id);
		}
		return target;
	}

	/**
	 * Sets the default configuration for the project. Note that this will also
	 * update the default target if needed.
	 *  
	 * @param project
	 * @param newDefault
	 */
	public static void setDefaultConfiguration(IProject project, IConfiguration newDefault) {
		if (project == null || newDefault == null) {
			return;
		}
		// Set the default in build information for the project 
		IManagedBuildInfo info = getBuildInfo(project);
		if (info != null) {
			info.setDefaultConfiguration(newDefault);
		}
	}
	
	/* (non-Javadoc)
	 * 
	 * @param config
	 * @param option
	 */
	private static void notifyListeners(IConfiguration config, IOption option) {
		// Continue if change is something that effect the scanner
		if (!(option.getValueType() == IOption.INCLUDE_PATH 
			|| option.getValueType() == IOption.PREPROCESSOR_SYMBOLS)) {
			return;
		}
		// Figure out if there is a listener for this change
		IResource resource = config.getOwner();
		List listeners = (List) getBuildModelListeners().get(resource);
		if (listeners == null) {
			return;
		}
		ListIterator iter = listeners.listIterator();
		while (iter.hasNext()) {
			((IScannerInfoChangeListener)iter.next()).changeNotification(resource, (IScannerInfo)getBuildInfo(resource, false));
		}
	}

	/**
	 * Set the string value for an option for a given config.
	 * 
	 * @param config The configuration the option belongs to.
	 * @param option The option to set the value for.
	 * @param value The boolean that the option should contain after the change.
	 */
	public static void setOption(IConfiguration config, IOption option, boolean value) {
		try {
			// Request a value change and set dirty if real change results
			config.setOption(option, value);
			notifyListeners(config, option);
		} catch (BuildException e) {
			return;
		}
	}

	/**
	 * Set the string value for an option for a given config.
	 * 
	 * @param config The configuration the option belongs to.
	 * @param option The option to set the value for.
	 * @param value The value that the option should contain after the change.
	 */
	public static void setOption(IConfiguration config, IOption option, String value) {
		try {
			config.setOption(option, value);
			notifyListeners(config, option);
		} catch (BuildException e) {
			return;
		}
	}
	
	/**
	 * Set the string array value for an option for a given config.
	 * 
	 * @param config The configuration the option belongs to.
	 * @param option The option to set the value for.
	 * @param value The values the option should contain after the change.
	 */
	public static void setOption(IConfiguration config, IOption option, String[] value) {
		try {
			config.setOption(option, value);
			notifyListeners(config, option);				
		} catch (BuildException e) {
			return;
		}
	}

	/**
	 * @param config
	 * @param tool
	 * @param command
	 */
	public static void setToolCommand(IConfiguration config, ITool tool, String command) {
		// The tool may be a reference.
		if (tool instanceof IToolReference) {
			// If so, just set the command in the reference
			((IToolReference)tool).setToolCommand(command);
		} else {
			config.setToolCommand(tool, command);
		}
	}
	
	/**
	 * Saves the build information associated with a project and all resources
	 * in the project to the build info file.
	 * 
	 * @param project
	 * @param force 
	 */
	public static void saveBuildInfo(IProject project, boolean force) {
		// Create document
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.newDocument();
			Element rootElement = doc.createElement(ROOT_ELEM_NAME);	
			doc.appendChild(rootElement);

			// Save the build info
			ManagedBuildInfo buildInfo = (ManagedBuildInfo) getBuildInfo(project);
			if (buildInfo != null && (force == true || buildInfo.isDirty())) {
				buildInfo.serialize(doc, rootElement);
		
				// Transform the document to something we can save in a file
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				transformer.setOutputProperty(OutputKeys.METHOD, "xml");	//$NON-NLS-1$
				transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");	//$NON-NLS-1$
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");	//$NON-NLS-1$
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(stream);
				transformer.transform(source, result);
				
				// Save the document
				IFile projectFile = project.getFile(FILE_NAME);
				InputStream inputStream = new ByteArrayInputStream(stream.toByteArray());
				if (projectFile.exists()) {
					projectFile.setContents(inputStream, IResource.FORCE, null);
				} else {
					projectFile.create(inputStream, IResource.FORCE, null);
				}

				// Close the streams
				stream.close();
				inputStream.close();
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformerFactoryConfigurationError e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// The save failed
			e.printStackTrace();
		} catch (CoreException e) {
			// Save to IFile failed
			e.printStackTrace();
		}
	}

	/**
	 * @param resource
	 */
	public static void removeBuildInfo(IResource resource) {
		try {
			resource.setSessionProperty(buildInfoProperty, null);
		} catch (CoreException e) {
		}
	}

	/**
	 * Resets the build information for the project and configuration specified in the arguments. 
	 * The build information will contain the settings defined in the plugin manifest. 
	 * 
	 * @param project
	 * @param configuration
	 */
	public static void resetConfiguration(IProject project, IConfiguration configuration) {
		// Make sure the extensions are loaded
		loadExtensions();

		// Find out the parent of the configuration
		IConfiguration parentConfig = configuration.getParent();

		// Get the config element for the parent from the map
		IManagedConfigElement configElement = getConfigElement(parentConfig);
		
		// reset the configuration
		((Configuration)configuration).reset(configElement);
	}
	

	/**
	 * @param target
	 */
	public static void addExtensionTarget(Target target) {
		if (extensionTargets == null) {
			extensionTargets = new ArrayList();
		}
		
		extensionTargets.add(target);
		getExtensionTargetMap().put(target.getId(), target);
	}
		
	/**
	 * @param tool
	 */
	public static void addExtensionTool(Tool tool) {
		getExtensionToolMap().put(tool.getId(), tool);
	}

	/**
	 * Creates a new target for the resource based on the parentTarget.
	 * 
	 * @param resource
	 * @param parentTarget
	 * @return new <code>ITarget</code> with settings based on the parent passed in the arguments
	 * @throws BuildException
	 */
	public static ITarget createTarget(IResource resource, ITarget parentTarget)
		throws BuildException
	{
		IResource owner = parentTarget.getOwner();
		
		if (owner != null && owner.equals(resource))
			// Already added
			return parentTarget; 
			
		if (resource instanceof IProject) {
			// Must be an extension target (why?)
			if (owner != null)
				throw new BuildException(ManagedBuilderCorePlugin.getResourceString("ManagedBuildManager.error.owner_not_null")); //$NON-NLS-1$
		} else {
			// Owner must be owned by the project containing this resource
			if (owner == null)
				throw new BuildException(ManagedBuilderCorePlugin.getResourceString("ManagedBuildManager.error.null_owner")); //$NON-NLS-1$
			if (!owner.equals(resource.getProject()))
				throw new BuildException(ManagedBuilderCorePlugin.getResourceString("ManagedBuildManager.error.owner_not_project")); //$NON-NLS-1$
		}
		
		// Passed validation so create the target.
		return new Target(resource, parentTarget);
	}
	
	/* (non-Javadoc)
	 * Load the build information for the specified resource from its project
	 * file. Pay attention to the version number too.
	 */
	private static ManagedBuildInfo loadBuildInfo(IProject project) {
		ManagedBuildInfo buildInfo = null;
		IFile file = project.getFile(FILE_NAME);
		if (!file.exists())
			return null;
	
		try {
			InputStream stream = file.getContents();
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = parser.parse(stream);
			Node rootElement = document.getFirstChild();
			if (rootElement.getNodeName().equals(ROOT_ELEM_NAME)) {
				buildInfo = new ManagedBuildInfo(project, (Element)rootElement);
				project.setSessionProperty(buildInfoProperty, buildInfo);
			}
		} catch (Exception e) {
			buildInfo = null;
		}

		return buildInfo;
	}

	/* (non-Javadoc)
	 * Since the class does not have a constructor but all public methods
	 * call this method first, it is effectively a startup method
	 */
	private static void loadExtensions() {
		if (extensionTargetsLoaded)
			return;
		
		// Get those extensions
		IPluginDescriptor descriptor = ManagedBuilderCorePlugin.getDefault().getDescriptor();
		// Get the version of the manifest
/*		PluginVersionIdentifier version = descriptor.getVersionIdentifier();
		if (version.isGreaterThan(new PluginVersionIdentifier(buildInfoVersion))) {
			//TODO: The version of the Plug-in is greater than what the manager thinks it understands
		}
*/		// We can read the manifest
		IExtensionPoint extensionPoint = descriptor.getExtensionPoint(EXTENSION_POINT_ID);
		IExtension[] extensions = extensionPoint.getExtensions();
		// First call the constructors
		for (int i = 0; i < extensions.length; ++i) {
			IExtension extension = extensions[i];
			IConfigurationElement[] elements = extension.getConfigurationElements();
			loadConfigElements(DefaultManagedConfigElement.convertArray(elements));
		}
		// Then call resolve.
		Iterator toolIter = getExtensionToolMap().values().iterator();
		while (toolIter.hasNext()) {
			try {
				Tool tool = (Tool)toolIter.next();
				tool.resolveReferences();
			} catch (Exception ex) {
				// TODO: log
				ex.printStackTrace();
			}
		}
		Iterator targetIter = getExtensionTargetMap().values().iterator();
		while (targetIter.hasNext()) {
			try {
				Target target = (Target)targetIter.next();
				target.resolveReferences();
			} catch (Exception ex) {
				// TODO: log
				ex.printStackTrace();
			}
		}
		// Let's never do that again
		extensionTargetsLoaded = true;
	}

	private static void loadConfigElements(IManagedConfigElement[] elements) {
		for (int toolIndex = 0; toolIndex < elements.length; ++toolIndex) {
			try {
				IManagedConfigElement element = elements[toolIndex];
				// Load the targets
				if (element.getName().equals(ITool.TOOL_ELEMENT_NAME)) {
					new Tool(element);
				} else if (element.getName().equals(ITarget.TARGET_ELEMENT_NAME)) {
					new Target(element);
				} else if (element.getName().equals(IManagedConfigElementProvider.ELEMENT_NAME)) {
					// don't allow nested config providers.
					if (element instanceof DefaultManagedConfigElement) {
						IManagedConfigElement[] providedConfigs;
						IManagedConfigElementProvider provider = createConfigProvider(
								(DefaultManagedConfigElement)element);
						providedConfigs = provider.getConfigElements();
						loadConfigElements(providedConfigs);
					}
				}
			} catch (Exception ex) {
				// TODO: log
				ex.printStackTrace();
			}
		}
	}
	
	private static IManagedConfigElementProvider createConfigProvider(
		DefaultManagedConfigElement element) throws CoreException {

		return (IManagedConfigElementProvider)element.getConfigurationElement().
			createExecutableExtension(IManagedConfigElementProvider.CLASS_ATTRIBUTE);
	}

	
	/**
	 * @param project
	 * @return
	 */
	public static boolean manages(IResource resource) {
		// The managed build manager manages build information for the 
		// resource IFF it it is a project and has a build file with the proper
		// root element
		IProject project = null;
		if (resource instanceof IProject){
			project = (IProject)resource;
		} else if (resource instanceof IFile) {
			project = ((IFile)resource).getProject();
		} else {
			return false;
		}
		IFile file = project.getFile(FILE_NAME);
		if (file.exists()) {
			try {
				InputStream stream = file.getContents();
				DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document document = parser.parse(stream);
				Node rootElement = document.getFirstChild();
				if (rootElement.getNodeName().equals(ROOT_ELEM_NAME)) {
					return true;
				} 
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}

	private static ManagedBuildInfo findBuildInfo(IResource resource, boolean create) {
		// Make sure the extension information is loaded first
		loadExtensions();
		ManagedBuildInfo buildInfo = null;
		try {
			buildInfo = (ManagedBuildInfo)resource.getSessionProperty(buildInfoProperty);
			// Make sure that if a project has build info, that the info is not corrupted
			if (buildInfo != null) {
				buildInfo.updateOwner(resource);
			}
		} catch (CoreException e) {
			return buildInfo;
		}
		
		if (buildInfo == null && resource instanceof IProject) {
			buildInfo = loadBuildInfo((IProject)resource);
		}
		
		if (buildInfo == null && create) {
			try {
				// Create a new build info object for the project
				buildInfo = new ManagedBuildInfo(resource);
				// Associate the build info with the project for the duration of the session
				resource.setSessionProperty(buildInfoProperty, buildInfo);
			} catch (CoreException e) {
				buildInfo = null;
			}
		}
		
		return buildInfo;
	}
	
	/**
	 * Answers the build information for the <code>IResource</code> in the
	 * argument. If the <code>create</code> is true, then a new build information
	 * repository will be created for the resource. 
	 * 
	 * @param resource
	 * @param create
	 * @return IManagedBuildInfo
	 */
	public static IManagedBuildInfo getBuildInfo(IResource resource, boolean create) {
		return (IManagedBuildInfo) findBuildInfo(resource, create);
	}

	/**
	 * Answers, but does not create, the managed build information for the 
	 * argument.
	 * 
	 * @see ManagedBuildManager#getBuildInfo(IResource, boolean)
	 * @param resource
	 * @return IManagedBuildInfo
	 */
	public static IManagedBuildInfo getBuildInfo(IResource resource) {
		return (IManagedBuildInfo) findBuildInfo(resource, false);
	}

	public static String getBuildInfoVersion() {
		return buildInfoVersion;
	}

	/*
	 * @return
	 */
	private static Map getBuildModelListeners() {
		if (buildModelListeners == null) {
			buildModelListeners = new HashMap();
		}
		return buildModelListeners;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScannerInfoProvider#getScannerInformation(org.eclipse.core.resources.IResource)
	 */
	public IScannerInfo getScannerInformation(IResource resource) {
		return (IScannerInfo) getBuildInfo(resource, false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScannerInfoProvider#subscribe(org.eclipse.cdt.core.parser.IScannerInfoChangeListener)
	 */
	public synchronized void subscribe(IResource resource, IScannerInfoChangeListener listener) {
		IResource project = null;
		if (resource instanceof IProject) {
			project = resource;
		} else if (resource instanceof IFile) {
			project = ((IFile)resource).getProject();
		} else {
			return;
		}
		// Get listeners for this resource
		Map map = getBuildModelListeners();
		List list = (List) map.get(project);
		if (list == null) {
			// Create a new list
			list = new ArrayList();
		}
		if (!list.contains(listener)) {
			// Add the new listener for the resource
			list.add(listener);
			map.put(project, list);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScannerInfoProvider#unsubscribe(org.eclipse.cdt.core.parser.IScannerInfoChangeListener)
	 */
	public synchronized void unsubscribe(IResource resource, IScannerInfoChangeListener listener) {
		IResource project = null;
		if (resource instanceof IProject) {
			project = resource;
		} else if (resource instanceof IFile) {
			project = ((IFile)resource).getProject();
		} else {
			return;
		}
		// Remove the listener
		Map map = getBuildModelListeners();
		List list = (List) map.get(project);
		if (list != null && !list.isEmpty()) {
			// The list is not empty so try to remove listener
			list.remove(listener);
			map.put(project, list);
		}
	}
	
	private static Map getConfigElementMap() {
		if (configElementMap == null) {
			configElementMap = new HashMap();
		}
		return configElementMap;
	}
	
	/**
	 * This method public for implementation reasons.  Not intended for use 
	 * by clients.
	 */
	public static void putConfigElement(IBuildObject buildObj, IManagedConfigElement configElement) {
		getConfigElementMap().put(buildObj, configElement);
	}

	/**
	 * This method public for implementation reasons.  Not intended for use 
	 * by clients.
	 */
	public static IManagedConfigElement getConfigElement(IBuildObject buildObj) {
		return (IManagedConfigElement)getConfigElementMap().get(buildObj);
	}

}

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
import java.util.Arrays;
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
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.DefaultManagedConfigElement;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedMakeMessages;
import org.eclipse.cdt.managedbuilder.internal.core.Target;
import org.eclipse.cdt.managedbuilder.internal.core.Tool;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.cdt.managedbuilder.scannerconfig.IManagedScannerInfoCollector;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;

/**
 * This is the main entry point for getting at the build information
 * for the managed build system. 
 */
public class ManagedBuildManager extends AbstractCExtension implements IScannerInfoProvider {

	private static final QualifiedName buildInfoProperty = new QualifiedName(ManagedBuilderCorePlugin.getUniqueIdentifier(), "managedBuildInfo");	//$NON-NLS-1$
	private static final String ROOT_NODE_NAME = "ManagedProjectBuildInfo";	//$NON-NLS-1$
	public static final String SETTINGS_FILE_NAME = ".cdtbuild";	//$NON-NLS-1$
	private static final ITarget[] emptyTargets = new ITarget[0];
	public static final String INTERFACE_IDENTITY = ManagedBuilderCorePlugin.getUniqueIdentifier() + ".ManagedBuildManager";	//$NON-NLS-1$
	public static final String EXTENSION_POINT_ID = ManagedBuilderCorePlugin.getUniqueIdentifier() + ".ManagedBuildInfo";	//$NON-NLS-1$
	private static final String REVISION_ELEMENT_NAME = "managedBuildRevision";	//$NON-NLS-1$
	private static final String VERSION_ELEMENT_NAME = "fileVersion";	//$NON-NLS-1$
	private static final String MANIFEST_VERSION_ERROR ="ManagedBuildManager.error.manifest.version.error";	//$NON-NLS-1$
	private static final String PROJECT_VERSION_ERROR ="ManagedBuildManager.error.project.version.error";	//$NON-NLS-1$
	
	// This is the version of the manifest and project files that
	private static final PluginVersionIdentifier buildInfoVersion = new PluginVersionIdentifier(2, 0, 0);
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
		try {
			// Make sure the extensions are loaded
			loadExtensions();
		} catch (BuildException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
	
	/**
	 * Sets the currently selected target.  This is used while the project
	 * property pages are displayed
	 * 
	 * @param project
	 * @param target
	 */
	public static void setSelectedTarget(IProject project, ITarget target) {
		if (project == null || target == null) {
			return;
		}
		// Set the default in build information for the project 
		IManagedBuildInfo info = getBuildInfo(project);
		if (info != null) {
			info.setSelectedTarget(target);
		}
	}

	public static IManagedBuilderMakefileGenerator getMakefileGenerator(String targetId) {
		try {
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extension = registry.getExtensionPoint(EXTENSION_POINT_ID);
			if (extension != null) {
				// There could be many of these
				IExtension[] extensions = extension.getExtensions();
				for (int i = 0; i < extensions.length; i++) {
					IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
					for (int j = 0; j < configElements.length; j++) {
						IConfigurationElement element = configElements[j];
						if (element.getName().equals(ITarget.TARGET_ELEMENT_NAME)) { 
							if (element.getAttribute(ITarget.ID).equals(targetId)) {
								if (element.getAttribute(ManagedBuilderCorePlugin.MAKEGEN_ID) != null) {
									return (IManagedBuilderMakefileGenerator) element.createExecutableExtension(ManagedBuilderCorePlugin.MAKEGEN_ID);
								}
							}
						}
					}
				}
			}
		} 
		catch (CoreException e) {
			// Probably not defined
			ManagedBuilderCorePlugin.log(e);
		}
		return null;
	}

	/** 
	 * Targets may have a scanner collector defined that knows how to discover 
	 * built-in compiler defines and includes search paths. Find the scanner 
	 * collector implentation for the target specified.
	 * 
	 * @param string the unique id of the target to search for
	 * @return an implementation of <code>IManagedScannerInfoCollector</code>
	 */
	public static IManagedScannerInfoCollector getScannerInfoCollector(String targetId) {
		try {
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extension = registry.getExtensionPoint(EXTENSION_POINT_ID);
			if (extension != null) {
				// There could be many of these
				IExtension[] extensions = extension.getExtensions();
				for (int i = 0; i < extensions.length; i++) {
					IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
					for (int j = 0; j < configElements.length; j++) {
						IConfigurationElement element = configElements[j];
						if (element.getName().equals(ITarget.TARGET_ELEMENT_NAME)) { 
							if (element.getAttribute(ITarget.ID).equals(targetId)) {
								if (element.getAttribute(ManagedBuilderCorePlugin.SCANNER_INFO_ID) != null) {
									return (IManagedScannerInfoCollector) element.createExecutableExtension(ManagedBuilderCorePlugin.SCANNER_INFO_ID);
								}
							}
						}
					}
				}
			}
		} 
		catch (CoreException e) {
			// Probably not defined
			ManagedBuilderCorePlugin.log(e);
		}
		return null;
	}

	/**
	 * Gets the currently selected target.  This is used while the project
	 * property pages are displayed
	 * 
	 * @param project
	 * @return target
	 */
	public static ITarget getSelectedTarget(IProject project) {
		if (project == null) {
			return null;
		}
		// Set the default in build information for the project 
		IManagedBuildInfo info = getBuildInfo(project);
		if (info != null) {
			return info.getSelectedTarget();
		}
		return null;
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
			((IScannerInfoChangeListener)iter.next()).changeNotification(resource, (IScannerInfo)getBuildInfo(resource));
		}
	}

	/**
	 * Adds the version of the managed build system to the project 
	 * specified in the argument.
	 * 
	 * @param newProject the project to version
	 */
	public static void setNewProjectVersion(IProject newProject) {
		// Get the build info for the argument
		ManagedBuildInfo info = findBuildInfo(newProject);
		info.setVersion(buildInfoVersion.toString());		
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
			
			// Get the build information for the project
			ManagedBuildInfo buildInfo = (ManagedBuildInfo) getBuildInfo(project);

			// Save the build info
			if (buildInfo != null && (force == true || buildInfo.isDirty())) {
				// For post-2.0 projects, there will be a version
				String projectVersion = buildInfo.getVersion();
				if (projectVersion != null) {
					ProcessingInstruction instruction = doc.createProcessingInstruction(VERSION_ELEMENT_NAME, projectVersion);
					doc.appendChild(instruction);
				}
				Element rootElement = doc.createElement(ROOT_NODE_NAME);	
				doc.appendChild(rootElement);
				buildInfo.serialize(doc, rootElement);
		
				// Transform the document to something we can save in a file
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				transformer.setOutputProperty(OutputKeys.METHOD, "xml");	//$NON-NLS-1$
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");	//$NON-NLS-1$
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(stream);
				transformer.transform(source, result);
				
				// Save the document
				IFile projectFile = project.getFile(SETTINGS_FILE_NAME);
				String utfString = stream.toString("UTF8");	//$NON-NLS-1$
				if (projectFile.exists()) {
					projectFile.setContents(new ByteArrayInputStream(utfString.getBytes()), IResource.FORCE, null);
				} else {
					projectFile.create(new ByteArrayInputStream(utfString.getBytes()), IResource.FORCE, null);
				}

				// Close the streams
				stream.close();
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
		try {
			loadExtensions();
		} catch (BuildException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
			// Must be an extension target
			if (owner != null)
				throw new BuildException(ManagedMakeMessages.getResourceString("ManagedBuildManager.error.owner_not_null")); //$NON-NLS-1$
		} else {
			// Owner must be owned by the project containing this resource
			if (owner == null)
				throw new BuildException(ManagedMakeMessages.getResourceString("ManagedBuildManager.error.null_owner")); //$NON-NLS-1$
			if (!owner.equals(resource.getProject()))
				throw new BuildException(ManagedMakeMessages.getResourceString("ManagedBuildManager.error.owner_not_project")); //$NON-NLS-1$
		}
		
		// Passed validation so create the target.
		return new Target(resource, parentTarget);
	}
	
	/**
	 * @param resource
	 * @return
	 */
	public static IStatus initBuildInfoContainer(IResource resource) {
		ManagedBuildInfo buildInfo = null;

		// Get the build info associated with this project for this session
		try {
			buildInfo = findBuildInfo(resource.getProject());
			initBuildInfoContainer(buildInfo);
		} catch (CoreException e) {
			ManagedBuilderCorePlugin.log(e);
			return new Status(IStatus.ERROR, 
				ManagedBuilderCorePlugin.getUniqueIdentifier(), 
				-1, 
				e.getLocalizedMessage(), 
				e);
		}
		return new Status(IStatus.OK, 
			ManagedBuilderCorePlugin.getUniqueIdentifier(), 
			IStatus.OK, 
			ManagedMakeMessages.getFormattedString("ManagedBuildInfo.message.init.ok", resource.getName()),	//$NON-NLS-1$ 
			null);
	}
	
	/* (non-Javadoc)
	 * Private helper method to intialize the path entry container once and 
	 * only once when the build info is first loaded or created.
	 * 
	 * @param info
	 * @throws CoreException
	 */
	private static void initBuildInfoContainer(ManagedBuildInfo info) throws CoreException {
		// Now associate the path entry container with the project
		ICProject cProject = info.getCProject();
		// This does not block the workspace or trigger delta events
		IPathEntry[] entries = cProject.getRawPathEntries();
		// Make sure the container for this project is in the path entries
		List newEntries = new ArrayList(Arrays.asList(entries));
		if (!newEntries.contains(ManagedBuildInfo.containerEntry)) {
			// In this case we should trigger an init and deltas
			newEntries.add(ManagedBuildInfo.containerEntry);
			cProject.setRawPathEntries((IPathEntry[])newEntries.toArray(new IPathEntry[newEntries.size()]), new NullProgressMonitor());
			info.setContainerCreated(true);
		}
	}
	
	private static boolean isVersionCompatible(IExtension extension) {
		// We can ignore the qualifier
		PluginVersionIdentifier version = null;

		// Get the version of the manifest
		IConfigurationElement[] elements = extension.getConfigurationElements();
		
		// Find the version string in the manifest
		for (int index = 0; index < elements.length; ++index) {
			IConfigurationElement element = elements[index];
			if (element.getName().equals(REVISION_ELEMENT_NAME)) {
				version = new PluginVersionIdentifier(element.getAttribute(VERSION_ELEMENT_NAME));
				break;
			}
		}
		
		if (version == null) {
			// This is a 1.2 manifest and we are compatible for now
			return true;
		} else {
			return(buildInfoVersion.isCompatibleWith(version));
		}
	}
	
	/* (non-Javadoc)
	 * Load the build information for the specified resource from its project
	 * file. Pay attention to the version number too.
	 */
	private static ManagedBuildInfo loadBuildInfo(IProject project) {
		ManagedBuildInfo buildInfo = null;
		IFile file = project.getFile(SETTINGS_FILE_NAME);
		if (!file.exists())
			return null;
	
		// So there is a project file, load the information there
		try {
			InputStream stream = file.getContents();
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = parser.parse(stream);
			String fileVersion = null;
			
			// Get the first element in the project file
			Node rootElement = document.getFirstChild();
			
			// Since 2.0 this will be a processing instruction containing version
			if (rootElement.getNodeType() != Node.PROCESSING_INSTRUCTION_NODE) {
				// This is a 1.2 project and it must be updated

			} else {
				// Make sure that the version is compatible with the manager
				fileVersion = rootElement.getNodeValue();
				PluginVersionIdentifier version = new PluginVersionIdentifier(fileVersion);
				if (!buildInfoVersion.isCompatibleWith(version)) {
					throw new BuildException(ManagedMakeMessages.getResourceString(PROJECT_VERSION_ERROR)); 
				}
				if (buildInfoVersion.isGreaterThan(version)) {
					// TODO Upgrade the project
				}
			}
			
			// Now get the project root element (there should be only one)
			NodeList nodes = document.getElementsByTagName(ROOT_NODE_NAME);
			if (nodes.getLength() > 0) {
				Node node = nodes.item(0);
				buildInfo = new ManagedBuildInfo(project, (Element)node);
				if (fileVersion != null) {
					buildInfo.setVersion(fileVersion);
				}
				project.setSessionProperty(buildInfoProperty, buildInfo);
			}
		} catch (Exception e) {
			ManagedBuilderCorePlugin.log(e);
			buildInfo = null;
		}
		return buildInfo;
	}

	/* (non-Javadoc)
	 * Since the class does not have a constructor but all public methods
	 * call this method first, it is effectively a startup method
	 */
	private static void loadExtensions() throws BuildException {
		if (extensionTargetsLoaded)
			return;
		
		// Get those extensions
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_POINT_ID);
		IExtension[] extensions = extensionPoint.getExtensions();
				
		// First call the constructors
		for (int i = 0; i < extensions.length; ++i) {
			IExtension extension = extensions[i];
			// Can we read this manifest
			if (!isVersionCompatible(extension)) {
				//The version of the Plug-in is greater than what the manager thinks it understands
				throw new BuildException(ManagedMakeMessages.getResourceString(MANIFEST_VERSION_ERROR));
			}			
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
	
	/**
	 * Creates a new build information object and associates it with the 
	 * resource in the argument. Note that the information contains no 
	 * build target or configuation information. It is the respoinsibility 
	 * of the caller to populate it. It is also important to note that the 
	 * caller is responsible for associating an IPathEntryContainer with the 
	 * build information after it has been populated. 
	 * <p>
	 * The typical sequence of calls to add a new build information object to 
	 * a managed build project is
	 * <p><pre>
	 * ManagedBuildManager.createBuildInfo(project);
	 * &#047;&#047; Do whatever initialization you need here
	 * ManagedBuildManager.createTarget(project);
	 * ManagedBuildManager.initBuildInfoContainer(project);
	 *   
	 * @param resource The resource the build information is associated with
	 */
	public static void createBuildInfo(IResource resource) {
		ManagedBuildInfo buildInfo = new ManagedBuildInfo(resource);
		try {
			// Associate the build info with the project for the duration of the session
			resource.setSessionProperty(buildInfoProperty, buildInfo);
		} catch (CoreException e) {
			// There is no point in keeping the info around if it isn't associated with the project
			ManagedBuilderCorePlugin.log(e);
			buildInfo = null;
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
		IFile file = project.getFile(SETTINGS_FILE_NAME);
		if (file.exists()) {
			try {
				InputStream stream = file.getContents();
				DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document document = parser.parse(stream);
				NodeList nodes = document.getElementsByTagName(ROOT_NODE_NAME);
				return (nodes.getLength() > 0);
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * Provate helper method that first checks to see if a build information 
	 * object has been associated with the project for the current workspace 
	 * session. If one cannot be found, one is created from the project file 
	 * associated with the argument. If there is no prject file or the load 
	 * fails for some reason, the method will return <code>null</code> 
	 *  
	 * @param resource
	 * @return
	 */
	private static ManagedBuildInfo findBuildInfo(IResource resource/*, boolean create*/) {
		// I am sick of NPEs
		if (resource == null) return null;

		// Make sure the extension information is loaded first
		try {
			loadExtensions();
		} catch (BuildException e) {
			e.printStackTrace();
			return null;
		}
		
		ManagedBuildInfo buildInfo = null;

		// Check if there is any build info associated with this project for this session
		try {
			buildInfo = (ManagedBuildInfo)resource.getSessionProperty(buildInfoProperty);
			// Make sure that if a project has build info, that the info is not corrupted
			if (buildInfo != null) {
				buildInfo.updateOwner(resource);
			}
		} catch (CoreException e) {
			ManagedBuilderCorePlugin.log(e);
			return null;
		}
		
		// Nothing in session store, so see if we can load it from cdtbuild
		if (buildInfo == null && resource instanceof IProject) {
			buildInfo = loadBuildInfo((IProject)resource);
			try {
				// Check if the project needs its container initialized
				initBuildInfoContainer(buildInfo);
			} catch (CoreException e) {
				// We can live without a path entry container if the build information is valid
				ManagedBuilderCorePlugin.log(e);
			}
		}
		return buildInfo;
	}
	
	/**
	 * Finds, but does not create, the managed build information for the 
	 * argument.
	 * 
	 * @see ManagedBuildManager#initBuildInfo(IResource)
	 * @param resource The resource to search for managed build information on.
	 * @return IManagedBuildInfo The build information object for the resource.
	 */
	public static IManagedBuildInfo getBuildInfo(IResource resource) {
		return (IManagedBuildInfo) findBuildInfo(resource.getProject());
	}

	/**
	 * Answers the current version of the managed builder plugin.
	 * 
	 * @return the current version of the managed builder plugin
	 */
	public static PluginVersionIdentifier getBuildInfoVersion() {
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
		return (IScannerInfo) getBuildInfo(resource.getProject());
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

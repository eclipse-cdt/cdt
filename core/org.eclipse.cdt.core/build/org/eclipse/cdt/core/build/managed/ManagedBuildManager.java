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
package org.eclipse.cdt.core.build.managed;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.Serializer;
import org.apache.xml.serialize.SerializerFactory;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.core.build.managed.ResourceBuildInfo;
import org.eclipse.cdt.internal.core.build.managed.Target;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.QualifiedName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This is the main entry point for getting at the build information
 * for the managed build system. 
 */
public class ManagedBuildManager {

	private static final QualifiedName buildInfoProperty
		= new QualifiedName(CCorePlugin.PLUGIN_ID, "buildInfo");

	private static final ITarget[] emptyTargets = new ITarget[0];

	// Targets defined by extensions (i.e., not associated with a resource)
	private static boolean extensionTargetsLoaded = false;
	private static List extensionTargets;
	private static Map extensionTargetMap;

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
		int size = extensionTargets.size()
			+ (definedTargets != null ? definedTargets.size() : 0);

		ITarget[] targets = new ITarget[size];
		
		int n = 0;
		for (int i = 0; i < extensionTargets.size(); ++i)
			targets[n++] = (ITarget)extensionTargets.get(i);
		
		if (definedTargets != null)
			for (int i = 0; i < definedTargets.size(); ++i)
				targets[n++] = (ITarget)definedTargets.get(i);
				
		return targets;
	}

	/**
	 * Returns the targets owned by this project.  If none are owned,
	 * an empty array is returned.
	 * 
	 * @param project
	 * @return
	 */
	public static ITarget[] getTargets(IResource resource) {
		ResourceBuildInfo buildInfo = getBuildInfo(resource);
		
		if (buildInfo != null) {
			List targets = buildInfo.getTargets();
			return (ITarget[])targets.toArray(new ITarget[targets.size()]);
		} else {
			return emptyTargets;
		}
	}

	public static ITarget getTarget(IResource resource, String id) {
		if (resource != null) {
			ResourceBuildInfo buildInfo = getBuildInfo(resource);
			if (buildInfo != null)
				return buildInfo.getTarget(id);
		}

		ITarget target = (ITarget)extensionTargetMap.get(id);
		if (target != null)
			return target;
			
		return null;
	}

	/**
	 * Adds a new target to the resource based on the parentTarget.
	 * 
	 * @param resource
	 * @param parentTarget
	 * @return
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
				throw new BuildException("addTarget: owner not null");
		} else {
			// Owner must be owned by the project containing this resource
			if (owner == null)
				throw new BuildException("addTarget: null owner");
			if (!owner.equals(resource.getProject()))
				throw new BuildException("addTarget: owner not project");
		}
		
		// Passed validation
		return new Target(resource, parentTarget);
	}
	
	/**
	 * Saves the build information associated with a project and all resources
	 * in the project to the build info file.
	 * 
	 * @param project
	 */
	public static void saveBuildInfo(IProject project) {
		// Create document
		Document doc = new DocumentImpl();
		Element rootElement = doc.createElement("buildInfo");
		doc.appendChild(rootElement);

		// Populate from buildInfo
		// To do - find other resources also
		ResourceBuildInfo buildInfo = getBuildInfo(project);
		if (buildInfo != null)
			buildInfo.serialize(doc, rootElement);
		
		// Save the document
		ByteArrayOutputStream s = new ByteArrayOutputStream();
		OutputFormat format = new OutputFormat();
		format.setIndenting(true);
		format.setLineSeparator(System.getProperty("line.separator")); //$NON-NLS-1$
		String xml = null;
		try {
			Serializer serializer
				= SerializerFactory.getSerializerFactory(Method.XML).makeSerializer(new OutputStreamWriter(s, "UTF8"), format);
			serializer.asDOMSerializer().serialize(doc);
			xml = s.toString("UTF8"); //$NON-NLS-1$		
			IFile rscFile = project.getFile(".cdtbuild");
			InputStream inputStream = new ByteArrayInputStream(xml.getBytes());
			// update the resource content
			if (rscFile.exists()) {
				rscFile.setContents(inputStream, IResource.FORCE, null);
			} else {
				rscFile.create(inputStream, IResource.FORCE, null);
			}
		} catch (Exception e) {
			return;
		}
	}

	public static void removeBuildInfo(IResource resource) {
		try {
			resource.setSessionProperty(buildInfoProperty, null);
		} catch (CoreException e) {
		}
	}
	
	// Private stuff

	public static void addExtensionTarget(Target target) {
		if (extensionTargets == null) {
			extensionTargets = new ArrayList();
			extensionTargetMap = new HashMap();
		}
		
		extensionTargets.add(target);
		extensionTargetMap.put(target.getId(), target);
	}
		
	private static void loadExtensions() {
		if (extensionTargetsLoaded)
			return;
		extensionTargetsLoaded = true;

		IExtensionPoint extensionPoint
			= CCorePlugin.getDefault().getDescriptor().getExtensionPoint("ManagedBuildInfo");
		IExtension[] extensions = extensionPoint.getExtensions();
		for (int i = 0; i < extensions.length; ++i) {
			IExtension extension = extensions[i];
			IConfigurationElement[] elements = extension.getConfigurationElements();
			for (int j = 0; j < elements.length; ++j) {
				IConfigurationElement element = elements[j];
				if (element.getName().equals("target")) {
					new Target(element);
				}
			}
		}
	}

	private static ResourceBuildInfo loadBuildInfo(IProject project) {
		ResourceBuildInfo buildInfo = null;
		IFile file = project.getFile(".cdtbuild");
		if (!file.exists())
			return null;
	
		try {
			InputStream stream = file.getContents();
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = parser.parse(stream);
			Node rootElement = document.getFirstChild();
			if (rootElement.getNodeName().equals("buildInfo")) {
				buildInfo = new ResourceBuildInfo(project, (Element)rootElement);
				project.setSessionProperty(buildInfoProperty, buildInfo);
			}
		} catch (Exception e) {
			buildInfo = null;
		}

		return buildInfo;
	}

	public static ResourceBuildInfo getBuildInfo(IResource resource, boolean create) {
		ResourceBuildInfo buildInfo = null;
		try {
			buildInfo = (ResourceBuildInfo)resource.getSessionProperty(buildInfoProperty);
		} catch (CoreException e) {
		}
		
		if (buildInfo == null && resource instanceof IProject) {
			buildInfo = loadBuildInfo((IProject)resource);
		}
		
		if (buildInfo == null && create) {
			try {
				buildInfo = new ResourceBuildInfo();
				resource.setSessionProperty(buildInfoProperty, buildInfo);
			} catch (CoreException e) {
				buildInfo = null;
			}
		}
		
		return buildInfo;
	}
	
	public static ResourceBuildInfo getBuildInfo(IResource resource) {
		return getBuildInfo(resource, false);
	}
}

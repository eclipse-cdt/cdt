package org.eclipse.cdt.internal.core.build.managed;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
 * **********************************************************************/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.cdt.core.build.managed.BuildException;
import org.eclipse.cdt.core.build.managed.IConfiguration;
import org.eclipse.cdt.core.build.managed.IResourceBuildInfo;
import org.eclipse.cdt.core.build.managed.ITarget;
import org.eclipse.cdt.core.build.managed.ITool;
import org.eclipse.core.resources.IResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ResourceBuildInfo implements IResourceBuildInfo {

	private IResource owner;
	private Map targetMap;
	private List targets;
	private Map defaultConfigurations;
	private ITarget defaultTarget;
	
	public ResourceBuildInfo() {
		targetMap = new HashMap();
		targets = new ArrayList();
		defaultConfigurations = new HashMap();
	}
	
	public ResourceBuildInfo(IResource owner, Element element) {
		this();
		// The id of the default configuration
		String defaultTargetId = null;
		List configIds = new ArrayList();
		Node child = element.getFirstChild();
		while (child != null) {
			if (child.getNodeName().equals("target")) {
				new Target(this, (Element)child);
			} else if (child.getNodeName().equals("defaultConfig")) {
				// We may not have read the config in yet, so just cache it
				configIds.add(((Element)child).getAttribute("id"));
			} else if (child.getNodeName().equals("defaultTarget")) {
				defaultTargetId = ((Element)child).getAttribute("id");
			}
			child = child.getNextSibling();
		}
		// All the available targets have been read in
		defaultTarget = (ITarget) targetMap.get(defaultTargetId);
		// Now we have a misserable O(N^2) operation (oh well, the data sets are small)
		ListIterator stringIter = configIds.listIterator();
		while (stringIter.hasNext()){
			String confId = (String) stringIter.next();
			ListIterator targIter = targets.listIterator();
			while (targIter.hasNext()) {
				Target targ = (Target) targIter.next();
				IConfiguration conf = targ.getConfiguration(confId);
				if (conf != null) {
					defaultConfigurations.put(targ.getId(), conf);
					break;
				}				
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IResourceBuildInfo#addTarget(org.eclipse.cdt.core.build.managed.ITarget)
	 */
	public void addTarget(ITarget target) {
		targetMap.put(target.getId(), target);
		targets.add(target);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IResourceBuildInfo#getBuildArtifactName()
	 */
	public String getBuildArtifactName() {
		// Get the default target and use its value
		String name = getDefaultTarget().getArtifactName();
		return name == null ? new String() : name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IResourceBuildInfo#getDefaultConfiguration()
	 */
	public IConfiguration getDefaultConfiguration(ITarget target) {
		// Get the default config associated with the defalt target
		IConfiguration config = (IConfiguration) defaultConfigurations.get(target.getId());

		// If null, look up the first configuration associated with the target
		if (config == null) {
			IConfiguration[] configs = getDefaultTarget().getConfigurations();
			if (configs.length > 0) {
				config = configs[0];
			}
		}
		return config;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IResourceBuildInfo#getDefaultTarget()
	 */
	public ITarget getDefaultTarget() {
		if (defaultTarget == null) {
			defaultTarget = (ITarget) targets.get(0);
		}
		return defaultTarget;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IResourceBuildInfo#getFlagsForSource(java.lang.String)
	 */
	public String getFlagsForSource(String extension) {
		// Get all the tools for the current config
		IConfiguration config = getDefaultConfiguration(getDefaultTarget());
		ITool[] tools = config.getTools();
		for (int index = 0; index < tools.length; index++) {
			ITool tool = tools[index];
			if (tool.buildsFileType(extension)) {
				String flags = new String();
				try {
					flags = tool.getToolFlags();
				} catch (BuildException e) {
					// Give it your best shot with the next tool
					continue;
				}
				return flags;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IResourceBuildInfo#getToolFlags(java.lang.String)
	 */
	public String getFlagsForTarget(String extension) {
		// Get all the tools for the current config
		IConfiguration config = getDefaultConfiguration(getDefaultTarget());
		ITool[] tools = config.getTools();
		for (int index = 0; index < tools.length; index++) {
			ITool tool = tools[index];
			if (tool.producesFileType(extension)) {
				String flags = new String();
				try {
					flags = tool.getToolFlags();
				} catch (BuildException e) {
					// TODO: handle exception
				}
				return flags;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IResourceBuildInfo#getOutputExtension(java.lang.String)
	 */
	public String getOutputExtension(String resourceExtension) {
		// Get all the tools for the current config
		IConfiguration config = getDefaultConfiguration(getDefaultTarget());
		ITool[] tools = config.getTools();
		for (int index = 0; index < tools.length; index++) {
			ITool tool = tools[index];
			String output = tool.getOutputExtension(resourceExtension);
			if (output != null) {
				return output;
			}
		}
		return null;
	}

	public IResource getOwner() {
		return owner;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IResourceBuildInfo#getTarget(org.eclipse.cdt.core.build.managed.IConfiguration)
	 */
	public ITarget getTarget(String id) {
		return (ITarget) targetMap.get(id);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IResourceBuildInfo#getTargets(org.eclipse.cdt.core.build.managed.IConfiguration)
	 */
	public List getTargets() {
		return targets;	
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IResourceBuildInfo#getToolForSource(java.lang.String)
	 */
	public String getToolForSource(String extension) {
		// Get all the tools for the current config
		IConfiguration config = getDefaultConfiguration(getDefaultTarget());
		ITool[] tools = config.getTools();
		for (int index = 0; index < tools.length; index++) {
			ITool tool = tools[index];
			if (tool.buildsFileType(extension)) {
				return tool.getToolCommand();
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IResourceBuildInfo#getToolInvocation(java.lang.String)
	 */
	public String getToolForTarget(String extension) {
		// Get all the tools for the current config
		IConfiguration config = getDefaultConfiguration(getDefaultTarget());
		ITool[] tools = config.getTools();
		for (int index = 0; index < tools.length; index++) {
			ITool tool = tools[index];
			if (tool.producesFileType(extension)) {
				return tool.getToolCommand();
			}
		}
		return null;
	}

	public void serialize(Document doc, Element element) {
		// Write out each target and their default config
		for (int i = 0; i < targets.size(); ++i) {
			Element targetElement = doc.createElement("target");
			element.appendChild(targetElement);
			((Target)targets.get(i)).serialize(doc, targetElement);
			IConfiguration config = getDefaultConfiguration((ITarget)targets.get(i));
			if (config != null) {
				Element configEl = doc.createElement("defaultConfig");
				element.appendChild(configEl);
				configEl.setAttribute("id", config.getId());
			}
		}
		// Persist the default target
		if (getDefaultTarget() != null){
			Element targEl = doc.createElement("defaultTarget");
			element.appendChild(targEl);
			targEl.setAttribute("id", getDefaultTarget().getId());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IResourceBuildInfo#setDefaultConfiguration(org.eclipse.cdt.core.build.managed.IConfiguration)
	 */
	public void setDefaultConfiguration(IConfiguration configuration) {
		// Get the target associated with the argument
		ITarget target = configuration.getTarget();
		// Make sure it is the default
		setDefaultTarget(target);
		defaultConfigurations.put(target.getId(), configuration);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IResourceBuildInfo#setDefaultTarget(org.eclipse.cdt.core.build.managed.ITarget)
	 */
	public void setDefaultTarget(ITarget target) {
		if (defaultTarget != null && defaultTarget.getId().equals(target.getId())) {
			return;
		}
		defaultTarget = target;		
	}

}

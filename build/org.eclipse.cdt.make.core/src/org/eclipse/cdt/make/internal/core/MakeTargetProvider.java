/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.internal.core;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.IMakeTargetListener;
import org.eclipse.cdt.make.core.IMakeTargetProvider;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.MakeTargetEvent;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MakeTargetProvider implements IMakeTargetProvider, IResourceChangeListener {
	private static String TARGET_BUILD_EXT = MakeCorePlugin.getUniqueIdentifier() + ".MakeTargetBuilder"; //$NON-NLS-1$

	private static String BUILD_TARGET_ELEMENT = "buildTargets"; //$NON-NLS-1$
	private static String TARGET_ELEMENT = "target"; //$NON-NLS-1$

	private ListenerList listeners = new ListenerList();
	private HashMap projectMap = new HashMap();
	private HashMap builderMap;

	public MakeTargetProvider() {
	}

	public IMakeTarget addTarget(IContainer container, String targetBuilderID, String targetName) throws CoreException {
		if (container instanceof IWorkspaceRoot) {
			throw new CoreException(new Status(IStatus.ERROR, MakeCorePlugin.getUniqueIdentifier(), -1, MakeCorePlugin.getResourceString("MakeTargetProvider.add_to_workspace_root"), null)); //$NON-NLS-1$
		}
		IProject project = container.getProject();
		HashMap targetMap = (HashMap) projectMap.get(project);
		if (targetMap == null) {
			targetMap = initializeTargets(project);
		}
		ArrayList list = (ArrayList) targetMap.get(container);
		MakeTarget target = new MakeTarget(targetBuilderID, targetName);
		if (list != null && list.contains(target)) {
			throw new CoreException(new Status(IStatus.ERROR, MakeCorePlugin.getUniqueIdentifier(), -1, MakeCorePlugin.getResourceString("MakeTargetProvider.target_exists"), null)); //$NON-NLS-1$
		}
		target.setContainer(container);
		if (list == null) {
			list = new ArrayList();
			targetMap.put(container, list);
		}
		list.add(target);
		notifyListeners(new MakeTargetEvent(this, MakeTargetEvent.TARGET_ADD, target));
		return target;
	}

	public void removeTarget(IMakeTarget target) throws CoreException {
		IProject project = target.getContainer().getProject();
		HashMap targetMap = (HashMap) projectMap.get(project);
		if (targetMap == null) {
			targetMap = initializeTargets(project);
		}
		ArrayList list = (ArrayList) targetMap.get(target.getContainer());
		if (list != null && !list.contains(target)) {
			return;
		}
		list.remove(target);
		if (list.size() == 0) {
			targetMap.remove(list);
		}
		if (targetMap.size() == 0) {
			projectMap.remove(project);
		}
		notifyListeners(new MakeTargetEvent(this, MakeTargetEvent.TARGET_REMOVED, target));
	}

	public void renameTarget(IMakeTarget target, String name) throws CoreException {
		IProject project = target.getContainer().getProject();
		HashMap targetMap = (HashMap) projectMap.get(project);
		if (targetMap == null) {
			targetMap = initializeTargets(project);
		}
		ArrayList list = (ArrayList) targetMap.get(target.getContainer());
		if (list != null && !list.contains(target)) {
			throw new CoreException(new Status(IStatus.ERROR, MakeCorePlugin.getUniqueIdentifier(), -1, MakeCorePlugin.getResourceString("MakeTargetProvider.target_does_not_exists"), null)); //$NON-NLS-1$
		}
		((MakeTarget) target).setName(name);
		notifyListeners(new MakeTargetEvent(this, MakeTargetEvent.TARGET_CHANGED, target));
	}

	public IMakeTarget[] getTargets(IContainer container) throws CoreException {
		IProject project = container.getProject();
		HashMap targetMap = (HashMap) projectMap.get(project);
		if (targetMap == null) {
			targetMap = initializeTargets(project);
		}
		ArrayList list = (ArrayList) targetMap.get(container);
		if (list != null) {
			return (IMakeTarget[]) list.toArray(new IMakeTarget[list.size()]);
		}
		return new IMakeTarget[0];
	}

	private HashMap initializeTargets(IProject project) throws CoreException {
		HashMap targetMap = new HashMap();
		IPath targetFilePath = MakeCorePlugin.getDefault().getStateLocation().append(project.getName());
		File targetFile = targetFilePath.toFile();
		if (targetFile.exists()) {
			try {
				FileInputStream file = new FileInputStream(targetFile);
				DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document document = parser.parse(file);
				Node node = document.getFirstChild();
				if (node.getNodeName().equals(BUILD_TARGET_ELEMENT)) {
					NodeList list = node.getChildNodes();
					for( int i = 0; i < list.getLength(); i++) {
						Node item = list.item(i);
						if ( item.getNodeName().equals(TARGET_ELEMENT)) {
							NamedNodeMap attr = item.getAttributes();
							MakeTarget target = new MakeTarget(attr.getNamedItem("targetID").getNodeValue(), attr.getNamedItem("name").getNodeValue()); //$NON-NLS-1$ //$NON-NLS-2$
							
//							targetMap.put(container, target);
						}
					}
				}
			} catch (Exception e) {
				throw new CoreException(
					new Status(IStatus.ERROR, MakeCorePlugin.getUniqueIdentifier(), -1, MakeCorePlugin.getResourceString("MakeTargetProvider.failed_initializing_targets"), e)); //$NON-NLS-1$
			}
		}
		return targetMap;
	}

	public IProject[] getTargetBuilderProjects() throws CoreException {
		Vector tProj = new Vector();
		IProject project[] = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < project.length; i++) {
			IProjectDescription description = project[i].getDescription();
			ICommand builder[] = description.getBuildSpec();
			for (int j = 0; j < builder.length; j++) {
				if (builderMap.containsValue(builder[j].getBuilderName())) {
					tProj.add(project[i]);
					break;
				}
			}
		}
		return (IProject[]) tProj.toArray(new IProject[tProj.size()]);
	}

	public void startup() {
		initializeBuilders();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	public void shutdown() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}

	private void initializeBuilders() {
		builderMap = new HashMap();

		IExtensionPoint point = MakeCorePlugin.getDefault().getDescriptor().getExtensionPoint(MakeTargetProvider.TARGET_BUILD_EXT);
		IExtension[] ext = point.getExtensions();
		for (int i = 0; i < ext.length; i++) {
			IConfigurationElement[] element = ext[i].getConfigurationElements();
			for (int j = 0; j < element.length; j++) {
				if (element[j].getName().equals("builder")) { //$NON-NLS-1$
					String builderID = element[j].getAttribute("builderID"); //$NON-NLS-1$
					String targetID = element[j].getAttribute("id"); //$NON-NLS-1$
					builderMap.put(targetID, builderID);
				}
			}
		}
	}

	private void notifyListeners(MakeTargetEvent event) {
		Object[] list = listeners.getListeners();
		for (int i = 0; i < list.length; i++) {
			((IMakeTargetListener) list[i]).targetChanged(event);
		}
	}

	public void addListener(IMakeTargetListener listener) {
		listeners.add(listener);
	}

	public void removeListener(IMakeTargetListener listener) {
		listeners.remove(listeners);
	}

	public void resourceChanged(IResourceChangeEvent event) {
		// dinglis-TODO listen for project that add/remove a target type builder

	}
}
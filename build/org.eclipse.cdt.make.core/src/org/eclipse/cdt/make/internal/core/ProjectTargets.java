/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     James Blackburn (Broadcom Corp.) - Use ICStorageElement
 *     Red Hat Inc. - Add set method
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.XmlStorageUtil;
import org.eclipse.cdt.make.core.IMakeCommonBuildInfo;
import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.Document;

public class ProjectTargets {

	private static final String MAKE_TARGET_KEY = MakeCorePlugin.getUniqueIdentifier() + ".buildtargets"; //$NON-NLS-1$
	private static final String TARGETS_EXT = "targets"; //$NON-NLS-1$

	private static final String BUILD_TARGET_ELEMENT = "buildTargets"; //$NON-NLS-1$
	private static final String TARGET_ELEMENT = "target"; //$NON-NLS-1$
	private static final String TARGET_ATTR_ID = "targetID"; //$NON-NLS-1$
	private static final String TARGET_ATTR_PATH = "path"; //$NON-NLS-1$
	private static final String TARGET_ATTR_NAME = "name"; //$NON-NLS-1$
	private static final String TARGET_STOP_ON_ERROR = "stopOnError"; //$NON-NLS-1$
	private static final String TARGET_USE_DEFAULT_CMD = "useDefaultCommand"; //$NON-NLS-1$
	private static final String TARGET_ARGUMENTS = "buildArguments"; //$NON-NLS-1$
	private static final String TARGET_COMMAND = "buildCommand"; //$NON-NLS-1$
	private static final String TARGET_RUN_ALL_BUILDERS = "runAllBuilders"; //$NON-NLS-1$
	private static final String BAD_TARGET = "buidlTarget"; //$NON-NLS-1$
	private static final String TARGET = "buildTarget"; //$NON-NLS-1$

	private HashMap<IContainer, List<IMakeTarget>> targetMap = new HashMap<IContainer, List<IMakeTarget>>();

	private IProject project;

	public ProjectTargets(MakeTargetManager manager, IProject project) {
		boolean writeTargets = false;
		File targetFile = null;

		this.project = project;

		ICStorageElement rootElement = null;
		try {
			ICDescriptor descriptor = CCorePlugin.getDefault().getCProjectDescription(getProject(), true);
			rootElement = descriptor.getProjectStorageElement(MAKE_TARGET_KEY);

			//Historical ... fall back to the workspace and look in previous XML file location
			if (rootElement.getChildren().length == 0) {
				IPath targetFilePath = MakeCorePlugin.getDefault().getStateLocation().append(project.getName()).addFileExtension(
						TARGETS_EXT);
				targetFile = targetFilePath.toFile();
				try {
					InputStream input = new FileInputStream(targetFile);
					ICStorageElement oldElement = translateInputStreamToDocument(input);
					rootElement.importChild(oldElement);
					writeTargets = true; // update the project description
				} catch (FileNotFoundException ex) {
					/* Ignore */
				}
			}

			extractMakeTargetsFromDocument(rootElement, manager);
			// If write targets then we have converted previous make targets
			if (writeTargets) {
				saveTargets();
				if (targetFile != null) {
					targetFile.delete(); // removed old
				}
			}
		} catch (Exception e) {
			MakeCorePlugin.log(e);
		}
	}

	public IMakeTarget[] get(IContainer container) {
		List<IMakeTarget> list = targetMap.get(container);
		if (list != null) {
			return list.toArray(new IMakeTarget[list.size()]);
		}
		return new IMakeTarget[0];
	}

	public void set(IContainer container, IMakeTarget[] targets) throws CoreException {
		List<IMakeTarget> newList = new ArrayList<IMakeTarget>();
		for (IMakeTarget target : targets) {
			target.setContainer(container);
			if (newList.contains(target)) {
				throw new CoreException(new Status(IStatus.ERROR, MakeCorePlugin.getUniqueIdentifier(), -1,
						MakeMessages.getString("MakeTargetManager.target_exists"), null)); //$NON-NLS-1$
			}
			newList.add(target);
		}
		targetMap.put(container, newList);
	}

	public IMakeTarget findTarget(IContainer container, String name) {
		List<IMakeTarget> list = targetMap.get(container);
		if (list != null) {
			for (IMakeTarget target : list) {
				if (name.equals(target.getName())) {
					return target;
				}
			}
		}
		return null;
	}

	public void add(IMakeTarget target) throws CoreException {
		List<IMakeTarget> list = targetMap.get(target.getContainer());
		if (list != null && list.contains(target)) {
			throw new CoreException(new Status(IStatus.ERROR, MakeCorePlugin.getUniqueIdentifier(), -1,
					MakeMessages.getString("MakeTargetManager.target_exists"), null)); //$NON-NLS-1$
		}
		if (list == null) {
			list = new ArrayList<IMakeTarget>();
			targetMap.put(target.getContainer(), list);
		}
		list.add(target);
	}

	public boolean contains(IMakeTarget target) {
		List<IMakeTarget> list = targetMap.get(target.getContainer());
		if (list != null && list.contains(target)) {
			return true;
		}
		return false;
	}

	public boolean remove(IMakeTarget target) {
		List<IMakeTarget> list = targetMap.get(target.getContainer());
		if (list == null || !list.contains(target)) {
			return false;
		}
		boolean found = list.remove(target);
		if (list.size() == 0) {
			targetMap.remove(list);
		}
		return found;
	}

	public IProject getProject() {
		return project;
	}

	/**
	 * Persist the MakeTarget as a child of parent
	 * 
	 * @return created ICStorageElement
	 */
	private ICStorageElement createTargetElement(ICStorageElement parent, IMakeTarget target) {
		ICStorageElement targetElem = parent.createChild(TARGET_ELEMENT);
		targetElem.setAttribute(TARGET_ATTR_NAME, target.getName());
		targetElem.setAttribute(TARGET_ATTR_ID, target.getTargetBuilderID());
		targetElem.setAttribute(TARGET_ATTR_PATH, target.getContainer().getProjectRelativePath().toString());
		ICStorageElement elem = targetElem.createChild(TARGET_COMMAND);
		elem.setValue(target.getBuildAttribute(IMakeCommonBuildInfo.BUILD_COMMAND, "make")); //$NON-NLS-1$

		String targetAttr = target.getBuildAttribute(IMakeCommonBuildInfo.BUILD_ARGUMENTS, null);
		if ( targetAttr != null) {
			elem = targetElem.createChild(TARGET_ARGUMENTS);
			elem.setValue(targetAttr);
		}

		targetAttr = target.getBuildAttribute(IMakeTarget.BUILD_TARGET, null);
		if (targetAttr != null) {
			elem = targetElem.createChild(TARGET);
			elem.setValue(targetAttr);
		}

		elem = targetElem.createChild(TARGET_STOP_ON_ERROR);
		elem.setValue(new Boolean(target.isStopOnError()).toString());

		elem = targetElem.createChild(TARGET_USE_DEFAULT_CMD);
		elem.setValue(new Boolean(target.isDefaultBuildCmd()).toString());

		elem = targetElem.createChild(TARGET_RUN_ALL_BUILDERS);
		elem.setValue(new Boolean(target.runAllBuilders()).toString());

		return targetElem;
	}

	/**
	 * Saves the targets to the project description
	 */
	public void saveTargets() throws CoreException {
		ICDescriptor descriptor = CCorePlugin.getDefault().getCProjectDescription(getProject(), true);
		ICStorageElement rootElement = descriptor.getProjectStorageElement(MAKE_TARGET_KEY);

		//Nuke the children since we are going to write out new ones
		rootElement.clear();

		// Fetch the ProjectTargets as ICStorageElements
		rootElement = rootElement.createChild(BUILD_TARGET_ELEMENT);
		for (Entry<IContainer, List<IMakeTarget>> e : targetMap.entrySet())
			for (IMakeTarget target : e.getValue())
				createTargetElement(rootElement, target);

		//Save the results
		descriptor.saveProjectData();
	}

	/**
	 * This method loads an old style XML document provided in the input stream
	 * and returns an ICStorageElemnt wrapping it.
	 * 
	 * @return ICStorageElement or null
	 */
	protected ICStorageElement translateInputStreamToDocument(InputStream input) {
		try {
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
			return XmlStorageUtil.createCStorageTree(document);
		} catch (Exception e) {
			MakeCorePlugin.log(e);
		}
		return null;
	}

	/**
	 * Extract the make target information which is contained in the Storage Element
	 *
	 * @param root - root element
	 * @param manager - MakeTargetManager
	 */
	protected void extractMakeTargetsFromDocument(ICStorageElement root, MakeTargetManager manager) {
		for (ICStorageElement node : root.getChildren()) {
			if (node.getName().equals(BUILD_TARGET_ELEMENT)) {
				for (ICStorageElement child : node.getChildren()) {
					node = child;
					if (node.getName().equals(TARGET_ELEMENT)) {
						IContainer container = null;
						String path = node.getAttribute(TARGET_ATTR_PATH);
						if (path != null && !path.equals("")) { //$NON-NLS-1$
							container = project.getFolder(path);
						} else {
							container = project;
						}
						try {
							IMakeTarget target = new MakeTarget(manager, project, node.getAttribute(TARGET_ATTR_ID),
									node.getAttribute(TARGET_ATTR_NAME));
							target.setContainer(container);
							ICStorageElement[] option = node.getChildrenByName(TARGET_STOP_ON_ERROR);
							if (option.length > 0) {
								target.setStopOnError(Boolean.valueOf(option[0].getValue()).booleanValue());
							}
							option =  node.getChildrenByName(TARGET_USE_DEFAULT_CMD);
							if (option.length > 0) {
								target.setUseDefaultBuildCmd(Boolean.valueOf(option[0].getValue()).booleanValue());
							}
							option =  node.getChildrenByName(TARGET_COMMAND);
							if (option.length > 0) {
								target.setBuildAttribute(IMakeCommonBuildInfo.BUILD_COMMAND, option[0].getValue());
							}
							option = node.getChildrenByName(TARGET_ARGUMENTS);
							if (option.length > 0) {
								target.setBuildAttribute(IMakeCommonBuildInfo.BUILD_ARGUMENTS, option[0].getValue());
							} else if (!target.isDefaultBuildCmd()) {
								// Clear build-arguments set in target constructor to project defaults
								target.setBuildAttribute(IMakeCommonBuildInfo.BUILD_ARGUMENTS, ""); //$NON-NLS-1$
							}
							option = node.getChildrenByName(BAD_TARGET);
							if (option.length > 0) {
								target.setBuildAttribute(IMakeTarget.BUILD_TARGET, option[0].getValue());
							}
							option = node.getChildrenByName(TARGET);
							if (option.length > 0) {
								target.setBuildAttribute(IMakeTarget.BUILD_TARGET, option[0].getValue());
							}
							option = node.getChildrenByName(TARGET_RUN_ALL_BUILDERS);
							if (option.length > 0) {
								target.setRunAllBuilders(Boolean.valueOf(option[0].getValue()).booleanValue());
							}
							add(target);
						} catch (CoreException e) {
							MakeCorePlugin.log(e);
						}
					}
				}
			}
		}
	}
}

/*******************************************************************************
 * Copyright (c) 2000, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.util.XmlStorageElement;
import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ProjectTargets {

	private static final String MAKE_TARGET_KEY = MakeCorePlugin.getUniqueIdentifier() + ".buildtargets"; //$NON-NLS-1$
	private static final String TARGETS_EXT = "targets"; //$NON-NLS-1$

	private static final String BUILD_TARGET_ELEMENT = "buildTargets"; //$NON-NLS-1$
	private static final String TARGET_ELEMENT = "target"; //$NON-NLS-1$
	
	private static final String TARGETS_STORAGE_ID = "build.Targets"; //$NON-NLS-1$

	private HashMap targetMap = new HashMap();
//	private IConfiguration configuraion;
	private ICProjectDescription projDes;
//	private IProject project;

	public ProjectTargets(ProjectTargets targets, ICProjectDescription des, IConfiguration cfg) {
		projDes = des;
		for(Iterator iter = targets.targetMap.entrySet().iterator(); iter.hasNext();){
			Map.Entry entry = (Map.Entry)iter.next();
			IContainer cr = (IContainer)entry.getKey();
			List list = (List)entry.getValue();
			
			List targetsList = new ArrayList(list.size());
			targetMap.put(cr, targetsList);
			for(int i = 0; i < list.size(); i++){
				MakeTarget target = (MakeTarget)list.get(i);
				MakeTarget cloneTarget = new MakeTarget(target, cfg);
				targetsList.add(cloneTarget);
			}
		}
	}

	public ProjectTargets(MakeTargetManager manager, ICProjectDescription projDes, IConfiguration cfg) {
		boolean writeTargets = false;
		File targetFile = null;

//		this.project = project;
		this.projDes = projDes;
		IProject project = projDes.getProject();
		
//		ICConfigurationDescription des = ManagedBuildManager.getDescriptionForConfiguration(cfg);
		ICStorageElement el = null;
		
		try {
			ICStorageElement rootEl = getStorageElement(projDes, false);
			
			if(rootEl != null){
				ICStorageElement children[] = rootEl.getChildren();
				for(int i = 0; i < children.length; i++){
					if(BUILD_TARGET_ELEMENT.equals(children[i].getName())){
						el = children[i];
						break;
					}
				}
			}
		} catch (CoreException e1) {
		}
//		Document document = translateCDTProjectToDocument();

		//Historical ... fall back to the workspace and look in previous
		// location
		if (el == null) {
			IPath targetFilePath = ManagedBuilderCorePlugin.getDefault().getStateLocation().append(project.getName()).addFileExtension(
					TARGETS_EXT);
			targetFile = targetFilePath.toFile();
			try {
				InputStream input = new FileInputStream(targetFile);
				Document document = translateInputStreamToDocument(input);
				Element element = document.getDocumentElement();
				el = new XmlStorageElement(element);
				writeTargets = true; // update cdtproject
			} catch (FileNotFoundException ex) {
				/* Ignore */
			}
		}

		if (el != null) {
			extractMakeTargetsFromDocument(el, manager, cfg);
			if (writeTargets) {
				try {
					saveTargets();
//					translateDocumentToCDTProject(doc);
				} catch (Exception e) {
					targetFile = null;
				}
				if (targetFile != null) {
					targetFile.delete(); // removed old
				}
			}
		}
	}
	
/*	public IConfiguration getConfiguration(){
		return configuraion;
	}
*/
	protected String getString(Node target, String tagName) {
		Node node = searchNode(target, tagName);
		return node != null ? (node.getFirstChild() == null ? null : node.getFirstChild().getNodeValue()) : null;
	}

	protected Node searchNode(Node target, String tagName) {
		NodeList list = target.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i).getNodeName().equals(tagName))
				return list.item(i);
		}
		return null;
	}

	public IMakeTarget[] get(IContainer container) {
		ArrayList list = (ArrayList) targetMap.get(container);
		if (list != null) {
			return (IMakeTarget[]) list.toArray(new IMakeTarget[list.size()]);
		}
		return new IMakeTarget[0];
	}

	public IMakeTarget findTarget(IContainer container, String name) {
		ArrayList list = (ArrayList) targetMap.get(container);
		if (list != null) {
			Iterator targets = list.iterator();
			while (targets.hasNext()) {
				IMakeTarget target = (IMakeTarget) targets.next();
				if (target.getName().equals(name)) {
					return target;
				}
			}
		}
		return null;
	}

	public void add(MakeTarget target) throws CoreException {
		ArrayList list = (ArrayList) targetMap.get(target.getContainer());
		if (list != null && list.contains(target)) {
			throw new CoreException(new Status(IStatus.ERROR, MakeCorePlugin.getUniqueIdentifier(), -1,
					MakeMessages.getString("MakeTargetManager.target_exists"), null)); //$NON-NLS-1$
		}
		if (list == null) {
			list = new ArrayList();
			targetMap.put(target.getContainer(), list);
		}
		list.add(target);
	}

	public boolean contains(MakeTarget target) {
		ArrayList list = (ArrayList) targetMap.get(target.getContainer());
		if (list != null && list.contains(target)) {
			return true;
		}
		return false;
	}

	public boolean remove(MakeTarget target) {
		ArrayList list = (ArrayList) targetMap.get(target.getContainer());
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
		return projDes.getProject();
	}

	protected void storeTargets(ICStorageElement element) throws CoreException {
		Iterator container = targetMap.entrySet().iterator();
		while (container.hasNext()) {
			List targets = (List) ((Map.Entry) container.next()).getValue();
			for (int i = 0; i < targets.size(); i++) {
				MakeTarget target = (MakeTarget) targets.get(i);
				ICStorageElement child = element.createChild(TARGET_ELEMENT);
				target.serialize(child);
			}
		}
	}

	public void saveTargets() throws CoreException {
		ICProjectDescription des = projDes.isReadOnly() ?
				CoreModel.getDefault().getProjectDescription(projDes.getProject()) 
				:
					projDes;

		ICStorageElement rootEl = getStorageElement(des, true);
		rootEl.clear();
		ICStorageElement el = rootEl.createChild(BUILD_TARGET_ELEMENT);
		storeTargets(el);
		
		CoreModel.getDefault().setProjectDescription(des.getProject(), des);
	}

	private ICStorageElement getStorageElement(ICProjectDescription des, boolean create) throws CoreException{
//		return create || des.containsStorage(TARGETS_STORAGE_ID) ?
//				des.getStorage(TARGETS_STORAGE_ID) : null;
		return des.getStorage(TARGETS_STORAGE_ID, create);
	}

	/**
	 * This method parses the .cdtproject file for the XML document describing the build targets.
	 * 
	 * @param input
	 * @return
	 */
/*	protected Document translateCDTProjectToDocument() {
		Document document = null;
		Element rootElement = null;
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			ICDescriptor descriptor;
			descriptor = CCorePlugin.getDefault().getCProjectDescription(getProject(), true);

			rootElement = descriptor.getProjectData(MAKE_TARGET_KEY);
		} catch (ParserConfigurationException e) {
			return document;
		} catch (CoreException e) {
			return document;
		}
		NodeList list = rootElement.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Node appendNode = document.importNode(list.item(i), true);
				document.appendChild(appendNode);
				break; // should never have multiple <buildtargets>
			}
		}
		return document;
	}
*/
	/**
	 * This method parses the input stream for the XML document describing the build targets.
	 * 
	 * @param input
	 * @return
	 */
	protected Document translateInputStreamToDocument(InputStream input) {
		Document document = null;
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
		} catch (Exception e) {
			MakeCorePlugin.log(e);
		}
		return document;
	}

	/**
	 * Extract the make target information which is contained in the XML Document
	 * 
	 * @param document
	 */
	protected void extractMakeTargetsFromDocument(ICStorageElement el, MakeTargetManager manager, IConfiguration cfg) {
//		Node node = document.getFirstChild();
//		if (node != null && node.getNodeName().equals(BUILD_TARGET_ELEMENT)) {
			ICStorageElement list[] = el.getChildren();
			ICConfigurationDescription cfgDes = projDes.getActiveConfiguration();
/*			IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(cfgDes);
			if(cfg == null){
				ICConfigurationDescription dess[] = projDes.getConfigurations();
				for(int i = 0; i < dess.length; i++){
					cfg = ManagedBuildManager.getConfigurationForDescription(dess[i]);
					if(cfg != null)
						break;
				}
			}
*/			
			if(cfg != null){
				for (int i = 0; i < list.length; i++) {
					ICStorageElement node = list[i];
					if (node.getName().equals(TARGET_ELEMENT)) {
						try {
							MakeTarget target = new MakeTarget(manager, cfg, node);
							add(target);
						} catch (CoreException e) {
							ManagedBuilderCorePlugin.log(e);
						}
					}
				}
			}
//		}
	}
	
	public ICProjectDescription getProjectDescription(){
		return projDes;
	}
}

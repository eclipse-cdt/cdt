/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICExtension;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.core.ICOwnerInfo;
import org.eclipse.cdt.core.settings.model.ICConfigExtensionReference;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.settings.model.util.CExtensionUtil;
import org.eclipse.cdt.internal.core.settings.model.CConfigurationDescriptionCache;
import org.eclipse.cdt.internal.core.settings.model.CConfigurationSpecSettings;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescriptionManager;
import org.eclipse.cdt.internal.core.settings.model.CStorage;
import org.eclipse.cdt.internal.core.settings.model.ExceptionFactory;
import org.eclipse.cdt.internal.core.settings.model.IInternalCCfgInfo;
import org.eclipse.cdt.internal.core.settings.model.InternalXmlStorageElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CConfigBasedDescriptor implements ICDescriptor {
	private static final String CEXTENSION_NAME = "cextension"; //$NON-NLS-1$

	private ICConfigurationDescription fCfgDes;
	private IProject fProject;
	private COwner fOwner;
	private HashMap fDesMap = new HashMap();
	private HashMap fStorageDataElMap = new HashMap();
	private boolean fApplyOnChange = true;
	private boolean fIsDirty;
	
	class CConfigBaseDescriptorExtensionReference implements ICExtensionReference{
		private ICConfigExtensionReference fCfgExtRef;
		CConfigBaseDescriptorExtensionReference(ICConfigExtensionReference cfgRef){
			fCfgExtRef = cfgRef; 
		}

		public ICExtension createExtension() throws CoreException {
			InternalCExtension cExtension = null;
			IConfigurationElement el = CExtensionUtil.getFirstConfigurationElement(fCfgExtRef, CEXTENSION_NAME, false);
			cExtension = (InternalCExtension)el.createExecutableExtension("run"); //$NON-NLS-1$
			cExtension.setExtensionReference(this);
			cExtension.setProject(fProject);
			return (ICExtension)cExtension;
		}

		public ICDescriptor getCDescriptor() {
			return CConfigBasedDescriptor.this;
		}

		public String getExtension() {
			return fCfgExtRef.getExtensionPoint();
		}

		public String getExtensionData(String key) {
			return fCfgExtRef.getExtensionData(key);
		}

		public IConfigurationElement[] getExtensionElements()
				throws CoreException {
			IConfigurationElement el = CExtensionUtil.getFirstConfigurationElement(fCfgExtRef, CEXTENSION_NAME, false);
			if(el != null)
				return el.getChildren();
			return new IConfigurationElement[0];
		}

		public String getID() {
			return fCfgExtRef.getID();
		}

		public void setExtensionData(String key, String value)
				throws CoreException {
			if(!CDataUtil.objectsEqual(fCfgExtRef.getExtensionData(key), value)){
				fIsDirty = true;
				fCfgExtRef.setExtensionData(key, value);
				checkApply();
			}
		}
	}

	public CConfigBasedDescriptor(ICConfigurationDescription des) throws CoreException{
		this(des, true);
	}

	public CConfigBasedDescriptor(ICConfigurationDescription des, boolean write) throws CoreException{
		updateConfiguration(des, write);
	}
	
	public void setApplyOnChange(boolean apply){
		if(fApplyOnChange == apply)
			return;
		
		fApplyOnChange = apply;
	}
	
	public boolean isApplyOnChange(){
		return fApplyOnChange;
	}
	
	public void apply(boolean force) throws CoreException{
		if(force || fIsDirty){
			ICProjectDescription des = fCfgDes.getProjectDescription();
			if(des.isCdtProjectCreating())
				des.setCdtProjectCreated();
			CProjectDescriptionManager.getInstance().setProjectDescription(fProject, des);
			fIsDirty = false;
		}
	}
	
	private void checkApply() throws CoreException {
		if(fApplyOnChange){
			apply(false);
			fIsDirty = false;
		} else {
			fIsDirty = true;
		}
	}
	
	public ICExtensionReference create(String extensionPoint, String id)
			throws CoreException {
		ICConfigExtensionReference ref = fCfgDes.create(extensionPoint, id);

		//write is done for all configurations to avoid "data loss" on configuration change
		ICProjectDescription des = fCfgDes.getProjectDescription();
		ICConfigurationDescription cfgs[] = des.getConfigurations();
		for(int i = 0; i < cfgs.length; i++){
			ICConfigurationDescription cfg = cfgs[i];
			if(cfg != fCfgDes){
				try {
					cfg.create(extensionPoint, id);
				} catch (CoreException e){
				}
			}
		}
		
		ICExtensionReference r = create(ref);
		fIsDirty = true;
		checkApply();
		return r;
	}
	
	void setDirty(boolean dirty){
		fIsDirty = dirty;
	}
	
	public void updateConfiguration(ICConfigurationDescription des) throws CoreException{
		updateConfiguration(des, true);
	}

	public void updateConfiguration(ICConfigurationDescription des, boolean write) throws CoreException{
		if(write && des instanceof CConfigurationDescriptionCache)
			throw new IllegalArgumentException();
		
		fCfgDes = des;
		fProject = fCfgDes.getProjectDescription().getProject();
		CConfigurationSpecSettings settings = ((IInternalCCfgInfo)fCfgDes).getSpecSettings(); 
		fOwner = settings.getCOwner();
//		settings.setDescriptor(this);
		fStorageDataElMap.clear();
	}
	
	private CConfigBaseDescriptorExtensionReference create(ICConfigExtensionReference ref){
		CConfigBaseDescriptorExtensionReference dr = new CConfigBaseDescriptorExtensionReference(ref);
		ArrayList list = (ArrayList)fDesMap.get(ref.getExtensionPoint());
		if(list == null){
			list = new ArrayList(1);
			fDesMap.put(ref.getExtensionPoint(), list);
		} else {
			list.ensureCapacity(list.size() + 1);
		}
		list.add(dr);
		return dr;
	}

	public ICExtensionReference[] get(String extensionPoint) {
		ICConfigExtensionReference cfgRefs[] = fCfgDes.get(extensionPoint);
		if(cfgRefs.length == 0){
			return new ICExtensionReference[0];
		}
		
		ICExtensionReference[] extRefs = new ICExtensionReference[cfgRefs.length];
		ArrayList list = (ArrayList)fDesMap.get(extensionPoint);
//		if(list == null){
//			list = new ArrayList(cfgRefs.length);
//			fDesMap.put(extensionPoint, list);
//		}

//		list = (ArrayList)list.clone();
//
//		CConfigBaseDescriptorExtensionReference[] refs = (CConfigBaseDescriptorExtensionReference[])list.
//			toArray(new CConfigBaseDescriptorExtensionReference[list.size()]);
		int num = cfgRefs.length - 1;
			
		for(int i = cfgRefs.length - 1; i >= 0; i--){
			ICConfigExtensionReference ref = cfgRefs[i];
			int k = list != null ? list.size() - 1 : -1;
			
			for(; k >= 0; k--){
				CConfigBaseDescriptorExtensionReference r = (CConfigBaseDescriptorExtensionReference)list.get(k);
				if(r.fCfgExtRef == ref){
					extRefs[num--] = r;
					list.remove(k);
					break;
				}
			}
			if(k < 0){
				extRefs[num--] = new CConfigBaseDescriptorExtensionReference(ref);
			}
		}
		
		if(list == null){
			list = new ArrayList(cfgRefs.length);
			fDesMap.put(extensionPoint, list);
		} else {
			list.clear();
			list.ensureCapacity(cfgRefs.length);
		}
		
		list.addAll(Arrays.asList(extRefs));
		list.trimToSize();
		return extRefs;
	}

	public ICExtensionReference[] get(String extensionPoint, boolean update)
			throws CoreException {
		ICExtensionReference[] refs = get(extensionPoint);
		if(refs.length == 0 && update){
			boolean prevApplyOnChange = fApplyOnChange;
			fApplyOnChange = false;
			fOwner.update(fProject, this, extensionPoint);
			fApplyOnChange = prevApplyOnChange;
			checkApply();
			refs = get(extensionPoint);
		}
		return get(extensionPoint);
	}

	public String getPlatform() {
		return fOwner.getPlatform();
	}

	public IProject getProject() {
		return fProject;
	}

	public Element getProjectData(String id) throws CoreException {
		synchronized(CProjectDescriptionManager.getInstance()){
			Element el = (Element)fStorageDataElMap.get(id);
			if(el == null || el.getParentNode() == null){
				InternalXmlStorageElement storageEl = (InternalXmlStorageElement)fCfgDes.getStorage(id, false);
				if(storageEl == null){
					try {
						DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
						Document doc = builder.newDocument();
						el = CStorage.createStorageXmlElement(doc, id);
						doc.appendChild(el);
					} catch (ParserConfigurationException e) {
						throw ExceptionFactory.createCoreException(e);
					}
				} else {
					el = CProjectDescriptionManager.getInstance().createXmlElementCopy(storageEl);
				}
				fStorageDataElMap.put(id, el);
			}
			return el;
		}
	}

	public ICOwnerInfo getProjectOwner() {
		return fOwner;
	}

	public void remove(ICExtensionReference extension) throws CoreException {
		ICConfigExtensionReference ref =((CConfigBaseDescriptorExtensionReference)extension).fCfgExtRef;
		fCfgDes.remove(ref);

		//write is done for all configurations to avoid "data loss" on configuration change
		ICProjectDescription des = fCfgDes.getProjectDescription();
		ICConfigurationDescription cfgs[] = des.getConfigurations();
		for(int i = 0; i < cfgs.length; i++){
			ICConfigurationDescription cfg = cfgs[i];
			if(cfg != fCfgDes){
				try {
					ICConfigExtensionReference rs[] = cfg.get(ref.getExtensionPoint());
					for(int k = 0; k < rs.length; k++){
						if(ref.getID().equals(rs[i].getID())){
							cfg.remove(rs[i]);
							break;
						}
					}
				} catch (CoreException e) {
				}
			}
		}
		fIsDirty = true;
		checkApply();
	}

	public void remove(String extensionPoint) throws CoreException {
		fCfgDes.remove(extensionPoint);
		
		//write is done for all configurations to avoid "data loss" on configuration change
		ICProjectDescription des = fCfgDes.getProjectDescription();
		ICConfigurationDescription cfgs[] = des.getConfigurations();
		for(int i = 0; i < cfgs.length; i++){
			ICConfigurationDescription cfg = cfgs[i];
			if(cfg != fCfgDes){
				try {
					cfg.remove(extensionPoint);
				} catch (CoreException e) {
				}
			}
		}
		fIsDirty = true;
		checkApply();
	}

	public void saveProjectData() throws CoreException {
		if(CProjectDescriptionManager.getInstance().getDescriptorManager().reconsile(this, fCfgDes.getProjectDescription()))
			fIsDirty = true;
		
		checkApply();
	}
	
	public Map getStorageDataElMap(){
		return (HashMap)fStorageDataElMap.clone(); 
	}
	
	public ICConfigurationDescription getConfigurationDescription() {
		return fCfgDes;
	}
}

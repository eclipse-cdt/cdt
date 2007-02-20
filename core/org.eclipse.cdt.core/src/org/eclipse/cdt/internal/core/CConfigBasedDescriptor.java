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
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICExtension;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.core.ICOwnerInfo;
import org.eclipse.cdt.core.settings.model.ICConfigExtensionReference;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.util.CExtensionUtil;
import org.eclipse.cdt.internal.core.settings.model.CConfigurationDescription;
import org.eclipse.cdt.internal.core.settings.model.CConfigurationSpecSettings;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescriptionManager;
import org.eclipse.cdt.internal.core.settings.model.IInternalCCfgInfo;
import org.eclipse.cdt.internal.core.settings.model.InternalXmlStorageElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
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
			fCfgExtRef.setExtensionData(key, value);
			checkApply();
		}
	}
	
	public CConfigBasedDescriptor(ICConfigurationDescription des) throws CoreException{
		updateConfiguration(des);
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
			CProjectDescriptionManager.getInstance().setProjectDescription(fProject, fCfgDes.getProjectDescription());
			fIsDirty = false;
		}
	}
	
	private void checkApply() throws CoreException {
		if(fApplyOnChange){
			CProjectDescriptionManager.getInstance().setProjectDescription(fProject, fCfgDes.getProjectDescription());
			fIsDirty = false;
		} else {
			fIsDirty = true;
		}
	}
	
	public ICExtensionReference create(String extensionPoint, String id)
			throws CoreException {
		ICConfigExtensionReference ref = fCfgDes.create(extensionPoint, id);
		ICExtensionReference r = create(ref);
		checkApply();
		return r;
	}
	
	public void updateConfiguration(ICConfigurationDescription des) throws CoreException{
		fCfgDes = des;
		fProject = fCfgDes.getProjectDescription().getProject();
		fOwner = ((IInternalCCfgInfo)fCfgDes).getSpecSettings().getCOwner();
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
		if(update)
			fOwner.update(fProject, this, extensionPoint);
		return get(extensionPoint);
	}

	public String getPlatform() {
		return fOwner.getPlatform();
	}

	public IProject getProject() {
		return fProject;
	}

	public Element getProjectData(String id) throws CoreException {
		Element el = (Element)fStorageDataElMap.get(id);
		if(el == null){
			InternalXmlStorageElement storageEl = (InternalXmlStorageElement)fCfgDes.getStorage(id, true);
			el = CProjectDescriptionManager.getInstance().createXmlElementCopy(storageEl);
			fStorageDataElMap.put(id, el);
		}
		return el;
	}

	public ICOwnerInfo getProjectOwner() {
		return fOwner;
	}

	public void remove(ICExtensionReference extension) throws CoreException {
		ICConfigExtensionReference ref =((CConfigBaseDescriptorExtensionReference)extension).fCfgExtRef;
		fCfgDes.remove(ref);
		checkApply();
	}

	public void remove(String extensionPoint) throws CoreException {
		fCfgDes.remove(extensionPoint);
		checkApply();
	}

	public void saveProjectData() throws CoreException {
		CConfigurationSpecSettings specSettings = ((IInternalCCfgInfo)fCfgDes).getSpecSettings(); 
		for(Iterator iter = fStorageDataElMap.entrySet().iterator(); iter.hasNext();){
			Map.Entry entry = (Map.Entry)iter.next();
			String id = (String)entry.getKey();
			Element el = (Element)entry.getValue();
			InternalXmlStorageElement storEl = new InternalXmlStorageElement(el, false);
			specSettings.importStorage(id, storEl);
			iter.remove();
		}
		checkApply();
	}

	public ICConfigurationDescription getConfigurationDescription() {
		return fCfgDes;
	}

}

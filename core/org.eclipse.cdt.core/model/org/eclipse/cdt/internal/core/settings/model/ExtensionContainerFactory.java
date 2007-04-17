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
package org.eclipse.cdt.internal.core.settings.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.CExternalSetting;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.extension.CExternalSettingProvider;
import org.eclipse.cdt.internal.core.settings.model.CExternalSettingsManager.CContainerRef;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

public class ExtensionContainerFactory extends CExternalSettingContainerFactory {
	static final String FACTORY_ID = CCorePlugin.PLUGIN_ID + ".extension.container.factory";
	private static final String EXTENSION_ID = CCorePlugin.PLUGIN_ID + ".externalSettingsProvider";
	
	private static ExtensionContainerFactory fInstance;
	private Map fDescriptorMap;

	private static class NullProvider extends CExternalSettingProvider {
		private static final NullProvider INSTANCE = new NullProvider();
		
		public CExternalSetting[] getSettings(IProject project, ICConfigurationDescription cfg) {
			return new CExternalSetting[0];
		}
		
	}
	
	private static class CESContainer extends CExternalSettingsContainer {
		private CExternalSetting[] fSettings;

		CESContainer(CExternalSetting[] settings){
			fSettings = (CExternalSetting[])settings.clone();
		}
		
		public CExternalSetting[] getExternalSettings() {
			return (CExternalSetting[])fSettings.clone();
		}
		
	}
	
	private static class CExtensionSettingProviderDescriptor {
		private static final String PROVIDER = "provider";
		private static final String CLASS = "class";
		
		private IExtension fExtension;
		private IConfigurationElement fProviderElement;
		private String fId;
		private String fName;
		private CExternalSettingProvider fProvider;
		
		CExtensionSettingProviderDescriptor(IExtension extension){
			fId = extension.getUniqueIdentifier();
			fName = extension.getLabel();
			fExtension = extension;
		}
		
		public String getId(){
			return fId;
		}

		public String getName(){
			return fName;
		}

		private CExternalSettingProvider getProvider(){
			if(fProvider == null){
				try {
					fProvider = createProvider();
				} catch (CoreException e) {
					CCorePlugin.log(e);
				}
				if(fProvider == null){
					fProvider = NullProvider.INSTANCE;
				}
			}
			return fProvider;
		}
		
		CExternalSettingsContainer getContainer(IProject project, ICConfigurationDescription cfg){
			return new CESContainer(getProvider().getSettings(project, cfg));
		}

		CExternalSettingProvider createProvider() throws CoreException{
			IConfigurationElement el = getProviderElement();
			if(el != null){
				Object obj = el.createExecutableExtension(CLASS);
				if(obj instanceof CExternalSettingProvider){
					return (CExternalSettingProvider)obj;
				} else
					throw ExceptionFactory.createCoreException(SettingsModelMessages.getString("invalid setting provider class specified"));
			}
			throw ExceptionFactory.createCoreException(SettingsModelMessages.getString("provider element not specified"));
		}
		
		private IConfigurationElement getProviderElement(){
			if(fProviderElement == null)
				fProviderElement = getProviderElement(fExtension);
			return fProviderElement;
		}
		
		private static IConfigurationElement getProviderElement(IExtension ext){
			IConfigurationElement els[] = ext.getConfigurationElements();
			for(int i = 0; i < els.length; i++){
				IConfigurationElement el = els[i];
				String name = el.getName();
				if(PROVIDER.equals(name))
					return el;
			}					
			return null;
		}
	}
	
	private Map getProviderDescriptorMap(){
		if(fDescriptorMap == null){
			initProviderInfoSynch();
		}
		return fDescriptorMap;
	}
	
	private synchronized void initProviderInfoSynch(){
		if(fDescriptorMap != null)
			return;
		
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_ID);
		IExtension exts[] = extensionPoint.getExtensions();
		fDescriptorMap = new HashMap();
		
		for(int i = 0; i < exts.length; i++){
			CExtensionSettingProviderDescriptor dr = new CExtensionSettingProviderDescriptor(exts[i]);
			fDescriptorMap.put(dr.getId(), dr);
		}
	}

	private ExtensionContainerFactory(){
	}
	
	public static ExtensionContainerFactory getInstance(){
		if(fInstance == null){
			fInstance = new ExtensionContainerFactory();
		}
		return fInstance;
	}

	public CExternalSettingsContainer createContainer(String id,
			IProject project, ICConfigurationDescription cfgDes) throws CoreException {
		CExtensionSettingProviderDescriptor dr = (CExtensionSettingProviderDescriptor)getProviderDescriptorMap().get(id);
		if(dr != null)
			return dr.getContainer(project, cfgDes);
		return CExternalSettingsManager.NullContainer.INSTANCE;
	}
	
	public static String[] getReferencedProviderIds(ICConfigurationDescription cfg){
		CContainerRef[] refs = CExternalSettingsManager.getInstance().getReferences(cfg, FACTORY_ID);
		String[] ids = new String[refs.length];
		for(int i = 0; i < refs.length; i++){
			ids[i] = refs[i].getContainerId();
		}
		return ids;
	}
	
	public static void setReferencedProviderIds(ICConfigurationDescription cfg, String ids[]){
		Set newIdsSet = new HashSet(Arrays.asList(ids));
		Set oldIdsSet = new HashSet(Arrays.asList(getReferencedProviderIds(cfg)));
		Set newIdsSetCopy = new HashSet(newIdsSet);
		newIdsSet.removeAll(oldIdsSet);
		oldIdsSet.removeAll(newIdsSetCopy);
		
		if(oldIdsSet.size() != 0){
			for(Iterator iter = oldIdsSet.iterator(); iter.hasNext();){
				removeReference(cfg, (String)iter.next());
			}
		}

		if(newIdsSet.size() != 0){
			for(Iterator iter = newIdsSet.iterator(); iter.hasNext();){
				createReference(cfg, (String)iter.next());
			}
		}
	}
	
	public static void updateReferencedProviderIds(ICConfigurationDescription cfg, String ids[]){
		Set newIdsSet = new HashSet(Arrays.asList(ids));
		Set oldIdsSet = new HashSet(Arrays.asList(getReferencedProviderIds(cfg)));
		Set newIdsSetCopy = new HashSet(newIdsSet);
		newIdsSetCopy.removeAll(oldIdsSet);
		newIdsSet.removeAll(newIdsSetCopy);
		
		if(newIdsSet.size() != 0){
			for(Iterator iter = newIdsSet.iterator(); iter.hasNext();){
				providerChanged(cfg, (String)iter.next());
			}
		}
	}
	
	private static void createReference(ICConfigurationDescription cfg, String id){
		CContainerRef cr = createContainerRef(id);
		CExternalSettingsManager.getInstance().addContainer(cfg, cr);
	}
	
	private static void providerChanged(ICConfigurationDescription cfg, String id){
		CContainerRef cr = createContainerRef(id);
		CExternalSettingsManager.getInstance().containerContentsChanged(cfg, cr);
		
	}

	private static void removeReference(ICConfigurationDescription cfg, String id){
		CContainerRef cr = createContainerRef(id);
		CExternalSettingsManager.getInstance().removeContainer(cfg, cr);
	}

	private static CContainerRef createContainerRef(String id){
		return new CContainerRef(FACTORY_ID, id);
	}
}

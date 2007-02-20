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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ILanguageDescriptor;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFileDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.ICSettingObject;
import org.eclipse.cdt.core.settings.model.ICSettingsStorage;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.ICTargetPlatformSetting;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationDataProvider;
import org.eclipse.cdt.core.settings.model.extension.CFolderData;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.cdt.core.settings.model.extension.CResourceData;
import org.eclipse.cdt.core.settings.model.extension.ICProjectConverter;
import org.eclipse.cdt.core.settings.model.extension.impl.CDataFacroty;
import org.eclipse.cdt.core.settings.model.extension.impl.CDefaultConfigurationData;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.settings.model.util.KindBasedStore;
import org.eclipse.cdt.core.settings.model.util.ListComparator;
import org.eclipse.cdt.internal.core.CConfigBasedDescriptorManager;
import org.eclipse.cdt.internal.core.envvar.ContributedEnvironment;
import org.eclipse.cdt.internal.core.model.CElementDelta;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.framework.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.SAXException;

public class CProjectDescriptionManager {
	private static final String ACTIVE_CFG = "activeConfiguration";
	private static final QualifiedName ACTIVE_CFG_PROPERTY = new QualifiedName(CCorePlugin.PLUGIN_ID, ACTIVE_CFG);
	
	private static final String OLD_PROJECT_DESCRIPTION = "cdtproject"; //$NON-NLS-1$
	private static final String OLD_CDTPROJECT_FILE_NAME = ".cdtproject";	//$NON-NLS-1$
	private static final String OLD_PROJECT_OWNER_ID = "id"; //$NON-NLS-1$
	private static final String CONVERTED_CFG_NAME = "convertedConfig"; //$NON-NLS-1$
	private static final String CONVERTED_CFG_ID_PREFIX = "converted.config"; //$NON-NLS-1$

	private static final String STORAGE_FILE_NAME = ".cproject";	//$NON-NLS-1$
	private static final QualifiedName PROJECT_DESCRCIPTION_PROPERTY = new QualifiedName(CCorePlugin.PLUGIN_ID, "projectDescription");	//$NON-NLS-1$
	private static final String ROOT_ELEMENT_NAME = "cproject";	//$NON-NLS-1$
	private static final String VERSION_ELEMENT_NAME = "fileVersion";	//$NON-NLS-1$
	public static final Version DESCRIPTION_VERSION = new Version("4.0"); 	//$NON-NLS-1$
	final static String MODULE_ID = "org.eclipse.cdt.core.settings";	//$NON-NLS-1$
	static final String CONFIGURATION = "cconfiguration";	//$NON-NLS-1$
	private static final ICLanguageSettingEntry[] EMPTY_LANGUAGE_SETTINGS_ENTRIES_ARRAY = new ICLanguageSettingEntry[0];
	private static final ICElementDelta[] EMPTY_CELEMENT_DELTA = new ICElementDelta[0];
	private static final ICLanguageSetting[] EMPTY_LANGUAGE_SETTINGS_ARRAY = new ICLanguageSetting[0];
	private static final String PREFERENCES_STORAGE = "preferences";	//$NON-NLS-1$
	private static final String PREFERENCE_BUILD_SYSTEM_ELEMENT = "buildSystem";	//$NON-NLS-1$
	private static final String ID = "id";	//$NON-NLS-1$
	private static final String PREFERENCE_CFG_ID_PREFIX = "preference.";	//$NON-NLS-1$
	private static final String PREFERENCE_CFG_NAME = "Preference Configuration";	//$NON-NLS-1$
	private static final String ROOT_PREFERENCE_ELEMENT = "preferences";	//$NON-NLS-1$
	public static final String DEFAULT_PROVIDER_ID = CCorePlugin.PLUGIN_ID + ".defaultConfigDataProvider"; //$NON-NLS-1$
	private static final String DEFAULT_CFG_ID_PREFIX = CCorePlugin.PLUGIN_ID + ".default.config"; //$NON-NLS-1$
	private static final String DEFAULT_CFG_NAME = "Configuration"; //$NON-NLS-1$

	private class CompositeSafeRunnable implements ISafeRunnable {
		private List fRunnables = new ArrayList();
		private boolean fStopOnErr;
		
		public void add(ISafeRunnable runnable){
			fRunnables.add(runnable);
		}

		public void handleException(Throwable exception) {
		}

		public void run() throws Exception {
			for(Iterator iter = fRunnables.iterator(); iter.hasNext();){
				ISafeRunnable r = (ISafeRunnable)iter.next();
				try {
					r.run();
				} catch (Exception e){
					r.handleException(e);
					if(fStopOnErr)
						throw e;
				}
			}
		}
	}
	
	private class DesSerializationRunnable implements ISafeRunnable {
		private ICProjectDescription fDes;
		private ICStorageElement fElement;
		
		public DesSerializationRunnable(ICProjectDescription des, ICStorageElement el) {
			fDes = des;
			fElement = el;
		}

		public void handleException(Throwable exception) {
		}

		public void run() throws Exception {
			serialize(fDes.getProject(), STORAGE_FILE_NAME, fElement);
			((ContributedEnvironment)CCorePlugin.getDefault().getBuildEnvironmentManager().getContributedEnvironment()).serialize(fDes);
		}
		
	}
	private class ProjectInfoHolder{
		ICProjectDescription fDescription;
		ScannerInfoProviderProxy fSIProvider;
	}
	
	private class ListenerDescriptor{
		ICProjectDescriptionListener fListener;
		int fEventTypes;
		
		public ListenerDescriptor(ICProjectDescriptionListener listener, int eventTypes) {
			fListener = listener;
			fEventTypes = eventTypes;
		}
		
		public boolean handlesEvent(int eventType){
			return (eventType & fEventTypes) != 0;
		}
	}
	
	private Map fProviderMap;
	private CProjectConverterDesciptor fConverters[];
	private List fListeners = new ArrayList();
	private Map fPreferenceMap = new HashMap();
	private CConfigBasedDescriptorManager fDescriptorManager;
	private ThreadLocal fLoaddingDescriptions = new ThreadLocal();

//	private CStorage fPrefCfgStorage;
	private ICDataProxyContainer fPrefUpdater = new ICDataProxyContainer(){


		public void updateChild(CDataProxy child, boolean write) {
			if(write){
				try {
					((CConfigurationDescription)child).doWritable();
				} catch (CoreException e) {
					CCorePlugin.log(e);
				}
			}
		}

		public ICSettingObject[] getChildSettings() {
			return (ICSettingObject[])fPreferenceMap.values().toArray(new CConfigurationDescriptionCache[fPreferenceMap.size()]);
		}
	};
	
//	Map fDescriptionMap = new HashMap();
	private static CProjectDescriptionManager fInstance;

	private CProjectDescriptionManager(){
		
	}
	
	public static CProjectDescriptionManager getInstance(){
		if(fInstance == null)
			fInstance = new CProjectDescriptionManager();
		return fInstance;
	}

	public void startup(){
		if(fDescriptorManager == null){
			fDescriptorManager = CConfigBasedDescriptorManager.getInstance();
			fDescriptorManager.startup();
		}
	}

	public void shutdown(){
		if(fDescriptorManager != null){
			fDescriptorManager.shutdown();
		}
	}
	
	private ICProjectDescription getDescriptionLoadding(IProject project){
		Map map = getDescriptionLoaddingMap(false);
		if(map != null)
			return (ICProjectDescription)map.get(project);
		return null;
	}
	
	private void setDescriptionLoadding(IProject project, ICProjectDescription des){
		Map map = getDescriptionLoaddingMap(true);
		map.put(project, des);
	}
	
	private void clearDescriptionLoadding(IProject project){
		Map map = getDescriptionLoaddingMap(false);
		if(map != null)
			map.remove(project);
	}
	
	private Map getDescriptionLoaddingMap(boolean create){
		Map map = (Map)fLoaddingDescriptions.get();
		if(map == null && create){
			map = new HashMap();
			fLoaddingDescriptions.set(map);
		}
		return map;
	}

	public ICProjectDescription getProjectDescription(IProject project, boolean write){
		ICProjectDescription des = null;
		des = getLoaddedDescription(project);
		IProjectDescription eDes = null; 

		if(des == null)
			des = getDescriptionLoadding(project);
		
		if(des == null){
			try {
				des = loadProjectDescription(project);
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
			if(des == null){
				//TODO: check if conversion needed
				try {
					eDes = project.getDescription();
					des = getConvertedDescription(project, eDes);
				} catch (CoreException e) {
				}
			}
	
			if(des != null){
				if(setLoaddedDescription(project, des, false)){

					if(eDes != null)
						saveConversion(project, eDes, (CProjectDescription)des, new NullProgressMonitor());
	
					CProjectDescriptionEvent event = createLoaddedEvent(des);
					notifyListeners(event);
					
					des = getLoaddedDescription(project);
					
					ICProjectDescription updatedDes = ExternalSettingsManager.getInstance().updateReferencedSettings(des);
					if(updatedDes != des){
						try {
							setProjectDescription(project, updatedDes);
						} catch (CoreException e) {
						}
					}
				}
			}
		}
		
		if(des != null && write){
			CProjectDescription cache = (CProjectDescription)des;
			ICStorageElement el = null;
			try {
				el = cache.getRootStorageElement();
				el = copyElement((InternalXmlStorageElement)el, false);
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
			
			des = new CProjectDescription((CProjectDescription)des, false, el);
			CProjectDescriptionEvent event = createCopyCreatedEvent(des, cache);
			notifyListeners(event);
		}
		return des;
	}
	
	private CProjectDescriptionEvent createLoaddedEvent(ICProjectDescription des){
		return new CProjectDescriptionEvent(CProjectDescriptionEvent.LOADDED,
				null,
				des,
				null,
				null);
	}

	private CProjectDescriptionEvent createCopyCreatedEvent(ICProjectDescription newDes, ICProjectDescription oldDes){
		return new CProjectDescriptionEvent(CProjectDescriptionEvent.COPY_CREATED,
				null,
				newDes,
				oldDes,
				null);
	}

	CProjectDescriptionEvent createAboutToApplyEvent(ICProjectDescription newDes, ICProjectDescription oldDes){
		return new CProjectDescriptionEvent(CProjectDescriptionEvent.ABOUT_TO_APPLY,
				null,
				newDes,
				oldDes,
				null);
	}

	CProjectDescriptionEvent createAppliedEvent(ICProjectDescription newDes, ICProjectDescription oldDes, ICProjectDescription appliedDes, ICDescriptionDelta delta){
		return new CProjectDescriptionEvent(CProjectDescriptionEvent.APPLIED,
				delta,
				newDes,
				oldDes,
				appliedDes);
	}
	
	private Object[] loadProjectDescriptionFromOldstyleStorage(IProject project) throws CoreException {
		ICStorageElement rootEl = readOldCDTProjectFile(project);
		if(rootEl != null){
			String ownerId = rootEl.getAttribute(OLD_PROJECT_OWNER_ID);
			CProjectDescription des = (CProjectDescription)createProjectDescription(project, false);
			String id = CDataUtil.genId(CONVERTED_CFG_ID_PREFIX);
			des.createConvertedConfiguration(id, CONVERTED_CFG_NAME, rootEl);
			return new Object[]{ownerId, des};
		}
		return null;
	}

	private ICProjectDescription getConvertedDescription(IProject project, IProjectDescription eDes) throws CoreException{
		Object info[] = loadProjectDescriptionFromOldstyleStorage(project);
		CProjectDescription des;
		String ownerId;
		try {
			if(info != null){
				ownerId = (String)info[0];
				des = (CProjectDescription)info[1];
				setDescriptionLoadding(project, des);
			} else {
				ownerId = null;
				des = null;
			}
			
			ICProjectConverter converter = getConverter(project, ownerId, des);
			if(converter != null){
				CProjectDescription convertedDes = (CProjectDescription)converter.convertProject(project, eDes, ownerId, des);
				if(convertedDes != null){
					ICConfigurationDescription activeCfg = convertedDes.getActiveConfiguration();
					if(activeCfg != null){
						checkBuildSystemChange(project, eDes, activeCfg.getBuildSystemId(), null, new NullProgressMonitor());
			//			if(convertedDes != null)
							des = convertedDes;
					} 
				}
			} else {
//				des;
			}
			
			if(des != null && des.isValid()){
				//TODO: should be set via the CModel operation?
				InternalXmlStorageElement el = null;
				try {
					el = copyElement((InternalXmlStorageElement)des.getRootStorageElement(), false);
				} catch (CoreException e2) {
				}
	
				des = new CProjectDescription(des, true, el);
				
				try {
					((InternalXmlStorageElement)des.getRootStorageElement()).setReadOnly(true);
				} catch (CoreException e1) {
				}
			}
		}finally{
			clearDescriptionLoadding(project);
		}
		return des;
	}
	
	private void saveConversion(final IProject proj, 
			final IProjectDescription eDes,
			CProjectDescription des,
			final IProgressMonitor monitor) {
		
		CompositeSafeRunnable r = new CompositeSafeRunnable();
		r.add(new ISafeRunnable(){

			public void handleException(Throwable exception) {
			}

			public void run() throws Exception {
				proj.setDescription(eDes, monitor);
			}
		});
		
		try {
			r.add(createDesSerializationRunnable(des));
		} catch (CoreException e1) {
			CCorePlugin.log(e1);
		}
		
		runWspModification(r, monitor);
	}
	
	private void runWspModification(final ISafeRunnable runnable, IProgressMonitor monitor){
		IWorkspace wsp = ResourcesPlugin.getWorkspace();
		boolean scheduleRule = true;
		if(!wsp.isTreeLocked()) {
			ISchedulingRule rule = wsp.getRoot();
			IJobManager mngr = Job.getJobManager();
			try{
				mngr.beginRule(rule, monitor);
				scheduleRule = false;
				try {
					runnable.run();
				} catch (Exception e){
					runnable.handleException(e);
				}
			} catch (Exception e) {
			} finally {
				mngr.endRule(rule);
			}
		}
		
		if(scheduleRule){
			Job job = new Job("info serialization"){
				protected IStatus run(IProgressMonitor monitor) {
					try {
						runnable.run();
					} catch (Exception e) {
						runnable.handleException(e);
					}
					return Status.OK_STATUS;
				}
			};
			
			job.setRule(wsp.getRoot());
			job.setSystem(true);
			job.schedule();
		}
	}
	
	private ICProjectConverter getConverter(IProject project, String oldOwnerId, ICProjectDescription des){
		CProjectConverterDesciptor[] converterDess = getConverterDescriptors();
		ICProjectConverter converter = null;
		for(int i = 0; i < converterDess.length; i++){
			if(converterDess[i].canConvertProject(project, oldOwnerId, des)){
				try {
					converter = converterDess[i].getConverter();
				} catch (CoreException e) {
				}
				if(converter != null)
					break;
			}
		}
		return converter;
	}
	
	private CProjectConverterDesciptor[] getConverterDescriptors(){
		if(fConverters == null){
			initConverterInfoSynch();
		}
		return fConverters;
	}
	
	private synchronized void initConverterInfoSynch(){
		if(fConverters != null)
			return;
		
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(CProjectConverterDesciptor.PROJECT_CONVERTER_EXTPOINT_ID);
		IExtension exts[] = extensionPoint.getExtensions();
		fConverters = new CProjectConverterDesciptor[exts.length];
		
		for(int i = 0; i < exts.length; i++){
			fConverters[i] = new CProjectConverterDesciptor(exts[i]);
		}
	}

	
	public ICProjectDescription createProjectDescription(IProject project, boolean loadIfExists) throws CoreException{
		ICProjectDescription des = loadIfExists ? getProjectDescription(project) : null;
		
		if(des == null){
			ICStorageElement element = createStorage(project, false, true, false);
			des = new CProjectDescription(project, element, false);
		}
		return des;
	}
	
	private ICProjectDescription getLoaddedDescription(IProject project){
		try {
			ProjectInfoHolder holder = getInfoHolder(project);
			if(holder != null)
				return holder.fDescription;
		} catch (CoreException e) {
		}
		return null;
	}
	
	private ProjectInfoHolder getInfoHolder(final IProject project) throws CoreException{
		return (ProjectInfoHolder)project.getSessionProperty(PROJECT_DESCRCIPTION_PROPERTY);
	}
	
	public synchronized ScannerInfoProviderProxy getScannerInfoProviderProxy(IProject project){
		ProjectInfoHolder holder = null;
		try {
			holder = getInfoHolder(project);
		} catch (CoreException e) {
			holder = new ProjectInfoHolder();
		}
		boolean needSet = false;
		if(holder == null){
			holder = new ProjectInfoHolder();
			needSet = true;
		}
		
		ScannerInfoProviderProxy provider = holder.fSIProvider;
		if(provider == null){
			provider = new ScannerInfoProviderProxy(project);
			holder.fSIProvider = provider;
		}
		
		if(needSet)
			setInfoHolder(project, holder);
		
		return provider;
	}
	
	private void setInfoHolder(final IProject project, final ProjectInfoHolder holder){
		try {
			project.setSessionProperty(PROJECT_DESCRCIPTION_PROPERTY, holder);
		} catch (CoreException e){
			//TODO: externalize
			final ProjectInfoHolder f = holder; 
			Job setDesJob = new Job("Set loadded description job"){ 	//$NON-NLS-1$
				protected IStatus run(IProgressMonitor monitor){
					try {
						project.setSessionProperty(PROJECT_DESCRCIPTION_PROPERTY, f);
					} catch (CoreException e) {
						return e.getStatus();
					}
					return Status.OK_STATUS;
				}
			};
			
			setDesJob.setRule(ResourcesPlugin.getWorkspace().getRoot());
			setDesJob.setPriority(Job.INTERACTIVE);
			setDesJob.setSystem(true);
			setDesJob.schedule();
		}
	}

	synchronized boolean setLoaddedDescription(final IProject project, ICProjectDescription des, boolean overwriteIfExists){
		try {
			ProjectInfoHolder holder = getInfoHolder(project);
			boolean needSet = false;
			if(holder == null){
				holder = new ProjectInfoHolder();
				needSet = true;
			}

			boolean shouldOverwrite = overwriteIfExists || holder.fDescription == null;
			if(shouldOverwrite){
				holder.fDescription = des;
				if(needSet){
					setInfoHolder(project, holder);
				}
			}
			return shouldOverwrite;
		} catch (CoreException e) {
		}
		return false;
	}

	private ICProjectDescription loadProjectDescription(IProject project) throws CoreException{
		ICStorageElement storage = CProjectDescriptionManager.getInstance().createStorage(project, true, false, true);
		CProjectDescription des = new CProjectDescription(project, storage, true);
		if(des != null){
			try {
				setDescriptionLoadding(project, des);
				des.loadDatas();
			}finally{
				clearDescriptionLoadding(project);
			}
		}
		return des;
	}

	public ICProjectDescription getProjectDescription(IProject project){
		return getProjectDescription(project, true);
	}
	
	private void storeActiveCfgId(ICProjectDescription des, String id){
		try {
			des.getProject().setPersistentProperty(ACTIVE_CFG_PROPERTY, id);
		} catch (CoreException e) {
			// Hitting this error just means the default config is not set
		}
	}

	void checkActiveCfgChange(CProjectDescription newDes, CProjectDescription oldDes, IProgressMonitor monitor){
		if(newDes == null)
			return;
		ICConfigurationDescription newCfg = newDes.getActiveConfiguration();
		if(newCfg == null)
			return;
		
		
		ICConfigurationDescription oldCfg = oldDes != null ? oldDes.getActiveConfiguration() : null;

		String newId = newCfg.getId();
		String oldId = oldCfg != null ? oldCfg.getId() : null;
		
		if(newDes.needsActiveCfgIdPersistence() || !newId.equals(oldId)){
			storeActiveCfgId(newDes, newId);
		}
		
		String newBsId = newCfg.getBuildSystemId();
		String oldBsId = oldCfg != null ? oldCfg.getBuildSystemId() : null;
		
		try {
			checkBuildSystemChange(newDes.getProject(), newBsId, oldBsId, monitor);
		} catch (CoreException e) {
		}
	}
	
	String loadActiveCfgId(ICProjectDescription des){
		try {
			return des.getProject().getPersistentProperty(ACTIVE_CFG_PROPERTY);
		} catch (CoreException e) {
			// Hitting this error just means the default config is not set
		}
		return null;
	}


	private void checkBuildSystemChange(IProject project, String newBsId, String oldBsId, IProgressMonitor monitor) throws CoreException{
		checkBuildSystemChange(project, null, newBsId, oldBsId, monitor);
	}

	private void checkBuildSystemChange(IProject project, IProjectDescription des, String newBsId, String oldBsId, IProgressMonitor monitor) throws CoreException{
		CConfigurationDataProviderDescriptor newDr = newBsId != null ? getCfgProviderDescriptor(newBsId) : null;
		CConfigurationDataProviderDescriptor oldDr = oldBsId != null ? getCfgProviderDescriptor(oldBsId) : null;
		
		String newNatures[] = newDr != null ? newDr.getNatureIds() : null;
		String oldNatures[] = oldDr != null ? oldDr.getNatureIds() : null;
		List[] natureDiff = ListComparator.compare(newNatures, oldNatures);
		
//		String newBuilderIds[] = newDr.getBuilderIds();
//		String oldBuilderIds[] = oldDr.getBuilderIds();
//		List[] builderDiff = ListComparator.compare(newBuilderIds, oldBuilderIds);
		
		if(natureDiff != null /*|| builderDiff != null*/){
			boolean applyDes = false;
			if(des == null){
				des = project.getDescription();
				applyDes = true;
			}
			
			String natureIds[] = des.getNatureIds();
			if(natureDiff[1] != null){
				List remaining = ListComparator.getAdded(natureIds, natureDiff[1].toArray());
				if(remaining != null){
					natureIds = (String[])remaining.toArray(new String[remaining.size()]); 
				}
			}
			
			if(natureDiff[0] != null){
				Set set = new HashSet();
				set.addAll(Arrays.asList(natureIds));
				set.addAll(natureDiff[0]);
				natureIds = (String[])set.toArray(new String[set.size()]);
			}
//			if(builderDiff != null){
//	
//			}

			if(natureDiff != null)
				des.setNatureIds(natureIds);
			
			if(applyDes)
				project.setDescription(des, monitor);
		}
	}

	
	public void setProjectDescription(IProject project, ICProjectDescription des) throws CoreException {
		if(!des.isModified())
			return;

		if(((CProjectDescription)des).isLoadding())
			throw ExceptionFactory.createCoreException("description is being loadded");
		
		CModelManager manager = CModelManager.getDefault();
		ICProject cproject = manager.create(project);

		SetCProjectDescriptionOperation op = new SetCProjectDescriptionOperation(cproject, (CProjectDescription)des);
		op.runOperation(new NullProgressMonitor());
		
		
//		CProjectDescription newDes = new CProjectDescription((CProjectDescription)des, true);
		
/*TODO: calculate delta, etc.
 		ICProjectDescription previousDes = getProjecDescription(project, false);

		if(des != previousDes){
			ICConfigurationDescription cfgDess[] = des.getConfigurations();
		}
*/
//		ICProjectDescription oldDes = getProjectDescription(project, false);
//		setLoaddedDescription(newDes.getProject(), newDes);
		
//		ICProjectDescriptionDelta delta = createDelta(newDes, oldDes);
		//TODO: notify listeners
		
//		ICStorageElement element = newDes.getRootStorageElement();
		
//		serialize(newDes.getProject(), STORAGE_FILE_NAME, element);
		
//		serialize(newDes);
	}
	
	private ISafeRunnable createDesSerializationRunnable(CProjectDescription des) throws CoreException{
		final ICStorageElement element = des.getRootStorageElement();
		
		ISafeRunnable r = new DesSerializationRunnable(des, element);
		return r;
	}
	void serialize(final CProjectDescription des) throws CoreException{
		ISafeRunnable r = createDesSerializationRunnable(des);
		runWspModification(r, new NullProgressMonitor());
		
//		IWorkspace wsp = ResourcesPlugin.getWorkspace();
//		
//		if(wsp.isTreeLocked()){
//			Job job = new Job("info serialization"){
//				protected IStatus run(IProgressMonitor monitor) {
//					try {
//						serialize(des.getProject(), STORAGE_FILE_NAME, element);
//						((ContributedEnvironment)CCorePlugin.getDefault().getBuildEnvironmentManager().getContributedEnvironment()).serialize(des);
//					} catch (CoreException e) {
//						return e.getStatus();
//					}
//					return Status.OK_STATUS;
//				}
//			};
//			
//			job.setRule(wsp.getRoot());
//			job.setSystem(true);
//			job.schedule();
//		} else {
//			serialize(des.getProject(), STORAGE_FILE_NAME, element);
//			((ContributedEnvironment)CCorePlugin.getDefault().getBuildEnvironmentManager().getContributedEnvironment()).serialize(des);
//		}

//		serialize(des.getProject(), STORAGE_FILE_NAME, element);
	}
	
	private void serializePreference(String key, ICStorageElement element) throws CoreException{
		Document doc = getDocument(element);
		
		// Transform the document to something we can save in a file
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		FileOutputStream fileStream = null;
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");	//$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");	//$NON-NLS-1$
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(stream);
			transformer.transform(source, result);
			
			// Save the document
			File file = getPreferenceFile(key);
			String utfString = stream.toString("UTF-8");	//$NON-NLS-1$
	
			if (file.exists()) {
//				if (projectFile.isReadOnly()) {						
//	
//					// Inform Eclipse that we are intending to modify this file
//					// This will provide the user the opportunity, via UI prompts, to fetch the file from source code control
//					// reset a read-only file protection to write etc.
//					// If there is no shell, i.e. shell is null, then there will be no user UI interaction
//	
//					//TODO
//					//IStatus status = projectFile.getWorkspace().validateEdit(new IFile[]{projectFile}, shell);
//					
//					// If the file is still read-only, then we should not attempt the write, since it will
//					// just fail - just throw an exception, to be caught below, and inform the user
//					// For other non-successful status, we take our chances, attempt the write, and pass
//					// along any exception thrown
//					
//					//if (!status.isOK()) {
//					 //   if (status.getCode() == IResourceStatus.READ_ONLY_LOCAL) {
//					  //  	stream.close();
//	    	           //     throw new CoreException(status);						
//					    //}
//					//}
//				}
//				projectFile.setContents(new ByteArrayInputStream(utfString.getBytes("UTF-8")), IResource.FORCE, new NullProgressMonitor());	//$NON-NLS-1$
			} else {
				file.createNewFile();
			}
			fileStream = new FileOutputStream(file);
			fileStream.write(utfString.getBytes());
			fileStream.close();
			// Close the streams
			stream.close();
		} catch (TransformerConfigurationException e){
			throw ExceptionFactory.createCoreException(e);
		} catch (TransformerException e) {
			throw ExceptionFactory.createCoreException(e);
		} catch (IOException e) {
			throw ExceptionFactory.createCoreException(e);
		}
	}

	
	private void serialize(IProject project, String file, ICStorageElement element) throws CoreException{
		Document doc = getDocument(element);
		
		// Transform the document to something we can save in a file
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");	//$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");	//$NON-NLS-1$
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(stream);
			transformer.transform(source, result);
			
			// Save the document
			IFile projectFile = project.getFile(file);
			String utfString = stream.toString("UTF-8");	//$NON-NLS-1$
	
			if (projectFile.exists()) {
				if (projectFile.isReadOnly()) {						
	
					// Inform Eclipse that we are intending to modify this file
					// This will provide the user the opportunity, via UI prompts, to fetch the file from source code control
					// reset a read-only file protection to write etc.
					// If there is no shell, i.e. shell is null, then there will be no user UI interaction
	
					//TODO
					//IStatus status = projectFile.getWorkspace().validateEdit(new IFile[]{projectFile}, shell);
					
					// If the file is still read-only, then we should not attempt the write, since it will
					// just fail - just throw an exception, to be caught below, and inform the user
					// For other non-successful status, we take our chances, attempt the write, and pass
					// along any exception thrown
					
					//if (!status.isOK()) {
					 //   if (status.getCode() == IResourceStatus.READ_ONLY_LOCAL) {
					  //  	stream.close();
	    	           //     throw new CoreException(status);						
					    //}
					//}
				}
				projectFile.setContents(new ByteArrayInputStream(utfString.getBytes("UTF-8")), IResource.FORCE, new NullProgressMonitor());	//$NON-NLS-1$
			} else {
				projectFile.create(new ByteArrayInputStream(utfString.getBytes("UTF-8")), IResource.FORCE, new NullProgressMonitor());	//$NON-NLS-1$
			}
			// Close the streams
			stream.close();
		} catch (TransformerConfigurationException e){
			throw ExceptionFactory.createCoreException(e);
		} catch (TransformerException e) {
			throw ExceptionFactory.createCoreException(e);
		} catch (IOException e) {
			throw ExceptionFactory.createCoreException(e);
		}
	}
	
	private Document getDocument(ICStorageElement element){
		return ((InternalXmlStorageElement)element).fElement.getOwnerDocument();
	}

	Map createCfgStorages(ICProjectDescription des) throws CoreException{
		Map map = new HashMap();
		ICStorageElement rootElement = des.getStorage(MODULE_ID, false);
		if(rootElement != null){
			ICStorageElement children[] = rootElement.getChildren();
			
			for(int i = 0; i < children.length; i++){
				ICStorageElement el = children[i];
				if(CONFIGURATION.equals(el.getName())){
					String id = el.getAttribute(CConfigurationSpecSettings.ID);
					if(id != null)
						map.put(id, el);
				}
			}
		}
		return map;
	}
	
	ICLanguageSetting findLanguagSettingForFile(String fileName, IProject project, ICLanguageSetting settings[]){
	//	if(cType != null){
	//		setting = findLanguageSettingForContentTypeId(cType.getId(), settings, true);
	//		if(setting == null)
	//			setting = findLanguageSettingForContentTypeId(cType.getId(), settings, false);
	//	}
		ICLanguageSetting setting = null;
		int index = fileName.lastIndexOf('.');
		if(index > 0){
			String ext = fileName.substring(index + 1).trim();
			if(ext.length() > 0){
				setting = findLanguageSettingForExtension(ext, settings);
			}
		}
		return setting;
	}

	public CLanguageData findLanguagDataForFile(String fileName, IProject project, CLanguageData datas[]){
		//	if(cType != null){
		//		setting = findLanguageSettingForContentTypeId(cType.getId(), settings, true);
		//		if(setting == null)
		//			setting = findLanguageSettingForContentTypeId(cType.getId(), settings, false);
		//	}
			CLanguageData data = null;
			int index = fileName.lastIndexOf('.');
			if(index > 0){
				String ext = fileName.substring(index + 1).trim();
				if(ext.length() > 0){
					data = findLanguageDataForExtension(ext, datas);
				}
			}
			return data;
		}
	
	public CLanguageData findLanguageDataForExtension(String ext, CLanguageData datas[]/*, boolean src*/){
		CLanguageData data;
		for(int i = 0; i < datas.length; i++){
			data = datas[i]; 
			String exts[] = data.getSourceExtensions();
/*			if(src){
				if(setting.getSourceContentType() == null){
					exts = setting.getSourceExtensions();
				}
			} else {
				if(setting.getHeaderContentType() == null){
					exts = setting.getHeaderExtensions();
				}
			}
*/			
			if(exts != null && exts.length != 0){
				for(int j = 0; j < exts.length; j++){
					if(ext.equals(exts[j]))
						return data;
				}
			}
		}
		return null;
	}

	
	public ICLanguageSetting findLanguageSettingForContentTypeId(String id, ICLanguageSetting settings[]/*, boolean src*/){
		for(int i = 0; i < settings.length; i++){
			String ids[] = settings[i].getSourceContentTypeIds();
			if(ListComparator.indexOf(id, ids) != -1)
				return settings[i];
		}
		return null;
	}
	
	public ICLanguageSetting[] findCompatibleSettingsForContentTypeId(String id, ICLanguageSetting[] settings/*, boolean src*/){
		IContentTypeManager manager = Platform.getContentTypeManager();
		IContentType cType = manager.getContentType(id);
		if(cType != null){
			String [] exts = cType.getFileSpecs(IContentType.FILE_EXTENSION_SPEC);
			if(exts != null && exts.length != 0){
				List list = new ArrayList();
				ICLanguageSetting setting;
				for(int i = 0; i < exts.length; i++){
					setting = findLanguageSettingForExtension(exts[i], settings/*, src*/);
					if(setting != null)
						list.add(setting);
				}
				return (ICLanguageSetting[])list.toArray(new ICLanguageSetting[list.size()]);
			}
		}
		return EMPTY_LANGUAGE_SETTINGS_ARRAY;
	}

	public ICLanguageSetting findLanguageSettingForExtension(String ext, ICLanguageSetting settings[]/*, boolean src*/){
		ICLanguageSetting setting;
		for(int i = 0; i < settings.length; i++){
			setting = settings[i]; 
			String exts[] = setting.getSourceExtensions();
/*			if(src){
				if(setting.getSourceContentType() == null){
					exts = setting.getSourceExtensions();
				}
			} else {
				if(setting.getHeaderContentType() == null){
					exts = setting.getHeaderExtensions();
				}
			}
*/			
			if(exts != null && exts.length != 0){
				for(int j = 0; j < exts.length; j++){
					if(ext.equals(exts[j]))
						return setting;
				}
			}
		}
		return null;
	}

	ICStorageElement createStorage(ICSettingsStorage storage, String cfgId) throws CoreException{
		ICStorageElement rootElement = storage.getStorage(MODULE_ID, true);
		ICStorageElement children[] = rootElement.getChildren();
		ICStorageElement element = null;

		for(int i = 0; i < children.length; i++){
			if(CONFIGURATION.equals(children[i].getName())
					&& cfgId.equals(children[i].getAttribute(CConfigurationSpecSettings.ID))){
				element = children[i];
				break;
			}
		}
		
		if(element == null){
			element = rootElement.createChild(CONFIGURATION);
			element.setAttribute(CConfigurationSpecSettings.ID, cfgId);
		}
		
		return element;
	}
	
	CConfigurationData loadData(ICConfigurationDescription des) throws CoreException{
		CConfigurationDataProvider provider = getProvider(des);
		return provider.loadConfiguration(des);
	}
	
	CConfigurationData applyData(ICConfigurationDescription des, CConfigurationData base) throws CoreException {
		CConfigurationDataProvider provider = getProvider(des);
		return provider.applyConfiguration(des, base);
	}
	
	void removeData(ICConfigurationDescription des, CConfigurationData data) throws CoreException{
		CConfigurationDataProvider provider = getProvider(des);
		provider.removeConfiguration(des, data);
	}
	
	CConfigurationData createData(ICConfigurationDescription des, CConfigurationData base, boolean clone) throws CoreException{
		CConfigurationDataProvider provider = getProvider(des);
		return provider.createConfiguration(des, base, clone);
	}
	
	private CConfigurationDataProvider getProvider(ICConfigurationDescription des) throws CoreException{
		CConfigurationDataProviderDescriptor providerDes = getCfgProviderDescriptor(des);
		if(providerDes == null)
			throw ExceptionFactory.createCoreException("required build system is not installed");
		
		return providerDes.getProvider();
	}

	private CConfigurationDataProviderDescriptor getCfgProviderDescriptor(ICConfigurationDescription des){
		return getCfgProviderDescriptor(des.getBuildSystemId());
	}

	private CConfigurationDataProviderDescriptor getCfgProviderDescriptor(String id){
		initProviderInfo();
		
		return (CConfigurationDataProviderDescriptor)fProviderMap.get(id);
	}
	
	private synchronized void initProviderInfo(){
		if(fProviderMap != null)
			return;
		
		initProviderInfoSynch();
	}
	
	private synchronized void initProviderInfoSynch(){
		if(fProviderMap != null)
			return;
		
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(CConfigurationDataProviderDescriptor.DATA_PROVIDER_EXTPOINT_ID);
		IExtension exts[] = extensionPoint.getExtensions();
		fProviderMap = new HashMap(exts.length);
		
		for(int i = 0; i < exts.length; i++){
			CConfigurationDataProviderDescriptor des = new CConfigurationDataProviderDescriptor(exts[i]);
			fProviderMap.put(des.getId(), des);
		}
	}
	
	
	
/*	CConfigurationSpecSettings createConfigurationSpecSettings(ICConfigurationDescription cfg) throws CoreException{
		CConfigurationSpecSettings settings = null;
		if(cfg instanceof CConfigurationDescriptionCache){
			settings = new CConfigurationSpecSettings(cfg, createStorage(cfg.getProjectDescription(), cfg.getId()));
		} else {
			ICProjectDescription des = getProjecDescription(cfg.getProjectDescription().getProject(), false);
			CConfigurationDescriptionCache cache = (CConfigurationDescriptionCache)des.getConfigurationById(cfg.getId());
			if(cache != null){
				settings = new CConfigurationSpecSettings(cfg, cache.getSpecSettings());
			} else {
				settings = new CConfigurationSpecSettings(cfg, createStorage(cfg.getProjectDescription(), cfg.getId()));
			}
		}
		return settings;
	}
*/	
	
	private ICStorageElement readOldCDTProjectFile(IProject project) throws CoreException {
		ICStorageElement storage = null;
		try {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = null;
		InputStream stream = getSharedProperty(project, OLD_CDTPROJECT_FILE_NAME);
		if(stream != null){
			doc = builder.parse(stream);
			NodeList nodeList = doc.getElementsByTagName(OLD_PROJECT_DESCRIPTION);

			if (nodeList != null && nodeList.getLength() > 0) {
				Node node = nodeList.item(0);
				storage = new InternalXmlStorageElement((Element)node, false);
			}
		}
		} catch(ParserConfigurationException e){
			throw ExceptionFactory.createCoreException(e);
		} catch (SAXException e) {
			throw ExceptionFactory.createCoreException(e);
		} catch (IOException e) {
			throw ExceptionFactory.createCoreException(e);
		}
		return storage;
	}
	
	public ICStorageElement createPreferenceStorage(String key, boolean createEmptyIfNotFound, boolean readOnly) throws CoreException{
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = null;
			Element element = null;
			InputStream stream = null;

				try{
					stream = getPreferenceProperty(key);
					if(stream != null){
						doc = builder.parse(stream);
						
						// Get the first element in the project file
						Node rootElement = doc.getFirstChild();
						
						if (rootElement.getNodeType() != Node.PROCESSING_INSTRUCTION_NODE) {
							throw ExceptionFactory.createCoreException("invalid project file format: no PROCESSING_INSTRUCTION_NODE defined");
						} else {
							// Make sure that the version is compatible with the manager
							String fileVersion = rootElement.getNodeValue();
							Version version = new Version(fileVersion);
							if (DESCRIPTION_VERSION.compareTo(version) != 0) {
								throw ExceptionFactory.createCoreException("incompatible preference file format: versions do not match");
							}
						}
						
						// Now get the project root element (there should be only one)
						NodeList nodes = doc.getElementsByTagName(ROOT_PREFERENCE_ELEMENT);
						if (nodes.getLength() == 0)
							throw ExceptionFactory.createCoreException("invalid preference file format");
						Node node = nodes.item(0);
						if(node.getNodeType() != Node.ELEMENT_NODE)
							throw ExceptionFactory.createCoreException("invalid preference file format");
						element = (Element)node;
					} else if(!createEmptyIfNotFound){
						throw ExceptionFactory.createCoreException("storage file not found");
					}
				} catch (FactoryConfigurationError e) {
					if(!createEmptyIfNotFound)
						throw ExceptionFactory.createCoreException(e.getLocalizedMessage());
				} catch (SAXException e) {
					if(!createEmptyIfNotFound)
						throw ExceptionFactory.createCoreException(e);
				} catch (IOException e) {
					if(!createEmptyIfNotFound)
						throw ExceptionFactory.createCoreException(e);
				} finally {
					if(stream != null){
						try {
							stream.close();
						} catch (IOException e) {
						}
					}
				}
			
			if(element == null) {
				doc = builder.newDocument();
				ProcessingInstruction instruction = doc.createProcessingInstruction(VERSION_ELEMENT_NAME, DESCRIPTION_VERSION.toString());
				doc.appendChild(instruction);
				element = doc.createElement(ROOT_PREFERENCE_ELEMENT);	
				doc.appendChild(element);
			}
			return new InternalXmlStorageElement(element, null, false, readOnly);
		} catch (ParserConfigurationException e) {
			throw ExceptionFactory.createCoreException(e);
		}
	}
	
	private InputStream getPreferenceProperty(String key) {
		InputStream stream = null;
		File file = getPreferenceFile(key);
		if (file.exists()) {
			try {
				stream = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				CCorePlugin.log(e);
			}
		}
		return stream;
	}
	
	private File getPreferenceFile(String key){
		IPath path = CCorePlugin.getDefault().getStateLocation();
		path = path.append(key);
		return path.toFile();
	}
	
	
	ICStorageElement createStorage(IProject project, boolean reCreate, boolean createEmptyIfNotFound, boolean readOnly) throws CoreException{
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = null;
			Element element = null;
			InputStream stream = null;
			if(reCreate){
				try{
					stream = getSharedProperty(project, STORAGE_FILE_NAME);
					if(stream != null){
						doc = builder.parse(stream);
						
						// Get the first element in the project file
						Node rootElement = doc.getFirstChild();
						
						if (rootElement.getNodeType() != Node.PROCESSING_INSTRUCTION_NODE) {
							throw ExceptionFactory.createCoreException("invalid project file format: no PROCESSING_INSTRUCTION_NODE defined");
						} else {
							// Make sure that the version is compatible with the manager
							String fileVersion = rootElement.getNodeValue();
							Version version = new Version(fileVersion);
							if (DESCRIPTION_VERSION.compareTo(version) != 0) {
								throw ExceptionFactory.createCoreException("incompatible project file format: versions do not match");
							}
						}
						
						// Now get the project root element (there should be only one)
						NodeList nodes = doc.getElementsByTagName(ROOT_ELEMENT_NAME);
						if (nodes.getLength() == 0)
							throw ExceptionFactory.createCoreException("invalid project file format");
						Node node = nodes.item(0);
						if(node.getNodeType() != Node.ELEMENT_NODE)
							throw ExceptionFactory.createCoreException("invalid project file format");
						element = (Element)node;
					} else if(!createEmptyIfNotFound){
						throw ExceptionFactory.createCoreException("storage file not found");
					}
				} catch (FactoryConfigurationError e) {
					if(!createEmptyIfNotFound)
						throw ExceptionFactory.createCoreException(e.getLocalizedMessage());
				} catch (SAXException e) {
					if(!createEmptyIfNotFound)
						throw ExceptionFactory.createCoreException(e);
				} catch (IOException e) {
					if(!createEmptyIfNotFound)
						throw ExceptionFactory.createCoreException(e);
				} finally {
					if(stream != null){
						try {
							stream.close();
						} catch (IOException e) {
						}
					}
				}
			}
			
			if(element == null) {
				doc = builder.newDocument();
				ProcessingInstruction instruction = doc.createProcessingInstruction(VERSION_ELEMENT_NAME, DESCRIPTION_VERSION.toString());
				doc.appendChild(instruction);
				element = doc.createElement(ROOT_ELEMENT_NAME);	
				doc.appendChild(element);
			}
			
			return new InternalXmlStorageElement(element, null, false, readOnly);
		} catch (ParserConfigurationException e) {
			throw ExceptionFactory.createCoreException(e);
		}
	}
	
	
	public static File toLocalFile(URI uri, IProgressMonitor monitor) throws CoreException {
		IFileStore fileStore = EFS.getStore(uri);
		File localFile = fileStore.toLocalFile(EFS.NONE, monitor);
		if (localFile ==null)
			// non local file system
			localFile= fileStore.toLocalFile(EFS.CACHE, monitor);
		return localFile;
	}
	
	public InputStream getSharedProperty(IProject project, String key) throws CoreException {

		InputStream stream = null;
		IFile rscFile = project.getFile(key);
		if (rscFile.exists()) {
			stream = rscFile.getContents();
		} else {
			// when a project is imported, we get a first delta for the addition of the .project, but the .classpath is not accessible
			// so default to using java.io.File
			// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=96258
			URI location = rscFile.getLocationURI();
			if (location != null) {
				File file = toLocalFile(location, null/*no progress monitor available*/);
				if (file != null && file.exists()) {
					try {
						stream = new FileInputStream(file);
					} catch (FileNotFoundException e) {
						throw ExceptionFactory.createCoreException(e);
					}
				}
			}
		}
		return stream;
	}

/*	public IScannerInfoProvider getScannerInfoProvider(IProject project){
		
		return ScannerProvider.getInstance();
	}
*/	
	ICDescriptionDelta createDelta(ICProjectDescription newDescription, ICProjectDescription oldDescription){
		CProjectDescriptionDelta delta = new CProjectDescriptionDelta(newDescription, oldDescription);
		
		if(delta.getDeltaKind() == ICDescriptionDelta.CHANGED){
			ICConfigurationDescription[] cfgs = newDescription.getConfigurations();
			for(int i = 0; i < cfgs.length; i++){
				ICConfigurationDescription oldCfg = oldDescription.getConfigurationById(cfgs[i].getId());
				CProjectDescriptionDelta cfgDelta = createDelta(cfgs[i], oldCfg);
				if(cfgDelta != null){
					delta.addChild(cfgDelta);
				}
			}
			
			cfgs = oldDescription.getConfigurations();
			for(int i = 0; i < cfgs.length; i++){
				ICConfigurationDescription newCfg = newDescription.getConfigurationById(cfgs[i].getId());
				if(newCfg == null)
					delta.addChild(createDelta(null, cfgs[i]));
			}
			
			if(checkCfgChange(newDescription, oldDescription, true))
				delta.addChangeFlags(ICDescriptionDelta.ACTIVE_CFG);

			if(checkCfgChange(newDescription, oldDescription, false))
				delta.addChangeFlags(ICDescriptionDelta.INDEX_CFG);

		}
		return delta.isEmpty() ? null : delta;
	}
	
	private boolean checkCfgChange(ICProjectDescription newDes, ICProjectDescription oldDes, boolean active){
		ICConfigurationDescription newCfg, oldCfg;
		
		if(active){
			newCfg = newDes.getActiveConfiguration();
			oldCfg = oldDes.getActiveConfiguration();
		} else {
			newCfg = ((CProjectDescription)newDes).getIndexConfiguration();
			oldCfg = ((CProjectDescription)oldDes).getIndexConfiguration();
		}
		
		if(newCfg == null){
			return oldCfg != null;
		} else if (oldCfg == null){
			return true;
		} 
		return !newCfg.getId().equals(oldCfg.getId());
	}
	
/*	void postProcessNewDescriptionCache(CProjectDescription des, ICProjectDescriptionDelta delta){
		if(delta == null && delta.getDeltaKind() != ICProjectDescriptionDelta.CHANGED)
			return;
		
		ICConfigurationDescription indexCfg = des.getIndexConfiguration();
		ICConfigurationDescription activeCfg = des.getActiveConfiguration();
		ICProjectDescriptionDelta activeCfgDelta = findDelta(activeCfg.getId(), delta);
		if(indexCfg != activeCfg){
			switch(activeCfgDelta.getDeltaKind()){
			case ICProjectDescriptionDelta.CHANGED:
				des.setIndexConfiguration(activeCfg);
			}
		}
		
		
	}
*/	
	private ICDescriptionDelta findDelta(String id, ICDescriptionDelta delta){
		ICDescriptionDelta children[] = delta.getChildren();
		ICSettingObject obj;
		for(int i = 0; i < children.length; i++){
			obj = children[i].getSetting();
			if(obj.getId().equals(id))
				return children[i];
		}
		return null;
	}
	
	public int calculateDescriptorFlags(ICConfigurationDescription newCfg, ICConfigurationDescription oldCfg){
		try {
			int flags = 0;
			CConfigurationSpecSettings newSettings = ((IInternalCCfgInfo)newCfg).getSpecSettings();
			CConfigurationSpecSettings oldSettings = ((IInternalCCfgInfo)oldCfg).getSpecSettings();
			String newId = newSettings.getCOwnerId();
			String oldId = oldSettings.getCOwnerId();
			if(!CDataUtil.objectsEqual(newId, oldId))
				flags |= ICDescriptionDelta.OWNER;
			
			Map newMap = newSettings.getExtensionMapCopy();
			Map oldMap = oldSettings.getExtensionMapCopy();
			
			for(Iterator iter = newMap.entrySet().iterator(); iter.hasNext();){
				Map.Entry entry = (Map.Entry)iter.next();
				iter.remove();
				CConfigExtensionReference[] oldRefs = (CConfigExtensionReference[])oldMap.remove(entry.getKey());
				if(oldRefs == null){
					flags |= ICDescriptionDelta.EXT_REF;
					break;
				}
				
				CConfigExtensionReference[] newRefs = (CConfigExtensionReference[])entry.getValue();
				if(newRefs.length != oldRefs.length){
					flags |= ICDescriptionDelta.EXT_REF;
					break;
				}
				
				Set newSet = new HashSet(Arrays.asList(newRefs));
				Set oldSet = new HashSet(Arrays.asList(oldRefs));
				if(newSet.size() != oldSet.size()){
					flags |= ICDescriptionDelta.EXT_REF;
					break;
				}

				newSet.removeAll(oldSet);
				if(newSet.size() != 0){
					flags |= ICDescriptionDelta.EXT_REF;
					break;
				}
			}
			
			return flags;
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return 0;
	}

	public CProjectDescriptionDelta createDelta(ICConfigurationDescription newCfg, ICConfigurationDescription oldCfg){
		CProjectDescriptionDelta delta = new CProjectDescriptionDelta(newCfg, oldCfg);
		
		if(delta.getDeltaKind() == ICDescriptionDelta.CHANGED){
			ICFolderDescription[] foDess = newCfg.getFolderDescriptions();
			for(int i = 0; i < foDess.length; i++){
				ICResourceDescription oldDes = oldCfg.getResourceDescription(foDess[i].getPath(), true);
				if(oldDes != null && oldDes.getType() == ICSettingBase.SETTING_FOLDER){
					CProjectDescriptionDelta foDelta = createDelta(foDess[i], (ICFolderDescription)oldDes);
					if(foDelta != null)
						delta.addChild(foDelta);
				} else {
					delta.addChild(createDelta(foDess[i], null));
				}
			}
			
			foDess = oldCfg.getFolderDescriptions();
			for(int i = 0; i < foDess.length; i++){
				ICResourceDescription newDes = newCfg.getResourceDescription(foDess[i].getPath(), true);
				if(newDes == null || newDes.getType() != ICSettingBase.SETTING_FOLDER){
					delta.addChild(createDelta(null, foDess[i]));
				}
			}

			ICFileDescription[] fiDess = newCfg.getFileDescriptions();
			for(int i = 0; i < fiDess.length; i++){
				ICResourceDescription oldDes = oldCfg.getResourceDescription(fiDess[i].getPath(), true);
				if(oldDes != null && oldDes.getType() == ICSettingBase.SETTING_FILE){
					CProjectDescriptionDelta fiDelta = createDelta(fiDess[i], (ICFileDescription)oldDes);
					if(fiDelta != null)
						delta.addChild(fiDelta);
				} else {
					delta.addChild(createDelta(fiDess[i], null));
				}
			}
			
			fiDess = oldCfg.getFileDescriptions();
			for(int i = 0; i < fiDess.length; i++){
				ICResourceDescription newDes = newCfg.getResourceDescription(fiDess[i].getPath(), true);
				if(newDes == null || newDes.getType() != ICSettingBase.SETTING_FILE){
					delta.addChild(createDelta(null, fiDess[i]));
				}
			}

			CProjectDescriptionDelta tpsDelta = createDelta(newCfg.getTargetPlatformSetting(), oldCfg.getTargetPlatformSetting());
			if(tpsDelta != null)
				delta.addChild(tpsDelta);
			
			if(!newCfg.getName().equals(oldCfg.getName())){
				delta.addChangeFlags(ICDescriptionDelta.NAME);
			}
			
			ICSourceEntry newEntries[] = newCfg.getSourceEntries();
			ICSourceEntry oldEntries[] = oldCfg.getSourceEntries();
			
			if(newEntries.length > oldEntries.length){
				delta.addChangeFlags(ICDescriptionDelta.SOURCE_ADDED);
			} else {
				ICSourceEntry newEntry;
				for(int i = 0; i < newEntries.length; i++){
					boolean found = false;
					newEntry = newEntries[i];
					for(int j = 0; j < oldEntries.length; j++){
						if(newEntry.equals(oldEntries[j])){
							found = true;
							break;
						}
					}
					
					if(!found){
						delta.addChangeFlags(ICDescriptionDelta.SOURCE_ADDED);
						break;
					}
				}
			}
			
			if(oldEntries.length > newEntries.length){
				delta.addChangeFlags(ICDescriptionDelta.SOURCE_REMOVED);
			} else {
				ICSourceEntry oldEntry;
				for(int i = 0; i < oldEntries.length; i++){
					boolean found = false;
					oldEntry = oldEntries[i];
					for(int j = 0; j < newEntries.length; j++){
						if(oldEntry.equals(newEntries[j])){
							found = true;
							break;
						}
					}
					
					if(!found){
						delta.addChangeFlags(ICDescriptionDelta.SOURCE_REMOVED);
						break;
					}
				}
			}
			
			ExternalSettingsManager.getInstance().calculateCfgExtSettingsDelta(delta);
			
			int drFlags = calculateDescriptorFlags(newCfg, oldCfg);
			if(drFlags != 0)
				delta.addChangeFlags(drFlags);
		}

		return delta.isEmpty() ? null : delta;
	}
	
	private CProjectDescriptionDelta createDelta(ICFolderDescription newFo, ICFolderDescription oldFo){
		CProjectDescriptionDelta delta = new CProjectDescriptionDelta(newFo, oldFo);
		
		if(delta.getDeltaKind() == ICDescriptionDelta.CHANGED){
			ICLanguageSetting newLss[] = newFo.getLanguageSettings();
			ICLanguageSetting oldLss[] = oldFo.getLanguageSettings();
			List newList = new ArrayList(Arrays.asList(newLss));
			List oldList = new ArrayList(Arrays.asList(oldLss));
			List matched = sortSettings(newList, oldList);
			
			for(Iterator iter = matched.iterator(); iter.hasNext();){
				ICLanguageSetting[] match = (ICLanguageSetting[])iter.next();
				CProjectDescriptionDelta lsDelta = createDelta(match[0], match[1]);
				if(lsDelta != null)
					delta.addChild(lsDelta);
			}

			for(Iterator iter = newList.iterator(); iter.hasNext();){
				ICLanguageSetting added = (ICLanguageSetting)iter.next();
				delta.addChild(createDelta(added, null));
			}

			for(Iterator iter = oldList.iterator(); iter.hasNext();){
				ICLanguageSetting removed = (ICLanguageSetting)iter.next();
				delta.addChild(createDelta(null, removed));
			}

//			HashMap oldMap = new HashMap();
//			for(int i = 0; i < oldLss.length; i++){
//				oldMap.put(oldLss[i].getId(), oldLss[i]);
//			}

//			for(int i = 0; i < newLss.length; i++){
//				ICLanguageSetting oldLs = (ICLanguageSetting)oldMap.remove(newLss[i].getId());
//				CProjectDescriptionDelta lsDelta = createDelta(newLss[i], oldLs);
//				if(lsDelta != null)
//					delta.addChild(lsDelta);
//			}
			
//			if(!oldMap.isEmpty()){
//				for(Iterator iter = oldMap.values().iterator(); iter.hasNext();){
//					delta.addChild(createDelta(null, (ICLanguageSetting)iter.next()));
//				}
//			}

//			if(!newFo.getPath().equals(oldFo.getPath()))
//				delta.addChangeFlags(ICProjectDescriptionDelta.PATH);
			
			if(newFo.isExcluded() != oldFo.isExcluded())
				delta.addChangeFlags(ICDescriptionDelta.EXCLUDE);
		}
		
		return delta.isEmpty() ? null : delta;
	}
	
	private List sortSettings(List settings1,
			List settings2){
		ICLanguageSetting setting1;
		ICLanguageSetting setting2;
		List result = new ArrayList();
		for(Iterator iter1 = settings1.iterator(); iter1.hasNext();){
			setting1 = (ICLanguageSetting)iter1.next();
			for(Iterator iter2 = settings2.iterator(); iter2.hasNext();){
				setting2 = (ICLanguageSetting)iter2.next();
				
				if(setting2.getId().equals(setting1.getId())){
					iter1.remove();
					iter2.remove();
					ICLanguageSetting [] match = new ICLanguageSetting[2];
					match[0] = setting1;
					match[1] = setting2;
					result.add(match);
					break;
				}
			}
		}
		
		for(Iterator iter1 = settings1.iterator(); iter1.hasNext();){
			setting1 = (ICLanguageSetting)iter1.next();
			String lId = setting1.getLanguageId();
			if(lId != null){
				for(Iterator iter2 = settings2.iterator(); iter2.hasNext();){
					setting2 = (ICLanguageSetting)iter2.next();
	
					if(lId.equals(setting2.getLanguageId())){
						iter1.remove();
						iter2.remove();
						ICLanguageSetting [] match = new ICLanguageSetting[2];
						match[0] = setting1;
						match[1] = setting2;
						result.add(match);
						break;
					}
				}
			}
		}

		for(Iterator iter1 = settings1.iterator(); iter1.hasNext();){
			setting1 = (ICLanguageSetting)iter1.next();
			String cTypeIds1[] = setting1.getSourceContentTypeIds();
			if(cTypeIds1.length != 0){
				for(Iterator iter2 = settings2.iterator(); iter2.hasNext();){
					setting2 = (ICLanguageSetting)iter2.next();
					if(Arrays.equals(cTypeIds1, setting2.getSourceContentTypeIds())){
						iter1.remove();
						iter2.remove();
						ICLanguageSetting [] match = new ICLanguageSetting[2];
						match[0] = setting1;
						match[1] = setting2;
						result.add(match);
						break;
					}
				}
			}
		}

		for(Iterator iter1 = settings1.iterator(); iter1.hasNext();){
			setting1 = (ICLanguageSetting)iter1.next();
			if(setting1.getSourceContentTypeIds().length == 0){
				String srcExts[]  = setting1.getSourceExtensions();
				if(srcExts.length != 0){
					for(Iterator iter2 = settings2.iterator(); iter2.hasNext();){
						setting2 = (ICLanguageSetting)iter2.next();
						if(setting2.getSourceContentTypeIds().length == 0){
							if(Arrays.equals(srcExts, setting2.getSourceExtensions())){
								iter1.remove();
								iter2.remove();
								ICLanguageSetting [] match = new ICLanguageSetting[2];
								match[0] = setting1;
								match[1] = setting2;
								result.add(match);
								break;
							}
						}
					}
				}
			}
		}
		return result;
	}
	
	private CProjectDescriptionDelta createDelta(ICFileDescription newFi, ICFileDescription oldFi){
		CProjectDescriptionDelta delta = new CProjectDescriptionDelta(newFi, oldFi);
		
		if(delta.getDeltaKind() == ICDescriptionDelta.CHANGED){
			CProjectDescriptionDelta lsDelta = createDelta(newFi.getLanguageSetting(), oldFi.getLanguageSetting());
			if(lsDelta != null)
				delta.addChild(lsDelta);
			
//			if(!newFi.getPath().equals(oldFi.getPath()))
//				delta.addChangeFlags(ICProjectDescriptionDelta.PATH);
			
			if(newFi.isExcluded() != oldFi.isExcluded())
				delta.addChangeFlags(ICDescriptionDelta.EXCLUDE);
		}			

		return delta.isEmpty() ? null : delta;
	}
	
	private CProjectDescriptionDelta createDelta(ICLanguageSetting newLs, ICLanguageSetting oldLs){
		CProjectDescriptionDelta delta = new CProjectDescriptionDelta(newLs, oldLs);
		
		if(delta.getDeltaKind() == ICDescriptionDelta.CHANGED){
			int kinds[] = KindBasedStore.getLanguageEntryKinds();
			int kind;
			int addedKinds = 0;
			int removedKinds = 0;
			int reorderedKinds = 0;
			for(int i = 0; i < kinds.length; i++){
				kind = kinds[i];
				ICLanguageSettingEntry newEntries[] = newLs.getSettingEntries(kind);
				ICLanguageSettingEntry oldEntries[] = oldLs.getSettingEntries(kind);
				boolean[] change = calculateSettingsChanges(newEntries, oldEntries);
				
				if(change[0])
					addedKinds |= kind;
				if(change[1])
					removedKinds |= kind;
				if(change[2])
					reorderedKinds |= kind;
			}
			delta.setAddedLanguageEntriesKinds(addedKinds);
			delta.setRemovedLanguageEntriesKinds(removedKinds);
			delta.setReorderedLanguageEntriesKinds(reorderedKinds);
			
			String[] newCtIds = newLs.getSourceContentTypeIds();
			String[] oldCtIds = oldLs.getSourceContentTypeIds();
			
			if(!Arrays.equals(newCtIds, oldCtIds))
				delta.addChangeFlags(ICDescriptionDelta.SOURCE_CONTENT_TYPE);
				
			
			String[] newExts = newLs.getSourceExtensions();
			String[] oldExts = oldLs.getSourceExtensions();
			if(!Arrays.equals(newExts, oldExts))
				delta.addChangeFlags(ICDescriptionDelta.SOURCE_ENTENSIONS);
			

//			newCt = newLs.getHeaderContentType();
//			oldCt = oldLs.getHeaderContentType();
			
//			if(!compare(newCt, oldCt))
//				delta.addChangeFlags(ICDescriptionDelta.HEADER_CONTENT_TYPE);

//			newExts = newLs.getHeaderExtensions();
//			oldExts = oldLs.getHeaderExtensions();
//			if(!Arrays.equals(newExts, oldExts))
//				delta.addChangeFlags(ICDescriptionDelta.HEADER_ENTENSIONS);
		}
		
		return delta.isEmpty() ? null : delta;
	}
	
	private boolean[] calculateSettingsChanges(ICLanguageSettingEntry newEntries[], ICLanguageSettingEntry oldEntries[]) {
		boolean result[] = new boolean[3];

		// if nothing was known before do not generate any deltas.
		if (oldEntries == null) {
			return result;
		}
		// Sanity checks
		if (newEntries == null) {
			newEntries = EMPTY_LANGUAGE_SETTINGS_ENTRIES_ARRAY;
		}

		// Check the removed entries.
		for (int i = 0; i < oldEntries.length; i++) {
			boolean found = false;
			for (int j = 0; j < newEntries.length; j++) {
				if (oldEntries[i].equals(newEntries[j])) {
					found = true;
					break;
				}
			}
			if(!found){
				result[1] = true;
				break;
			}
		}

		// Check the new entries.
		for (int i = 0; i < newEntries.length; i++) {
			boolean found = false;
			for (int j = 0; j < oldEntries.length; j++) {
				if (newEntries[i].equals(oldEntries[j])) {
					found = true;
					break;
				}
			}
			if(!found){
				result[0] = true;
				break;
			}
		}

		// Check for reorder
		if (!result[0] && !result[1] && oldEntries.length == newEntries.length) {
			for (int i = 0; i < newEntries.length; i++) {
				if (!newEntries[i].equals(oldEntries[i])) {
					result[2] = true;
					break;
				}
			}
		}
		// They may have remove some duplications, catch here .. consider it as reordering.
		if (!result[0] && !result[1] && oldEntries.length != newEntries.length) {
			result[2] = true;
		}

		return result;
	}

	
	private boolean compare(Object o1, Object o2){
		if(o1 != null)
			return o1.equals(o2);
		return o2 == null;
	}

/*	public boolean entriesEqual(ICLanguageSettingEntry entries1[], ICLanguageSettingEntry entries2[]){
		if(entries1.length != entries2.length)
			return false;
		
		for(int i = 0; i < entries1.length; i++){
			if(!entries1[i].equals(entries2[i]))
				return false;
		}
		
		return true;
	}
*/	
	private CProjectDescriptionDelta createDelta(ICTargetPlatformSetting newTPS, ICTargetPlatformSetting oldTPS){
		CProjectDescriptionDelta delta = new CProjectDescriptionDelta(newTPS, oldTPS);
		if(!Arrays.equals(newTPS.getBinaryParserIds(), oldTPS.getBinaryParserIds()))
			delta.addChangeFlags(ICDescriptionDelta.BINARY_PARSER_IDS);
		
		return delta.isEmpty() ? null : delta;
	}

	ICElementDelta[] generateCElementDeltas(ICProject cProject, ICDescriptionDelta projDesDelta){
		if(projDesDelta == null)
			return EMPTY_CELEMENT_DELTA;

		int kind = projDesDelta.getDeltaKind();
		switch(kind){
		case ICDescriptionDelta.CHANGED:
			CProjectDescription newDes = (CProjectDescription)projDesDelta.getNewSetting();
			CProjectDescription oldDes = (CProjectDescription)projDesDelta.getOldSetting();
//			int flags = projDesDelta.getChangeFlags();
			ICConfigurationDescription activeCfg = newDes.getActiveConfiguration();
			ICConfigurationDescription indexCfg = newDes.getIndexConfiguration();
			if(indexCfg != activeCfg){
				ICDescriptionDelta delta = findDelta(activeCfg.getId(), projDesDelta);
				if(delta != null && delta.getDeltaKind() == ICDescriptionDelta.CHANGED){
					indexCfg = activeCfg;
					newDes.setIndexConfiguration(activeCfg);
				}
			}
			ICConfigurationDescription oldIndexCfg = oldDes.getIndexConfiguration();
			ICDescriptionDelta indexDelta;
			if(oldIndexCfg.getId().equals(indexCfg.getId())){
				indexDelta = findDelta(indexCfg.getId(), projDesDelta);
			} else {
				indexDelta = createDelta(indexCfg, oldIndexCfg);
			}
			if(indexDelta != null){
				List list = new ArrayList(); 
				generateCElementDeltasFromCfgDelta(cProject, indexDelta, list);
				return (ICElementDelta[])list.toArray(new ICElementDelta[list.size()]);
			}
			return EMPTY_CELEMENT_DELTA;
		case ICDescriptionDelta.ADDED:
		case ICDescriptionDelta.REMOVED:
			break;
		}
		return EMPTY_CELEMENT_DELTA;
	}

	private List generateCElementDeltasFromCfgDelta(ICProject cProject, ICDescriptionDelta cfgDelta, List list){
		int kind = cfgDelta.getDeltaKind();
		switch(kind){
		case ICDescriptionDelta.CHANGED:
			int descriptionFlags = cfgDelta.getChangeFlags();
			int celementFlags = 0;
			if((descriptionFlags & ICDescriptionDelta.SOURCE_ADDED) != 0)
				celementFlags |= ICElementDelta.F_ADDED_PATHENTRY_SOURCE;
			if((descriptionFlags & ICDescriptionDelta.SOURCE_REMOVED) != 0)
				celementFlags |= ICElementDelta.F_REMOVED_PATHENTRY_SOURCE;
			
			if(celementFlags != 0){
				CElementDelta cElDelta = new CElementDelta(cProject.getCModel());
				cElDelta.changed(cProject, celementFlags);
				list.add(cElDelta);
			}

			ICDescriptionDelta children[] = cfgDelta.getChildren();
			ICDescriptionDelta child;
			int type;
			for(int i = 0; i < children.length; i++){
				child = children[i];
				type = child.getSettingType();
				if(type == ICSettingBase.SETTING_FILE || type == ICSettingBase.SETTING_FOLDER){
					generateCElementDeltasFromResourceDelta(cProject, child, list);
				}
			}
		case ICDescriptionDelta.ADDED:
		case ICDescriptionDelta.REMOVED:
			break;
		}
		return list;
	}
	
	private List generateCElementDeltasFromResourceDelta(ICProject cProject, ICDescriptionDelta delta, List list){
		int kind = delta.getDeltaKind();
		ICDescriptionDelta parentDelta = delta.getParent();
		ICElement el;
//		IProject project = cProject.getProject();
		IResource rc = null;
		
		ICResourceDescription oldRcDes;
		ICResourceDescription newRcDes;
		IPath path;
		switch(kind){
		case ICDescriptionDelta.REMOVED:		
			oldRcDes = (ICResourceDescription)delta.getOldSetting();
			path = oldRcDes.getPath();
			newRcDes = ((ICConfigurationDescription)parentDelta.getNewSetting()).getResourceDescription(path, false);
			break;
		case ICDescriptionDelta.CHANGED:
//			if((delta.getChangeFlags() & ICProjectDescriptionDelta.PATH) == 0){
				newRcDes = (ICResourceDescription)delta.getNewSetting();
				path = newRcDes.getPath();
				oldRcDes = (ICResourceDescription)delta.getOldSetting();
				break;
//			}
			//if path changed treat as added
		case ICDescriptionDelta.ADDED:
		default:
			newRcDes = (ICResourceDescription)delta.getNewSetting();
			path = newRcDes.getPath();
			oldRcDes = ((ICConfigurationDescription)parentDelta.getOldSetting()).getResourceDescription(path, false);
			break;
		}
		path = path.makeRelative();
		el = null;
		try {
			el = cProject.findElement(path);
			rc = el.getResource();
		} catch (CModelException e) {
//			int i = 0;
		}
//			rc = ResourcesPlugin.getWorkspace().getRoot().findMember(project.getFullPath().append(path));
		if(rc != null){
			if(rc.getType() == IResource.FILE){
				String fileName = path.lastSegment();
				ICLanguageSetting newLS = getLanguageSetting(newRcDes, fileName);
				ICLanguageSetting oldLS = getLanguageSetting(oldRcDes, fileName);
				ICDescriptionDelta ld = createDelta(newLS, oldLS);
				generateCElementDeltasFromLanguageDelta(el, ld, list);
			} else {
				if(newRcDes.getType() == ICSettingBase.SETTING_FOLDER){
					ICFolderDescription oldFoDes = oldRcDes.getType() == ICSettingBase.SETTING_FOLDER ?
								(ICFolderDescription)oldRcDes : null;
					ICDescriptionDelta folderDelta = createDelta((ICFolderDescription)newRcDes, oldFoDes);
					if(folderDelta != null){
						ICDescriptionDelta children[] = folderDelta.getChildren();
						ICDescriptionDelta child;
						for(int i = 0; i < children.length; i++){
							child = children[i];
							if(child.getSettingType() == ICSettingBase.SETTING_LANGUAGE){
								generateCElementDeltasFromLanguageDelta(el, child, list);
							}
						}
					}
				} else {
					//ERROR?
				}
					
			}
		}
		return list;
	}
	
	private ICLanguageSetting getLanguageSetting(ICResourceDescription rcDes, String fileName){
		if(rcDes.getType() == ICSettingBase.SETTING_FILE){
			return ((ICFileDescription)rcDes).getLanguageSetting();
		}
		return ((ICFolderDescription)rcDes).getLanguageSettingForFile(fileName);
	}

	private List generateCElementDeltasFromLanguageDelta(ICElement el, ICDescriptionDelta delta, List list){
		if(delta == null)
			return list;
		
		int flags = 0;
		flags |= calculateEntriesFlags(delta.getAddedEntriesKinds(), true); 
		flags |= calculateEntriesFlags(delta.getRemovedEntriesKinds(), false); 
		flags |= calculateEntriesFlags(delta.getReorderedEntriesKinds(), true);
		if(flags != 0){
			CElementDelta cElDelta = new CElementDelta(el.getCModel());
			cElDelta.changed(el, flags);
			list.add(cElDelta);
		}
		return list;
	}
	
	private int calculateEntriesFlags(int languageDeltaKinds, boolean added){
		int flags = 0;
		int kindsArray[] = kindsToArray(languageDeltaKinds);
		
		for(int i = 0; i < kindsArray.length; i++){
			switch(kindsArray[i]){
			case ICLanguageSettingEntry.INCLUDE_PATH:
				flags |= ICElementDelta.F_CHANGED_PATHENTRY_INCLUDE;
				break;
			case ICLanguageSettingEntry.INCLUDE_FILE:
				flags |= ICElementDelta.F_CHANGED_PATHENTRY_INCLUDE;
				break;
			case ICLanguageSettingEntry.MACRO:
				flags |= ICElementDelta.F_CHANGED_PATHENTRY_MACRO;
				break;
			case ICLanguageSettingEntry.MACRO_FILE:
				flags |= ICElementDelta.F_CHANGED_PATHENTRY_MACRO;
				break;
			case ICLanguageSettingEntry.LIBRARY_PATH:
				flags |= added ? ICElementDelta.F_ADDED_PATHENTRY_LIBRARY
						: ICElementDelta.F_REMOVED_PATHENTRY_LIBRARY;
				break;
			case ICLanguageSettingEntry.LIBRARY_FILE:
				flags |= added ? ICElementDelta.F_ADDED_PATHENTRY_LIBRARY
						: ICElementDelta.F_REMOVED_PATHENTRY_LIBRARY;
				break;
			}
		}
		return flags;
	}
	
	int[] kindsToArray(int kinds){
		int allKinds[] = KindBasedStore.getLanguageEntryKinds();
		int kindsArray[] = new int[allKinds.length];
		int num = 0;
		for(int i = 0; i < allKinds.length; i++){
			if((allKinds[i] & kinds) != 0){
				kindsArray[num++] = allKinds[i];
			}
		}
		
		if(num < allKinds.length){
			int tmp[] = new int[num];
			if(num > 0)
				System.arraycopy(kindsArray, 0, tmp, 0, num);
			kindsArray = tmp;
		}
		return kindsArray;
	}
	
	public void addListener(ICProjectDescriptionListener listener, int eventTypes){
		synchronized(this){
			fListeners.add(new ListenerDescriptor(listener, eventTypes));
		}
	}

	public void removeListener(ICProjectDescriptionListener listener){
		synchronized(this){
			int size = fListeners.size();
			ListenerDescriptor des;
			for(int i = 0; i < size; i++){
				des = (ListenerDescriptor)fListeners.get(i);
				if(des.fListener == listener){
					fListeners.remove(des);
					break;
				}
			}
		}
	}
	
	private ListenerDescriptor[] getListeners(){
		synchronized(this){
			return (ListenerDescriptor[])fListeners.toArray(new ListenerDescriptor[fListeners.size()]);
		}
	}
	
	void notifyListeners(CProjectDescriptionEvent event){
		ListenerDescriptor[] listeners = getListeners();
		int eventType = event.getEventType();
		for(int i = 0; i < listeners.length; i++){
			if(listeners[i].handlesEvent(eventType)){
				listeners[i].fListener.handleEvent(event);
			}
		}
	}
	
	public Element createXmlElementCopy(InternalXmlStorageElement el) throws CoreException{
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.newDocument();
			Element newXmlEl = null;
			if(el.fElement.getParentNode().getNodeType() == Node.DOCUMENT_NODE){
				Document baseDoc = el.fElement.getOwnerDocument();
				NodeList list = baseDoc.getChildNodes();
				for(int i = 0; i < list.getLength(); i++){
					Node node = list.item(i);
					node = importAddNode(doc, node);
					if(node.getNodeType() == Node.ELEMENT_NODE && newXmlEl == null){
						newXmlEl = (Element)node;
					}
				}
				
			} else {
				newXmlEl = (Element)importAddNode(doc, el.fElement);
			}
//			Document baseDoc = el.fElement.getOwnerDocument();
//			Element baseEl = baseDoc.getDocumentElement();
//			Element newXmlEl = (Element)doc.importNode(baseEl, true);

			
//			doc.appendChild(newXmlEl);
			return newXmlEl;
		} catch (ParserConfigurationException e) {
			throw ExceptionFactory.createCoreException(e);
		} catch (FactoryConfigurationError e) {
			throw ExceptionFactory.createCoreException(e);
		}
		
	}

	InternalXmlStorageElement copyElement(InternalXmlStorageElement el, boolean readOnly) throws CoreException {
//		try {
//			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//			Document doc = builder.newDocument();
//			Element newXmlEl = null;
//			if(el.fElement.getParentNode().getNodeType() == Node.DOCUMENT_NODE){
//				Document baseDoc = el.fElement.getOwnerDocument();
//				NodeList list = baseDoc.getChildNodes();
//				for(int i = 0; i < list.getLength(); i++){
//					Node node = list.item(i);
//					node = importAddNode(doc, node);
//					if(node.getNodeType() == Node.ELEMENT_NODE && newXmlEl == null){
//						newXmlEl = (Element)node;
//					}
//				}
//				
//			}
//			
//			
////			 = (Element)doc.importNode(el.fElement, true);
//			
////			Document baseDoc = el.fElement.getOwnerDocument();
////			Element baseEl = baseDoc.getDocumentElement();
////			Element newXmlEl = (Element)doc.importNode(baseEl, true);
//
//			
////			doc.appendChild(newXmlEl);
//			return new InternalXmlStorageElement(newXmlEl, el.getParent(),
//					el.isParentRefAlowed(), el.getAttributeFilters(),
//					el.getChildFilters(), readOnly);
//		} catch (ParserConfigurationException e) {
//			throw ExceptionFactory.createCoreException(e);
//		} catch (FactoryConfigurationError e) {
//			throw ExceptionFactory.createCoreException(e);
//		}
		Element newXmlEl = createXmlElementCopy(el);
		return new InternalXmlStorageElement(newXmlEl, el.getParent(),
				el.isParentRefAlowed(), el.getAttributeFilters(),
				el.getChildFilters(), readOnly);
	}
	
	private Node importAddNode(Document doc, Node node){
		if(node.getOwnerDocument().equals(doc)){
			node = node.cloneNode(true);
		} else {
			node = doc.importNode(node, true);
		}
		
		return doc.appendChild(node);
	}

	CStorage copyStorage(CStorage el, boolean readOnly) throws CoreException {
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.newDocument();
			Element newXmlEl = (Element)doc.importNode(el.fElement, true);
			
//			Document baseDoc = el.fElement.getOwnerDocument();
//			Element baseEl = baseDoc.getDocumentElement();
//			Element newXmlEl = (Element)doc.importNode(baseEl, true);

			
			doc.appendChild(newXmlEl);
			return new CStorage(newXmlEl, readOnly);
		} catch (ParserConfigurationException e) {
			throw ExceptionFactory.createCoreException(e);
		} catch (FactoryConfigurationError e) {
			throw ExceptionFactory.createCoreException(e);
		}

	}
	void checkRemovedConfigurations(ICDescriptionDelta delta){
		if(delta == null)
			return;
		
		ICDescriptionDelta cfgDeltas[] = delta.getChildren();
		for(int i = 0; i < cfgDeltas.length; i++){
			if(cfgDeltas[i].getDeltaKind() == ICDescriptionDelta.REMOVED){
				CConfigurationDescriptionCache des = (CConfigurationDescriptionCache)cfgDeltas[i].getOldSetting();
				CConfigurationData data = des.getConfigurationData(); 
				try {
					removeData(des, data);
				} catch (CoreException e) {
				}
			}
		}
		
	}

	public ICConfigurationDescription getPreferenceConfiguration(String buildSystemId) throws CoreException {
		return getPreferenceConfiguration(buildSystemId, true);
	}

	public ICConfigurationDescription getPreferenceConfiguration(String buildSystemId, boolean write) throws CoreException {
		ICConfigurationDescription des = getLoaddedPreference(buildSystemId);
		if(des == null){
			try {
				des = loadPreference(buildSystemId);
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
			
			if(des == null){
				try {
					des = createNewPreference(buildSystemId);
				} catch (CoreException e) {
					CCorePlugin.log(e);
				}
			}
			
			setLoaddedPreference(buildSystemId, (CConfigurationDescriptionCache)des);
		}
		if(des != null && write){
			try {
				des = createWritablePreference((CConfigurationDescriptionCache)des);
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
		return des;
	}
	
	public void setPreferenceConfiguration(String buildSystemId, ICConfigurationDescription des) throws CoreException{
		if(!needSavePreference(buildSystemId, des))
			return;
		
		CConfigurationDescriptionCache cache = createPreferenceCache(des);
		
		savePreferenceConfiguration(buildSystemId, cache);
		
		setLoaddedPreference(buildSystemId, cache);
	}
	
	private void savePreferenceConfiguration(String buildStystemId, CConfigurationDescriptionCache cache) throws CoreException{
		ICStorageElement el = cache.getSpecSettings().getRootStorageElement();
		saveBuildSystemConfigPreferenceStorage(buildStystemId, el);
	}
	
	private void saveBuildSystemConfigPreferenceStorage(String buildSystemId, ICStorageElement el) throws CoreException{
		ICStorageElement cur = getBuildSystemConfigPreferenceStorage(buildSystemId);
		ICStorageElement parent = cur.getParent();
		parent.removeChild(cur);
		parent.importChild(el);
		savePreferenceStorage(PREFERENCES_STORAGE, MODULE_ID, parent);
	}
	
	private boolean needSavePreference(String buildSystemId, ICConfigurationDescription des){
		if(des.isModified()
				|| !des.isPreferenceConfiguration()
				|| !des.getBuildSystemId().equals(buildSystemId))
			return true;
		
		return false;
	}
	
	private ICConfigurationDescription createWritablePreference(CConfigurationDescriptionCache cache) throws CoreException{
		return new CConfigurationDescription(cache, fPrefUpdater);
	}
	
	private CConfigurationDescriptionCache createPreferenceCache(ICConfigurationDescription des) throws CoreException{
		IInternalCCfgInfo cfgDes = (IInternalCCfgInfo)des;
		CConfigurationData baseData = cfgDes.getConfigurationData(false);
		if(baseData instanceof CConfigurationDescriptionCache){
			baseData = ((CConfigurationDescriptionCache)baseData).getConfigurationData();
		}
		CConfigurationSpecSettings settings = cfgDes.getSpecSettings();
		ICStorageElement rootEl = getBuildSystemConfigPreferenceStorage(des.getBuildSystemId(), true, false);
		ICStorageElement rootParent = rootEl.getParent();
		rootParent.removeChild(rootEl);
		ICStorageElement baseRootEl = settings.getRootStorageElement();
		rootEl = rootParent.importChild(baseRootEl);
		CConfigurationDescriptionCache cache = new CConfigurationDescriptionCache(baseData, cfgDes.getSpecSettings(), null, rootEl, true);
		
		return cache;
	}
	
	private ICConfigurationDescription createNewPreference(String buildSystemId) throws CoreException {
		ICStorageElement cfgEl = getBuildSystemConfigPreferenceStorage(buildSystemId, true, false);
		
		String id = PREFERENCE_CFG_ID_PREFIX + buildSystemId;
		CConfigurationDescription des = new CConfigurationDescription(id, PREFERENCE_CFG_NAME, buildSystemId, cfgEl, fPrefUpdater);
		
		return createPreferenceCache(des);
	}
	
//	private CStorage createBuildSystemCfgPrefStore() throws CoreException{
//		ICStorageElement el = getPreferenceStorage(PREFERENCES_STORAGE, MODULE_ID, true, false);
//		
//		CStorage store = new CStorage((InternalXmlStorageElement)el);
//		
//		return store;
//	}
//	
//	ICSettingsStorage getBuildSystemCfgPrefStore() throws CoreException{
//		if(fPrefCfgStorage == null){
//			fPrefCfgStorage = createBuildSystemCfgPrefStore();
//		}
//		
//		return copyStorage(fPrefCfgStorage, false);
//	}

	ICStorageElement getBuildSystemConfigPreferenceStorage(String buildSystemId) throws CoreException{
		return getBuildSystemConfigPreferenceStorage(buildSystemId, true, false);
	}

	private ICStorageElement getBuildSystemConfigPreferenceStorage(String buildSystemId, boolean createIfNotDound, boolean readOnly) throws CoreException{
		ICStorageElement el = getPreferenceStorage(PREFERENCES_STORAGE, MODULE_ID, createIfNotDound, readOnly);
		ICStorageElement cfgEl = null;
		
		if(el != null){
			ICStorageElement children[] = el.getChildren();
			for(int i = 0; i < children.length; i++){
				ICStorageElement child = children[i];
				if(PREFERENCE_BUILD_SYSTEM_ELEMENT.equals(child.getName())){
					if(buildSystemId.equals(child.getAttribute(ID))){
						cfgEl = child;
						break;
					}
				}
				
			}
			
			if(cfgEl == null){
				cfgEl = el.createChild(PREFERENCE_BUILD_SYSTEM_ELEMENT);
				cfgEl.setAttribute(ID, buildSystemId);
			}
				
		}
		
		return cfgEl;
	}
	
	private ICConfigurationDescription loadPreference(String buildSystemId) throws CoreException{
		ICStorageElement el = getPreferenceStorage(PREFERENCES_STORAGE, MODULE_ID, false, false);
		
		ICStorageElement children[] = el.getChildren();
		ICStorageElement cfgEl = null;
		for(int i = 0; i < children.length; i++){
			ICStorageElement child = children[i];
			if(PREFERENCE_BUILD_SYSTEM_ELEMENT.equals(child.getName())){
				if(buildSystemId.equals(child.getAttribute(ID))){
					cfgEl = child;
					break;
				}
			}
			
		}
		CConfigurationDescriptionCache cache = new CConfigurationDescriptionCache(cfgEl, null);
		cache.loadData();
		return cache;
	}
	
	public ICStorageElement getPreferenceStorage(String prefKey, String storageId, boolean createIfNotDound, boolean readOnly) throws CoreException{
		CStorage store = getPreferenceStore(prefKey, createIfNotDound, readOnly);
		
		return store.getStorage(storageId, createIfNotDound);
	}
	
	private CStorage getPreferenceStore(String prefKey,  boolean createIfNotDound, boolean readOnly) throws CoreException{
		ICStorageElement el = createPreferenceStorage(prefKey, createIfNotDound, readOnly);
		
		CStorage store = new CStorage((InternalXmlStorageElement)el);
		
		return store;
	}
	
	public void savePreferenceStorage(String prefKey, String storageId, ICStorageElement el) throws CoreException{
		CStorage store = getPreferenceStore(prefKey, true, false);
		store.importStorage(storageId, el);
		
		ICStorageElement rootEl = new InternalXmlStorageElement(store.fElement, store.isReadOnly());
		serializePreference(prefKey, rootEl);
	}
	
	private CConfigurationDescriptionCache getLoaddedPreference(String buildSystemId){
		return (CConfigurationDescriptionCache)fPreferenceMap.get(buildSystemId);
	}

	private void setLoaddedPreference(String buildSystemId, CConfigurationDescriptionCache des){
		fPreferenceMap.put(buildSystemId, des);
	}

	public CConfigBasedDescriptorManager getDescriptorManager(){
		return fDescriptorManager;
	}

	public CConfigurationData createDefaultConfigData(IProject project, CDataFacroty factory) throws CoreException{
		return createDefaultConfigData(project, CDataUtil.genId(DEFAULT_CFG_ID_PREFIX), DEFAULT_CFG_NAME, factory);
	}

	public CConfigurationData createDefaultConfigData(IProject project, String id, String name, CDataFacroty factory) throws CoreException{
		if(factory == null)
			factory = new CDataFacroty();
		
		CDefaultConfigurationData data = (CDefaultConfigurationData)factory.createConfigurationdata(id, name, null, false);
		data.initEmptyData();
		adjustDefaultConfig(data);
		
		data.setModified(false);
		return data;
	}
	
	public CConfigurationData adjustDefaultConfig(CConfigurationData cfg){
		LanguageManager mngr = LanguageManager.getInstance();
		ILanguageDescriptor dess[] = mngr.getLanguageDescriptors();
		Map map = mngr.getContentTypeIdToLanguageDescriptionsMap();
		
		CResourceData[] rcDatas = cfg.getResourceDatas();
		for(int i = 0; i < rcDatas.length; i++){
			if(rcDatas[i].getType() == ICSettingBase.SETTING_FOLDER){
				adjustFolderData((CFolderData)rcDatas[i], dess, new HashMap(map));
			}
		}
		
		return cfg;
	}

	
	private static void adjustFolderData(CFolderData data, ILanguageDescriptor dess[], HashMap map){
		Map langMap = new HashMap();
		for(int i = 0; i < dess.length; i++){
			langMap.put(dess[i].getId(), dess[i]);
		}
		CLanguageData lDatas[] = data.getLanguageDatas();
		for(int i = 0; i < lDatas.length; i++){
			CLanguageData lData = (CLanguageData)lDatas[i];
			String langId = lData.getLanguageId();
			if(langId != null){
				ILanguageDescriptor des = (ILanguageDescriptor)langMap.remove(langId);
				adjustLanguageData(data, lData, des);
						continue;
			} else {
				String[] cTypeIds = lData.getSourceContentTypeIds();
				for(int c = 0; c < cTypeIds.length; c++){
					String cTypeId = cTypeIds[c];
					ILanguageDescriptor[] langs = (ILanguageDescriptor[])map.remove(cTypeId);
					if(langs != null && langs.length != 0){
						for(int q = 0; q < langs.length; q++){
							langMap.remove(langs[q].getId());
						}
								
						adjustLanguageData(data, lData, langs[0]);
					}
				}
			}
		}
			
		if(!langMap.isEmpty()){
			addLangs(data, langMap, map);
		}
		
	}
	
	private static CLanguageData adjustLanguageData(CFolderData data, CLanguageData lData, ILanguageDescriptor des){
		String [] cTypeIds = des.getContentTypeIds();
		String srcIds[] = lData.getSourceContentTypeIds();
		
		Set landTypes = new HashSet(Arrays.asList(cTypeIds));
		landTypes.removeAll(Arrays.asList(srcIds));
		
		if(landTypes.size() != 0){
			List srcList = new ArrayList();
			srcList.addAll(landTypes);
			lData.setSourceContentTypeIds((String[])srcList.toArray(new String[srcList.size()]));
		}
		
		if(!des.getId().equals(lData.getLanguageId())){
			lData.setLanguageId(des.getId());
		}
		return lData;
	}
	
	private static void addLangs(CFolderData data, Map langMap, Map cTypeToLangMap){
		List list = new ArrayList(langMap.values());
		ILanguageDescriptor des;
		while(list.size() != 0){
			des = (ILanguageDescriptor)list.remove(list.size() - 1);
			String[] ctypeIds = des.getContentTypeIds();
			boolean addLang = false;
			for(int i = 0; i < ctypeIds.length; i++){
				ILanguageDescriptor[] langs = (ILanguageDescriptor[])cTypeToLangMap.remove(ctypeIds[i]);
				if(langs != null && langs.length != 0){
					addLang = true;
					for(int q = 0; q < langs.length; q++){
						list.remove(langs[q]);
					}
				}
			}
			
			if(addLang){
				data.createLanguageDataForContentTypes(des.getId(), ctypeIds);
			}
		}
	}

	public boolean isNewStyleIndexCfg(IProject project){
		ICProjectDescription des = getProjectDescription(project, false);
		if(des != null)
			return isNewStyleIndexCfg(des);
		return false;
	}

	public boolean isNewStyleIndexCfg(ICProjectDescription des){
		ICConfigurationDescription cfgDes = ((CProjectDescription)des).getIndexConfiguration();
		if(cfgDes != null)
			return isNewStyleCfg(cfgDes);
		return false;
	}

	public boolean isNewStyleCfg(ICConfigurationDescription cfgDes){
		if(cfgDes == null)
			return false;
		
		CConfigurationData data = ((IInternalCCfgInfo)cfgDes).getConfigurationData(false);
		if(data instanceof CConfigurationDescriptionCache){
			data = ((CConfigurationDescriptionCache)data).getConfigurationData();
		}
		
		return data != null && !PathEntryConfigurationDataProvider.isPathEntryData(data);
	}

}

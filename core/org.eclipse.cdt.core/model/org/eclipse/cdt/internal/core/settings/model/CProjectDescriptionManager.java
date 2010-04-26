/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 * IBM Corporation
 * James Blackburn (Broadcom Corp.)
 * Alex Blewitt Bug 132511 - nature order not preserved
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

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
import org.eclipse.cdt.core.settings.model.CExternalSetting;
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICBuildSetting;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICDescriptionDelta;
import org.eclipse.cdt.core.settings.model.ICFileDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionListener;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionWorkspacePreferences;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingObject;
import org.eclipse.cdt.core.settings.model.ICSettingsStorage;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.ICTargetPlatformSetting;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationDataProvider;
import org.eclipse.cdt.core.settings.model.extension.CFileData;
import org.eclipse.cdt.core.settings.model.extension.CFolderData;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.cdt.core.settings.model.extension.CResourceData;
import org.eclipse.cdt.core.settings.model.extension.ICProjectConverter;
import org.eclipse.cdt.core.settings.model.extension.impl.CDataFactory;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.settings.model.util.CSettingEntryFactory;
import org.eclipse.cdt.core.settings.model.util.KindBasedStore;
import org.eclipse.cdt.core.settings.model.util.ListComparator;
import org.eclipse.cdt.core.settings.model.util.PathSettingsContainer;
import org.eclipse.cdt.core.settings.model.util.PatternNameMap;
import org.eclipse.cdt.internal.core.CConfigBasedDescriptorManager;
import org.eclipse.cdt.internal.core.model.CElementDelta;
import org.eclipse.cdt.internal.core.settings.model.CExternalSettinsDeltaCalculator.ExtSettingsDelta;
import org.eclipse.cdt.internal.core.settings.model.xml.InternalXmlStorageElement;
import org.eclipse.cdt.internal.core.settings.model.xml.XmlStorage;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ISavedState;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
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

import com.ibm.icu.text.MessageFormat;

/**
 * The CProjectDescriptionManager is to marshall the loading and storing
 * of CDT Project Descriptions.
 *
 * This class delegates loading and store of the project model to the appropriate
 * AbstractCProjectDescriptionStorage for the Project Description.  [ Discovered at Project load
 * time.]
 *
 * Users should not synchronize on the singleton instance of this class. It is the job of
 * the AbstractCProjectDescriptionStorage to ensure thread safe access to the backing store
 * as described in that interface.
 *
 * Previously this class created and persisted
 * @see ICProjectDescriptionManager
 */
public class CProjectDescriptionManager implements ICProjectDescriptionManager {
	public static final int INTERNAL_GET_IGNORE_CLOSE = 1 << 31 ;

	private static final String VERSION_ELEMENT_NAME = "fileVersion";	//$NON-NLS-1$
	/** Preference Version 4.0 & 5.0 are equivalent for us. Version was inadvertently bumped
	 *  when during project description storage work.
	 *  This is the minimum preference version we support loading.*/
	public static final Version MIN_DESCRIPTION_VERSION = new Version("4.0"); //$NON-NLS-1$
	/** Current preference file storage version */
	public static final Version DESCRIPTION_VERSION = new Version("5.0"); 	//$NON-NLS-1$
	public final static String MODULE_ID = "org.eclipse.cdt.core.settings";	//$NON-NLS-1$
	static final String CONFIGURATION = "cconfiguration";	//$NON-NLS-1$
	private static final ICLanguageSettingEntry[] EMPTY_LANGUAGE_SETTINGS_ENTRIES_ARRAY = new ICLanguageSettingEntry[0];
	private static final ICElementDelta[] EMPTY_CELEMENT_DELTA = new ICElementDelta[0];
	private static final ICLanguageSetting[] EMPTY_LANGUAGE_SETTINGS_ARRAY = new ICLanguageSetting[0];
	private static final String PREFERENCES_STORAGE = "preferences";	//$NON-NLS-1$
	private static final String PREFERENCE_BUILD_SYSTEM_ELEMENT = "buildSystem";	//$NON-NLS-1$
	private static final String PREFERENCES_ELEMENT = "preferences";	//$NON-NLS-1$
	private static final String ID = "id";	//$NON-NLS-1$
	private static final String PREFERENCE_CFG_ID_PREFIX = "preference.";	//$NON-NLS-1$
	private static final String PREFERENCE_CFG_NAME = SettingsModelMessages.getString("CProjectDescriptionManager.15"); //$NON-NLS-1$
	private static final String ROOT_PREFERENCE_ELEMENT = "preferences";	//$NON-NLS-1$
	private static final String DEFAULT_CFG_ID_PREFIX = CCorePlugin.PLUGIN_ID + ".default.config"; //$NON-NLS-1$
	private static final String DEFAULT_CFG_NAME = "Configuration"; //$NON-NLS-1$

	private static final QualifiedName SCANNER_INFO_PROVIDER_PROPERTY = new QualifiedName(CCorePlugin.PLUGIN_ID, "scannerInfoProvider"); //$NON-NLS-1$

	static class CompositeWorkspaceRunnable implements IWorkspaceRunnable {
		private List<IWorkspaceRunnable> fRunnables = new ArrayList<IWorkspaceRunnable>();
		private String fName;
		private boolean fStopOnErr;

		CompositeWorkspaceRunnable(String name){
			if(name == null)
				name = "";	//$NON-NLS-1$
			fName = name;
		}

		public void add(IWorkspaceRunnable runnable){
			fRunnables.add(runnable);
		}

		public void run(IProgressMonitor monitor) throws CoreException {
			try {
				monitor.beginTask(fName, fRunnables.size());

				for (IWorkspaceRunnable r : fRunnables) {
					IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
					try {
						r.run(subMonitor);
					} catch (CoreException e){
						if(fStopOnErr)
							throw e;
					} catch (RuntimeException e) {
						if(fStopOnErr)
							throw e;
					} finally {
						subMonitor.done();
					}
				}
			} finally {
				monitor.done();
			}
		}

		public boolean isEmpty(){
			return fRunnables.isEmpty();
		}
	}

	/**
	 * Container class for ICProjectDescription change listeners
	 */
	private static class ListenerDescriptor{
		final ICProjectDescriptionListener fListener;
		final int fEventTypes;

		public ListenerDescriptor(ICProjectDescriptionListener listener, int eventTypes) {
			fListener = listener;
			fEventTypes = eventTypes;
		}

		public boolean handlesEvent(int eventType){
			return (eventType & fEventTypes) != 0;
		}
	}

	private volatile Map<String, CConfigurationDataProviderDescriptor> fProviderMap;
	private volatile CProjectConverterDesciptor fConverters[];
	/** Set of Listeners listening for Project Description Deltas */
	private Set<ListenerDescriptor> fListeners = new CopyOnWriteArraySet<ListenerDescriptor>();
	private Map<String, CConfigurationDescriptionCache> fPreferenceMap = new HashMap<String, CConfigurationDescriptionCache>();
	private volatile CConfigBasedDescriptorManager fDescriptorManager;
	private ResourceChangeHandler fRcChangeHandler;
	private CProjectDescriptionWorkspacePreferences fPreferences;
	private boolean fAllowEmptyCreatingDescription = true; // allowed by default

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
			return fPreferenceMap.values().toArray(new CConfigurationDescriptionCache[fPreferenceMap.size()]);
		}
	};

	/** The CProjectDescriptionManager instance */
	private static volatile CProjectDescriptionManager fInstance;

	private CProjectDescriptionManager(){}

	public static CProjectDescriptionManager getInstance(){
		if(fInstance == null)
			synchronized(CProjectDescriptionManager.class) {
				if (fInstance == null)
					fInstance = new CProjectDescriptionManager();
			}
		return fInstance;
	}

	public void projectClosedRemove(IProject project) {
		CProjectDescriptionStorageManager.getInstance().projectClosedRemove(project);
	}

	/**
	 *
	 * @param from 	Project to move the description from
	 * @param to	Project where the description is moved to
	 * @return		<b>ICProjectDescription</b> - non serialized, modified, writable
	 * project description. To serialize, call <code>setProjectDescription()</code>
	 *
	 */
	public ICProjectDescription projectMove(IProject from, IProject to) {
		CProjectDescriptionStorageManager.getInstance().projectMove(from, to);

		int flags = CProjectDescriptionManager.INTERNAL_GET_IGNORE_CLOSE |
					ICProjectDescriptionManager.GET_WRITABLE;
		CProjectDescription des = (CProjectDescription)getProjectDescription(to, flags);
		// set configuration descriptions to "writable" state
		if (des != null) {
			for (ICConfigurationDescription cfgDes : des.getConfigurations()) {
					des.updateChild((CConfigurationDescription)cfgDes, true);
			}
		}
		return des;
	}


	public Job startup(){
		if(fRcChangeHandler == null){
			fRcChangeHandler = new ResourceChangeHandler();

			ResourcesPlugin.getWorkspace().addResourceChangeListener(
					fRcChangeHandler,
					IResourceChangeEvent.POST_CHANGE
					| IResourceChangeEvent.PRE_DELETE
					| IResourceChangeEvent.PRE_CLOSE
			/*| IResourceChangeEvent.POST_BUILD*/);

			if(fDescriptorManager == null){
				fDescriptorManager = CConfigBasedDescriptorManager.getInstance();
				fDescriptorManager.startup();
			}

			CExternalSettingsManager.getInstance().startup();
		}
		return createPostStartupJob();
	}

	private Job createPostStartupJob() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		Job rcJob = new Job(SettingsModelMessages.getString("CProjectDescriptionManager.0")){ //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try{
					startSaveParticipant();
				} catch (CoreException e){
					CCorePlugin.log(e);
					return e.getStatus();
				}
				return new Status(
						IStatus.OK,
						CCorePlugin.PLUGIN_ID,
						IStatus.OK,
						"", //$NON-NLS-1$
						null);
			}
		};

		rcJob.setRule(root);
		rcJob.setPriority(Job.INTERACTIVE);
		rcJob.setSystem(true);
		return rcJob;
	}

	/*
	 * This method adds a save participant and resource change listener
	 * Throws CoreException if the methods fails to add a save participant.
	 * The resource change listener in not added in this case either.
	 */
	private void startSaveParticipant() throws CoreException{
		// Set up a listener for resource change events
		ISavedState lastState =
			ResourcesPlugin.getWorkspace().addSaveParticipant(CCorePlugin.PLUGIN_ID, fRcChangeHandler);

		if (lastState != null) {
			lastState.processResourceChangeEvents(fRcChangeHandler);
		}
	}

	public void shutdown(){
		CExternalSettingsManager.getInstance().shutdown();

		if(fDescriptorManager != null) {
			fDescriptorManager.shutdown();
			fDescriptorManager = null;
		}

		if(fRcChangeHandler != null) {
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(fRcChangeHandler);
			fRcChangeHandler = null;
		}

		CProjectDescriptionStorageManager.getInstance().shutdown();
	}

	public ICProjectDescription getProjectDescription(IProject project, boolean write) {
		return getProjectDescription(project, true, write);
	}

	public ICProjectDescription getProjectDescription(IProject project, boolean load, boolean write) {
		int flags = load ? 0 : GET_IF_LOADDED;
		flags |= write ? GET_WRITABLE : 0;

		return getProjectDescription(project, flags);
	}

	public ICProjectDescription getProjectDescription(IProject project, int flags) {
		try {
			return getProjectDescriptionInternal(project, flags);
		} catch (CoreException e) {
			// FIXME Currently the resource change handler ResourceChangeHandler.getProjectDescription(...)
			// Does this when the project is closed. Don't log an error or the tests will fail
//				CCorePlugin.log(e);
		}
		return null;
	}

	/**
	 * Base method for getting a Project's Description 
	 * @param project
	 * @param flags
	 * @return ICProjectDescription
	 * @throws CoreException if project description isn't available
	 */
	private ICProjectDescription getProjectDescriptionInternal(IProject project, int flags) throws CoreException {
		AbstractCProjectDescriptionStorage storage = getProjectDescriptionStorage(project);
		return storage.getProjectDescription(flags, new NullProgressMonitor());
	}	

	/**
	 * Run the workspace modification in the current thread using the workspace scheduling rule
	 * Equivalent to: <code>runWspModification(IWorkspaceRunnable, ResourcecPlugin.getWorkspace().getRoot(), IProgressMonitor)</code>
	 *<br/><br/>
	 * Note that if the workspace is locked, or the current job / thread doesn't contain the workspace
	 * scheduling rule, then we schedule a job to run the {@link IWorkspaceRunnable}
	 *<br/><br/>
	 * The scheduled job is returned, or null if the operation was run immediately.
	 *
	 * @param runnable the IWorkspaceRunnable to run
	 * @param monitor
	 * @return scheduled job or null if the operation was run immediately
	 */
	public static Job runWspModification(final IWorkspaceRunnable runnable, IProgressMonitor monitor) {
		return runWspModification(runnable, ResourcesPlugin.getWorkspace().getRoot(), monitor);
	}

	/**
	 * Either runs the modification in the current thread (if the workspace is not locked)
	 * or schedules a runnable to perform the operation
	 * @param runnable
	 * @param monitor
	 * @return scheduled job or null if the operation was run immediately
	 */
	public static Job runWspModification(final IWorkspaceRunnable runnable, final ISchedulingRule rule, IProgressMonitor monitor){
		if(monitor == null)
			monitor = new NullProgressMonitor();

		// Should the rule be scheduled, or run immediately
		boolean scheduleRule =  ResourcesPlugin.getWorkspace().isTreeLocked();

		// Check whether current job contains rule 'rule'
		// If not, we must schedule another job to execute the runnable
		if (!scheduleRule) {
			Job currentJob = Job.getJobManager().currentJob();
			if (currentJob != null && currentJob.getRule() != null && !currentJob.getRule().contains(rule))
				scheduleRule = true;
		}

		if(!scheduleRule) {
			// Run immediately
			IJobManager mngr = Job.getJobManager();
			try{
				mngr.beginRule(rule, monitor);
				runAtomic(runnable, rule, monitor);
			} catch (Exception e) {
				CCorePlugin.log(e);
			} finally {
				if(!scheduleRule)
					monitor.done();
				mngr.endRule(rule);
			}
		} else {
			// schedule a job for it
			Job job = new Job(SettingsModelMessages.getString("CProjectDescriptionManager.12")){ //$NON-NLS-1$
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						runAtomic(runnable, rule, monitor);
					} catch (CoreException e) {
						CCorePlugin.log(e);
						return e.getStatus();
					} finally {
						monitor.done();
					}
					return Status.OK_STATUS;
				}
			};

			job.setRule(rule);
			job.setSystem(true);
			job.schedule();
			return job;
		}
		return null;
	}

	private static void runAtomic(final IWorkspaceRunnable r, ISchedulingRule rule, IProgressMonitor monitor) throws CoreException{
		IWorkspace wsp = ResourcesPlugin.getWorkspace();
		wsp.run(new IWorkspaceRunnable(){
			public void run(IProgressMonitor monitor) throws CoreException {
				try {
					r.run(monitor);
				} catch (Exception e){
					CCorePlugin.log(e);
					throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, e.getMessage(), e));
				}
			}
		}, rule, IWorkspace.AVOID_UPDATE, monitor);
	}

	public void updateProjectDescriptions(IProject[] projects, IProgressMonitor monitor) throws CoreException{
		if(monitor == null)
			monitor = new NullProgressMonitor();

		try {
			IWorkspace wsp = ResourcesPlugin.getWorkspace();
			if(projects == null)
				projects = wsp.getRoot().getProjects();
			final ICProjectDescription dess[] = new ICProjectDescription[projects.length];
			int num = 0;
			for (IProject project : projects) {
				ICProjectDescription des = getProjectDescription(project, false, true);
				if(des != null)
					dess[num++] = des;
			}

			if(num != 0){
				final int[] fi = new int[1];
				fi[0] = num;
				runWspModification(new IWorkspaceRunnable(){

					public void run(IProgressMonitor monitor) throws CoreException {
						monitor.beginTask(SettingsModelMessages.getString("CProjectDescriptionManager.13"), fi[0]); //$NON-NLS-1$

						for (ICProjectDescription des : dess) {
							if(des == null)
								break;
							IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
							try {
								setProjectDescription(des.getProject(), des, true, subMonitor);
							} catch (CoreException e){
								CCorePlugin.log(e);
							} finally {
								subMonitor.done();
							}
						}
					}
				}, monitor);

			}
		} finally {
			monitor.done();
		}

	}

	public ICProjectConverter getConverter(IProject project, String oldOwnerId, ICProjectDescription des){
		CProjectConverterDesciptor[] converterDess = getConverterDescriptors();
		ICProjectConverter converter = null;
		for (CProjectConverterDesciptor converterDes : converterDess) {
			if(converterDes.canConvertProject(project, oldOwnerId, des)){
				try {
					converter = converterDes.getConverter();
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
		CProjectConverterDesciptor[] dess = new CProjectConverterDesciptor[exts.length];

		for(int i = 0; i < exts.length; i++){
			dess[i] = new CProjectConverterDesciptor(exts[i]);
		}

		fConverters = dess;
	}

	public ICProjectDescription createProjectDescription(IProject project, boolean loadIfExists) throws CoreException{
		return createProjectDescription(project, loadIfExists, false);
	}

	public ICProjectDescription createProjectDescription(IProject project, boolean loadIfExists, boolean creating) throws CoreException{
		int flags = ICProjectDescriptionManager.GET_WRITABLE | ICProjectDescriptionManager.GET_CREATE_DESCRIPTION;
		flags |= loadIfExists ? 0 : ICProjectDescriptionManager.GET_EMPTY_PROJECT_DESCRIPTION;
		flags |= creating ? ICProjectDescriptionManager.PROJECT_CREATING : 0;

		return getProjectDescriptionInternal(project, flags);
	}

	public ScannerInfoProviderProxy getScannerInfoProviderProxy(IProject project){
		ICProjectDescription des = getProjectDescription(project, false);
		if(des == null){
			return new ScannerInfoProviderProxy(project);
		}

		ScannerInfoProviderProxy provider = (ScannerInfoProviderProxy)des.getSessionProperty(SCANNER_INFO_PROVIDER_PROPERTY);
		if(provider == null){
			provider = new ScannerInfoProviderProxy(project);
			des.setSessionProperty(SCANNER_INFO_PROVIDER_PROPERTY, provider);
		} else {
			provider.updateProject(project);
		}

		return provider;
	}

	public ICProjectDescription getProjectDescription(IProject project){
		return getProjectDescription(project, true);
	}

	/*
	 * returns true if the project description was modified false - otherwise
	 */
	public boolean checkHandleActiveCfgChange(CProjectDescription newDes, ICProjectDescription oldDes, IProjectDescription eDes, IProgressMonitor monitor){
		if(newDes == null)
			return false;
		ICConfigurationDescription newCfg = newDes.getActiveConfiguration();
		if(newCfg == null)
			return false;

		ICConfigurationDescription oldCfg = oldDes != null ? oldDes.getActiveConfiguration() : null;

		checkActiveCfgChange(newDes, oldDes);
		checkSettingCfgChange(newDes, oldDes);

		boolean modified = false;

		try {
			if(checkBuildSystemChange(eDes, newCfg, oldCfg, monitor))
				modified = true;
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}

		try {
			if(checkProjectRefChange(eDes, newDes, newCfg, oldCfg, monitor))
				modified = true;
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}

		return modified;
	}

//	String loadActiveCfgId(ICProjectDescription des){
//		try {
//			return des.getProject().getPersistentProperty(ACTIVE_CFG_PROPERTY);
//		} catch (CoreException e) {
//			CCorePlugin.log(e);
//		}
//		return null;
//	}

	private Collection<IProject> projSetFromProjNameSet(Collection<String> projNames){
		if(projNames.size() == 0)
			return new HashSet<IProject>(0);

		Set<IProject> set = new LinkedHashSet<IProject>();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		for (String sproj : projNames)
			set.add(root.getProject(sproj));

		return set;
	}

	/**
	 * Fix up platform references having changed CDT configuration references
	 */
	@SuppressWarnings("unchecked")
	private boolean checkProjectRefChange(IProjectDescription des, ICProjectDescription newCDesc, ICConfigurationDescription newCfg, ICConfigurationDescription oldCfg, IProgressMonitor monitor) throws CoreException{
		if(newCfg == null)
			return false;

		Map<String, String> oldMap = oldCfg != null ? oldCfg.getReferenceInfo() : Collections.EMPTY_MAP;
		Map<String, String> newMap = newCfg.getReferenceInfo();

		// If there's been no change nothing to do
		if (newMap.equals(oldMap))
			return false;

		// We're still looking at the same configuration - any refs removed?
		HashSet<String> removedRefs = new HashSet<String>();
		if (oldCfg != null && oldCfg.getId().equals(newCfg.getId())) {
			removedRefs.addAll(oldMap.keySet());
			removedRefs.removeAll(newMap.keySet());
		}

		// Get the full set of references from all configuration
		LinkedHashSet<String> allRefs = new LinkedHashSet<String>();
		for (ICConfigurationDescription cfg : newCDesc.getConfigurations())
			allRefs.addAll(cfg.getReferenceInfo().keySet());

		// Don't remove a reference if it's referenced by any configuration in the project description
		removedRefs.removeAll(allRefs);

		Collection<IProject> oldProjects = new LinkedHashSet<IProject>(Arrays.asList(des.getReferencedProjects()));
		Collection<IProject> newProjects = projSetFromProjNameSet(allRefs);

		// If there are no changes, just return
		if (removedRefs.isEmpty() && oldProjects.containsAll(newProjects))
			return false;

		// Ensure the Eclipse configuration references all projects we reference
		oldProjects.addAll(newProjects);
		// Removing any projects we no longer referece
		oldProjects.removeAll(projSetFromProjNameSet(removedRefs));

		des.setReferencedProjects(oldProjects.toArray(new IProject[oldProjects.size()]));
		return true;
	}


//	private void checkBuildSystemChange(IProject project, String newBsId, String oldBsId, IProgressMonitor monitor) throws CoreException{
//		checkBuildSystemChange(project, null, newBsId, oldBsId, monitor);
//	}

	private boolean checkActiveCfgChange(CProjectDescription des,
			ICProjectDescription oldDes
//			ICConfigurationDescription newCfg,
//			ICConfigurationDescription oldCfg
			){
		ICConfigurationDescription oldCfg = oldDes != null ? oldDes.getActiveConfiguration() : null;
//		String newId = newCfg.getId();
		String oldId = oldCfg != null ? oldCfg.getId() : null;


		return des.checkPersistActiveCfg(oldId, false);
	}

	private boolean checkSettingCfgChange(CProjectDescription des,
			ICProjectDescription oldDes
//			ICConfigurationDescription newCfg,
//			ICConfigurationDescription oldCfg
			){
		ICConfigurationDescription oldCfg = oldDes != null ? oldDes.getDefaultSettingConfiguration() : null;
//		String newId = newCfg.getId();
		String oldId = oldCfg != null ? oldCfg.getId() : null;


		return des.checkPersistSettingCfg(oldId, false);
	}

	private boolean checkBuildSystemChange(IProjectDescription des,
			ICConfigurationDescription newCfg,
			ICConfigurationDescription oldCfg,
			IProgressMonitor monitor) throws CoreException{
		String newBsId = newCfg != null ? newCfg.getBuildSystemId() : null;
		String oldBsId = oldCfg != null ? oldCfg.getBuildSystemId() : null;

		CConfigurationDataProviderDescriptor newDr = newBsId != null ? getCfgProviderDescriptor(newBsId) : null;
		CConfigurationDataProviderDescriptor oldDr = oldBsId != null ? getCfgProviderDescriptor(oldBsId) : null;

		List<String> newNatures, oldNatures, conflictingNatures;
		newNatures = oldNatures = conflictingNatures = Collections.emptyList();
		if (oldDr != null)
			oldNatures = Arrays.asList(oldDr.getNatureIds());
		if (newDr != null) {
			newNatures = Arrays.asList(newDr.getNatureIds());
			conflictingNatures = Arrays.asList(newDr.getConflictingNatureIds());
		}

		// List of existing natureIds
		final String[] natureIds = des.getNatureIds();

		// Get the set of items to remove ({oldNatures} - {newNatures}) + conflictingNatures
		Set<String> toRemove = new HashSet<String>(oldNatures);
		toRemove.removeAll(newNatures); 		// Don't remove items we're re-adding
		toRemove.addAll(conflictingNatures);	// Add conflicting natures for removal
		// Modify an ordered set of the existing natures with the changes
		final LinkedHashSet<String> cur = new LinkedHashSet<String>(Arrays.asList(natureIds));
		cur.addAll(newNatures);
		cur.removeAll(toRemove);

		final String[] newNatureIds = cur.toArray(new String[cur.size()]);
		if (!Arrays.equals(newNatureIds, natureIds)) {
			des.setNatureIds(newNatureIds);
			return true;
		}

		return false;
	}

	public void setProjectDescription(IProject project, ICProjectDescription des) throws CoreException {
		setProjectDescription(project, des, false, null);
	}

	public void setProjectDescription(IProject project, ICProjectDescription des, boolean force, IProgressMonitor monitor) throws CoreException {
		int flags = force ? SET_FORCE : 0;
		setProjectDescription(project, des, flags, monitor);
	}

	static boolean checkFlags(int flags, int check){
		return (flags & check) == check;
	}

	/** ThreadLocal flag to let CDescriptor know whether already in a setProjectDescription */
	ThreadLocal<Boolean> settingProjectDescription = new ThreadLocal<Boolean>(){@Override protected Boolean initialValue() {return false;}};
	public void setProjectDescription(IProject project, ICProjectDescription des, int flags, IProgressMonitor monitor) throws CoreException {
		try {
			settingProjectDescription.set(true);
			if(des != null){
				if (!project.isAccessible())
					throw ExceptionFactory.createCoreException(MessageFormat.format(CCorePlugin.getResourceString("ProjectDescription.ProjectNotAccessible"), new Object[] {project.getName()})); //$NON-NLS-1$

				if(!des.isValid() && (!fAllowEmptyCreatingDescription || !des.isCdtProjectCreating()))
					throw ExceptionFactory.createCoreException(SettingsModelMessages.getString("CProjectDescriptionManager.17") + project.getName()); //$NON-NLS-1$

				if(!checkFlags(flags, SET_FORCE) && !des.isModified())
					return;

				if(((CProjectDescription)des).isLoading()){
					throw ExceptionFactory.createCoreException("description is being loadded"); //$NON-NLS-1$
				}

				if(((CProjectDescription)des).isApplying()){
					throw ExceptionFactory.createCoreException("description is being applied"); //$NON-NLS-1$
				}
			}
			CProjectDescriptionStorageManager.getInstance().setProjectDescription(project, des, flags, monitor);
		} finally {
			settingProjectDescription.set(false);
		}
	}

	/**
	 * Indicates that a setProjectDescription is currently in progress to prevent recursive setProjectDescription
	 * @return boolean
	 */
	public boolean isCurrentThreadSetProjectDescription() {
		return settingProjectDescription.get();
	}

	/**
	 * Base for getting a project desc's storage. project must be accessible.
	 * @param project
	 * @return ProjectDescription storage
	 * @throws CoreException if Project isn't accessible
	 */
	private AbstractCProjectDescriptionStorage getProjectDescriptionStorage(IProject project) throws CoreException {
		if (project == null || !project.isAccessible())
			throw ExceptionFactory.createCoreException(MessageFormat.format(CCorePlugin.getResourceString("ProjectDescription.ProjectNotAccessible"), new Object[] {project != null ? project.getName() : "<null>"})); //$NON-NLS-1$ //$NON-NLS-2$
		AbstractCProjectDescriptionStorage storage = CProjectDescriptionStorageManager.getInstance().getProjectDescriptionStorage(project);
		if (storage == null)
			throw ExceptionFactory.createCoreException(SettingsModelMessages.getString("CProjectDescriptionManager.FailedToGetStorage") + project.getName()); //$NON-NLS-1$
		return storage;
	}

	/**
	 * Return an ICSettingsStorage based on the provided ICStorageElement
	 * in the given IProject
	 * @param project
	 * @return ICSettingsStorages
	 */
	public ICSettingsStorage getStorageForElement(IProject project, ICStorageElement element) throws CoreException {
		if (project != null)
			return getProjectDescriptionStorage(project).getStorageForElement(element);
		// project is null means it's a preference element, uses XmlStorages
		return new XmlStorage((InternalXmlStorageElement)element);
	}

	private void serializePreference(String key, InternalXmlStorageElement element) throws CoreException{
		Document doc = element.fElement.getOwnerDocument();

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
			byte[] bytes;
			try {
				bytes = utfString.getBytes("UTF-8"); //$NON-NLS-1$
			} catch (UnsupportedEncodingException e){
				bytes = utfString.getBytes();
			}
			fileStream.write(bytes);
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

	public ICLanguageSetting findLanguageSettingForContentTypeId(String id, ICLanguageSetting settings[]/*, boolean src*/){
		for (ICLanguageSetting setting : settings) {
			String ids[] = setting.getSourceContentTypeIds();
			if(ListComparator.indexOf(id, ids) != -1)
				return setting;
		}
		return null;
	}

	public ICLanguageSetting[] findCompatibleSettingsForContentTypeId(String id, ICLanguageSetting[] settings/*, boolean src*/){
		IContentTypeManager manager = Platform.getContentTypeManager();
		IContentType cType = manager.getContentType(id);
		if(cType != null){
			String [] exts = cType.getFileSpecs(IContentType.FILE_EXTENSION_SPEC);
			if(exts != null && exts.length != 0){
				List<ICLanguageSetting> list = new ArrayList<ICLanguageSetting>();
				ICLanguageSetting setting;
				for (String ext : exts) {
					setting = findLanguageSettingForExtension(ext, settings/*, src*/);
					if(setting != null)
						list.add(setting);
				}
				return list.toArray(new ICLanguageSetting[list.size()]);
			}
		}
		return EMPTY_LANGUAGE_SETTINGS_ARRAY;
	}

	public ICLanguageSetting findLanguageSettingForExtension(String ext, ICLanguageSetting settings[]/*, boolean src*/){
		for (ICLanguageSetting setting : settings) {
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
				for (String ex: exts) {
					if(ext.equals(ex))
						return setting;
				}
			}
		}
		return null;
	}

	/**
	 * Returns a map of configurations elements as discovered from the supplied project
	 * description
	 * @param des
	 * @return Map String -> ICStorageElement: configuration name -> configuration ICStorageElement
	 * @throws CoreException
	 */
	Map<String, ICStorageElement> createCfgStorages(ICProjectDescription des) throws CoreException{
		LinkedHashMap<String, ICStorageElement> map = new LinkedHashMap<String, ICStorageElement>();
		ICStorageElement rootElement = des.getStorage(MODULE_ID, false);
		if(rootElement != null){
			ICStorageElement children[] = rootElement.getChildren();

			for (ICStorageElement el : children) {
				if(CONFIGURATION.equals(el.getName())){
					String id = el.getAttribute(CConfigurationSpecSettings.ID);
					if(id != null)
						map.put(id, el);
				}
			}
		}
		return map;
	}

	/**
	 * Create the configuration storage cfgId in the project description
	 * @param storage the root settingsStorage of the project
	 * @param cfgId the configuration Id desire
	 * @return the cfgId as discovered in the project description or a new ICStorageElement with that Id
	 * @throws CoreException on failure to create storage
	 */
	ICStorageElement createStorage(ICSettingsStorage storage, String cfgId) throws CoreException  {
		ICStorageElement rootElement = storage.getStorage(MODULE_ID, true); // throws CoreException
		ICStorageElement children[] = rootElement.getChildren();
		ICStorageElement element = null;

		for (ICStorageElement el : children) {
			if(CONFIGURATION.equals(el.getName())
					&& cfgId.equals(el.getAttribute(CConfigurationSpecSettings.ID))){
				element = el;
				break;
			}
		}

		if(element == null){
			element = rootElement.createChild(CONFIGURATION);
			element.setAttribute(CConfigurationSpecSettings.ID, cfgId);
		}

		return element;
	}

	/**
	 * Creates a new configuration storage based on an existing 'base' storage.
	 * If a configuration with the new ID already exists in the passed in project storage
	 * a CoreException is thrown.
	 * @param storage the setting storage of the current project description
	 * @param cfgId configID of the new configuration - must be unique in
	 * @param base the base (spec settings) storage element from which settings should be copied.
	 * @return ICStorageElement representing the new configuration
	 * @throws CoreException on failure
	 */
	ICStorageElement createStorage(ICSettingsStorage storage, String cfgId, ICStorageElement base) throws CoreException{
		ICStorageElement rootElement = storage.getStorage(MODULE_ID, true);
		ICStorageElement children[] = rootElement.getChildren();
		for (ICStorageElement child : children) {
			if(CONFIGURATION.equals(child.getName())
					&& cfgId.equals(child.getAttribute(CConfigurationSpecSettings.ID)))
				throw ExceptionFactory.createCoreException(MessageFormat
						.format(SettingsModelMessages.getString("CProjectDescriptionManager.cfgIDAlreadyExists"), //$NON-NLS-1$
								new Object[] {cfgId}));
		}
		ICStorageElement config = rootElement.importChild(base);
		config.setAttribute(CConfigurationSpecSettings.ID, cfgId);
		return config;
	}

	/**
	 * Remove the storage with the supplied configuration Id from the project
	 * @param storage The root settingsStorage of the project
	 * @param cfgId the configuration ID which would be
	 * @throws CoreException
	 */
	void removeStorage(ICSettingsStorage storage, String cfgId) throws CoreException{
		ICStorageElement rootElement = storage.getStorage(MODULE_ID, false);
		if(rootElement != null){
			ICStorageElement children[] = rootElement.getChildren();

			for (ICStorageElement el: children) {
				if(CONFIGURATION.equals(el.getName())
						&& cfgId.equals(el.getAttribute(CConfigurationSpecSettings.ID))){
					rootElement.removeChild(el);
					break;
				}
			}
		}
	}

	CConfigurationData loadData(ICConfigurationDescription des, IProgressMonitor monitor) throws CoreException{
		if(monitor == null)
			monitor = new NullProgressMonitor();

		CConfigurationDataProvider provider = getProvider(des);
		return provider.loadConfiguration(des, monitor);
	}

	CConfigurationData applyData(CConfigurationDescriptionCache des, ICConfigurationDescription baseDescription, CConfigurationData base, SettingsContext context, IProgressMonitor monitor) throws CoreException {
		if(monitor == null)
			monitor = new NullProgressMonitor();

		CConfigurationDataProvider provider = getProvider(des);
		context.init(des);
		return provider.applyConfiguration(des, baseDescription, base, context, monitor);
	}

	void notifyCached(ICConfigurationDescription des, CConfigurationData data, IProgressMonitor monitor) {
		if(monitor == null)
			monitor = new NullProgressMonitor();

		try {
			CConfigurationDataProvider provider = getProvider(des);
			provider.dataCached(des, data, monitor);
		} catch (CoreException e){
			CCorePlugin.log(e);
		}
	}

	void removeData(ICConfigurationDescription des, CConfigurationData data, IProgressMonitor monitor) throws CoreException{
		if(monitor == null)
			monitor = new NullProgressMonitor();

		CConfigurationDataProvider provider = getProvider(des);
		provider.removeConfiguration(des, data, monitor);
	}

	CConfigurationData createData(ICConfigurationDescription des, ICConfigurationDescription baseDescription, CConfigurationData base, boolean clone, IProgressMonitor monitor) throws CoreException{
		if(monitor == null)
			monitor = new NullProgressMonitor();

		CConfigurationDataProvider provider = getProvider(des);
		return provider.createConfiguration(des, baseDescription, base, clone, monitor);
	}

	private CConfigurationDataProvider getProvider(ICConfigurationDescription des) throws CoreException{
		CConfigurationDataProviderDescriptor providerDes = getCfgProviderDescriptor(des);
		if(providerDes == null)
			throw ExceptionFactory.createCoreException(SettingsModelMessages.getString("CProjectDescriptionManager.1")); //$NON-NLS-1$

		return providerDes.getProvider();
	}

	private CConfigurationDataProviderDescriptor getCfgProviderDescriptor(ICConfigurationDescription des){
		return getCfgProviderDescriptor(des.getBuildSystemId());
	}

	private CConfigurationDataProviderDescriptor getCfgProviderDescriptor(String id){
		initProviderInfo();
		return fProviderMap.get(id);
	}

	private void initProviderInfo(){
		if(fProviderMap != null)
			return;

		synchronized (this) {
			if(fProviderMap != null)
				return;

			IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(CConfigurationDataProviderDescriptor.DATA_PROVIDER_EXTPOINT_ID);
			IExtension exts[] = extensionPoint.getExtensions();
			fProviderMap = new HashMap<String, CConfigurationDataProviderDescriptor>(exts.length);

			for (IExtension ext : exts) {
				CConfigurationDataProviderDescriptor des = new CConfigurationDataProviderDescriptor(ext);
				fProviderMap.put(des.getId(), des);
			}
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
							throw ExceptionFactory.createCoreException(SettingsModelMessages.getString("CProjectDescriptionManager.2")); //$NON-NLS-1$
						} else {
							String fileVersion = rootElement.getNodeValue();
							Version version = new Version(fileVersion);
							// Make sure that the version is compatible with the manager
							// Version must between min version and current version inclusive
							if (MIN_DESCRIPTION_VERSION.compareTo(version) > 0 || DESCRIPTION_VERSION.compareTo(version) < 0) {
								throw ExceptionFactory.createCoreException(SettingsModelMessages.getString("CProjectDescriptionManager.3")); //$NON-NLS-1$
							}
						}

						// Now get the project root element (there should be only one)
						NodeList nodes = doc.getElementsByTagName(ROOT_PREFERENCE_ELEMENT);
						if (nodes.getLength() == 0)
							throw ExceptionFactory.createCoreException(SettingsModelMessages.getString("CProjectDescriptionManager.4")); //$NON-NLS-1$
						Node node = nodes.item(0);
						if(node.getNodeType() != Node.ELEMENT_NODE)
							throw ExceptionFactory.createCoreException(SettingsModelMessages.getString("CProjectDescriptionManager.5")); //$NON-NLS-1$
						element = (Element)node;
					} else if(!createEmptyIfNotFound){
						throw ExceptionFactory.createCoreException(SettingsModelMessages.getString("CProjectDescriptionManager.6")); //$NON-NLS-1$
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

	public static File toLocalFile(URI uri, IProgressMonitor monitor) throws CoreException {
		IFileStore fileStore = EFS.getStore(uri);
		File localFile = fileStore.toLocalFile(EFS.NONE, monitor);
		if (localFile ==null)
			// non local file system
			localFile= fileStore.toLocalFile(EFS.CACHE, monitor);
		return localFile;
	}

	ICDescriptionDelta createDelta(ICProjectDescription newDescription, ICProjectDescription oldDescription){
		CProjectDescriptionDelta delta = new CProjectDescriptionDelta(newDescription, oldDescription);

		if(delta.getDeltaKind() == ICDescriptionDelta.CHANGED){
			ICConfigurationDescription[] cfgs = newDescription.getConfigurations();
			for (ICConfigurationDescription cfg : cfgs) {
				ICConfigurationDescription oldCfg = oldDescription.getConfigurationById(cfg.getId());
				CProjectDescriptionDelta cfgDelta = createDelta(cfg, oldCfg);
				if(cfgDelta != null){
					delta.addChild(cfgDelta);
				}
			}

			cfgs = oldDescription.getConfigurations();
			for (ICConfigurationDescription cfg : cfgs) {
				ICConfigurationDescription newCfg = newDescription.getConfigurationById(cfg.getId());
				if(newCfg == null)
					delta.addChild(createDelta(null, cfg));
			}

			if(checkCfgChange(newDescription, oldDescription, true))
				delta.addChangeFlags(ICDescriptionDelta.ACTIVE_CFG);

			if(checkCfgChange(newDescription, oldDescription, false))
				delta.addChangeFlags(ICDescriptionDelta.INDEX_CFG);

			if(oldDescription.isCdtProjectCreating() && !newDescription.isCdtProjectCreating())
				delta.addChangeFlags(ICDescriptionDelta.PROJECT_CREAION_COMPLETED);
		}
		return delta.isEmpty() ? null : delta;
	}

	private boolean checkCfgChange(ICProjectDescription newDes, ICProjectDescription oldDes, boolean active){
		ICConfigurationDescription newCfg, oldCfg;

		if(active){
			newCfg = newDes.getActiveConfiguration();
			oldCfg = oldDes.getActiveConfiguration();
		} else {
			newCfg = newDes.getDefaultSettingConfiguration();
			oldCfg = oldDes.getDefaultSettingConfiguration();
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
		for (ICDescriptionDelta child : children) {
			obj = child.getSetting();
			if(obj.getId().equals(id))
				return child;
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

			Map<String, CConfigExtensionReference[]> newMap = newSettings.getExtensionMapCopy();
			Map<String, CConfigExtensionReference[]> oldMap = oldSettings.getExtensionMapCopy();

			Iterator<Map.Entry<String, CConfigExtensionReference[]>> iter = newMap.entrySet().iterator();
			while(iter.hasNext()) {
				Map.Entry<String, CConfigExtensionReference[]> entry = iter.next();
				iter.remove();
				CConfigExtensionReference[] oldRefs = oldMap.remove(entry.getKey());
				if(oldRefs == null){
					flags |= ICDescriptionDelta.EXT_REF;
					break;
				}

				CConfigExtensionReference[] newRefs = entry.getValue();
				if(newRefs.length != oldRefs.length){
					flags |= ICDescriptionDelta.EXT_REF;
					break;
				}

				Set<CConfigExtensionReference> newSet = new HashSet<CConfigExtensionReference>(Arrays.asList(newRefs));
				Set<CConfigExtensionReference> oldSet = new HashSet<CConfigExtensionReference>(Arrays.asList(oldRefs));
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
		IInternalCCfgInfo newInfo = (IInternalCCfgInfo)newCfg;
		IInternalCCfgInfo oldInfo = (IInternalCCfgInfo)oldCfg;
		if(delta.getDeltaKind() == ICDescriptionDelta.CHANGED){
			ICFolderDescription[] foDess = newCfg.getFolderDescriptions();
			for (ICFolderDescription foDes : foDess) {
				ICResourceDescription oldDes = oldCfg.getResourceDescription(foDes.getPath(), true);
				if(oldDes != null && oldDes.getType() == ICSettingBase.SETTING_FOLDER){
					CProjectDescriptionDelta foDelta = createDelta(foDes, (ICFolderDescription)oldDes);
					if(foDelta != null)
						delta.addChild(foDelta);
				} else {
					delta.addChild(createDelta(foDes, null));
				}
			}

			foDess = oldCfg.getFolderDescriptions();
			for (ICFolderDescription foDes : foDess) {
				ICResourceDescription newDes = newCfg.getResourceDescription(foDes.getPath(), true);
				if(newDes == null || newDes.getType() != ICSettingBase.SETTING_FOLDER){
					delta.addChild(createDelta(null, foDes));
				}
			}

			ICFileDescription[] fiDess = newCfg.getFileDescriptions();
			for (ICFileDescription fiDes : fiDess) {
				ICResourceDescription oldDes = oldCfg.getResourceDescription(fiDes.getPath(), true);
				if(oldDes != null && oldDes.getType() == ICSettingBase.SETTING_FILE){
					CProjectDescriptionDelta fiDelta = createDelta(fiDes, (ICFileDescription)oldDes);
					if(fiDelta != null)
						delta.addChild(fiDelta);
				} else {
					delta.addChild(createDelta(fiDes, null));
				}
			}

			fiDess = oldCfg.getFileDescriptions();
			for (ICFileDescription fiDes : fiDess) {
				ICResourceDescription newDes = newCfg.getResourceDescription(fiDes.getPath(), true);
				if(newDes == null || newDes.getType() != ICSettingBase.SETTING_FILE){
					delta.addChild(createDelta(null, fiDes));
				}
			}

			CProjectDescriptionDelta bsDelta = createDelta(newCfg.getBuildSetting(), oldCfg.getBuildSetting());
			if(bsDelta != null)
				delta.addChild(bsDelta);

			CProjectDescriptionDelta tpsDelta = createDelta(newCfg.getTargetPlatformSetting(), oldCfg.getTargetPlatformSetting());
			if(tpsDelta != null)
				delta.addChild(tpsDelta);

			if(!newCfg.getName().equals(oldCfg.getName())){
				delta.addChangeFlags(ICDescriptionDelta.NAME);
			}

			if (!CDataUtil.objectsEqual(newCfg.getDescription(), oldCfg.getDescription())) {
				delta.addChangeFlags(ICDescriptionDelta.DESCRIPTION);
			}

			ICSourceEntry newEntries[] = newCfg.getSourceEntries();
			ICSourceEntry oldEntries[] = oldCfg.getSourceEntries();

			if(newEntries.length > oldEntries.length){
				delta.addChangeFlags(ICDescriptionDelta.SOURCE_ADDED);
			} else {
				for (ICSourceEntry newEntry : newEntries) {
					boolean found = false;
					for (ICSourceEntry oldEntry : oldEntries) {
						if(newEntry.equals(oldEntry)){
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
				for (ICSourceEntry oldEntry : oldEntries) {
					boolean found = false;
					for (ICSourceEntry newEntry : newEntries) {
						if(oldEntry.equals(newEntry)){
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

			try {
				CConfigurationSpecSettings newSettings = newInfo.getSpecSettings();
				CConfigurationSpecSettings oldSettings = oldInfo.getSpecSettings();
				if(!newSettings.extRefSettingsEqual(oldSettings))
					delta.addChangeFlags(ICDescriptionDelta.EXT_REF);
			} catch (CoreException e){
				CCorePlugin.log(e);
			}


			calculateCfgExtSettingsDelta(delta);

			int drFlags = calculateDescriptorFlags(newCfg, oldCfg);
			if(drFlags != 0)
				delta.addChangeFlags(drFlags);
		}

		return delta.isEmpty() ? null : delta;
	}

	private void calculateCfgExtSettingsDelta(CProjectDescriptionDelta delta){
		ICConfigurationDescription newDes = (ICConfigurationDescription)delta.getNewSetting();
		ICConfigurationDescription oldDes = (ICConfigurationDescription)delta.getOldSetting();
		ExtSettingsDelta[] deltas = getSettingChange(newDes, oldDes);
		int flags = 0;
		int addedRemoved = ICDescriptionDelta.EXTERNAL_SETTINGS_ADDED | ICDescriptionDelta.EXTERNAL_SETTINGS_REMOVED;
		if(deltas != null ){
			for (ExtSettingsDelta dt : deltas) {
				ICSettingEntry[][] d = dt.getEntriesDelta();
				if(d[0] != null)
					flags |= ICDescriptionDelta.EXTERNAL_SETTINGS_ADDED;
				if(d[1] != null)
					flags |= ICDescriptionDelta.EXTERNAL_SETTINGS_REMOVED;

				if((flags & (addedRemoved)) == addedRemoved)
					break;
			}
//			delta.setExtSettingsDeltas(deltas);
			if(flags != 0)
				delta.addChangeFlags(flags);
		}

		int cfgRefFlags = calcRefChangeFlags(newDes, oldDes);
		if(cfgRefFlags != 0)
			delta.addChangeFlags(cfgRefFlags);
	}

	private int calcRefChangeFlags(ICConfigurationDescription newDes, ICConfigurationDescription oldDes){
		Map<String, String> newMap = newDes != null ? newDes.getReferenceInfo() : null;
		Map<String, String> oldMap = oldDes != null ? oldDes.getReferenceInfo() : null;

		int flags = 0;
		if(newMap == null || newMap.size() == 0){
			if(oldMap != null && oldMap.size() != 0){
				flags = ICDescriptionDelta.CFG_REF_REMOVED;
			}
		} else {
			if(oldMap == null || oldMap.size() == 0){
				flags = ICDescriptionDelta.CFG_REF_ADDED;
			} else {
				boolean stop = false;
				for (Map.Entry<String, String> newEntry : newMap.entrySet()) {
					String newProj = newEntry.getKey();
					String newCfg = newEntry.getValue();
					String oldCfg = oldMap.remove(newProj);
					if(!newCfg.equals(oldCfg)){
						flags |= ICDescriptionDelta.CFG_REF_ADDED;
						if(oldCfg != null){
							flags |= ICDescriptionDelta.CFG_REF_REMOVED;
							stop = true;
						}
						if(stop)
							break;
					}
				}

				if(!oldMap.isEmpty())
					flags |= ICDescriptionDelta.CFG_REF_REMOVED;
			}
		}

		return flags;
	}


	private ExtSettingsDelta[] getSettingChange(ICConfigurationDescription newDes, ICConfigurationDescription oldDes){
		CExternalSetting[] newSettings = newDes != null ? (CExternalSetting[])newDes.getExternalSettings() : null;
		CExternalSetting[] oldSettings = oldDes != null ? (CExternalSetting[])oldDes.getExternalSettings() : null;
		return CExternalSettinsDeltaCalculator.getInstance().getSettingChange(newSettings, oldSettings);
	}

	private CProjectDescriptionDelta createDelta(ICFolderDescription newFo, ICFolderDescription oldFo){
		CProjectDescriptionDelta delta = new CProjectDescriptionDelta(newFo, oldFo);

		if(delta.getDeltaKind() == ICDescriptionDelta.CHANGED){
			ICLanguageSetting newLss[] = newFo.getLanguageSettings();
			ICLanguageSetting oldLss[] = oldFo.getLanguageSettings();
			List<ICLanguageSetting> newList = new ArrayList<ICLanguageSetting>(Arrays.asList(newLss));
			List<ICLanguageSetting> oldList = new ArrayList<ICLanguageSetting>(Arrays.asList(oldLss));
			List<ICLanguageSetting[]> matched = sortSettings(newList, oldList);

			for (ICLanguageSetting[] match : matched) {
				CProjectDescriptionDelta lsDelta = createDelta(match[0], match[1]);
				if(lsDelta != null)
					delta.addChild(lsDelta);
			}

			for (ICLanguageSetting added : newList) {
				delta.addChild(createDelta(added, null));
			}

			for (ICLanguageSetting removed : oldList) {
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

	private List<ICLanguageSetting[]> sortSettings(List<ICLanguageSetting> settings1,
			List<ICLanguageSetting> settings2){
		ICLanguageSetting setting1;
		ICLanguageSetting setting2;
		List<ICLanguageSetting[]> result = new ArrayList<ICLanguageSetting[]>();
		for(Iterator<ICLanguageSetting> iter1 = settings1.iterator(); iter1.hasNext();){
			setting1 = iter1.next();
			for(Iterator<ICLanguageSetting> iter2 = settings2.iterator(); iter2.hasNext();){
				setting2 = iter2.next();

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

		for(Iterator<ICLanguageSetting> iter1 = settings1.iterator(); iter1.hasNext();){
			setting1 = iter1.next();
			String lId = setting1.getLanguageId();
			if(lId != null){
				for(Iterator<ICLanguageSetting> iter2 = settings2.iterator(); iter2.hasNext();){
					setting2 = iter2.next();

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

		for(Iterator<ICLanguageSetting> iter1 = settings1.iterator(); iter1.hasNext();){
			setting1 = iter1.next();
			String cTypeIds1[] = setting1.getSourceContentTypeIds();
			if(cTypeIds1.length != 0){
				for(Iterator<ICLanguageSetting> iter2 = settings2.iterator(); iter2.hasNext();){
					setting2 = iter2.next();
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

		for(Iterator<ICLanguageSetting> iter1 = settings1.iterator(); iter1.hasNext();){
			setting1 = iter1.next();
			if(setting1.getSourceContentTypeIds().length == 0){
				String srcExts[]  = setting1.getSourceExtensions();
				if(srcExts.length != 0){
					for(Iterator<ICLanguageSetting> iter2 = settings2.iterator(); iter2.hasNext();){
						setting2 = iter2.next();
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
			if (!CDataUtil.objectsEqual(newLs.getLanguageId(), oldLs.getLanguageId()))
				delta.addChangeFlags(ICDescriptionDelta.LANGUAGE_ID);

			int kinds[] = KindBasedStore.getLanguageEntryKinds();
			int addedKinds = 0;
			int removedKinds = 0;
			int reorderedKinds = 0;
			for (int kind : kinds) {
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

		Set<ICLanguageSettingEntry> newEntrySet = new HashSet<ICLanguageSettingEntry>(Arrays.asList(newEntries));
		Set<ICLanguageSettingEntry> oldEntrySet = new HashSet<ICLanguageSettingEntry>(Arrays.asList(oldEntries));

		// Check the removed entries.
		for (ICLanguageSettingEntry oldEntry : oldEntries) {
			boolean found = false;
			if (newEntrySet.contains(oldEntry)) {
				found = true;
				break;
			}
			if(!found){
				result[1] = true;
				break;
			}
		}

		// Check the new entries.
		for (ICLanguageSettingEntry newEntry : newEntries) {
			boolean found = false;
			if (oldEntrySet.contains(newEntry)) {
				found = true;
				break;
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
	private CProjectDescriptionDelta createDelta(ICBuildSetting  newBuildSetting, ICBuildSetting  oldBuildSetting){
		CProjectDescriptionDelta delta = new CProjectDescriptionDelta(newBuildSetting, oldBuildSetting);
		if(!Arrays.equals(newBuildSetting.getErrorParserIDs(), oldBuildSetting.getErrorParserIDs()))
			delta.addChangeFlags(ICDescriptionDelta.ERROR_PARSER_IDS);

		return delta.isEmpty() ? null : delta;
	}

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
			ICProjectDescription newDes = (ICProjectDescription)projDesDelta.getNewSetting();
			ICProjectDescription oldDes = (ICProjectDescription)projDesDelta.getOldSetting();
//			int flags = projDesDelta.getChangeFlags();
//			ICConfigurationDescription activeCfg = newDes.getActiveConfiguration();
			ICConfigurationDescription indexCfg = newDes.getDefaultSettingConfiguration();
//			if(indexCfg != activeCfg){
//				ICDescriptionDelta delta = findDelta(activeCfg.getId(), projDesDelta);
//				if(delta != null && delta.getDeltaKind() == ICDescriptionDelta.CHANGED){
//					indexCfg = activeCfg;
//					newDes.setIndexConfiguration(activeCfg);
//				}
//			}
			ICConfigurationDescription oldIndexCfg = oldDes.getDefaultSettingConfiguration();
			ICDescriptionDelta indexDelta;
			if(oldIndexCfg != null && oldIndexCfg.getId().equals(indexCfg.getId())){
				indexDelta = findDelta(indexCfg.getId(), projDesDelta);
			} else {
				indexDelta = createDelta(indexCfg, oldIndexCfg);
			}
			if(indexDelta != null){
				List<CElementDelta> list = new ArrayList<CElementDelta>();
				generateCElementDeltasFromCfgDelta(cProject, indexDelta, list);
				return list.toArray(new ICElementDelta[list.size()]);
			}
			return EMPTY_CELEMENT_DELTA;
		case ICDescriptionDelta.ADDED:
		case ICDescriptionDelta.REMOVED:
			break;
		}
		return EMPTY_CELEMENT_DELTA;
	}

	private List<CElementDelta> generateCElementDeltasFromCfgDelta(ICProject cProject, ICDescriptionDelta cfgDelta, List<CElementDelta> list){
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
			int type;
			for (ICDescriptionDelta child : children) {
				type = child.getSettingType();
				if(type == ICSettingBase.SETTING_FILE || type == ICSettingBase.SETTING_FOLDER){
					generateCElementDeltasFromResourceDelta(cProject, child, list);
				}
			}
			break;
		case ICDescriptionDelta.ADDED:
		case ICDescriptionDelta.REMOVED:
			break;
		}
		return list;
	}

	/**
	 * The method maps {@link ICDescriptionDelta} to {@link CElementDelta} which are added to the {@code list}.
	 * The delta will indicate modification of CElement for a given resource plus language settings
	 * if they changed (relative to parent resource description if the resource has no its own).
	 */
	private void generateCElementDeltasFromResourceDelta(ICProject cProject, ICDescriptionDelta delta, List<CElementDelta> list){
		int kind = delta.getDeltaKind();
		ICDescriptionDelta parentDelta = delta.getParent();

		ICResourceDescription oldRcDes;
		ICResourceDescription newRcDes;
		IPath path;
		switch(kind){
		case ICDescriptionDelta.REMOVED:
			oldRcDes = (ICResourceDescription)delta.getOldSetting();
			path = oldRcDes.getPath();
			newRcDes = ((ICConfigurationDescription)parentDelta.getNewSetting()).getResourceDescription(path, false);
			break;
		case ICDescriptionDelta.ADDED:
			newRcDes = (ICResourceDescription)delta.getNewSetting();
			path = newRcDes.getPath();
			oldRcDes = ((ICConfigurationDescription)parentDelta.getOldSetting()).getResourceDescription(path, false);
			break;
		case ICDescriptionDelta.CHANGED:
			newRcDes = (ICResourceDescription)delta.getNewSetting();
			path = newRcDes.getPath();
			oldRcDes = (ICResourceDescription)delta.getOldSetting();
			break;
		default:
			// Not possible
			CCorePlugin.log(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, SettingsModelMessages.getString("CProjectDescriptionManager.illegalDeltaKind")+kind)); //$NON-NLS-1$
			return;
		}
		path = path.makeRelative();

		ICElement el = null;
		try {
			el = cProject.findElement(path);
		} catch (CModelException e) {
			return;
		}
		IResource rc = el.getResource();

		if(rc != null){
			CElementDelta ceRcDelta = new CElementDelta(el.getCModel());
			ceRcDelta.changed(el, ICElementDelta.F_MODIFIERS);
			list.add(ceRcDelta);

			if(rc.getType() == IResource.FILE){
				String fileName = path.lastSegment();
				ICLanguageSetting newLS = getLanguageSetting(newRcDes, fileName);
				ICLanguageSetting oldLS = getLanguageSetting(oldRcDes, fileName);
				ICDescriptionDelta ld = createDelta(newLS, oldLS);
				generateCElementDeltasFromLanguageDelta(el, ld, list);
			} else {
				if(newRcDes.getType() != ICSettingBase.SETTING_FOLDER){
					CCorePlugin.log(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, SettingsModelMessages.getString("CProjectDescriptionManager.wrongTypeOfResourceDescription")+newRcDes)); //$NON-NLS-1$
					return;
				}
				ICFolderDescription newFoDes = (ICFolderDescription)newRcDes;

				if(oldRcDes.getType() != ICSettingBase.SETTING_FOLDER){
					CCorePlugin.log(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, SettingsModelMessages.getString("CProjectDescriptionManager.wrongTypeOfResourceDescription")+oldRcDes)); //$NON-NLS-1$
					return;
				}
				ICFolderDescription oldFoDes = (ICFolderDescription)oldRcDes;

				ICDescriptionDelta folderDelta = createDelta(newFoDes, oldFoDes);
				if (folderDelta != null) {
					for (ICDescriptionDelta child : folderDelta.getChildren()) {
						if(child.getSettingType() == ICSettingBase.SETTING_LANGUAGE){
							generateCElementDeltasFromLanguageDelta(el, child, list);
						}
					}
				}

			}
		}
	}

	private ICLanguageSetting getLanguageSetting(ICResourceDescription rcDes, String fileName){
		if(rcDes.getType() == ICSettingBase.SETTING_FILE){
			return ((ICFileDescription)rcDes).getLanguageSetting();
		}
		return ((ICFolderDescription)rcDes).getLanguageSettingForFile(fileName);
	}

	private List<CElementDelta> generateCElementDeltasFromLanguageDelta(ICElement el, ICDescriptionDelta delta, List<CElementDelta> list){
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

		for (int element : kindsArray) {
			switch(element){
			case ICSettingEntry.INCLUDE_PATH:
				flags |= ICElementDelta.F_CHANGED_PATHENTRY_INCLUDE;
				break;
			case ICSettingEntry.INCLUDE_FILE:
				flags |= ICElementDelta.F_CHANGED_PATHENTRY_INCLUDE;
				break;
			case ICSettingEntry.MACRO:
				flags |= ICElementDelta.F_CHANGED_PATHENTRY_MACRO;
				break;
			case ICSettingEntry.MACRO_FILE:
				flags |= ICElementDelta.F_CHANGED_PATHENTRY_MACRO;
				break;
			case ICSettingEntry.LIBRARY_PATH:
				flags |= added ? ICElementDelta.F_ADDED_PATHENTRY_LIBRARY
						: ICElementDelta.F_REMOVED_PATHENTRY_LIBRARY;
				break;
			case ICSettingEntry.LIBRARY_FILE:
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
		for (int kind : allKinds) {
			if((kind & kinds) != 0){
				kindsArray[num++] = kind;
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

	/*
	 * Methods for manipulating the set of project description listeners
	 */

	public void addCProjectDescriptionListener(ICProjectDescriptionListener listener, int eventTypes) {
		fListeners.add(new ListenerDescriptor(listener, eventTypes));
	}

	public void removeCProjectDescriptionListener(ICProjectDescriptionListener listener) {
		for (ListenerDescriptor listenerDescriptor : fListeners) {
			if (listenerDescriptor.fListener.equals(listener)) {
				fListeners.remove(listenerDescriptor);
				break;
			}
		}
	}

	public void notifyListeners(CProjectDescriptionEvent event){
		int eventType = event.getEventType();
		for (ListenerDescriptor listener : fListeners) {
			if (listener.handlesEvent(eventType))
				listener.fListener.handleEvent(event);
		}
	}

	void checkRemovedConfigurations(ICDescriptionDelta delta){
		if(delta == null)
			return;

		ICDescriptionDelta cfgDeltas[] = delta.getChildren();
		for (ICDescriptionDelta cfgDelta : cfgDeltas) {
			if(cfgDelta.getDeltaKind() == ICDescriptionDelta.REMOVED){
				CConfigurationDescriptionCache des = (CConfigurationDescriptionCache)cfgDelta.getOldSetting();
				CConfigurationData data = des.getConfigurationData();
				try {
					removeData(des, data, null);
				} catch (CoreException e) {
				}
			}
		}
	}

	public ICConfigurationDescription getPreferenceConfiguration(String buildSystemId) throws CoreException {
		return getPreferenceConfiguration(buildSystemId, true);
	}

	private void runContextOperations(SettingsContext context, IProgressMonitor monitor){
		IWorkspaceRunnable toRun = context.createOperationRunnable();
		if(toRun != null){
			runWspModification(toRun, monitor);
		} else if (monitor != null){
			monitor.done();
		}
	}

	public ICConfigurationDescription getPreferenceConfiguration(String buildSystemId, boolean write) throws CoreException {
		ICConfigurationDescription des = getLoaddedPreference(buildSystemId);
		if(des == null){
			try {
				des = loadPreference(buildSystemId);
			} catch (CoreException e) {
	//			CCorePlugin.log(e);
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
		CConfigurationDescriptionCache baseCache = null;
		if(baseData instanceof CConfigurationDescriptionCache){
			baseCache = (CConfigurationDescriptionCache)baseData;
			baseData = baseCache.getConfigurationData();
		}
		CConfigurationSpecSettings settings = cfgDes.getSpecSettings();
		ICStorageElement rootEl = getBuildSystemConfigPreferenceStorage(des.getBuildSystemId(), true, false);
		ICStorageElement rootParent = rootEl.getParent();
		rootParent.removeChild(rootEl);
		ICStorageElement baseRootEl = settings.getRootStorageElement();
		rootEl = rootParent.importChild(baseRootEl);
		CConfigurationDescriptionCache cache = new CConfigurationDescriptionCache(des, baseData, baseCache, cfgDes.getSpecSettings(), null, rootEl);
		CSettingEntryFactory factory = new CSettingEntryFactory();
		SettingsContext context = new SettingsContext(null);
		cache.applyData(factory, context);
		cache.doneInitialization();
		factory.clear();
		runContextOperations(context, null);
		return cache;
	}

	private ICConfigurationDescription createNewPreference(String buildSystemId) throws CoreException {
		ICStorageElement cfgEl = getBuildSystemConfigPreferenceStorage(buildSystemId, true, false);

		String id = PREFERENCE_CFG_ID_PREFIX + buildSystemId;
		CConfigurationDescription des = new CConfigurationDescription(id, PREFERENCE_CFG_NAME, buildSystemId, cfgEl, fPrefUpdater);

		return createPreferenceCache(des);
	}

//	private XmlStorage createBuildSystemCfgPrefStore() throws CoreException{
//		ICStorageElement el = getPreferenceStorage(PREFERENCES_STORAGE, MODULE_ID, true, false);
//
//		XmlStorage store = new XmlStorage((InternalXmlStorageElement)el);
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
			for (ICStorageElement child : children) {
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
		for (ICStorageElement child : children) {
			if(PREFERENCE_BUILD_SYSTEM_ELEMENT.equals(child.getName())){
				if(buildSystemId.equals(child.getAttribute(ID))){
					cfgEl = child;
					break;
				}
			}

		}
		CConfigurationDescriptionCache cache = new CConfigurationDescriptionCache(cfgEl, null);
		CSettingEntryFactory factory = new CSettingEntryFactory();
		cache.loadData(factory);
		cache.doneInitialization();
		factory.clear();
		return cache;
	}

	public ICStorageElement getPreferenceStorage(String prefKey, String storageId, boolean createIfNotDound, boolean readOnly) throws CoreException{
		XmlStorage store = getPreferenceStore(prefKey, createIfNotDound, readOnly);

		return store.getStorage(storageId, createIfNotDound);
	}

	private XmlStorage getPreferenceStore(String prefKey,  boolean createIfNotDound, boolean readOnly) throws CoreException{
		ICStorageElement el = createPreferenceStorage(prefKey, createIfNotDound, readOnly);

		XmlStorage store = new XmlStorage((InternalXmlStorageElement)el);

		return store;
	}

	public void savePreferenceStorage(String prefKey, String storageId, ICStorageElement el) throws CoreException{
		XmlStorage store = getPreferenceStore(prefKey, true, false);
		store.importStorage(storageId, el);

		InternalXmlStorageElement rootEl = new InternalXmlStorageElement(store.fElement, store.isReadOnly());
		serializePreference(prefKey, rootEl);
	}

	private CConfigurationDescriptionCache getLoaddedPreference(String buildSystemId){
		return fPreferenceMap.get(buildSystemId);
	}

	private void setLoaddedPreference(String buildSystemId, CConfigurationDescriptionCache des){
		fPreferenceMap.put(buildSystemId, des);
	}

	public CConfigBasedDescriptorManager getDescriptorManager(){
		return fDescriptorManager;
	}

	public CConfigurationData createDefaultConfigData(IProject project, CDataFactory factory) throws CoreException{
		return createDefaultConfigData(project, CDataUtil.genId(DEFAULT_CFG_ID_PREFIX), DEFAULT_CFG_NAME, factory);
	}

	public CConfigurationData createDefaultConfigData(IProject project, String id, String name, CDataFactory factory) throws CoreException{
		if(factory == null)
			factory = new CDataFactory();

		CConfigurationData data = CDataUtil.createEmptyData(id, name, factory, true);
//		CDataUtil.
////		data.initEmptyData();
//
//		CDataUtil.adjustConfig(data, factory);

		factory.setModified(data, false);
		return data;
	}

	public boolean isNewStyleIndexCfg(IProject project){
		ICProjectDescription des = getProjectDescription(project, false);
		if(des != null)
			return isNewStyleIndexCfg(des);
		return false;
	}

	public boolean isNewStyleIndexCfg(ICProjectDescription des){
		ICConfigurationDescription cfgDes = des.getDefaultSettingConfiguration();
		if(cfgDes != null)
			return isNewStyleCfg(cfgDes);
		return false;
	}

	public boolean isNewStyleProject(IProject project){
		return isNewStyleProject(getProjectDescription(project, false));
	}

	public boolean isNewStyleProject(ICProjectDescription des){
		if(des == null)
			return false;

		return isNewStyleIndexCfg(des);
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

//	public String[] getContentTypeFileSpecs (IProject project, IContentType type) {
//		String[] globalSpecs = type.getFileSpecs(IContentType.FILE_EXTENSION_SPEC);
//		IContentTypeSettings settings = null;
//		if (project != null) {
//			IScopeContext projectScope = new ProjectScope(project);
//			try {
//				settings = type.getSettings(projectScope);
//			} catch (Exception e) {}
//			if (settings != null) {
//				String[] specs = settings.getFileSpecs(IContentType.FILE_EXTENSION_SPEC);
//				if (specs.length > 0) {
//					int total = globalSpecs.length + specs.length;
//					String[] projSpecs = new String[total];
//					int i=0;
//					for (int j=0; j<specs.length; j++) {
//						projSpecs[i] = specs[j];
//						i++;
//					}
//					for (int j=0; j<globalSpecs.length; j++) {
//						projSpecs[i] = globalSpecs[j];
//						i++;
//					}
//					return projSpecs;
//				}
//			}
//		}
//		return globalSpecs;
//	}

//	public String[] getExtensionsFromContentTypes(IProject project, String[] typeIds){
//		return CDataUtil.getExtensionsFromContentTypes(project, typeIds);
//	}

	static ICLanguageSetting getLanguageSettingForFile(ICConfigurationDescription cfgDes, IPath path, boolean ignoreExcludeStatus){
		int segCount = path.segmentCount();
		if(segCount == 0)
			return null;

		ICResourceDescription rcDes = cfgDes.getResourceDescription(path, false);
		if(rcDes == null || (!ignoreExcludeStatus && rcDes.isExcluded()))
			return null;

		if(rcDes.getType() == ICSettingBase.SETTING_FOLDER){
			return ((ICFolderDescription)rcDes).getLanguageSettingForFile(path.lastSegment());
		}
		return ((ICFileDescription)rcDes).getLanguageSetting();
	}

	static private HashMap<HashSet<String>, CLanguageData> createExtSetToLDataMap(IProject project, CLanguageData[] lDatas){
		HashMap<HashSet<String>, CLanguageData> map = new HashMap<HashSet<String>, CLanguageData>();

		for (CLanguageData lData : lDatas) {
			String[] exts = CDataUtil.getSourceExtensions(project, lData);
			HashSet<String> set = new HashSet<String>(Arrays.asList(exts));
			map.put(set, lData);
		}

		return map;
	}

	static boolean removeNonCustomSettings(IProject project, CConfigurationData data){
		PathSettingsContainer cont = CDataUtil.createRcDataHolder(data);
		PathSettingsContainer[] children = cont.getChildren(false);
		PathSettingsContainer parent;
		CResourceData childRcData;
		boolean modified = false;
		for (PathSettingsContainer child : children) {
			childRcData = (CResourceData)child.getValue();
			if(childRcData.getType() == ICSettingBase.SETTING_FOLDER){
				CResourceData parentRcData = null;
				for(parent = child.getParentContainer();
					(parentRcData = (CResourceData)parent.getValue()).getType() != ICSettingBase.SETTING_FOLDER;
					parent = parent.getParentContainer()) {
					// no body, this loop is to find the parent
				}
				if(!settingsCustomized(project, (CFolderData)parentRcData, (CFolderData)childRcData, parent.isRoot())){
					try {
						data.removeResourceData(childRcData);
						child.remove();
						modified = true;
					} catch (CoreException e) {
						CCorePlugin.log(e);
					}
				}
			} else {
				parent = child.getParentContainer();
				if(!settingsCustomized(project, (CResourceData)parent.getValue(), (CFileData)childRcData)){
					try {
						data.removeResourceData(childRcData);
						child.remove();
						modified = true;
					} catch (CoreException e) {
						CCorePlugin.log(e);
					}
				}
			}

		}
		return modified;
	}

	static private boolean settingsCustomized(IProject project, CFolderData parent, CFolderData child, boolean isParentRoot){
		if(baseSettingsCustomized(parent, child))
			return true;

		CLanguageData[] childLDatas = child.getLanguageDatas();
		CLanguageData[] parentLDatas = parent.getLanguageDatas();

		// Note that parent-root can define more languages than regular folder where tools are filtered
		if(!isParentRoot && childLDatas.length != parentLDatas.length)
			return true;

		HashMap<HashSet<String>, CLanguageData> parentMap = createExtSetToLDataMap(project, parentLDatas);
		HashMap<HashSet<String>, CLanguageData> childMap = createExtSetToLDataMap(project, childLDatas);
		for (Map.Entry<HashSet<String>, CLanguageData> childEntry : childMap.entrySet()) {
			CLanguageData parentLData = parentMap.get(childEntry.getKey());
			if(parentLData == null)
				return true;

			CLanguageData childLData = childEntry.getValue();
			if(!langDatasEqual(parentLData, childLData))
				return true;
		}
		
		return false;
	}

	static private boolean settingsCustomized(IProject project, CResourceData parent, CFileData child){
		if(baseSettingsCustomized(parent, child))
			return true;

		CLanguageData lData = child.getLanguageData();

		if(parent.getType() == ICSettingBase.SETTING_FOLDER){
			CFolderData foParent = (CFolderData)parent;

			IPath childPath = child.getPath();
			String fileName = childPath.lastSegment();
			if(PatternNameMap.isPatternName(fileName))
				return true;

			CLanguageData parentLangData = CDataUtil.findLanguagDataForFile(fileName, project, foParent);

			return !langDatasEqual(lData, parentLangData);
		}

		CFileData fiParent = (CFileData)parent;
		CLanguageData parentLangData = fiParent.getLanguageData();
		return !langDatasEqual(lData, parentLangData);
	}

	static boolean langDatasEqual(CLanguageData lData1, CLanguageData lData2){
		if(lData1 == null)
			return lData2 == null;

		if(lData2 == null)
			return false;

		int kinds[] = KindBasedStore.getLanguageEntryKinds();
		for (int kind : kinds) {
			ICLanguageSettingEntry entries1[] = lData1.getEntries(kind);
			ICLanguageSettingEntry entries2[] = lData2.getEntries(kind);
			if(!Arrays.equals(entries1, entries2))
				return false;
		}

		return true;
	}

	private static boolean baseSettingsCustomized(CResourceData parent, CResourceData child){
//		if(parent.isExcluded() != child.isExcluded())
//			return true;

		if(child.hasCustomSettings())
			return true;

		return false;
	}


	public ICProjectDescriptionWorkspacePreferences getProjectDescriptionWorkspacePreferences(
			boolean write) {
		if(fPreferences == null){
			try {
				fPreferences = loadPreferences();
			} catch (CoreException e) {
			}
			if(fPreferences == null)
				fPreferences = new CProjectDescriptionWorkspacePreferences((ICStorageElement)null, null, true);
		}

		CProjectDescriptionWorkspacePreferences prefs = fPreferences;

		if(write)
			prefs = new CProjectDescriptionWorkspacePreferences(prefs, false);

		return prefs;
	}

	public boolean setProjectDescriptionWorkspacePreferences(ICProjectDescriptionWorkspacePreferences prefs,
						boolean updateProjects,
						IProgressMonitor monitor) {
		if(monitor == null)
			monitor = new NullProgressMonitor();
		boolean changed = false;
		ICProjectDescriptionWorkspacePreferences oldPrefs = getProjectDescriptionWorkspacePreferences(false);
		try {
			do {
				if(oldPrefs != prefs){
					if(prefs.getConfigurationRelations() != oldPrefs.getConfigurationRelations()){
						changed = true;
						break;
					}
				}
			} while(false);

			if(changed){
				CProjectDescriptionWorkspacePreferences basePrefs;
				if(prefs instanceof CProjectDescriptionWorkspacePreferences)
					basePrefs = (CProjectDescriptionWorkspacePreferences)prefs;
				else
					throw new IllegalArgumentException();

				fPreferences = new CProjectDescriptionWorkspacePreferences(basePrefs, null, true);

				storePreferences(fPreferences);

				if(updateProjects)
					updateProjectDescriptions(null, monitor);
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		} finally {
			monitor.done();
		}

		return changed;
	}

	private void storePreferences(CProjectDescriptionWorkspacePreferences prefs) throws CoreException {
		ICStorageElement el = getCProjectDescriptionPreferencesElement(true, false);
		prefs.serialize(el);
		saveCProjectDescriptionPreferencesElement(el);
	}

	private void saveCProjectDescriptionPreferencesElement(ICStorageElement el) throws CoreException{
		ICStorageElement cur = getCProjectDescriptionPreferencesElement(true, false);
		ICStorageElement parent = cur.getParent();
		parent.removeChild(cur);
		parent.importChild(el);
		savePreferenceStorage(PREFERENCES_STORAGE, MODULE_ID, parent);
	}

	private CProjectDescriptionWorkspacePreferences loadPreferences() throws CoreException{
		ICStorageElement el = getCProjectDescriptionPreferencesElement(false, true);
		return new CProjectDescriptionWorkspacePreferences(el, null, true);
	}

	private ICStorageElement getCProjectDescriptionPreferencesElement(boolean createIfNotFound, boolean readOnly) throws CoreException{
		ICStorageElement el = getPreferenceStorage(PREFERENCES_STORAGE, MODULE_ID, createIfNotFound, readOnly);
		ICStorageElement[] children = el.getChildren();
		for (ICStorageElement child : children) {
			if(PREFERENCES_ELEMENT.equals(child.getName()))
				return child;
		}
		if(createIfNotFound)
			return el.createChild(PREFERENCES_ELEMENT);
		throw ExceptionFactory.createCoreException(SettingsModelMessages.getString("CProjectDescriptionManager.14")); //$NON-NLS-1$
	}

	public void updateExternalSettingsProviders(String[] ids, IProgressMonitor monitor){
		ExtensionContainerFactory.updateReferencedProviderIds(ids, monitor);
	}

	boolean isEmptyCreatingDescriptionAllowed(){
		return fAllowEmptyCreatingDescription;
	}

	void setEmptyCreatingDescriptionAllowed(boolean allow){
		fAllowEmptyCreatingDescription = allow;
	}

}

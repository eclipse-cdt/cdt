/*******************************************************************************
 * Copyright (c) 2005, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer (QNX) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Andrew Ferguson (Symbian)
 *     Sergey Prigogin (Google)
 *     Tim Kelly (Nokia)
 *     Anna Dushistova (MontaVista)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexChangeListener;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.IIndexerStateListener;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.index.IndexerSetupParticipant;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ILanguageMappingChangeListener;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionListener;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IWritableIndex;
import org.eclipse.cdt.internal.core.index.IWritableIndexManager;
import org.eclipse.cdt.internal.core.index.IndexChangeEvent;
import org.eclipse.cdt.internal.core.index.IndexFactory;
import org.eclipse.cdt.internal.core.index.IndexerStateEvent;
import org.eclipse.cdt.internal.core.index.provider.IndexProviderManager;
import org.eclipse.cdt.internal.core.pdom.PDOM.IListener;
import org.eclipse.cdt.internal.core.pdom.db.ChunkCache;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMLinkageFactory;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMProjectIndexLocationConverter;
import org.eclipse.cdt.internal.core.pdom.indexer.AbstractPDOMIndexer;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;
import org.eclipse.cdt.internal.core.pdom.indexer.PDOMNullIndexer;
import org.eclipse.cdt.internal.core.pdom.indexer.PDOMRebuildTask;
import org.eclipse.cdt.internal.core.pdom.indexer.PDOMUpdateTask;
import org.eclipse.cdt.internal.core.pdom.indexer.ProjectIndexerInputAdapter;
import org.eclipse.cdt.internal.core.pdom.indexer.TranslationUnitCollector;
import org.eclipse.cdt.internal.core.pdom.indexer.TriggerNotificationTask;
import org.eclipse.cdt.internal.core.resources.PathCanonicalizationStrategy;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;

import com.ibm.icu.text.MessageFormat;

/**
 * The PDOM Provider. This is likely temporary since I hope
 * to integrate the PDOM directly into the core once it has
 * stabilized.
 */
public class PDOMManager implements IWritableIndexManager, IListener {
	private static final String TRACE_INDEXER_SETUP = CCorePlugin.PLUGIN_ID + "/debug/indexer/setup"; //$NON-NLS-1$

	private final class PCL implements IPreferenceChangeListener {
		private ICProject fProject;
		public PCL(ICProject prj) {
			fProject= prj;
		}

		@Override
		public void preferenceChange(PreferenceChangeEvent event) {
			if (fProject.getProject().isOpen()) {
				onPreferenceChange(fProject, event);
			}
		}
	}

	private static final QualifiedName dbNameProperty= new QualifiedName(CCorePlugin.PLUGIN_ID, "pdomName"); //$NON-NLS-1$

	public static final int[] IDS_FOR_LINKAGES_TO_INDEX = {
		ILinkage.CPP_LINKAGE_ID, ILinkage.C_LINKAGE_ID, ILinkage.FORTRAN_LINKAGE_ID
	};
	public static final int[] IDS_FOR_LINKAGES_TO_INDEX_C_FIRST = {
		ILinkage.C_LINKAGE_ID, ILinkage.CPP_LINKAGE_ID, ILinkage.FORTRAN_LINKAGE_ID
	};

	private final LinkedList<ICProject> fProjectQueue= new LinkedList<ICProject>();
	private final PDOMSetupJob fSetupJob;
	/**
	 * Protects fIndexerJob, fCurrentTask and fTaskQueue.
	 */
	private final LinkedList<IPDOMIndexerTask> fTaskQueue = new LinkedList<IPDOMIndexerTask>();
    private final PDOMIndexerJob fIndexerJob;
	private IPDOMIndexerTask fCurrentTask;
	private int fSourceCount, fHeaderCount, fTickCount;
	
	private final LinkedList<Runnable> fChangeEvents= new LinkedList<Runnable>();
	private final Job fNotificationJob;

	private final AtomicMultiSet<IIndexFileLocation> fFilesIndexedUnconditionlly= new AtomicMultiSet<IIndexFileLocation>(); 
	
    /**
     * Stores mapping from pdom to project, used to serialize creation of new pdoms.
     */
    private Map<IProject, IPDOM> fProjectToPDOM= new HashMap<IProject, IPDOM>();
    private Map<File, ICProject> fFileToProject= new HashMap<File, ICProject>();
	private ListenerList fChangeListeners= new ListenerList();
	private ListenerList fStateListeners= new ListenerList();
	
	private IndexChangeEvent fIndexChangeEvent= new IndexChangeEvent();
	private IndexerStateEvent fIndexerStateEvent= new IndexerStateEvent();

	private CModelListener fCModelListener= new CModelListener(this);
	private ILanguageMappingChangeListener fLanguageChangeListener = new LanguageMappingChangeListener(this);
	private LanguageSettingsChangeListener fLanguageSettingsChangeListener = new LanguageSettingsChangeListener(this);
	private final ICProjectDescriptionListener fProjectDescriptionListener;
	private final JobChangeListener fJobChangeListener;
	private final IPreferenceChangeListener fPreferenceChangeListener;
	
	private IndexFactory fIndexFactory= new IndexFactory(this);
    private IndexProviderManager fIndexProviderManager = new IndexProviderManager();
    
	/**
	 * Serializes creation of new indexer, when acquiring the lock you are 
	 * not allowed to hold a lock on fPDOMs.
	 */
	private Map<ICProject, IndexUpdatePolicy> fUpdatePolicies= new HashMap<ICProject, IndexUpdatePolicy>();
	private Set<String> fClosingProjects= new HashSet<String>();
	
	private Map<IProject, PCL> fPrefListeners= new HashMap<IProject, PCL>();
	private List<IndexerSetupParticipant> fSetupParticipants= new ArrayList<IndexerSetupParticipant>();
	private Set<ICProject> fPostponedProjects= new HashSet<ICProject>();
	private int fLastNotifiedState= IndexerStateEvent.STATE_IDLE;
	private boolean fInShutDown;

	boolean fTraceIndexerSetup;
    
	public PDOMManager() {
		PDOM.sDEBUG_LOCKS= "true".equals(Platform.getDebugOption(CCorePlugin.PLUGIN_ID + "/debug/index/locks"));  //$NON-NLS-1$//$NON-NLS-2$
		addIndexerSetupParticipant(new WaitForRefreshJobs());
		fProjectDescriptionListener= new CProjectDescriptionListener(this);
		fJobChangeListener= new JobChangeListener(this);
		fPreferenceChangeListener= new IPreferenceChangeListener() {
			@Override
			public void preferenceChange(PreferenceChangeEvent event) {
				onPreferenceChange(event);
			}
		};
		fSetupJob= new PDOMSetupJob(this);
		fIndexerJob= new PDOMIndexerJob(this);
		fNotificationJob= createNotifyJob();
	}
	
	public Job startup() {
		fInShutDown= false;
		Job postStartupJob= new Job(CCorePlugin.getResourceString("CCorePlugin.startupJob")) { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				postStartup();
				return Status.OK_STATUS;
			}
			@Override
			public boolean belongsTo(Object family) {
				return family == PDOMManager.this;
			}
		};
		postStartupJob.setSystem(true);
		return postStartupJob;	
	}

	/** 
	 * Called from a job after plugin start.
	 */
	protected void postStartup() {
		// the model listener is attached outside of the job in
		// order to avoid a race condition where its not noticed
		// that new projects are being created
		InstanceScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID).addPreferenceChangeListener(fPreferenceChangeListener);
		Job.getJobManager().addJobChangeListener(fJobChangeListener);
		adjustCacheSize();
		updatePathCanonicalizationStrategy();
		fIndexProviderManager.startup();
		
		fTraceIndexerSetup= String.valueOf(true).equals(Platform.getDebugOption(TRACE_INDEXER_SETUP));
		final CoreModel model = CoreModel.getDefault();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(fCModelListener, IResourceChangeEvent.POST_BUILD);
		model.addElementChangedListener(fCModelListener);
		LanguageManager.getInstance().registerLanguageChangeListener(fLanguageChangeListener);
		LanguageSettingsManager.registerLanguageSettingsChangeListener(fLanguageSettingsChangeListener);
		final int types= CProjectDescriptionEvent.DATA_APPLIED;
		CCorePlugin.getDefault().getProjectDescriptionManager().addCProjectDescriptionListener(fProjectDescriptionListener, types);

		try {
			ICProject[] projects= model.getCModel().getCProjects();
			for (ICProject project : projects) {
				addProject(project);
			}
		} catch (CModelException e) {
			CCorePlugin.log(e);
		}
	}

	public void shutdown() {
		fInShutDown= true;
		InstanceScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID).removePreferenceChangeListener(fPreferenceChangeListener);
		CCorePlugin.getDefault().getProjectDescriptionManager().removeCProjectDescriptionListener(fProjectDescriptionListener);
		final CoreModel model = CoreModel.getDefault();
		model.removeElementChangedListener(fCModelListener);
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(fCModelListener);
		LanguageSettingsManager.unregisterLanguageSettingsChangeListener(fLanguageSettingsChangeListener);
		LanguageManager.getInstance().unregisterLanguageChangeListener(fLanguageChangeListener);
		PDOMIndexerJob jobToCancel= null;
		synchronized (fTaskQueue) {
			fTaskQueue.clear();
			jobToCancel= fIndexerJob;
		}
		
		if (jobToCancel != null) {
			assert !Thread.holdsLock(fTaskQueue);
			jobToCancel.cancelJobs(null, false);
		}
		Job.getJobManager().removeJobChangeListener(fJobChangeListener);
	}

	protected void onPreferenceChange(PreferenceChangeEvent event) {
		String prop = event.getKey();
		if (prop.equals(CCorePreferenceConstants.INDEX_DB_CACHE_SIZE_PCT)
				|| prop.equals(CCorePreferenceConstants.MAX_INDEX_DB_CACHE_SIZE_MB)) {
			adjustCacheSize();
		} else if (prop.equals(CCorePreferenceConstants.TODO_TASK_TAGS) ||
				prop.equals(CCorePreferenceConstants.TODO_TASK_PRIORITIES) ||
				prop.equals(CCorePreferenceConstants.TODO_TASK_CASE_SENSITIVE)) {
			reindexAll();
		} else if (prop.equals(CCorePreferenceConstants.FILE_PATH_CANONICALIZATION)) {
			updatePathCanonicalizationStrategy();
			reindexAll();
		}
	}
	
	protected void adjustCacheSize() {
		IPreferencesService prefs = Platform.getPreferencesService();
		int cachePct= prefs.getInt(CCorePlugin.PLUGIN_ID, CCorePreferenceConstants.INDEX_DB_CACHE_SIZE_PCT, 10, null);
		int cacheMax= prefs.getInt(CCorePlugin.PLUGIN_ID, CCorePreferenceConstants.MAX_INDEX_DB_CACHE_SIZE_MB, 64, null);
		cachePct= Math.max(1, Math.min(50, cachePct));   // 1%-50%
		cacheMax= Math.max(1, cacheMax);                 // >= 1mb
		long m1= Runtime.getRuntime().maxMemory() / 100L * cachePct;
		long m2= Math.min(m1, cacheMax * 1024L * 1024L);
		ChunkCache.getSharedInstance().setMaxSize(m2);
	}

	private void updatePathCanonicalizationStrategy() {
		IPreferencesService prefs = Platform.getPreferencesService();
		boolean canonicalize = prefs.getBoolean(CCorePlugin.PLUGIN_ID, CCorePreferenceConstants.FILE_PATH_CANONICALIZATION, true, null);
		PathCanonicalizationStrategy.setPathCanonicalization(canonicalize);
	}

	public IndexProviderManager getIndexProviderManager() {
		return fIndexProviderManager;
	}

	/**
	 * Returns the pdom for the project. 
	 * @throws CoreException
	 */
	public IPDOM getPDOM(ICProject project) throws CoreException {
		synchronized (fProjectToPDOM) {
			IProject rproject = project.getProject();
			IPDOM pdom = fProjectToPDOM.get(rproject);
			if (pdom == null) {
				pdom= new PDOMProxy();
				fProjectToPDOM.put(rproject, pdom);
			}
			return pdom;
		}
	}
	
	/**
	 * Returns the pdom for the project. The call to the method may cause 
	 * opening the database. In case there is a version mismatch the data
	 * base is cleared, in case it does not exist it is created. In any
	 * case a pdom ready to use is returned.
	 * @throws CoreException
	 */
	private WritablePDOM getOrCreatePDOM(ICProject project) throws CoreException {
		synchronized (fProjectToPDOM) {
			IProject rproject = project.getProject();
			IPDOM pdomProxy= fProjectToPDOM.get(rproject);
			if (pdomProxy instanceof WritablePDOM) {
				return (WritablePDOM) pdomProxy;
			}

			String dbName= rproject.getPersistentProperty(dbNameProperty);
			File dbFile= null;
			if (dbName != null) {
				dbFile= fileFromDatabaseName(dbName);
				if (!dbFile.exists()) {
					dbFile= null;
					dbName= null;
				} else {
					ICProject currentCOwner= fFileToProject.get(dbFile);
					if (currentCOwner != null) {
						IProject currentOwner= currentCOwner.getProject();
						if (!currentOwner.exists()) {
							fFileToProject.remove(dbFile);
							dbFile.delete();
						}
						dbName= null;
						dbFile= null;
					}
				}
			}

			boolean fromScratch= false;
			if (dbName == null) {
				dbName = createNewDatabaseName(project);
				dbFile= fileFromDatabaseName(dbName);
				storeDatabaseName(rproject, dbName);
				fromScratch= true;
			}

			WritablePDOM pdom= new WritablePDOM(dbFile, new PDOMProjectIndexLocationConverter(rproject), getLinkageFactories());
			if (!pdom.isSupportedVersion() || fromScratch) {
				try {
					pdom.acquireWriteLock();
				} catch (InterruptedException e) {
					throw new CoreException(CCorePlugin.createStatus(Messages.PDOMManager_creationOfIndexInterrupted, e));
				}
				if (fromScratch) {
					pdom.setCreatedFromScratch(true);
				} else {
					pdom.clear();
					pdom.setClearedBecauseOfVersionMismatch(true);
				}
				writeProjectPDOMProperties(pdom, rproject);
				pdom.releaseWriteLock();
			}
			pdom.setASTFilePathResolver(new ProjectIndexerInputAdapter(project, false));
			pdom.addListener(this);
			
			fFileToProject.put(dbFile, project);
			fProjectToPDOM.put(rproject, pdom);
			if (pdomProxy instanceof PDOMProxy) {
				((PDOMProxy) pdomProxy).setDelegate(pdom);
			}
			return pdom;
		}
	}

	private Map<String, IPDOMLinkageFactory> getLinkageFactories() {
		return LanguageManager.getInstance().getPDOMLinkageFactoryMappings();
	}

	private void storeDatabaseName(IProject rproject, String dbName)
			throws CoreException {
		rproject.setPersistentProperty(dbNameProperty, dbName);
	}

	private String createNewDatabaseName(ICProject project) {
		String dbName;
		long time= System.currentTimeMillis();
		File file;
		do {
			dbName= getDefaultName(project, time++);
			file= fileFromDatabaseName(dbName);
		}
		while (file.exists());
		return dbName;
	}

	private File fileFromDatabaseName(String dbName) {
		return CCorePlugin.getDefault().getStateLocation().append(dbName).toFile();
	}

	private String getDefaultName(ICProject project, long time) {
		return project.getElementName() + "." + time + ".pdom";  //$NON-NLS-1$//$NON-NLS-2$
	}

	@Override
	public String getDefaultIndexerId() {
		return getIndexerId(null);
	}

	@Override
	public void setDefaultIndexerId(String indexerId) {
		IndexerPreferences.setDefaultIndexerId(indexerId);
	}
	
    @Override
	public String getIndexerId(ICProject project) {
    	IProject prj= project != null ? project.getProject() : null;
    	return IndexerPreferences.get(prj, IndexerPreferences.KEY_INDEXER_ID, IPDOMManager.ID_NO_INDEXER);
    }

    @Override
	public void setIndexerId(final ICProject project, String indexerId) {
    	IProject prj= project.getProject();
    	IndexerPreferences.set(prj, IndexerPreferences.KEY_INDEXER_ID, indexerId);
    	CCoreInternals.savePreferences(prj, IndexerPreferences.getScope(prj) == IndexerPreferences.SCOPE_PROJECT_SHARED);
    }
	
	protected void onPreferenceChange(ICProject cproject, PreferenceChangeEvent event) {
		if (IndexerPreferences.KEY_UPDATE_POLICY.equals(event.getKey())) {
			changeUpdatePolicy(cproject);
		} else {
			IProject project= cproject.getProject();
			if (project.exists() && project.isOpen()) {
				try {
					changeIndexer(cproject);
				} catch (Exception e) {
					CCorePlugin.log(e);
				}
			}
		}
	}

	private void changeUpdatePolicy(ICProject cproject) {
		assert !Thread.holdsLock(fProjectToPDOM);
		synchronized (fUpdatePolicies) {
			IndexUpdatePolicy policy= getPolicy(cproject);
			if (policy != null) {
				IPDOMIndexerTask task= policy.changePolicy(IndexerPreferences.getUpdatePolicy(cproject.getProject()));
				if (task != null) {
					enqueue(task);
				}
			}
		}
	}

	private void changeIndexer(ICProject cproject) throws CoreException {
		assert !Thread.holdsLock(fProjectToPDOM);
		
		// if there is no indexer, don't touch the preferences.
		IPDOMIndexer oldIndexer= getIndexer(cproject);
		if (oldIndexer == null) {
			return;
		}
		
		IProject prj= cproject.getProject();
		String newid= IndexerPreferences.get(prj, IndexerPreferences.KEY_INDEXER_ID, IPDOMManager.ID_NO_INDEXER);
		Properties props= IndexerPreferences.getProperties(prj);
		
		// Workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=359485
		synchronized (new ProjectScope(prj).getNode(CCorePlugin.PLUGIN_ID)) {
		synchronized (fUpdatePolicies) {
			if (fClosingProjects.contains(prj.getName())) {
				return;
			}
			oldIndexer= getIndexer(cproject);
			if (oldIndexer != null) {
				if (oldIndexer.getID().equals(newid)) {
					if (!oldIndexer.needsToRebuildForProperties(props)) {
						oldIndexer.setProperties(props);
						return;
					}
				}
				IPDOMIndexer indexer= newIndexer(newid, props);
				registerIndexer(cproject, indexer);
				createPolicy(cproject).clearTUs();
				if (oldIndexer instanceof AbstractPDOMIndexer) {
					if (IndexerPreferences.preferDefaultLanguage(((AbstractPDOMIndexer) oldIndexer).getProperties()) !=
						IndexerPreferences.preferDefaultLanguage(props)) {
						enqueue(new NotifyCModelManagerTask(cproject.getProject()));
					}
				}
				enqueue(new PDOMRebuildTask(indexer));
			}
		}}
		
		if (oldIndexer != null) {
			stopIndexer(oldIndexer);
		}
	}

	private void registerIndexer(ICProject project, IPDOMIndexer indexer) {
		assert Thread.holdsLock(fUpdatePolicies);
		indexer.setProject(project);
		registerPreferenceListener(project);
		createPolicy(project).setIndexer(indexer);
	}

	IPDOMIndexer getIndexer(ICProject project) {
		assert !Thread.holdsLock(fProjectToPDOM);
		synchronized (fUpdatePolicies) {
			IndexUpdatePolicy policy= getPolicy(project);
			if (policy != null) {
				return policy.getIndexer();
			}
		}
		return null;
	}

	void createIndexer(ICProject project, IProgressMonitor pm) throws InterruptedException {
		final IProject prj= project.getProject();
		final String name = prj.getName();
		if (fTraceIndexerSetup) 
			System.out.println("Indexer: Creation for project " + name); //$NON-NLS-1$
		
		assert !Thread.holdsLock(fProjectToPDOM);
		try {
			// Workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=359485
			synchronized (new ProjectScope(prj).getNode(CCorePlugin.PLUGIN_ID)) {
			synchronized (fUpdatePolicies) {
				if (fClosingProjects.contains(name)) {
					if (fTraceIndexerSetup) 
						System.out.println("Indexer: Aborting setup (1) for closing project " + name + " [1]"); //$NON-NLS-1$ //$NON-NLS-2$
					return;
				}
				
				WritablePDOM pdom= getOrCreatePDOM(project);
				Properties props= IndexerPreferences.getProperties(prj);
				IPDOMIndexer indexer= newIndexer(getIndexerId(project), props);
				IndexUpdatePolicy policy= createPolicy(project);

				boolean rebuild= 
					pdom.isClearedBecauseOfVersionMismatch() ||
					pdom.isCreatedFromScratch() ||
					policy.isInitialRebuildRequested();
				if (rebuild) {
					if (IPDOMManager.ID_NO_INDEXER.equals(indexer.getID())) {
						rebuild= false;
					}
					pdom.setClearedBecauseOfVersionMismatch(false);
					pdom.setCreatedFromScratch(false);
				}
				if (!rebuild) {
					registerIndexer(project, indexer);
					IPDOMIndexerTask task= policy.createTask();
					if (task != null) {
						enqueue(task);
					} else {
						enqueue(new TriggerNotificationTask(this, pdom));
					}
					if (policy.isAutomatic()) {
						boolean resume= false;
						pdom.acquireReadLock();
						try {
							resume= "true".equals(pdom.getProperty(IIndexFragment.PROPERTY_RESUME_INDEXER)); //$NON-NLS-1$
						} finally {
							pdom.releaseReadLock();
						}
						if (resume) {
							if (fTraceIndexerSetup) 
								System.out.println("Indexer: Resuming for project " + name); //$NON-NLS-1$

							enqueue(new PDOMUpdateTask(indexer,
									IIndexManager.UPDATE_CHECK_TIMESTAMPS | IIndexManager.UPDATE_CHECK_CONTENTS_HASH));
						}
					}
					return;
				}
			}}

			// rebuild is required, try import first.
			TeamPDOMImportOperation operation= new TeamPDOMImportOperation(project);
			operation.run(pm);

			// Workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=359485
			synchronized (new ProjectScope(prj).getNode(CCorePlugin.PLUGIN_ID)) {
			synchronized (fUpdatePolicies) {
				if (fClosingProjects.contains(name)) {
					if (fTraceIndexerSetup) 
						System.out.println("Indexer: Aborting setup for closing project " + name + " [2]"); //$NON-NLS-1$ //$NON-NLS-2$
					return;
				}

				Properties props= IndexerPreferences.getProperties(prj);
				IPDOMIndexer indexer = newIndexer(getIndexerId(project), props);
				registerIndexer(project, indexer);
				final IndexUpdatePolicy policy= createPolicy(project);
				policy.clearTUs();
				policy.clearInitialFlags();

				IPDOMIndexerTask task= null;
				if (operation.wasSuccessful()) {
					if (fTraceIndexerSetup) 
						System.out.println("Indexer: Imported shared index for project " + name); //$NON-NLS-1$ 
					task= new PDOMUpdateTask(indexer,
							IIndexManager.UPDATE_CHECK_TIMESTAMPS | IIndexManager.UPDATE_CHECK_CONTENTS_HASH);
				} else {
					if (fTraceIndexerSetup) 
						System.out.println("Indexer: Rebuiding for project " + name); //$NON-NLS-1$ 
					task= new PDOMRebuildTask(indexer);
				}
				enqueue(task);
			}}
		} catch (CoreException e) {
			// Ignore if project is no longer open
			if (prj.isOpen()) {
				CCorePlugin.log(e);
			}
		}
	}
		
    private IPDOMIndexer newIndexer(String indexerId, Properties props) throws CoreException  {
    	IPDOMIndexer indexer = null;
    	// Look up in extension point
    	IExtension indexerExt = Platform.getExtensionRegistry().getExtension(CCorePlugin.INDEXER_UNIQ_ID, indexerId);
    	if (indexerExt != null) {
    		IConfigurationElement[] elements = indexerExt.getConfigurationElements();
    		for (int i = 0; i < elements.length; ++i) {
    			IConfigurationElement element = elements[i];
    			if ("run".equals(element.getName())) { //$NON-NLS-1$
    				try {
						indexer = (IPDOMIndexer) element.createExecutableExtension("class"); //$NON-NLS-1$
						indexer.setProperties(props);
					} catch (CoreException e) {
						CCorePlugin.log(e);
					} 
    				break;
    			}
    		}
    	}

    	// Unknown index, default to the null one
    	if (indexer == null) 
    		indexer = new PDOMNullIndexer();

		return indexer;
    }

	public void enqueue(IPDOMIndexerTask subjob) {
    	synchronized (fTaskQueue) {
    		if (fCurrentTask != null && fCurrentTask.acceptUrgentTask(subjob)) {
    			return;
    		}
    		for (IPDOMIndexerTask task : fTaskQueue) {
				if (task.acceptUrgentTask(subjob)) {
					return;
				}
			}
    		fTaskQueue.addLast(subjob);
    		fIndexerJob.schedule();
		}
    }
    
	IPDOMIndexerTask getNextTask() {
		IPDOMIndexerTask result= null;
    	synchronized (fTaskQueue) {
    		if (fTaskQueue.isEmpty()) {
    			fCurrentTask= null;
    			fSourceCount= fHeaderCount= fTickCount= 0;
    		} else {
    			if (fCurrentTask != null) {
    				IndexerProgress info= fCurrentTask.getProgressInformation();
    				fSourceCount+= info.fCompletedSources;
    				fHeaderCount+= info.fCompletedHeaders;
    				// for the ticks we don't consider additional headers
    				fTickCount+= info.fCompletedSources + info.fPrimaryHeaderCount;
    			}
    			result= fCurrentTask= fTaskQueue.removeFirst();
    		}
		}
    	return result;
    }
    
    void cancelledIndexerJob(boolean byManager) {
    	synchronized (fTaskQueue) {
    		fCurrentTask= null;
    		if (!byManager) {
    			fTaskQueue.clear();
    		}
    		if (!fTaskQueue.isEmpty()) {
    			fIndexerJob.schedule();
    		}
    	}
    }
        
    @Override
	public boolean isIndexerIdle() {
    	return Job.getJobManager().find(this).length == 0;
    }

	void addProject(final ICProject cproject) {
		final String name = cproject.getProject().getName();
		if (fTraceIndexerSetup) {
			System.out.println("Indexer: Adding new project " + name); //$NON-NLS-1$
		}	
		
		synchronized (fUpdatePolicies) {
			fClosingProjects.remove(name);
		}
		
		setupProject(cproject);
	}

	void setupProject(final ICProject cproject) {
		if (fInShutDown)
			return;
		
		synchronized (fProjectQueue) {
			fProjectQueue.add(cproject);
		}
		fSetupJob.schedule();
	}

	ICProject getNextProject() {
		synchronized (fProjectQueue) {
			if (fProjectQueue.isEmpty())
				return null;
			return fProjectQueue.removeFirst();
		}
	}

	private void registerPreferenceListener(ICProject project) {
		IProject prj= project.getProject();
		PCL pcl= fPrefListeners.get(prj);
		if (pcl == null) {
			pcl= new PCL(project);
			fPrefListeners.put(prj, pcl);
		}
		IndexerPreferences.addChangeListener(prj, pcl);
	}

	private void unregisterPreferenceListener(ICProject project) {
		IProject prj= project.getProject();
		PCL pcl= fPrefListeners.remove(prj);
		if (pcl != null) {
			IndexerPreferences.removeChangeListener(prj, pcl);
		}
	}

	void changeProject(ICProject project, ITranslationUnit[] added, ITranslationUnit[] changed, ITranslationUnit[] removed) {
		assert !Thread.holdsLock(fProjectToPDOM);
		IPDOMIndexer indexer = getIndexer(project);
		if (indexer != null && indexer.getID().equals(IPDOMManager.ID_NO_INDEXER)) {
			return;
		}
		
		if (added.length > 0 || changed.length > 0 || removed.length > 0) {
			synchronized (fUpdatePolicies) {
				IndexUpdatePolicy policy= createPolicy(project);
				IPDOMIndexerTask task= policy.handleDelta(added, changed, removed);
				if (task != null) {
					enqueue(task);
				}
			}
		}
	}

	private IndexUpdatePolicy createPolicy(final ICProject project) {
		assert !Thread.holdsLock(fProjectToPDOM);
		synchronized (fUpdatePolicies) {
			IndexUpdatePolicy policy= fUpdatePolicies.get(project);
			if (policy == null) {
				policy= new IndexUpdatePolicy(project, IndexerPreferences.getUpdatePolicy(project.getProject()));
				fUpdatePolicies.put(project, policy);
			}
			return policy;
		}
	}

	private IndexUpdatePolicy getPolicy(final ICProject project) {
		synchronized (fUpdatePolicies) {
			return fUpdatePolicies.get(project);
		}
	}

	public void preDeleteProject(ICProject cproject) {
		preRemoveProject(cproject, true); 
	}

	public void preCloseProject(ICProject cproject) {
		preRemoveProject(cproject, false);
	}

	private void preRemoveProject(ICProject cproject, final boolean delete) {
		assert !Thread.holdsLock(fProjectToPDOM);

		final IProject rproject= cproject.getProject();
		final String name = rproject.getName();

		if (fTraceIndexerSetup) 
			System.out.println("Indexer: Removing project " + name + "; delete=" + delete); //$NON-NLS-1$ //$NON-NLS-2$ 

		IPDOMIndexer indexer;
		synchronized (fUpdatePolicies) {
			// Prevent recreating the indexer
			fClosingProjects.add(name);
			indexer= getIndexer(cproject);
		}

		if (indexer != null) {
			stopIndexer(indexer);
		}
    	unregisterPreferenceListener(cproject);
    	Object pdom= null;
    	synchronized (fProjectToPDOM) {
    		pdom = fProjectToPDOM.remove(rproject);
    		// if the project is closed allow to reuse the pdom.
    		if (pdom instanceof WritablePDOM && !delete) {
    			fFileToProject.remove(((WritablePDOM) pdom).getDB().getLocation());
    		}
    	}

    	if (pdom instanceof WritablePDOM) {
    		final WritablePDOM finalpdom= (WritablePDOM) pdom;
    		Job job= new Job(Messages.PDOMManager_ClosePDOMJob) {
    			@Override
				protected IStatus run(IProgressMonitor monitor) {
        			try {
        				finalpdom.acquireWriteLock();
        				try {
        					finalpdom.close();
        					if (delete) {
        						finalpdom.getDB().getLocation().delete();
        					}
        				} catch (CoreException e) {
        					CCorePlugin.log(e);
        				} finally {
        					finalpdom.releaseWriteLock();
        				}
        			} catch (InterruptedException e) {
        			}
    				return Status.OK_STATUS;
    			}
    		};
    		job.setSystem(true);
    		job.schedule();
    	}
    	
		synchronized (fUpdatePolicies) {
			fUpdatePolicies.remove(cproject);
		}
	}
	
	void removeProject(ICProject cproject, ICElementDelta delta) {
    	synchronized (fProjectToPDOM) {
			IProject rproject= cproject.getProject();
			fProjectToPDOM.remove(rproject);
			// don't remove the location, because it may not be reused when the project was deleted.
    	}
	}

	private void stopIndexer(IPDOMIndexer indexer) {
		assert !Thread.holdsLock(fProjectToPDOM);
		assert !Thread.holdsLock(fUpdatePolicies);
		ICProject project= indexer.getProject();
		synchronized (fUpdatePolicies) {
			IndexUpdatePolicy policy= getPolicy(project);
			if (policy != null) {
				if (policy.getIndexer() == indexer) {
					policy.clearTUs();
					policy.setIndexer(null);
				}
			}					
		}
		cancelIndexerJobs(indexer);
	}

	private void cancelIndexerJobs(IPDOMIndexer indexer) {
		PDOMIndexerJob jobToCancel= null;
		synchronized (fTaskQueue) {
			for (Iterator<IPDOMIndexerTask> iter = fTaskQueue.iterator(); iter.hasNext();) {
				IPDOMIndexerTask task= iter.next();
				if (task.getIndexer() == indexer) {
					iter.remove();
				}
			}
			jobToCancel= fIndexerJob;
		}
		
		if (jobToCancel != null) {
			assert !Thread.holdsLock(fTaskQueue);
			jobToCancel.cancelJobs(indexer, true);
		}
	}    
	
	private void reindexAll() {
		ICProject[] cProjects;
		try {
			cProjects = CoreModel.getDefault().getCModel().getCProjects();
			for (ICProject project : cProjects) {
				reindex(project);
			}
		} catch (CModelException e) {
			CCorePlugin.log(e);
		}
	}

	@Override
	public void reindex(final ICProject project) {
		Job job= new Job(Messages.PDOMManager_notifyJob_label) { 
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				IPDOMIndexer indexer= null;
				synchronized (fUpdatePolicies) {
					indexer= getIndexer(project);
					if (indexer == null) {
						createPolicy(project).requestInitialReindex();
						return Status.OK_STATUS;
					}
				}
				// don't attempt to hold lock on indexerMutex while canceling
				cancelIndexerJobs(indexer);

				synchronized (fUpdatePolicies) {
					indexer= getIndexer(project);
					if (indexer != null) {
						createPolicy(project).clearTUs();
						enqueue(new PDOMRebuildTask(indexer));
					}
				}
				return Status.OK_STATUS;
			}
			@Override
			public boolean belongsTo(Object family) {
				return family == PDOMManager.this;
			}
		};
		job.setSystem(true);
		job.schedule();
	}

	@Override
	public void addIndexChangeListener(IIndexChangeListener listener) {
		fChangeListeners.add(listener);
	}

	@Override
	public void removeIndexChangeListener(IIndexChangeListener listener) {
		fChangeListeners.remove(listener);
	}
	
	@Override
	public void addIndexerStateListener(IIndexerStateListener listener) {
		fStateListeners.add(listener);
	}

	@Override
	public void removeIndexerStateListener(IIndexerStateListener listener) {
		fStateListeners.remove(listener);
	}

    private Job createNotifyJob() {
    	Job notify= new Job(Messages.PDOMManager_notifyJob_label) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				while(true) {
					final Runnable r;
					synchronized (fChangeEvents) {
						if (fChangeEvents.isEmpty()) {
							return Status.OK_STATUS;
						}
						r= fChangeEvents.removeFirst();
					}
					r.run();
				}
			}
		};
		notify.setSystem(true);
		return notify;
    }
    
	private void scheduleNotification(Runnable notify) {
		if (fInShutDown)
			return;

		synchronized (fChangeEvents) {
			fChangeEvents.add(notify);
		}
		fNotificationJob.schedule();
	}


    void fireStateChange(final int state) {
    	synchronized (fStateListeners) {
    		if (fLastNotifiedState == state) {
    			return;
    		}
    		fLastNotifiedState= state;
    		if (fStateListeners.isEmpty()) {
    			return;
    		}
    		Runnable notify= new Runnable() {
    			@Override
				public void run() {
    				fIndexerStateEvent.setState(state);
    				Object[] listeners= fStateListeners.getListeners();
    				for (Object listener2 : listeners) {
    					final IIndexerStateListener listener = (IIndexerStateListener) listener2;
    					SafeRunner.run(new ISafeRunnable(){
    						@Override
							public void handleException(Throwable exception) {
    							CCorePlugin.log(exception);
    						}
    						@Override
							public void run() throws Exception {
    							listener.indexChanged(fIndexerStateEvent);
    						}
    					});
    				}
    			}
    		};
    		scheduleNotification(notify);
    	}
	}

	@Override
	public void handleChange(PDOM pdom, final PDOM.ChangeEvent e) {
		if (fChangeListeners.isEmpty()) {
			return;
		}
		
		ICProject project;
		synchronized (fProjectToPDOM) {
			project = fFileToProject.get(pdom.getPath());
		}		
		
		if (project != null) {
			final ICProject finalProject= project;
			Runnable notify= new Runnable() {
				@Override
				public void run() {
					fIndexChangeEvent.setAffectedProject(finalProject, e);
					Object[] listeners= fChangeListeners.getListeners();
					for (Object listener2 : listeners) {
						final IIndexChangeListener listener = (IIndexChangeListener) listener2;
						SafeRunner.run(new ISafeRunnable(){
							@Override
							public void handleException(Throwable exception) {
								CCorePlugin.log(exception);
							}
							@Override
							public void run() throws Exception {
								listener.indexChanged(fIndexChangeEvent);
							}
						});
					}
				}
			};
    		scheduleNotification(notify);
		}
	}

	@Override
	public boolean joinIndexer(final int waitMaxMillis, final IProgressMonitor monitor) {
		assert monitor != null;
		Thread th= null;
		if (waitMaxMillis != FOREVER) {
			final Thread callingThread= Thread.currentThread();
			th= new Thread() {
				@Override
				public void run() {
					try {
						Thread.sleep(waitMaxMillis);
						monitor.setCanceled(true);
						callingThread.interrupt();
					} catch (InterruptedException e) {
					}
				}
			};
			th.setDaemon(true);
			th.start();
		}
		try {
			try {
				Job.getJobManager().join(this, monitor);
				return true;
			} catch (OperationCanceledException e) {
			} catch (InterruptedException e) {
			}
			return Job.getJobManager().find(this).length == 0;
		} finally {
			if (th != null) {
				th.interrupt();
			}
		}
	}
		
	int getMonitorMessage(PDOMIndexerJob job, int currentTicks, int base) {
		assert !Thread.holdsLock(fTaskQueue);
		
		int sourceCount, sourceEstimate, headerCount, tickCount, tickEstimate;
		String detail= null;
		synchronized (fTaskQueue) {
			// add historic data
			sourceCount= sourceEstimate= fSourceCount;
			headerCount= fHeaderCount;
			tickCount= tickEstimate= fTickCount;

			// add future data
			for (IPDOMIndexerTask task : fTaskQueue) {
				final IndexerProgress info= task.getProgressInformation();
				sourceEstimate+= info.fRequestedFilesCount;
				tickEstimate+= info.getEstimatedTicks();
			}
			// add current data
			if (fCurrentTask != null) {
				final IndexerProgress info= fCurrentTask.getProgressInformation();
				sourceCount+= info.fCompletedSources;
				sourceEstimate+= info.fRequestedFilesCount - info.fPrimaryHeaderCount;
				headerCount+= info.fCompletedHeaders;
				// for the ticks we don't consider additional headers
				tickCount+= info.fCompletedSources + info.fPrimaryHeaderCount;
				tickEstimate+= info.getEstimatedTicks();
				detail= PDOMIndexerJob.sMonitorDetail;
			}
		}
		
		String msg= MessageFormat.format(Messages.PDOMManager_indexMonitorDetail, new Object[] { 
					new Integer(sourceCount), new Integer(sourceEstimate), 
					new Integer(headerCount)}); 
		if (detail != null) {
			msg += ": " + detail;  //$NON-NLS-1$
		}
		
		job.subTask(msg);
		if (tickCount > 0 && tickCount <= tickEstimate) {
			int newTick= tickCount*base/tickEstimate;
			if (newTick > currentTicks) {
				job.worked(newTick-currentTicks);
				return newTick;
			}
		}
		return currentTicks;
	}

	@Override
	public IWritableIndex getWritableIndex(ICProject project) throws CoreException {
		return fIndexFactory.getWritableIndex(project);
	}

	@Override
	public IIndex getIndex(ICProject project) throws CoreException {
		return fIndexFactory.getIndex(new ICProject[] {project}, 0);
	}

	@Override
	public IIndex getIndex(ICProject[] projects) throws CoreException {
		return fIndexFactory.getIndex(projects, 0);
	}

	@Override
	public IIndex getIndex(ICProject project, int options) throws CoreException {
		return fIndexFactory.getIndex(new ICProject[] {project}, options);
	}

	@Override
	public IIndex getIndex(ICProject[] projects, int options) throws CoreException {
		return fIndexFactory.getIndex(projects, options);
	}
	
	/**
     * Exports the project PDOM to the specified location, rewriting locations with
     * the specified location converter.
     * <br>
     * Note. This will acquire a write lock while the pdom is exported
	 * @param targetLocation a location that does not currently exist
	 * @param newConverter
	 * @throws CoreException
	 * @throws IllegalArgumentException if a file exists at targetLocation
	 */
	public void exportProjectPDOM(ICProject cproject, File targetLocation, final IIndexLocationConverter newConverter) throws CoreException {
		if (targetLocation.exists()) {
			boolean deleted= targetLocation.delete();
			if (!deleted) {
				throw new IllegalArgumentException(
						MessageFormat.format(Messages.PDOMManager_ExistingFileCollides,
								new Object[] {targetLocation})
				);
			}
		}
		try {
			// copy it
			PDOM pdom= getOrCreatePDOM(cproject);
			pdom.acquireReadLock();
			String oldID= null;
			try {
				oldID= pdom.getProperty(IIndexFragment.PROPERTY_FRAGMENT_ID);
				pdom.flush();
				FileChannel to = new FileOutputStream(targetLocation).getChannel();
				pdom.getDB().transferTo(to);
				to.close();
			} finally {
				pdom.releaseReadLock();
			}

			// overwrite internal location representations
			final WritablePDOM newPDOM = new WritablePDOM(targetLocation, pdom.getLocationConverter(), getLinkageFactories());			
			newPDOM.acquireWriteLock();
			try {
				newPDOM.rewriteLocations(newConverter);

				// ensure fragment id has a sensible value, in case callee's do not
				// overwrite their own values
				newPDOM.setProperty(IIndexFragment.PROPERTY_FRAGMENT_ID, "exported."+oldID); //$NON-NLS-1$
				newPDOM.close();
			} finally {
				newPDOM.releaseWriteLock();
			}
		} catch (IOException ioe) {
			throw new CoreException(CCorePlugin.createStatus(ioe.getMessage()));
		} catch (InterruptedException ie) {
			throw new CoreException(CCorePlugin.createStatus(ie.getMessage()));
		}
	}

	/**
	 * Resets the pdom for the project with the provided stream. 
	 * @throws CoreException
	 * @throws OperationCanceledException in case the thread was interrupted
	 * @since 4.0
	 */
	public void importProjectPDOM(ICProject project, InputStream stream) throws CoreException, IOException {
		// make a copy of the database
		String newName= createNewDatabaseName(project);
		File newFile= fileFromDatabaseName(newName);
		OutputStream out= new FileOutputStream(newFile);
		try {
			int version= 0;
			for (int i=0; i<4; i++) {
				byte b= (byte) stream.read();
				version= (version << 8) + (b & 0xff);
				out.write(b);
			}
			if (version > PDOM.getMaxSupportedVersion()) {
				final IStatus status = new Status(IStatus.WARNING, CCorePlugin.PLUGIN_ID, 0, CCorePlugin.getResourceString("PDOMManager.unsupportedHigherVersion"), null); //$NON-NLS-1$
				throw new CoreException(status); 
			}
			if ( !PDOM.isSupportedVersion( version ) ) {
				final IStatus status = new Status(IStatus.WARNING, CCorePlugin.PLUGIN_ID, 0, CCorePlugin.getResourceString("PDOMManager.unsupportedVersion"), null); //$NON-NLS-1$
				throw new CoreException(status); 
			}
			byte[] buffer= new byte[2048];
			int read;
			while ((read= stream.read(buffer)) >= 0) {
				out.write(buffer, 0, read);
			}
		} finally {
			out.close();
		}
		
		WritablePDOM pdom= (WritablePDOM) getPDOM(project);
		try {
			pdom.acquireWriteLock();
		} catch (InterruptedException e) {
			throw new OperationCanceledException();
		}
		try {
			pdom.reloadFromFile(newFile);
			storeDatabaseName(project.getProject(), newName);
			writeProjectPDOMProperties(pdom, project.getProject());
		} finally {
			pdom.releaseWriteLock();
		}
	}
	
	@Override
	public void export(ICProject project, String location, int options, IProgressMonitor monitor) throws CoreException {
		TeamPDOMExportOperation operation= new TeamPDOMExportOperation(project);
		operation.setTargetLocation(location);
		operation.setOptions(options);
		operation.run(monitor);
	}
	
	/**
	 * Write metadata appropriate for a project pdom
	 * @param pdom the pdom to write to
	 * @param project the project to write metadata about
	 * @throws CoreException
	 */
	public static void writeProjectPDOMProperties(WritablePDOM pdom, IProject project) throws CoreException {
		String DELIM = "\0"; //$NON-NLS-1$
		String id= CCorePlugin.PLUGIN_ID + ".pdom.project." + DELIM + project.getName() + DELIM; //$NON-NLS-1$
		pdom.setProperty(IIndexFragment.PROPERTY_FRAGMENT_ID, id);
	}

	@Override
	public boolean isProjectIndexed(ICProject proj) {
		return !IPDOMManager.ID_NO_INDEXER.equals(getIndexerId(proj));
	}

	@Override
	public boolean isIndexerSetupPostponed(ICProject proj) {
		synchronized (fSetupParticipants) {
			return fPostponedProjects.contains(proj);
		}
	}

	@Override
	public void update(ICElement[] tuSelection, int options) throws CoreException {
		Map<ICProject, List<ICElement>> projectsToElements= splitSelection(tuSelection);
		for (Map.Entry<ICProject, List<ICElement>> entry : projectsToElements.entrySet()) {
			ICProject project = entry.getKey();
			List<ICElement> filesAndFolders = entry.getValue();
			
			update(project, filesAndFolders, options);
		}
	}

	/**
	 * Computes a map from projects to a collection containing the minimal
	 * set of folders and files specifying the selection.
	 */
	private Map<ICProject, List<ICElement>> splitSelection(ICElement[] tuSelection) {
		HashMap<ICProject, List<ICElement>> result= new HashMap<ICProject, List<ICElement>>();
		allElements: for (int i = 0; i < tuSelection.length; i++) {
			ICElement element = tuSelection[i];
			if (element instanceof ICProject || element instanceof ICContainer || element instanceof ITranslationUnit) {
				ICProject project= element.getCProject();
				List<ICElement> set= result.get(project);
				if (set == null) {
					set= new ArrayList<ICElement>();
					result.put(project, set);
				}
				for (int j= 0; j<set.size(); j++) {
					ICElement other= set.get(j);
					if (contains(other, element)) {
						continue allElements;
					} else if (contains(element, other)) {
						set.set(j, element);
						continue allElements;
					}					
				}
				set.add(element);
			}
		}
		return result;
	}
	
	private boolean contains(final ICElement a, ICElement b) {
		if (a.equals(b)) {
			return true;
		}
		b= b.getParent();
		if (b == null) {
			return false;
		}
		return contains(a, b);
	}

	private void update(ICProject project, List<ICElement> filesAndFolders, int options) throws CoreException {
		assert !Thread.holdsLock(fProjectToPDOM);
		synchronized (fUpdatePolicies) {
			if ((options & IIndexManager.FORCE_INDEX_INCLUSION) != 0) {
				for (ICElement element : filesAndFolders) {
					if (element instanceof ITranslationUnit) {
						ITranslationUnit tu = (ITranslationUnit) element;
						IIndexFileLocation ifl = IndexLocationFactory.getIFL(tu);
						fFilesIndexedUnconditionlly.add(ifl);
					}
				}
			}
			if ((options & IIndexManager.RESET_INDEX_INCLUSION) != 0) {
				for (ICElement element : filesAndFolders) {
					if (element instanceof ITranslationUnit) {
						ITranslationUnit tu = (ITranslationUnit) element;
						IIndexFileLocation ifl = IndexLocationFactory.getIFL(tu);
						fFilesIndexedUnconditionlly.remove(ifl);
					}
				}
			}
			IPDOMIndexer indexer= getIndexer(project);
			PDOMUpdateTask task= new PDOMUpdateTask(indexer, options);
			task.setTranslationUnitSelection(filesAndFolders);
			if (indexer != null) {
				enqueue(task);
			}
		}
	}

	void handlePostBuildEvent() {
		assert !Thread.holdsLock(fProjectToPDOM);
		synchronized (fUpdatePolicies) {
			for (IndexUpdatePolicy policy : fUpdatePolicies.values()) {
				IPDOMIndexerTask task= policy.createTask();
				if (task != null) {
					enqueue(task);
				}
			}
		}
	}

	protected boolean postponeSetup(final ICProject cproject) {
		synchronized (fSetupParticipants) {
			for (IndexerSetupParticipant sp : fSetupParticipants) {
				if (sp.postponeIndexerSetup(cproject)) {
					fPostponedProjects.add(cproject);
					return true;
				}
			}
			fPostponedProjects.remove(cproject);
			final IndexerSetupParticipant[] participants= fSetupParticipants.toArray(new IndexerSetupParticipant[fSetupParticipants.size()]);
			Runnable notify= new Runnable() {
				@Override
				public void run() {
					for (final IndexerSetupParticipant p : participants) {
						SafeRunner.run(new ISafeRunnable(){
							@Override
							public void handleException(Throwable exception) {
								CCorePlugin.log(exception);
							}
							@Override
							public void run() throws Exception {
								p.onIndexerSetup(cproject);
							}
						});
					}
				}
			};
			scheduleNotification(notify);
		}
		return false;
	}

	/**
	 * @param participant
	 * @param project
	 */
	public void notifyIndexerSetup(IndexerSetupParticipant participant,	ICProject project) {
		if (fInShutDown)
			return;
		synchronized (fSetupParticipants) {
			if (fPostponedProjects.contains(project)) {
				setupProject(project);
			}
		}		
	}

	@Override
	public void addIndexerSetupParticipant(IndexerSetupParticipant participant) {
		synchronized (fSetupParticipants) {
			fSetupParticipants.add(participant);
		}
	}

	@Override
	public void removeIndexerSetupParticipant(IndexerSetupParticipant participant) {
		synchronized (fSetupParticipants) {
			fSetupParticipants.remove(participant);
			for (ICProject project : fPostponedProjects) {
				setupProject(project);
			}
		}
	}
	
	/**
	 * @param project
	 * @return whether the specified project has been registered. If a project has
	 * been registered, clients can call joinIndexer with the knowledge tasks have
	 * been enqueued.
	 */
	public boolean isProjectRegistered(ICProject project) {
		return getIndexer(project) != null;
	}
	
	/**
	 * @param cproject the project to check
	 * @return whether the content in the project fragment of the specified project's index
	 * is complete (contains all sources) and up to date.
	 * @throws CoreException
	 */
	public boolean isProjectContentSynced(ICProject cproject) throws CoreException {
		if (!"true".equals(IndexerPreferences.get(cproject.getProject(), IndexerPreferences.KEY_INDEX_ALL_FILES, null)))  //$NON-NLS-1$
			return true; // no check performed in this case
			
		Set<ITranslationUnit> sources= new HashSet<ITranslationUnit>();
		cproject.accept(new TranslationUnitCollector(sources, null, new NullProgressMonitor()));

		try {
			IIndex index= getIndex(cproject);
			index.acquireReadLock();
			try {
				for(ITranslationUnit tu : sources) {
					IResource resource= tu.getResource();
					if (resource instanceof IFile && isSubjectToIndexing(tu.getLanguage())) {
						IIndexFileLocation location= IndexLocationFactory.getWorkspaceIFL((IFile)resource);
						if (!areSynchronized(new HashSet<IIndexFileLocation>(), index, resource, location)) {
							return false;
						}
					}
				}
			} finally {
				index.releaseReadLock();
			}
		} catch (InterruptedException e) {
			CCorePlugin.log(e);
		}

		return true;
	}
	
	private boolean isSubjectToIndexing(ILanguage language) {
		final int linkageID=language.getLinkageID();
		for (int id : IDS_FOR_LINKAGES_TO_INDEX) {
			if (linkageID == id)
				return true;
		}
		return false;
	}

	/**
	 * Recursively checks that the specified file, and its include are up-to-date.
	 * @param trail a set of previously checked include file locations
	 * @param index the index to check against
	 * @param resource the resource to check from the workspace
	 * @param location the location to check from the index
	 * @return whether the specified file, and its includes are up-to-date.
	 * @throws CoreException
	 */
	private static boolean areSynchronized(Set<IIndexFileLocation> trail, IIndex index, IResource resource, IIndexFileLocation location) throws CoreException {
		if (!trail.contains(location)) {
			trail.add(location);
			
			IIndexFile[] file= index.getFiles(location);

			// pre-includes may be listed twice (191989)
			if (file.length < 1 || file.length > 2)
				return false;

			if (resource.getLocalTimeStamp() != file[0].getTimestamp())
				return false;

			// if it is up-to-date, the includes have not changed and may
			// be read from the index.
			IIndexInclude[] includes= index.findIncludes(file[0]);
			for(IIndexInclude inc : includes) {
				IIndexFileLocation newLocation= inc.getIncludesLocation();
				if (newLocation != null) {
					String path= newLocation.getFullPath();
					if (path != null) {
						IResource newResource= ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(path));
						if (!areSynchronized(trail, index, newResource, newLocation)) {
							return false;
						}
					}
				}
			}
		}
		
		return true;
	}

	public boolean isFileIndexedUnconditionally(IIndexFileLocation ifl) {
		return fFilesIndexedUnconditionlly.contains(ifl);
	}
}

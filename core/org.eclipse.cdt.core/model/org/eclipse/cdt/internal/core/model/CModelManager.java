package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.IArchive;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public class CModelManager implements IResourceChangeListener {

    /**
     * Unique handle onto the CModel
     */
    final CModel cModel = new CModel();
    
    public static HashSet OptionNames = new HashSet(20);
        
	/**
	 * Used to convert <code>IResourceDelta</code>s into <code>ICElementDelta</code>s.
	 */
	protected DeltaProcessor fDeltaProcessor = new DeltaProcessor();

	/**
	 * Queue of deltas created explicily by the C Model that
	 * have yet to be fired.
	 */
	private ArrayList fCModelDeltas = new ArrayList();

	/**
	 * Turns delta firing on/off. By default it is on.
	 */
	protected boolean fFire = true;

	/**
	 * Collection of listeners for C element deltas
	 */
	protected ArrayList fElementChangedListeners = new ArrayList();

	/**
	 * A map from ITranslationUnit to IWorkingCopy of the shared working copies.
	 */
	public Map sharedWorkingCopies = new HashMap();
	/**
	 * Set of elements which are out of sync with their buffers.
	 */
	protected Map elementsOutOfSynchWithBuffers = new HashMap(11);

	/**
	 * Infos cache.
	 */
	protected CModelCache cache = new CModelCache();

	/**
	 * This is a cache of the projects before any project addition/deletion has started.
	 */
	public ICProject[] cProjectsCache;

	/**
	 * The list of started BinaryRunners on projects.
	 */
	private HashMap binaryRunners = new HashMap();
	
	/**
	 * The lis of the SourceMappers on projects.
	 */
	private HashMap sourceMappers = new HashMap();

	public static final String [] sourceExtensions = {"c", "cxx", "cc", "C", "cpp"};

	public static final String [] headerExtensions = {"h", "hh", "hpp", "H"};
	
	public static final IWorkingCopy[] NoWorkingCopy = new IWorkingCopy[0];

	static CModelManager factory = null;
	
	private CModelManager() {
	}

	public static CModelManager getDefault() {
		if (factory == null) {
			factory = new CModelManager();

			// Register to the workspace;
			ResourcesPlugin.getWorkspace().addResourceChangeListener(factory,
				 IResourceChangeEvent.PRE_AUTO_BUILD
				| IResourceChangeEvent.POST_CHANGE
				| IResourceChangeEvent.PRE_DELETE
				| IResourceChangeEvent.PRE_CLOSE);
		}
		return factory;
	}

	/**
	 * Returns the CModel for the given workspace, creating
	 * it if it does not yet exist.
	 */
	public ICModel getCModel(IWorkspaceRoot root) {
		return getCModel();
		//return create(root);
	}

	public ICModel getCModel() {
		return cModel;
	}

	public ICElement create (IPath path) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		// Assume it is fullpath relative to workspace
		IResource res = root.findMember(path);
		if (res == null) {
			IPath rootPath = root.getLocation();
			if (path.equals(rootPath))
				return getCModel(root);
			res = root.getContainerForLocation(path);
			if (res == null || !res.exists())
				res = root.getFileForLocation(path);
			if (res != null && !res.exists())
				res = null;
		}
		// TODO: for extenal resources ??
		return create(res);
	}

	public ICElement create (IResource resource) {
		if (resource == null) {
			return null;
		}
		int type = resource.getType();
		switch (type) {
			case IResource.PROJECT :
				return create((IProject)resource);
			case IResource.FILE :
				return create((IFile)resource);
			case IResource.FOLDER :
				return create((IFolder)resource);
			case IResource.ROOT :
				return create((IWorkspaceRoot)resource);
			default :
				return null;
		}
	}

	public ICElement create(ICElement parent, IResource resource) {
		int type = resource.getType();
		switch (type) {
			case IResource.PROJECT :
				return create(parent, (IProject)resource);
			case IResource.FILE :
				return create(parent, (IFile)resource);
			case IResource.FOLDER :
				return create(parent, (IFolder)resource);
			case IResource.ROOT :
				return create(parent, (IWorkspaceRoot)resource);
			default :
				return null;
		}
	}

	public ICElement create(IFile file) {
		IResource parent = file.getParent();
		ICElement cparent = null;
		if (parent instanceof IFolder) {
			cparent = create((IFolder)parent);
		} else if (parent instanceof IProject) {
			cparent = create((IProject)parent);
		}
		if (cparent != null)
			return create(cparent, file);
		return null;
	}

	public ICElement create(ICElement parent, IFile file) {
		return create(parent, file, null);
	}

	public synchronized ICElement create(ICElement parent, IFile file, IBinaryFile bin) {
		ICElement cfile = null;
		
		if (isTranslationUnit(file)) {
			cfile = new TranslationUnit(parent, file);}
						
		if (file.exists()) {
			// Try to create the binaryFile first.
			if (bin == null) {
				bin = createBinaryFile(file);
			}
			if (bin != null) {
				if (bin.getType() == IBinaryFile.ARCHIVE) {
					cfile = new Archive(parent, file);
				} else {
					cfile = new Binary(parent, file, bin);
				}
			} 
		}
		// Added also to the Containers
		if (cfile != null && (cfile instanceof IBinary || cfile instanceof IArchive)) {
			if (bin == null) {
				bin = createBinaryFile(file);
			}
			if (bin != null) {
				if (bin.getType() == IBinaryFile.ARCHIVE) {
					CProject cproj = (CProject)cfile.getCProject();
					ArchiveContainer container = (ArchiveContainer)cproj.getArchiveContainer();
					container.addChild(cfile);
				} else if (bin.getType() == IBinaryFile.EXECUTABLE || bin.getType() == IBinaryFile.SHARED) {
					CProject cproj = (CProject)cfile.getCProject();
					BinaryContainer container = (BinaryContainer)cproj.getBinaryContainer();
					container.addChild(cfile);
				}
			}
		}
		return cfile;
	}

	public ICContainer create(IFolder folder) {
		IResource parent = folder.getParent();
		ICElement cparent = null;
		if (parent instanceof IFolder) {
			cparent = create ((IFolder)parent);
		} else if (parent instanceof IProject) {
			cparent = create ((IProject)parent);
		}
		if (cparent != null)
			return (ICContainer) create (cparent, folder);
		return null;
	}

	public ICContainer create(ICElement parent, IFolder folder) {
		return new CContainer(parent, folder);
	}
		
	public ICProject create(IProject project) {
		IResource parent = project.getParent();
		ICElement celement = null;
		if (parent instanceof IWorkspaceRoot) {
			celement = create ((IWorkspaceRoot)parent);
		}
		return create(celement, project);
	}

	public ICProject create(ICElement parent, IProject project) {
		if (hasCNature(project)){
			return new CProject(parent, project);
		}
		return null;
	}

	public ICModel create(IWorkspaceRoot root) {
		return getCModel();
		//return new CModel(root);
	}

	public void releaseCElement(ICElement celement) {

		// Guard.
		if (celement == null)
			return;

//System.out.println("RELEASE " + celement.getElementName());

		// Remove from the containers.
		int type = celement.getElementType();
		if (type == ICElement.C_ARCHIVE) {
//System.out.println("RELEASE Archive " + cfile.getElementName());
			CProject cproj = (CProject)celement.getCProject();
			ArchiveContainer container = (ArchiveContainer)cproj.getArchiveContainer();
			container.removeChild(celement);
		} else if (type == ICElement.C_BINARY) {
			if (! ((IBinary)celement).isObject()) {
//System.out.println("RELEASE Binary " + cfile.getElementName());
				CProject cproj = (CProject)celement.getCProject();
				BinaryContainer container = (BinaryContainer)cproj.getBinaryContainer();
				container.removeChild(celement);
			}
		}

		if (celement instanceof IParent) {
			CElementInfo info = ((CElement)celement).getElementInfo();
			if (info != null) {
				ICElement[] children = info.getChildren();
				for (int i = 0; i < children.length; i++) {
					releaseCElement(children[i]);
				}
				// Make sure we destroy the BinaryContainer and ArchiveContainer
				// Since they are not part of the children.
				if (info instanceof CProjectInfo) {
					CProjectInfo pinfo = (CProjectInfo) info;
					if (pinfo.vBin != null) {
						releaseCElement(pinfo.vBin);
					}
					if (pinfo.vLib != null) {
						releaseCElement(pinfo.vLib);
					}
				}
			}
		}

		// Remove the child from the parent list.
		Parent parent = (Parent)celement.getParent();
		if (parent != null) {
			parent.removeChild(celement);
		}

		removeInfo(celement);
	}

	public IBinaryParser getBinaryParser(IProject project) {
		try {
			return CCorePlugin.getDefault().getBinaryParser(project);
		} catch (CoreException e) {
		}
		return new NullBinaryParser();
	}

	public IBinaryFile createBinaryFile(IFile file) {
		try {
			IBinaryParser parser = getBinaryParser(file.getProject());
			return parser.getBinary(file.getLocation());
		} catch (IOException e) {
		}
		return null;
	}

	/**
	 * TODO: this is a temporary hack until, the CDescriptor manager is
	 * in place and could fire deltas of Parser change.
	 */
	public void resetBinaryParser(IProject project) {
		if (project != null) {
			ICElement celement = create(project);
			if (celement != null) {
				// Let the function remove the children
				// but it has the side of effect of removing the CProject also
				// so we have to recall create again.
				releaseCElement(celement);
				celement = create(project);
				// Fired and ICElementDelta.PARSER_CHANGED
				CElementDelta delta = new CElementDelta(getCModel());
				delta.binaryParserChanged(celement);
				registerCModelDelta(delta);
				fire();
			}
		}
	}
	
	public boolean isSharedLib(IFile file) {
		try {
			IBinaryParser parser = getBinaryParser(file.getProject());
			IBinaryFile bin = parser.getBinary(file.getLocation());
			return (bin.getType() == IBinaryFile.SHARED);
		} catch (IOException e) {
		}
		return false;
	}

	public boolean isObject(IFile file) {
		try {
			IBinaryParser parser = getBinaryParser(file.getProject());
			IBinaryFile bin = parser.getBinary(file.getLocation());
			return (bin.getType() == IBinaryFile.OBJECT);
		} catch (IOException e) {
		}
		return false;
	}

	public boolean isExecutable(IFile file) {
		try {
			IBinaryParser parser = getBinaryParser(file.getProject());
			IBinaryFile bin = parser.getBinary(file.getLocation());
			return (bin.getType() == IBinaryFile.EXECUTABLE);
		} catch (IOException e) {
			//e.printStackTrace();
		}
		return false;
	}

	public boolean isBinary(IFile file) {
		try {
			IBinaryParser parser = getBinaryParser(file.getProject());
			IBinaryFile bin = parser.getBinary(file.getLocation());
			return (bin.getType() == IBinaryFile.EXECUTABLE
				|| bin.getType() == IBinaryFile.OBJECT
				|| bin.getType() == IBinaryFile.SHARED
				|| bin.getType() == IBinaryFile.CORE);
		} catch (IOException e) {
		}
		return false;
	}

	public boolean isArchive(IFile file) {
		try {
			IBinaryParser parser = getBinaryParser(file.getProject());
			IBinaryFile bin = parser.getBinary(file.getLocation());
			return (bin.getType() == IBinaryFile.ARCHIVE);
		} catch (IOException e) {
		}
		return false;
	}

	public boolean isTranslationUnit(IFile file) {
		return isValidTranslationUnitName(file.getName());
	}

	public boolean isValidTranslationUnitName(String name){
		if (name == null) {
			return false;
		}
		int index = name.lastIndexOf('.');
		if (index == -1) {
			return false;
		}
		String ext = name.substring(index + 1);
		String[] cexts = getTranslationUnitExtensions();
		for (int i = 0; i < cexts.length; i++) {
			if (ext.equals(cexts[i]))
				return true;
		}
		return false;
	}
	
	public String[] getHeaderExtensions() {
		return headerExtensions;
	}
	
	public String[] getSourceExtensions() {
		return sourceExtensions;
	}

	public String[] getTranslationUnitExtensions() {
		String[] headers = getHeaderExtensions();
		String[] sources = getSourceExtensions();
		String[] cexts = new String[headers.length + sources.length];
		System.arraycopy(sources, 0, cexts, 0, sources.length);
		System.arraycopy(headers, 0, cexts, sources.length, headers.length);
		return cexts;
	}

	/* Only project with C nature and Open.  */
	public boolean hasCNature (IProject p) {
		boolean ok = false;
		try {
			ok = (p.isOpen() && p.hasNature(CProjectNature.C_NATURE_ID));
		} catch (CoreException e) {
			//throws exception if the project is not open.
			//System.out.println (e);
			//e.printStackTrace();
		}
		return ok;
	}

	/* Only project with C++ nature and Open.  */
	public boolean hasCCNature (IProject p) {
		boolean ok = false;
		try {
			ok = (p.isOpen() && p.hasNature(CCProjectNature.CC_NATURE_ID));
		} catch (CoreException e) {
			//throws exception if the project is not open.
			//System.out.println (e);
			//e.printStackTrace();
		}
		return ok;
	}

	public BinaryRunner getBinaryRunner(ICProject cProject) {
		BinaryRunner runner = null;
		synchronized(binaryRunners) {
			runner = (BinaryRunner)binaryRunners.get(cProject);
			if (runner == null) {
				runner = new BinaryRunner(cProject);
				binaryRunners.put(cProject, runner);
			}
		}
		return runner;
	}

	public SourceMapper getSourceMapper(ICProject cProject) {
		SourceMapper mapper = null;
		synchronized(sourceMappers) {
			mapper = (SourceMapper) sourceMappers.get(cProject);
			if (mapper == null) {
				mapper = new SourceMapper(cProject);
				sourceMappers.put(cProject, mapper);
			}
		}
		return mapper;
	}
	/**
	 * addElementChangedListener method comment.
	 */
	public synchronized void addElementChangedListener(IElementChangedListener listener) {
		if (fElementChangedListeners.indexOf(listener) < 0) {
			fElementChangedListeners.add(listener);
		}
	}

	/**
	 * removeElementChangedListener method comment.
	 */
	public synchronized void removeElementChangedListener(IElementChangedListener listener) {
		int i = fElementChangedListeners.indexOf(listener);
		if (i != -1) {
			fElementChangedListeners.remove(i);
		}
	}

	/**
	 * Registers the given delta with this manager. This API is to be
	 * used to registerd deltas that are created explicitly by the C
	 * Model. Deltas created as translations of <code>IResourceDeltas</code>
	 * are to be registered with <code>#registerResourceDelta</code>.
	 */
	public synchronized void registerCModelDelta(ICElementDelta delta) {
		fCModelDeltas.add(delta);
	}

	/**
	 * Notifies this C Model Manager that some resource changes have happened
	 * on the platform, and that the C Model should update any required
	 * internal structures such that its elements remain consistent.
	 * Translates <code>IResourceDeltas</code> into <code>ICElementDeltas</code>.
	 *
	 * @see IResourceDelta
	 * @see IResource 
	 */
	public void resourceChanged(IResourceChangeEvent event) {

		if (event.getSource() instanceof IWorkspace) {
			IResourceDelta delta = event.getDelta();
			IResource resource = event.getResource();
			switch(event.getType()){
				case IResourceChangeEvent.PRE_DELETE :
				try{
					if (resource.getType() == IResource.PROJECT && 	
					    ( ((IProject)resource).hasNature(CProjectNature.C_NATURE_ID) ||
					      ((IProject)resource).hasNature(CCProjectNature.CC_NATURE_ID) )){
						this.deleting((IProject) resource);}
				}catch (CoreException e){
				}
				break;

				case IResourceChangeEvent.PRE_AUTO_BUILD :
					// No need now.
					if(delta != null) {
						this.checkProjectsBeingAddedOrRemoved(delta);
					}										
				break;

				case IResourceChangeEvent.POST_CHANGE :
					try {
						if (delta != null) {
							ICElementDelta[] translatedDeltas = fDeltaProcessor.processResourceDelta(delta);
							if (translatedDeltas.length > 0) {
								for (int i= 0; i < translatedDeltas.length; i++) {
									registerCModelDelta(translatedDeltas[i]);
								}
							}
							fire();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}					
				break;
			}
		}
	}

	/**
	 * Fire C Model deltas, flushing them after the fact. 
	 * If the firing mode has been turned off, this has no effect. 
	 */
	public synchronized void fire() {
		if (fFire) {
			mergeDeltas();
			try {
				Iterator iterator = fCModelDeltas.iterator();
				while (iterator.hasNext()) {
					ICElementDelta delta= (ICElementDelta) iterator.next();

					// Refresh internal scopes

					ElementChangedEvent event= new ElementChangedEvent(delta);
	// Clone the listeners since they could remove themselves when told about the event 
	// (eg. a type hierarchy becomes invalid (and thus it removes itself) when the type is removed
					ArrayList listeners= (ArrayList) fElementChangedListeners.clone();
					for (int i= 0; i < listeners.size(); i++) {
						IElementChangedListener listener= (IElementChangedListener) listeners.get(i);
						listener.elementChanged(event);
					}
				}
			} finally {
				// empty the queue
				this.flush();
			}
		}
	}

	/**
	 * Flushes all deltas without firing them.
	 */
	protected synchronized void flush() {
		fCModelDeltas= new ArrayList();
	}

	/**
	 * Merged all awaiting deltas.
	 */
	private void mergeDeltas() {
		if (fCModelDeltas.size() <= 1)
			return;

		Iterator deltas = fCModelDeltas.iterator();
		ICElement cRoot = getCModel();
		CElementDelta rootDelta = new CElementDelta(cRoot);
		boolean insertedTree = false;
		while (deltas.hasNext()) {
			CElementDelta delta = (CElementDelta)deltas.next();
			ICElement element = delta.getElement();
			if (cRoot.equals(element)) {
				ICElementDelta[] children = delta.getAffectedChildren();
				for (int j = 0; j < children.length; j++) {
					CElementDelta projectDelta = (CElementDelta) children[j];
					rootDelta.insertDeltaTree(projectDelta.getElement(), projectDelta);
					insertedTree = true;
				}
			} else {
				rootDelta.insertDeltaTree(element, delta);
				insertedTree = true;
			}
		}
		if (insertedTree) {
			fCModelDeltas = new ArrayList(1);
			fCModelDeltas.add(rootDelta);
		} else {
			fCModelDeltas = new ArrayList(0);
		}
	}

	/**
	 * Runs a C Model Operation
	 */
	public void runOperation(CModelOperation operation, IProgressMonitor monitor) throws CModelException {
		boolean hadAwaitingDeltas = !fCModelDeltas.isEmpty();
		try {
			if (operation.isReadOnly()) {
				operation.run(monitor);
			} else {
		// use IWorkspace.run(...) to ensure that a build will be done in autobuild mode
				getCModel().getUnderlyingResource().getWorkspace().run(operation, monitor);
			}
		} catch (CoreException ce) {
			if (ce instanceof CModelException) {
				throw (CModelException)ce;
			} else {
				if (ce.getStatus().getCode() == IResourceStatus.OPERATION_FAILED) {
					Throwable e= ce.getStatus().getException();
					if (e instanceof CModelException) {
						throw (CModelException) e;
					}
				}
				throw new CModelException(ce);
			}
		} finally {
// fire only if there were no awaiting deltas (if there were, they would come from a resource modifying operation)
// and the operation has not modified any resource
			if (!hadAwaitingDeltas && !operation.hasModifiedResource()) {
				fire();
			} // else deltas are fired while processing the resource delta
		}
	}
	
	/**
	 * Process the given delta and look for projects being added, opened,
	 * or closed
	 */
	public void checkProjectsBeingAddedOrRemoved(IResourceDelta delta) {
		IResource resource = delta.getResource();
		switch (resource.getType()) {
			case IResource.ROOT :
			if (this.cProjectsCache == null) {
				this.cProjectsCache = this.getCModel().getCProjects();
			}
				
			IResourceDelta[] children = delta.getAffectedChildren();
			for (int i = 0, length = children.length; i < length; i++) {
				this.checkProjectsBeingAddedOrRemoved(children[i]);
			}			
			break;
		case IResource.PROJECT :
			// TO BE COMPLETED ...
			break;
		}
	}
	/** 
	 * Returns the set of elements which are out of synch with their buffers.
	 */
	protected Map getElementsOutOfSynchWithBuffers() {
		return this.elementsOutOfSynchWithBuffers;
	}
	
	/**
	 *  Returns the info for the element.
	 */
	public Object getInfo(ICElement element) {
		return this.cache.getInfo(element);
	}
	/**
	 *  Returns the info for this element without
	 *  disturbing the cache ordering.
	 */
	protected Object peekAtInfo(ICElement element) {
		return this.cache.peekAtInfo(element);
	}

	/**
	 * Puts the info for a C Model Element
	 */
	protected void putInfo(ICElement element, Object info) {
		this.cache.putInfo(element, info);
	}
	
	/** 
	 * Removes the info of this model element.
	 */
	protected void removeInfo(ICElement element) {
		this.cache.removeInfo(element);
	}

	/**
	 * 
	 */
	public void startup() {
		// Do any initialization.	
	}

	/**
	 * 
	 */
	public void shutdown() {
		if (this.fDeltaProcessor.indexManager != null){ // no more indexing
					this.fDeltaProcessor.indexManager.shutdown();
		}
		
		// Do any shutdown of services.
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(factory);	

		BinaryRunner[] runners = (BinaryRunner[])binaryRunners.values().toArray(new BinaryRunner[0]);
		for (int i = 0; i < runners.length; i++) {
			if (runners[i].isAlive()) {
				runners[i].interrupt();
			}
		}
	}
	
	public IndexManager getIndexManager() {
		return this.fDeltaProcessor.indexManager;
	}
	
	public void deleting(IProject project){
		//	discard all indexing jobs for this project
		this.getIndexManager().discardJobs(project.getName());
	}
}

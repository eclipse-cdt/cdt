package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.IArchive;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICFile;
import org.eclipse.cdt.core.model.ICFolder;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ICResource;
import org.eclipse.cdt.core.model.ICRoot;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.utils.elf.AR;
import org.eclipse.cdt.utils.elf.Elf;
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

	private HashMap fParsedResources =  new HashMap();	

	/**
	 * Used to convert <code>IResourceDelta</code>s into <code>IJavaElementDelta</code>s.
	 */
	protected DeltaProcessor fDeltaProcessor= new DeltaProcessor();

	/**
	 * Queue of deltas created explicily by the C Model that
	 * have yet to be fired.
	 */
	private ArrayList fCModelDeltas= new ArrayList();

	/**
	 * Turns delta firing on/off. By default it is on.
	 */
	protected boolean fFire= true;

	/**
	 * Collection of listeners for C element deltas
	 */
	protected ArrayList fElementChangedListeners= new ArrayList();

	public static final String [] cExtensions = {"c", "cxx", "cc", "C", "cpp", "h", "hh"};

	static CModelManager factory = null;
	
	private CModelManager() {
	}

	public static CModelManager getDefault() {
		if (factory == null) {
			factory = new CModelManager ();

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
	 * Returns the CRoot for the given workspace, creating
	 * it if it does not yet exist.
	 */
	public ICRoot getCRoot(IWorkspaceRoot root) {
		return create(root);
	}

	public ICRoot getCRoot () {
		return create(ResourcesPlugin.getWorkspace().getRoot());
	}

	public ICResource create (IPath path) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		// Assume it is fullpath relative to workspace
		IResource res = root.findMember(path);
		if (res == null) {
			IPath rootPath = root.getLocation();
			if (path.equals(rootPath))
				return getCRoot(root);
			res = root.getContainerForLocation(path);
			if (res == null)
				res = root.getFileForLocation(path);
		}
		return create (res);
	}

	public ICResource create (IResource resource) {
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

	public ICResource create(ICElement parent, IResource resource) {
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

	public ICFile create(IFile file) {
		IResource parent = file.getParent();
		ICElement cparent = null;
		if (parent instanceof IFolder) {
			cparent = create ((IFolder)parent);
		} else if (parent instanceof IProject) {
			cparent = create ((IProject)parent);
		}
		if (cparent != null)
			return (ICFile) create (cparent, file);
		return null;
	}

	public synchronized ICFile create(ICElement parent, IFile file) {
		ICFile cfile = (ICFile)fParsedResources.get(file);
		if (cfile == null) {
			if (file.exists()) {
				if (isArchive(file)) {
					cfile = new Archive(parent, file);
				} else if (isBinary(file)) {
					cfile = new Binary(parent, file);
				} else if (isTranslationUnit(file)) {
					cfile = new TranslationUnit(parent, file);
				} else {
					cfile = new CFile(parent, file);
				}
				fParsedResources.put(file, cfile);
			}
		}
		// Added also to the Containers
		if (cfile != null) {
			if (cfile instanceof IArchive) {
				CProject cproj = (CProject)cfile.getCProject();
				ArchiveContainer container = (ArchiveContainer)cproj.getArchiveContainer();
				container.addChild(cfile);
			} else if (cfile instanceof IBinary) {
				IBinary bin = (IBinary)cfile;
				if (bin.isExecutable() || bin.isSharedLib()) {
					CProject cproj = (CProject)cfile.getCProject();
					BinaryContainer container = (BinaryContainer)cproj.getBinaryContainer();
					container.addChild(bin);
				}
			}
		}
		return cfile;
	}

	public ICFolder create(IFolder folder) {
		IResource parent = folder.getParent();
		ICElement cparent = null;
		if (parent instanceof IFolder) {
			cparent = create ((IFolder)parent);
		} else if (parent instanceof IProject) {
			cparent = create ((IProject)parent);
		}
		if (cparent != null)
			return (ICFolder) create (cparent, folder);
		return null;
	}

	public synchronized ICFolder create(ICElement parent, IFolder folder) {
		ICFolder cfolder = (ICFolder)fParsedResources.get(folder);
		if (cfolder == null) {
			cfolder = new CFolder(parent, folder);
			fParsedResources.put(folder, cfolder);
		}
		return cfolder;
	}
		
	public ICProject create(IProject project) {
		IResource parent = project.getParent();
		ICElement celement = null;
		if (parent instanceof IWorkspaceRoot) {
			celement = create ((IWorkspaceRoot)parent);
		}
		return create(celement, project);
	}

	public synchronized ICProject create(ICElement parent, IProject project) {
		ICProject cproject = (ICProject)fParsedResources.get(project);
		if (cproject == null) {
			if (hasCNature(project)) {
				cproject = new CProject(parent, project);
				fParsedResources.put(project, cproject);
			}
		}
		return cproject;
	}

	public ICRoot create(IWorkspaceRoot root) {
		ICRoot croot = (ICRoot)fParsedResources.get(root);
		if (croot == null) {
			croot = new CRoot(root);
			fParsedResources.put(root, croot);
		}
		return croot;
	}

	private void removeChildrenContainer(Parent container, IResource resource) {
		if ( container.hasChildren() ) {
			ICElement[] children = container.getChildren();
			for (int i = 0; i < children.length; i++) {
				try {
					IResource r = children[i].getUnderlyingResource();
					if (r.equals(resource)) {
						container.removeChild(children[i]);
						break;
					}
				} catch (CModelException e) {
				}
			}
		}
	}


	public void releaseCElement(IResource resource) {
		ICElement celement = getCElement(resource);
		if (celement == null) {
			if (resource.exists()) {
				celement = create(resource);
			} else {
				// Make sure they are not in the Containers.
				CProject cproj = (CProject)create(resource.getProject());
				if (cproj != null) {
					Parent container = (Parent)cproj.getArchiveContainer();
					removeChildrenContainer(container, resource);
					container = (Parent)cproj.getBinaryContainer();
					removeChildrenContainer(container, resource);
				}
			}
		}
		releaseCElement(celement);
	}

	public void releaseCElement(ICElement celement) {

		// Guard.
		if (celement == null)
			return;

//System.out.println("RELEASE " + celement.getElementName());

		// Remove from the containers.
		if (celement.getElementType() == ICElement.C_FILE) {
			CFile cfile = (CFile)celement;
			if (cfile.isArchive()) {
//System.out.println("RELEASE Archive " + cfile.getElementName());
				CProject cproj = (CProject)cfile.getCProject();
				ArchiveContainer container = (ArchiveContainer)cproj.getArchiveContainer();
				container.removeChild(cfile);
			} else if (cfile.isBinary()) {
				if (! ((IBinary)celement).isObject()) {
//System.out.println("RELEASE Binary " + cfile.getElementName());
					CProject cproj = (CProject)cfile.getCProject();
					BinaryContainer container = (BinaryContainer)cproj.getBinaryContainer();
					container.removeChild(cfile);
				}
			}
		}

		Parent parent = (Parent)celement.getParent();
		if (parent != null) {
			parent.removeChild(celement);
		}
		fParsedResources.remove(celement);
	}

	public ICElement getCElement(IResource res) {
		return (ICElement)fParsedResources.get(res);
	}

	public ICElement getCElement(IPath path) {
		Iterator iterator = fParsedResources.keySet().iterator();
		while (iterator.hasNext()) {
			IResource res = (IResource)iterator.next();
			if (res.getFullPath().equals(path)) {
				return (ICElement)fParsedResources.get(res);
			}
		}
		return null;
	}

	public static boolean isSharedLib(IFile file) {
		try {
			Elf.Attribute attribute = Elf.getAttributes(file.getLocation().toOSString());
			if (attribute.getType() == Elf.Attribute.ELF_TYPE_SHLIB) {
				return true;
			}
		} catch (IOException e) {
			//e.printStackTrace();
		}
		return false;
	}

	public static boolean isObject(IFile file) {
		try {
			Elf.Attribute attribute = Elf.getAttributes(file.getLocation().toOSString());
			if (attribute.getType() == Elf.Attribute.ELF_TYPE_OBJ) {
				return true;
			}
		} catch (IOException e) {
			//e.printStackTrace();
		}
		return false;
	}

	public static boolean isExecutable(IFile file) {
		try {
			Elf.Attribute attribute = Elf.getAttributes(file.getLocation().toOSString());
			if (attribute.getType() == Elf.Attribute.ELF_TYPE_EXE) {
				return true;
			}
		} catch (IOException e) {
			//e.printStackTrace();
		}
		return false;
	}

	public static boolean isBinary(IFile file) {
		try {
			Elf.Attribute attribute = Elf.getAttributes(file.getLocation().toOSString());
			if (attribute.getType() == Elf.Attribute.ELF_TYPE_EXE
				|| attribute.getType() == Elf.Attribute.ELF_TYPE_OBJ
				|| attribute.getType() == Elf.Attribute.ELF_TYPE_SHLIB) {
				return true;
			}
		} catch (IOException e) {
			//e.printStackTrace();
		}
		return false;
	}

	public static boolean isArchive(IFile file) {
		AR ar = null;
		try {
			ar = new AR(file.getLocation().toOSString()); 
		} catch (IOException e) {
			//e.printStackTrace();
		}
		if (ar != null) {
			ar.dispose();
			return true;
		}
		return false;
	}

	public static boolean isTranslationUnit(IFile file) {
		return isValidTranslationUnitName(file.getName());
	}

	public static boolean isValidTranslationUnitName(String name){
		if (name == null) {
			return false;
		}
		int index = name.lastIndexOf('.');
		if (index == -1) {
			return false;
		}
		String ext = name.substring(index + 1);
		for (int i = 0; i < cExtensions.length; i++) {
			if (ext.equals(cExtensions[i]))
				return true;
		}
		return false;
	}

	/* Only project with C nature and Open.  */
	public static boolean hasCNature (IProject p) {
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
	public static boolean hasCCNature (IProject p) {
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
			IResource resource = event.getResource();
			IResourceDelta delta = event.getDelta();
			switch(event.getType()){
				case IResourceChangeEvent.PRE_DELETE :
					if(resource.getType() == IResource.PROJECT
						&& hasCNature((IProject)resource)) {
						releaseCElement(resource);
					}
				break;

				case IResourceChangeEvent.PRE_AUTO_BUILD :
					// will close project if affected by the property file change
				break;

				case IResourceChangeEvent.POST_CHANGE :
					if (delta != null) {
						try {
							ICElementDelta[] translatedDeltas = fDeltaProcessor.processResourceDelta(delta);
							if (translatedDeltas.length > 0) {
								for (int i= 0; i < translatedDeltas.length; i++) {
									registerCModelDelta(translatedDeltas[i]);
								}
							}
							fire();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				break;
			}
		}
	}

	/**
	 * Note that the project is about to be deleted.
	 *
	 * fix for 1FW67PA
	 */
	public void deleting(IResource resource) {
		deleting(getCElement(resource));
	}

	public void deleting(ICElement celement) {
		releaseCElement(celement);
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
		ICElement cRoot = getCRoot();
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
		if (insertedTree){
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
				getCRoot().getUnderlyingResource().getWorkspace().run(operation, monitor);
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
}

package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IArchiveContainer;
import org.eclipse.cdt.core.model.IBinaryContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.IPath;

import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.cdt.internal.core.sourcedependency.DependencyManager;

/**
 * This class is used by <code>CModelManager</code> to convert
 * <code>IResourceDelta</code>s into <code>ICElementDelta</code>s.
 * It also does some processing on the <code>CElement</code>s involved
 * (e.g. closing them or updating classpaths).
 */
public class DeltaProcessor {
	
	/**
	 * The <code>CElementDelta</code> corresponding to the <code>IResourceDelta</code> being translated.
	 */
	protected CElementDelta fCurrentDelta;
	
	protected IndexManager indexManager = new IndexManager();
	
	protected DependencyManager sourceDependencyManager = new DependencyManager();
	
	/* The C element that was last created (see createElement(IResource). 
	 * This is used as a stack of C elements (using getParent() to pop it, and 
	 * using the various get*(...) to push it. */
	ICElement currentElement;
	
	static final ICElementDelta[] NO_DELTA = new ICElementDelta[0];

	public static boolean VERBOSE = false;

	// Hold on the element bein renamed.
	ICElement movedFromElement = null;

	/**
	 * Creates the create corresponding to this resource.
	 * Returns null if none was found.
	 */
	protected ICElement createElement(IResource resource) {
		CModelManager manager = CModelManager.getDefault();
		if (resource == null)
			return null;
		ICElement celement = manager.create(resource);
		if (celement == null) {
			ICElement parent = manager.create(resource.getParent());
			// Probably it was deleted, find it
			if (parent instanceof IParent) {
				ICElement[] children = ((CElement)parent).getElementInfo().getChildren();
				for (int i = 0; i < children.length; i++) {
					IResource res = children[i].getResource();
					if (res != null && res.equals(resource)) {
						celement = children[i];
						break;
					}
				}
				// BUG 36424:
				// The Binary may only be visible in the BinaryContainers
				if (celement == null) {
					ICProject cproj = parent.getCProject();
					if (cproj != null) {
						IBinaryContainer bin = cproj.getBinaryContainer();
						children = ((CElement)bin).getElementInfo().getChildren();
						for (int i = 0; i < children.length; i++) {
							IResource res = children[i].getResource();
							if (res != null && res.equals(resource)) {
								celement = children[i];
								break;
							}
						}
					}
				}
				// BUG 36424:
				// The Archive may only be visible in the ArchiveContainers
				if (celement == null) {
					ICProject cproj = parent.getCProject();
					if (cproj != null) {
						IArchiveContainer bin = cproj.getArchiveContainer();
						children = ((CElement)bin).getElementInfo().getChildren();
						for (int i = 0; i < children.length; i++) {
							IResource res = children[i].getResource();
							if (res != null && res.equals(resource)) {
								celement = children[i];
								break;
							}
						}
					}				
				}
			}
		}
		return celement;
	}

	/**
	 * Creates the create corresponding to this resource.
	 * Returns null if none was found.
	 */
	protected ICElement createElement(IPath path) {
		return CModelManager.getDefault().create(path);
	}

	/**
	 * Release the Element from the CModel hastable.
	 * Returns null if none was found.
	 */
	protected void releaseCElement(ICElement celement) {
		CModelManager.getDefault().releaseCElement(celement);
	}

	/**
	 * Adds the given child handle to its parent's cache of children. 
	 */
	protected void addToParentInfo(Openable child) {
		Openable parent = (Openable) child.getParent();
		if (parent != null && parent.isOpen()) {
			CElementInfo info = (CElementInfo)parent.getElementInfo();
			info.addChild(child);
		}
	}

	/**
	 * Processing for an element that has been added:<ul>
	 * <li>If the element is a project, do nothing, and do not process
	 * children, as when a project is created it does not yet have any
	 * natures - specifically a java nature.
	 * <li>If the elemet is not a project, process it as added (see
	 * <code>basicElementAdded</code>.
	 * </ul>
	 */
	protected void elementAdded(ICElement element, IResourceDelta delta) {

		if (element instanceof Openable) {
			addToParentInfo((Openable)element);
		}
		if ((delta.getFlags() & IResourceDelta.MOVED_FROM) != 0) {
			//ICElement movedFromElement = createElement(delta.getMovedFromPath());
			if  (movedFromElement == null)
				movedFromElement = createElement(delta.getMovedFromPath());
			fCurrentDelta.movedTo(element, movedFromElement);
			movedFromElement = null;
		} else {
			fCurrentDelta.added(element);
		}
	}

	/**
	 * Processing for the closing of an element - there are two cases:<ul>
	 * <li>when a project is closed (in the platform sense), the
	 * 		CModel reports this as if the CProject has been removed.
	 * <li>otherwise, the CModel reports this
	 *		as a the element being closed (CHANGED + F_CLOSED).
	 * </ul>
	 * <p>In both cases, the children of the element are not processed. When
	 * a resource is closed, the platform reports all children as removed. This
	 * would effectively delete the classpath if we processed children.
	 */
	protected void elementClosed(ICElement element, IResourceDelta delta) {

		if (element.getElementType() == ICElement.C_PROJECT) {
			// treat project closing as removal
			elementRemoved(element, delta);
		} else {
			fCurrentDelta.closed(element);
		}
	}

	/**
	 * Processing for the opening of an element - there are two cases:<ul>
	 * <li>when a project is opened (in the platform sense), the
	 * 		CModel reports this as if the CProject has been added.
	 * <li>otherwise, the CModel reports this
	 *		as a the element being opened (CHANGED + F_CLOSED).
	 * </ul>
	 */
	protected void elementOpened(ICElement element, IResourceDelta delta) {

		if (element.getElementType() == ICElement.C_PROJECT) {
			// treat project opening as addition
			if (hasCNature(delta.getResource())) {
				elementAdded(element, delta);
			}
		} else {
			fCurrentDelta.opened(element);
		}
	}

	/*
	 * Closes the given element, which removes it from the cache of open elements.
	 */
	private void close(Openable element) {
		try {
			element.close();
		} catch (CModelException e) {
			// do nothing
		}
	}

	/**
	 * Generic processing for elements with changed contents:<ul>
	 * <li>The element is closed such that any subsequent accesses will re-open
	 * the element reflecting its new structure.
	 * <li>An entry is made in the delta reporting a content change (K_CHANGE with F_CONTENT flag set).
	 * </ul>
	 */
	protected void elementChanged(ICElement element, IResourceDelta delta) {
		if (element instanceof Openable) {
			close((Openable)element);
		}
		fCurrentDelta.changed(element, ICElementDelta.F_CONTENT);
	}

	/**
	 * Generic processing for a removed element:<ul>
	 * <li>Close the element, removing its structure from the cache
	 * <li>Remove the element from its parent's cache of children
	 * <li>Add a REMOVED entry in the delta
	 * </ul>
	 */
	protected void elementRemoved(ICElement element, IResourceDelta delta) {
		if ((delta.getFlags() & IResourceDelta.MOVED_TO) != 0) {
			IPath movedToPath = delta.getMovedToPath();
			// create the moved to element
			ICElement movedToElement = createElement(movedToPath);
			if (movedToElement == null) {
				// moved outside
				fCurrentDelta.removed(element);
			} else {
				movedFromElement = element;
				fCurrentDelta.movedFrom(element, movedToElement);
			}
		} else {
			fCurrentDelta.removed(element);
		}
		releaseCElement(element);
	}

	/**
	 * Filters the generated <code>CElementDelta</code>s to remove those
	 * which should not be fired (because they don't represent a real change
	 * in the C Model).
	 */
	protected ICElementDelta[] filterRealDeltas(ICElementDelta[] deltas) {

		int length = deltas.length;
		ICElementDelta[] realDeltas = null;
		int index = 0;
		for (int i = 0; i < length; i++) {
			CElementDelta delta = (CElementDelta)deltas[i];
			if (delta == null) {
				continue;
			}
			if (delta.getAffectedChildren().length > 0
				|| delta.getKind() == ICElementDelta.ADDED
				|| delta.getKind() == ICElementDelta.REMOVED
				|| (delta.getFlags() & ICElementDelta.F_CLOSED) != 0
				|| (delta.getFlags() & ICElementDelta.F_OPENED) != 0
				|| delta.resourceDeltasCounter > 0) {

				if (realDeltas == null) {
					realDeltas = new ICElementDelta[length];
				}
				realDeltas[index++] = delta;
			}
		}
		if (index > 0) {
			ICElementDelta[] result = new ICElementDelta[index];
			System.arraycopy(realDeltas, 0, result, 0, index);
			return result;
		} else {
			return NO_DELTA;
		}
	}

	/**
	 * Returns true if the given resource is contained in an open project
	 * with a java nature, otherwise false.
	 */
	protected boolean hasCNature(IResource resource) {
		// ensure the project has a C nature (if open)
		IProject project = resource.getProject();
		if (project.isOpen()) {
			return CoreModel.getDefault().hasCNature(project);
		}
		return false;
	}

	/**
	 * Converts a <code>IResourceDelta</code> rooted in a <code>Workspace</code> into
	 * the corresponding set of <code>ICElementDelta</code>, rooted in the
	 * relevant <code>CModel</code>s.
	 */
	public ICElementDelta[] processResourceDelta(IResourceDelta changes) {

		try {
			ICElement root = (ICModel)CModelManager.getDefault().getCModel();
			
/*
			try {
				changes.accept(new IResourceDeltaVisitor() {
					public boolean visit(IResourceDelta delta) {
						switch (delta.getKind()) {
							case IResourceDelta.ADDED :
							// handle added resource
							System.out.print("ADDED ");
							break;
							case IResourceDelta.REMOVED :
							// handle removed resource
							System.out.print("REMOVED ");
							break;
							case IResourceDelta.CHANGED :
							// handle changed resource
							System.out.print("CHANGED ");
							break;
						}
						System.out.println(delta.getResource());
						return true;
					}
				});
			} catch (CoreException e) {
			}
*/
			// get the workspace delta, and start processing there.
			IResourceDelta[] deltas = changes.getAffectedChildren();
			ICElementDelta[] translatedDeltas = new CElementDelta[deltas.length];
			//System.out.println("delta.length: " + deltas.length);
			for (int i = 0; i < deltas.length; i++) {
				IResourceDelta delta = deltas[i];
				fCurrentDelta = new CElementDelta(root);
				traverseDelta(root, delta); // traverse delta
				translatedDeltas[i] = fCurrentDelta;
			}
			return filterRealDeltas(translatedDeltas);
		} finally {
		}
	}
	
	/**
	 * Converts an <code>IResourceDelta</code> and its children into
	 * the corresponding <code>ICElementDelta</code>s.
	 * Return whether the delta corresponds to a resource on the classpath.
	 * If it is not a resource on the classpath, it will be added as a non-java
	 * resource by the sender of this method.
	 */
	protected void traverseDelta(ICElement parent, IResourceDelta delta) {
		try {
			ICElement current = updateCurrentDeltaAndIndex(delta);
			if (current == null) {
				nonCResourcesChanged(parent, delta);
			} else {
				parent = current;
			}
		} catch (CModelException e) {
		}
		IResourceDelta [] children = delta.getAffectedChildren();
		for (int i = 0; i < children.length; i++) {
				traverseDelta(parent, children[i]);
		}
	}

	protected void nonCResourcesChanged(ICElement parent, IResourceDelta delta) {
		CElementDelta elementDelta = fCurrentDelta.find(parent);
		if (elementDelta == null) {
			fCurrentDelta.changed(parent, ICElementDelta.F_CONTENT);
		} else {
			elementDelta.addResourceDelta(delta);
		}
		if (parent instanceof CContainer) {
			CElementInfo info = ((CContainer)parent).getElementInfo();
			if (info instanceof CContainerInfo) {
				((CContainerInfo)info).setNonCResources(null);
			}
		}
	}

	/*
	 * Update the current delta (ie. add/remove/change the given element) and update the
	 * correponding index.
	 * Returns whether the children of the given delta must be processed.
	 * @throws a CModelException if the delta doesn't correspond to a c element of the given type.
	 */
	private ICElement updateCurrentDeltaAndIndex(IResourceDelta delta) throws CModelException {

		IResource resource = delta.getResource();
		ICElement element = createElement(resource);

		switch (delta.getKind()) {
			case IResourceDelta.ADDED :
				if (element != null) {
					updateIndexAddResource(element, delta);
					elementAdded(element, delta);
				}
				break;

			case IResourceDelta.REMOVED :
				if (element != null) {
					updateIndexRemoveResource(element, delta);
					elementRemoved(element, delta);
				}
				break;

			case IResourceDelta.CHANGED :
				int flags = delta.getFlags();
				if ((flags & IResourceDelta.CONTENT) != 0) {
					// content has changed
					if (element != null) {
						elementChanged(element, delta);
						updateIndexAddResource(element, delta);
					}
				} else if (resource.getType() == IResource.PROJECT) {
					if ((flags & IResourceDelta.OPEN) != 0) {
						// project has been opened or closed
						IProject res = (IProject)resource;
						if (element != null) {
							if (res.isOpen()) {
								elementOpened(element, delta);
								updateIndexAddResource(element, delta);
							} else {
								elementClosed(element, delta);
								updateIndexRemoveResource(element, delta);
							}
						}
					} else if ((flags & IResourceDelta.DESCRIPTION) != 0) {
						if (element != null) {
							elementAdded(element, delta);
						}
					} else if (element != null) {
						elementChanged(element, delta);
					}
				} else if (element != null) {
					elementChanged(element, delta);
				}
				break;
		}
		return element;
	}

	protected void updateIndexAddResource(ICElement element, IResourceDelta delta) {
		//CModelManager.getDefault().getIndexManager().addResource(delta.getResource());
	
		if (indexManager == null)
			return;
		
	    switch (element.getElementType()) {
			case ICElement.C_PROJECT :
					this.indexManager.indexAll(element.getCProject().getProject());
					break;
	
			case ICElement.C_UNIT:
				IFile file = (IFile) delta.getResource();
				indexManager.addSource(file, file.getProject().getProject().getFullPath());
				break;						
	    }
		
	}

	protected void updateIndexRemoveResource(ICElement element, IResourceDelta delta) {
		//CModelManager.getDefault().getIndexManager().removeResource(delta.getResource());
	
		if (indexManager == null)
						return;

		switch (element.getElementType()) {
			case ICElement.C_PROJECT :
						this.indexManager.removeIndexFamily(element.getCProject().getProject().getFullPath());
						// NB: Discarding index jobs belonging to this project was done during PRE_DELETE
						break;
						// NB: Update of index if project is opened, closed, or its c nature is added or removed
						//     is done in updateCurrentDeltaAndIndex
						
			case ICElement.C_UNIT:
						IFile file = (IFile) delta.getResource();
						indexManager.remove(file.getFullPath().toString(), file.getProject().getProject().getFullPath());
						break;				
		}
	

	}
}

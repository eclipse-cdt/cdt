package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.IPath;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICRoot;
import org.eclipse.cdt.core.model.ICModelStatusConstants;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;

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

	/* The C element that was last created (see createElement(IResource). 
	 * This is used as a stack of C elements (using getParent() to pop it, and 
	 * using the various get*(...) to push it. */
	ICElement currentElement;
	
	static final ICElementDelta[] NO_DELTA = new ICElementDelta[0];

	public static boolean VERBOSE = false;

	// Hold on the element bein renamed.
	ICElement movedFromElement = null;

	/**
	 * Generic processing for elements with changed contents:<ul>
	 * <li>The element is closed such that any subsequent accesses will re-open
	 * the element reflecting its new structure.
	 * <li>An entry is made in the delta reporting a content change (K_CHANGE with F_CONTENT flag set).
	 * </ul>
	 */
	protected void contentChanged(ICElement element, IResourceDelta delta) {
		fCurrentDelta.changed(element, ICElementDelta.F_CONTENT);
	}

	/**
	 * Creates the create corresponding to this resource.
	 * Returns null if none was found.
	 */
	protected ICElement createElement(IResource resource) {
		if (resource == null)
			return null;
		return CModelManager.getDefault().create(resource);
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
	 * Release the Resource.
	 * Returns null if none was found.
	 */
	protected void releaseCElement(IResource resource) {
		CModelManager.getDefault().releaseCElement(resource);
	}

	/**
	 * get the CElement from the hashtable, if it exist without
	 * creating it.
	 * Returns null if none was found.
	 */
	protected ICElement getElement(IResource res) {
		return CModelManager.getDefault().getCElement(res);
	}

	protected ICElement getElement(IPath path) {
		return CModelManager.getDefault().getCElement(path);
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

		if ((delta.getFlags() & IResourceDelta.MOVED_FROM) != 0) {
			//ICElement movedFromElement = createElement(delta.getMovedFromPath());
			if  (movedFromElement == null)
				movedFromElement = getElement(delta.getMovedFromPath());
			fCurrentDelta.movedTo(element, movedFromElement);
			movedFromElement = null;
		} else {
			fCurrentDelta.added(element);
		}
	}

	/**
	 * Processing for the closing of an element - there are two cases:<ul>
	 * <li>when a project is closed (in the platform sense), the
	 * 		CRoot reports this as if the CProject has been removed.
	 * <li>otherwise, the CRoot reports this
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
	 * 		CRoot reports this as if the CProject has been added.
	 * <li>otherwise, the CRoot reports this
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


	private CModelException newInvalidElementType() {
		return new CModelException(new CModelStatus(ICModelStatusConstants.INVALID_ELEMENT_TYPES));
	}

	/**
	 * Converts a <code>IResourceDelta</code> rooted in a <code>Workspace</code> into
	 * the corresponding set of <code>ICElementDelta</code>, rooted in the
	 * relevant <code>CRoot</code>s.
	 */
	public ICElementDelta[] processResourceDelta(IResourceDelta changes) {

		try {
			ICElement root = (ICRoot)CModelManager.getDefault().getCRoot();
			currentElement = null;
			
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
			for (int i = 0; i < deltas.length; i++) {
				IResourceDelta delta = deltas[i];
				fCurrentDelta = new CElementDelta(root);
				traverseDelta(delta); // traverse delta
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
	protected void traverseDelta(IResourceDelta delta) {
		try {
			if (!updateCurrentDeltaAndIndex(delta)) {
				fCurrentDelta.addResourceDelta(delta);
			}
		} catch (CModelException e) {
		}
		IResourceDelta [] children = delta.getAffectedChildren();
		for (int i = 0; i < children.length; i++) {
				traverseDelta(children[i]);
		}
	}

	/*
	 * Update the current delta (ie. add/remove/change the given element) and update the
	 * correponding index.
	 * Returns whether the children of the given delta must be processed.
	 * @throws a CModelException if the delta doesn't correspond to a c element of the given type.
	 */
	private boolean  updateCurrentDeltaAndIndex(IResourceDelta delta) throws CModelException {

		ICElement element = null;
		IResource resource = delta.getResource();

		boolean isProcess = false;

		switch (delta.getKind()) {
			case IResourceDelta.ADDED :
				element = createElement(resource);
				if (element == null)
					throw newInvalidElementType();
				updateIndexAddResource(element, delta);
				elementAdded(element, delta);
				isProcess = true;
				break;

			case IResourceDelta.REMOVED :
				element = getElement(resource);
				if (element != null) {
					updateIndexRemoveResource(element, delta);
					elementRemoved(element, delta);
					isProcess = true;
				} else {
					releaseCElement(resource);
				}
				break;

			case IResourceDelta.CHANGED :
				element = createElement(resource);
				if (element == null)
					throw newInvalidElementType();
				int flags = delta.getFlags();
				if ((flags & IResourceDelta.CONTENT) != 0) {
					// content has changed
					contentChanged(element, delta);
					updateIndexAddResource(element, delta);
				} else if ((flags & IResourceDelta.OPEN) != 0) {
					// project has been opened or closed
					IProject res = (IProject)resource;
					if (res.isOpen()) {
						elementOpened(element, delta);
						updateIndexAddResource(element, delta);
					} else {
						elementClosed(element, delta);
						updateIndexRemoveResource(element, delta);
					}
					// when a project is open/closed don't process children
				} else if ((flags & IResourceDelta.DESCRIPTION) != 0) {
					elementAdded(element, delta);
				} // else if ((flags * IResourceDelta.MARKERS) != 0) {}
				// else if ((flags * IResourceDelta.TYPE) != 0) {}
				// else if ((flags * IResourceDelta.SYNC) != 0) {}
				isProcess = true;
				break;
		}
		return isProcess;
	}

	protected void updateIndexAddResource(ICElement element, IResourceDelta delta) {
		//CModelManager.getDefault().getIndexManager().addResource(delta.getResource());
	}

	protected void updateIndexRemoveResource(ICElement element, IResourceDelta delta) {
		//CModelManager.getDefault().getIndexManager().removeResource(delta.getResource());
	}
}

/*******************************************************************************
 * Copyright (c) 2002, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rational Software - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import java.util.Arrays;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IArchive;
import org.eclipse.cdt.core.model.IArchiveContainer;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.IBinaryContainer;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;

/**
 * This class is used by <code>CModelManager</code> to convert
 * <code>IResourceDelta</code>s into <code>ICElementDelta</code>s.
 * It also does some processing on the <code>CElement</code>s involved.
 * (e.g. closing them or updating binary containers).
 */
final class DeltaProcessor {
	/**
	 * The <code>CElementDelta</code> corresponding to the <code>IResourceDelta</code> being translated.
	 */
	private CElementDelta fCurrentDelta;
	
	static final ICElementDelta[] NO_DELTA = new ICElementDelta[0];

	// Hold on the element being renamed.
	private ICElement movedFromElement;

	/**
	 * Creates the create corresponding to this resource.
	 * Returns null if none was found.
	 */
	protected ICElement createElement(IResource resource) {
		if (resource == null) {
			return null;
		}

		CModelManager manager = CModelManager.getDefault();

		boolean shouldProcess = true;
		
		// Check for C nature or if the was a CNature
		if (!(resource instanceof IWorkspaceRoot)) {
			IProject project = resource.getProject();
			if (!(CoreModel.hasCNature(project) || CoreModel.hasCCNature(project))) {
				shouldProcess = false;
				CModel root = manager.getCModel();
				CModelInfo rootInfo = (CModelInfo)manager.peekAtInfo(root);
				if (rootInfo != null) {
					ICElement[] celements = rootInfo.getChildren();
					for (ICElement celement : celements) {
						IResource r = celement.getResource();
						if (project.equals(r)) {
							shouldProcess = true;
						}
					}
				}
			}
		}

		if (!shouldProcess) {
			return null;
		}

		ICElement celement = manager.create(resource, null);

		// BUG 36424:
		// The Binary may only be visible in the BinaryContainers
		try {
			if (celement == null && resource.getType() == IResource.FILE) {
				ICElement[] children;
				ICProject cproj = manager.create(resource.getProject());
				if (cproj != null && cproj.isOpen()) {
					IBinaryContainer bin = cproj.getBinaryContainer();
					if (bin.isOpen()) {
						children = ((CElement)bin).getElementInfo().getChildren();
						for (ICElement element : children) {
							IResource res = element.getResource();
							if (resource.equals(res)) {
								celement = element;
								break;
							}
						}
					}
				}
			}
			// BUG 36424:
			// The Archive may only be visible in the ArchiveContainers
			if (celement == null && resource.getType() == IResource.FILE) {
				ICElement[] children;
				ICProject cproj = manager.create(resource.getProject());
				if (cproj != null && cproj.isOpen()) {
					IArchiveContainer ar = cproj.getArchiveContainer();
					if (ar.isOpen()) {
						children = ((CElement)ar).getElementInfo().getChildren();
						for (ICElement element : children) {
							IResource res = element.getResource();
							if (resource.equals(res)) {
								celement = element;
								break;
							}
						}
					}
				}				
			}
			//  It is not a C resource if the parent is a Binary/ArchiveContainer
			// But we have to release too.
			if (celement != null && resource.getType() == IResource.FILE) {
				ICElement parent = celement.getParent();
				if (parent instanceof IArchiveContainer || parent instanceof IBinaryContainer) {
					releaseCElement(celement);
					celement = null;
				}
			}
		} catch (CModelException e) {
			return null;
		}
		return celement;
	}

	/**
	 * Adds the given child handle to its parent's cache of children. 
	 */
	protected void addToParentInfo(Openable child) throws CModelException {
		Openable parent = (Openable) child.getParent();
		if (parent != null && parent.isOpen()) {
			CElementInfo info = parent.getElementInfo();
			// Check if the element exists.
			if (!info.includesChild(child)) {
				info.addChild(child);
			}
		}
	}

	/**
	 * Removes the given element from its parents cache of children. If the
	 * element does not have a parent, or the parent is not currently open,
	 * this has no effect. 
	 */
	private void removeFromParentInfo(ICElement child) throws CModelException {
		CModelManager factory = CModelManager.getDefault();

		// Remove the child from the parent list.
		ICElement parent = child.getParent();
		if (parent != null && parent instanceof Parent && factory.peekAtInfo(parent) != null) {
			((Parent) parent).removeChild(child);
		}
	}

	/**
	 * Release the Element and cleaning.
	 */
	protected void releaseCElement(ICElement celement) throws CModelException {
		CModelManager factory = CModelManager.getDefault();
		int type = celement.getElementType();
		if (type == ICElement.C_ARCHIVE) {
			ICProject cproject = celement.getCProject();
			IArchiveContainer container = cproject.getArchiveContainer();
			fCurrentDelta.changed(container, ICElementDelta.CHANGED);		
		} else if (type == ICElement.C_BINARY) {
			ICProject cproject = celement.getCProject();
			IBinaryContainer container = cproject.getBinaryContainer();
			fCurrentDelta.changed(container, ICElementDelta.CHANGED);
		} else {
			// If an entire folder was deleted we need to update the
			// BinaryContainer/ArchiveContainer also.
			ICProject cproject = celement.getCProject();
			CProjectInfo pinfo = (CProjectInfo)factory.peekAtInfo(cproject);
			if (pinfo != null && pinfo.vBin != null) {
				if (factory.peekAtInfo(pinfo.vBin) != null) {
					ICElement[] bins = pinfo.vBin.getChildren();
					for (ICElement bin : bins) {
						if (celement.getPath().isPrefixOf(bin.getPath())) {
							fCurrentDelta.changed(pinfo.vBin, ICElementDelta.CHANGED);
						}
					}
				}
			}
			if (pinfo != null && pinfo.vLib != null) {
				if (factory.peekAtInfo(pinfo.vLib) != null) {
					ICElement[] ars = pinfo.vLib.getChildren();
					for (ICElement ar : ars) {
						if (celement.getPath().isPrefixOf(ar.getPath())) {
							fCurrentDelta.changed(pinfo.vBin, ICElementDelta.CHANGED);
						}
					}
				}
			}
		}
		removeFromParentInfo(celement);
		factory.releaseCElement(celement);
	}

	/**
	 * Creates the create corresponding to this resource.
	 * Returns null if none was found.
	 */
	protected ICElement createElement(IPath path) {
		return CModelManager.getDefault().create(path);
	}

	/**
	 * Processing for an element that has been added:<ul>
	 * <li>If the element is a project, do nothing, and do not process
	 * children, as when a project is created it does not yet have any
	 * natures - specifically a C nature.
	 * <li>If the element is not a project, process it as added (see
	 * <code>basicElementAdded</code>.
	 * </ul>
	 */
	protected void elementAdded(ICElement element, IResourceDelta delta) throws CModelException {

		if (element instanceof Openable) {
			addToParentInfo((Openable)element);
		}
		if ((delta.getFlags() & IResourceDelta.MOVED_FROM) != 0) {
			//ICElement movedFromElement = createElement(delta.getMovedFromPath());
			if  (movedFromElement == null) {
				movedFromElement = createElement(delta.getMovedFromPath());
			}
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
	 * a resource is closed, the platform reports all children as removed.
	 */
	protected void elementClosed(ICElement element, IResourceDelta delta) throws CModelException {

		if (element.getElementType() == ICElement.C_PROJECT) {
			// treat project closing as removal
			elementRemoved(element, delta);
			CModelInfo rootInfo = (CModelInfo)CModelManager.getDefault().getCModel().getElementInfo();
			rootInfo.setNonCResources(null);
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
	protected void elementOpened(ICElement element, IResourceDelta delta) throws CModelException {

		if (element.getElementType() == ICElement.C_PROJECT) {
			// treat project opening as addition
			if (hasCNature(delta.getResource())) {
				elementAdded(element, delta);
			}
			CModelInfo rootInfo = (CModelInfo)CModelManager.getDefault().getCModel().getElementInfo();
			rootInfo.setNonCResources(null);

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
	 * This is use to remove the cache info for IArchive and IBinary
	 * We can use IBinary.close() doing this will remove the binary
	 * for the virtual binary/archive containers.
	 * @param celement
	 */
	private void closeBinary(ICElement celement) {
		CModelManager factory = CModelManager.getDefault();
		CElementInfo pinfo = (CElementInfo)factory.peekAtInfo(celement);
		if (pinfo != null) {
			ICElement[] celems = pinfo.getChildren();
			for (int i = 0; i < celems.length; ++i) {
				closeBinary(celems[i]);
			}
			factory.removeInfo(celement);
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
		// For Binary/Archive We can not call close() to do the work
		// closing will remove the element from the {Binary,Archive}Container
		// We need to clear the cache explicitly
		if (element instanceof IBinary || element instanceof IArchive) {
			closeBinary(element);
		} else if (element instanceof Openable) {
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
	protected void elementRemoved(ICElement element, IResourceDelta delta) throws CModelException {
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
		}
		return NO_DELTA;
	}

	/**
	 * Returns true if the given resource is contained in an open project
	 * with a C nature, otherwise false.
	 */
	protected boolean hasCNature(IResource resource) {
		// ensure the project has a C nature (if open)
		IProject project = resource.getProject();
		if (project.isOpen()) {
			return CoreModel.hasCNature(project);
		}
		return false;
	}

	/**
	 * Converts a <code>IResourceDelta</code> rooted in a <code>Workspace</code> into
	 * the corresponding set of <code>ICElementDelta</code>, rooted in the
	 * relevant <code>CModel</code>s.
	 */
	public ICElementDelta[] processResourceDelta(IResourceDelta changes) {
		ICElement root = CModelManager.getDefault().getCModel();			
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
		ICElementDelta[] filteredDeltas= filterRealDeltas(translatedDeltas);
		// release deltas
		fCurrentDelta= null;
		return filteredDeltas;
	}
	
	/**
	 * Converts an <code>IResourceDelta</code> and its children into
	 * the corresponding <code>ICElementDelta</code>s.
	 * Return whether the delta corresponds to a C element.
	 * If it is not a C element, it will be added as a non-C
	 * resource by the sender of this method.
	 */
	protected void traverseDelta(ICElement parent, IResourceDelta delta) {
		boolean updateChildren = true;
		try {
			IResource resource = delta.getResource();
			ICElement current = createElement(resource);
			updateChildren = updateCurrentDeltaAndIndex(current, delta);
			if (current == null || current instanceof ICContainer) {
				if (parent != null)
					nonCResourcesChanged(parent, delta);
			} else if (current instanceof ICProject) {
				ICProject cprj = (ICProject) current;
				CModel cModel = CModelManager.getDefault().getCModel();
				if (!cprj.getProject().isOpen() || cModel.findCProject(cprj.getProject()) == null) {
					nonCResourcesChanged(parent, delta);
				}
			}
			parent = current;
		} catch (CModelException e) {
		}
		if (updateChildren){
			IResourceDelta [] children = delta.getAffectedChildren();
			for (IResourceDelta element : children) {
				traverseDelta(parent, element);
			}
		}
	}

	/**
	 * Add the resource delta to the right CElementDelta tree.
	 * @param parent
	 * @param delta
	 */
	protected void nonCResourcesChanged(ICElement parent, IResourceDelta delta) throws CModelException {
		if (parent instanceof Openable && ((Openable)parent).isOpen()) {
			CElementInfo info = ((Openable)parent).getElementInfo();
			switch (parent.getElementType()) {
			case ICElement.C_MODEL:
				((CModelInfo)info).setNonCResources(null);
				fCurrentDelta.addResourceDelta(delta);
				return;
			case ICElement.C_PROJECT: {
				final CProjectInfo pInfo= (CProjectInfo)info;
				pInfo.setNonCResources(null);
				
				ISourceRoot[] roots= pInfo.sourceRoots;
				if (roots != null) {
					ICProject cproject = (ICProject)parent;
					if (isFolderAddition(delta)) {
						// if source roots changed - refresh from scratch
						// see http://bugs.eclipse.org/215112
						pInfo.sourceRoots= null;
						ISourceRoot[] newRoots= cproject.getAllSourceRoots();
						if (!Arrays.equals(roots, newRoots)) {
							cproject.close();
							break;
						}
					}
					// deal with project == sourceroot.  For that case the parent could have been the sourceroot
					// so we must update the sourceroot nonCResource array also.
					for (ISourceRoot root : roots) {
						IResource r = root.getResource();
						if (r instanceof IProject) {
							CElementInfo cinfo = (CElementInfo) CModelManager.getDefault().peekAtInfo(root);
							if (cinfo instanceof CContainerInfo) {
								((CContainerInfo)cinfo).setNonCResources(null);
							}
						}
					}
				}
				break;
			}
			case ICElement.C_CCONTAINER:
				((CContainerInfo)info).setNonCResources(null);
				break;
			}
		}
		CElementDelta elementDelta = fCurrentDelta.find(parent);
		if (elementDelta == null) {
			fCurrentDelta.changed(parent, ICElementDelta.F_CONTENT);
			elementDelta = fCurrentDelta.find(parent);
			if (elementDelta != null) {
				elementDelta.addResourceDelta(delta);
			}
		} else {
			elementDelta.addResourceDelta(delta);
		}
	}

	/**
	 * Test whether this delta or any of its children represents a folder addition.
	 * @param delta
	 * @return <code>true</code>, if the delta contains at least one new folder
	 */
	private static boolean isFolderAddition(IResourceDelta delta) {
		if (delta.getResource().getType() != IResource.FOLDER)
			return false;
		if (delta.getKind() == IResourceDelta.ADDED)
			return true;
		IResourceDelta[] children= delta.getAffectedChildren();
		for (IResourceDelta element : children) {
			if (isFolderAddition(element)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * Update the current delta (ie. add/remove/change the given element) and update the
	 * corresponding index.
	 * Returns whether the children of the given delta must be processed.
	 * @throws a CModelException if the delta doesn't correspond to a c element of the given type.
	 */
	private boolean updateCurrentDeltaAndIndex(ICElement element, IResourceDelta delta) throws CModelException {

		IResource resource = delta.getResource();

		switch (delta.getKind()) {
			case IResourceDelta.ADDED :
				if (element != null) {
					elementAdded(element, delta);
					if (element instanceof ICContainer) {
						ICContainer container = (ICContainer) element;
						ICProject cProject = container.getCProject();
						// Always check whether the container is open.
						if (container.isOpen())
							return true;
						
						// Check binary container, if the new folder is on an output entry,
						// there may be new binaries to add
						if (cProject.isOnOutputEntry(resource)) {
							IBinaryContainer bin = cProject.getBinaryContainer();
							IArchiveContainer archive = cProject.getArchiveContainer();
							return bin.isOpen() || archive.isOpen();
						}
						return false;
					} else if (element instanceof ICProject) {
						return ((ICProject) element).isOpen();
					} else if (element instanceof IBinary) {
						if (((IBinary) element).showInBinaryContainer()) {
							ICProject cProject = element.getCProject();
							IBinaryContainer bin = cProject.getBinaryContainer();
							fCurrentDelta.changed(bin, ICElementDelta.F_CONTENT);
						}
					} else if (element instanceof IArchive) {
						ICProject cProject = element.getCProject();
						IArchiveContainer archive = cProject.getArchiveContainer();
						fCurrentDelta.changed(archive, ICElementDelta.F_CONTENT);
					}
				}
				return false;

			case IResourceDelta.REMOVED :
				if (element != null) {
					elementRemoved(element, delta);
				} else {
					// Bug 349564 - The 'Binaries' node does not always disappear when project is cleaned
					CModel cModel = CModelManager.getDefault().getCModel();
					ICProject cProject = cModel.findCProject(resource.getProject());
					if (cProject != null && cProject.isOnOutputEntry(resource)) {
						IBinaryContainer bin = cProject.getBinaryContainer();
						if (!bin.isOpen())
							fCurrentDelta.changed(bin, ICElementDelta.F_CONTENT);
						IArchiveContainer archive = cProject.getArchiveContainer();
						if (!archive.isOpen())
							fCurrentDelta.changed(archive, ICElementDelta.F_CONTENT);
					}
				}
				return element instanceof ICContainer;

			case IResourceDelta.CHANGED :
				int flags = delta.getFlags();
				if ((flags & IResourceDelta.CONTENT) != 0) {
					// content has changed
					if (element != null) {
						elementChanged(element, delta);
					}
				} else if (resource.getType() == IResource.PROJECT) {
					if ((flags & IResourceDelta.OPEN) != 0) {
						// project has been opened or closed
						IProject project = (IProject)resource;
						if (element != null) {
							if (project.isOpen()) {
								elementOpened(element, delta);
								return element instanceof ICProject && ((ICProject) element).isOpen();
							}
							elementClosed(element, delta);
							//Don't process children
							return false; 
						}
						return false;
					} 
					if ((flags & IResourceDelta.DESCRIPTION) != 0) {
						IProject res = (IProject)delta.getResource();
						CModel cModel = CModelManager.getDefault().getCModel();
						boolean wasCProject = cModel.findCProject(res) != null;
						boolean isCProject = CProject.hasCNature(res);
						if (wasCProject != isCProject) {
							// project's nature has been added or removed
							if (element != null) {
								// note its resources are still visible as roots to other projects
								if (isCProject) {
									elementOpened(element, delta);
								} else {
									elementRemoved(element, delta);
								}
								return true;
							}
						}
					}
				} else if ((flags & IResourceDelta.ENCODING) != 0) {
					if (element != null) {
						elementChanged(element, delta);
					}
				}
				return true;
		}
		return true;
	}

}

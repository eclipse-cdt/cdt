/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/

package org.eclipse.cdt.internal.core.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.filetype.ICFileTypeAssociation;
import org.eclipse.cdt.core.filetype.ICFileTypeResolver;
import org.eclipse.cdt.core.filetype.IResolverModel;
import org.eclipse.cdt.core.filetype.ResolverChangeEvent;
import org.eclipse.cdt.core.filetype.ResolverDelta;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IOpenable;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * ResolverProcessor
 */
public class ResolverProcessor  {

	CModelManager fManager;
	CElementDelta fCurrentDelta;

	public void processResolverChanges(ResolverChangeEvent event) {
		ICElement root = CModelManager.getDefault().getCModel();
		fCurrentDelta = new CElementDelta(root);
		fManager = CModelManager.getDefault();
		
		// Go through the events and generate deltas
		ResolverDelta[] deltas = event.getDeltas();
		ICElement[] celements = getAffectedElements(event.getResolver());
		for (int k = 0; k < celements.length; ++k) {
			ICElement celement = celements[k];
			for (int i = 0; i < deltas.length; ++i) {
				ResolverDelta delta = deltas[i];
				if (delta.getElementType() == ResolverDelta.ELEMENT_ASSOCIATION) {
					ICFileTypeAssociation association = (ICFileTypeAssociation)delta.getElement();
					if (association.getType().isTranslationUnit()) {
						try {
							switch (delta.getEventType()) {
							case ResolverDelta.EVENT_ADD:
								add(celement, association);
							break;
							case ResolverDelta.EVENT_REMOVE:
								remove(celement, association);
							break;
							}
						} catch (CModelException e) {
							//
						}
					}
				}
			}
		}
		if (fCurrentDelta.getAffectedChildren().length > 0) {
			fManager.fire(fCurrentDelta, ElementChangedEvent.POST_CHANGE);
		}
	}

	void add(ICElement celement, ICFileTypeAssociation association) throws CModelException {
		if (celement instanceof IOpenable) {
			int type = celement.getElementType();
			if (type < ICElement.C_UNIT) {
				CElementInfo info = (CElementInfo)fManager.peekAtInfo(celement);
				if (info != null) {
					try {
						IResource resource = celement.getResource();
						IResource[] members = null;
						if (resource instanceof IContainer) {
							members = ((IContainer)resource).members();
						}
						if (members != null) {
							for (int i = 0; i < members.length; ++i) {
								if (members[i] instanceof IFile) {
									IFile file = (IFile) members[i];
									if (association.matches(file.getName())) {
										ICElement newElement = CoreModel.getDefault().create(file);
										if (newElement != null) {
											elementAdded(newElement, celement);
										}
									}
								}
							}
							ICElement[] celements = info.getChildren();
							for (int i = 0; i < celements.length; ++i) {
								add(celements[i], association);
							}
						}
					} catch (CoreException e) {
						//
					}
				}
			}
		}
	}

	void remove(ICElement celement, ICFileTypeAssociation association) throws CModelException {
		if (celement instanceof IOpenable) {
			int type = celement.getElementType();
			if (type < ICElement.C_UNIT) {
				CElementInfo cinfo = (CElementInfo)fManager.peekAtInfo(celement);
				if (cinfo != null) {
					ICElement[] celements = cinfo.getChildren();
					for (int i = 0; i < celements.length; ++i) {
						if (celements[i].getElementType() == ICElement.C_UNIT) {
							if (association.matches(celements[i].getElementName())) {
								elementRemoved(celements[i], celement);
							}
						} else {
							remove(celements[i], association);
						}
					}
				}
			}
		}		
	}
	
	/**
	 * Add the resource delta to the right CElementDelta tree.
	 * @param parent
	 * @param delta
	 */
	private void nonCResourcesChanged(ICElement parent) {
		if (parent instanceof Openable && ((Openable)parent).isOpen()) {
			try {
				CElementInfo info = ((Openable)parent).getElementInfo();
				switch (parent.getElementType()) {
				case ICElement.C_MODEL:
					((CModelInfo)info).setNonCResources(null);
				return;
				case ICElement.C_PROJECT:
					((CProjectInfo)info).setNonCResources(null);
				break;
				case ICElement.C_CCONTAINER:
					((CContainerInfo)info).setNonCResources(null);
					if (parent instanceof ISourceRoot) {
						// if sourceRoot == Project we nee to update the nonCResource of the project also.
						if (parent.getResource() instanceof IProject) {
							ICElement cproject = parent.getCProject();
							CProjectInfo pinfo = (CProjectInfo)fManager.peekAtInfo(cproject);
							if (pinfo != null) {
								pinfo.setNonCResources(null);
							}
						}
					}
				break;
				}
			} catch (CModelException e) {
				//
			}
		}
	}

	private void elementAdded(ICElement celement, ICElement parent) throws CModelException {

		if (celement instanceof Openable) {
			addToParentInfo((Openable)celement);
		}
		fCurrentDelta.added(celement);
		nonCResourcesChanged(parent);
	}

	/**
	 * Adds the given child handle to its parent's cache of children. 
	 */
	private void addToParentInfo(Openable child) throws CModelException {
		Openable parent = (Openable) child.getParent();
		if (parent != null && parent.isOpen()) {
			CElementInfo info = parent.getElementInfo();
			// Check if the element exits
			if (!info.includesChild(child)) {
				info.addChild(child);
			}
		}
	}

	private void elementRemoved(ICElement celement, ICElement parent) throws CModelException {
		fCurrentDelta.removed(celement);
		nonCResourcesChanged(parent);
		removeFromParentInfo(celement);
		fManager.releaseCElement(celement);
	}

	/**
	 * Removes the given element from its parents cache of children. If the
	 * element does not have a parent, or the parent is not currently open,
	 * this has no effect. 
	 */
	private void removeFromParentInfo(ICElement child) throws CModelException {

		// Remove the child from the parent list.
		ICElement parent = child.getParent();
		if (parent != null && parent instanceof Parent && fManager.peekAtInfo(parent) != null) {
			((Parent)parent).removeChild(child);
		}
	}

	private ICElement[] getAffectedElements(ICFileTypeResolver resolver) {
		try {
			IResolverModel rmodel = CCorePlugin.getDefault().getResolverModel();
			ICModel cmodel = CoreModel.getDefault().getCModel();
			ICProject[] cprojects = cmodel.getCProjects();
			IContainer container = resolver.getContainer();

			if (container instanceof IProject || container instanceof IFolder) {
				IProject project = container.getProject();
				for (int i = 0; i < cprojects.length; i++) {
					if (project.equals(cprojects[i].getProject())) {
						return new ICElement[] { cprojects[i] };
					}
				}
				return CElement.NO_ELEMENTS;
			}
			// Assume a workspace resolver
			List list = new ArrayList(cprojects.length);
			for (int i = 0; i < cprojects.length; ++i) {
				if (!rmodel.hasCustomResolver(cprojects[i].getProject())) {
					list.add(cprojects[i]);
				}
			}
			return (ICElement[]) list.toArray(new ICElement[list.size()]);
		} catch (CModelException e) {
			//
		}
		return CElement.NO_ELEMENTS;
	}
}

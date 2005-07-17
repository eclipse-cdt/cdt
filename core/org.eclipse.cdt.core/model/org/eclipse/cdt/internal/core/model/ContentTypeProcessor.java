/*******************************************************************************
 * Copyright (c) 2002, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IOpenable;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager.ContentTypeChangeEvent;
import org.eclipse.core.runtime.preferences.IScopeContext;
/**
 * ContentType processor
 */
public class ContentTypeProcessor extends CModelOperation {

	CModelManager fManager;
	CElementDelta fCurrentDelta;
	ContentTypeChangeEvent[] fEvents;

	public ContentTypeProcessor(ContentTypeChangeEvent[] events) {
		super(CModelManager.getDefault().getCModel());
		this.fEvents = events;
		fManager = CModelManager.getDefault();
		ICElement root = fManager.getCModel();
		fCurrentDelta = new CElementDelta(root);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.CModelOperation#isReadOnly()
	 */
	public boolean isReadOnly() {
		return true;
	}

	protected void executeOperation() throws CModelException {
		for (int i = 0; i < fEvents.length; ++i) {
			IContentType contentType = fEvents[i].getContentType();
			IScopeContext context = fEvents[i].getContext();
			ICProject[] cprojects = getAffectedProjects(fEvents[i]);
			for (int k = 0; k < cprojects.length; ++k) {
				processContentType(cprojects[k], contentType, context);
			}
		}
		
		if (fCurrentDelta.getAffectedChildren().length > 0) {
			addDelta(fCurrentDelta);
		}
	}
	


	static public void processContentTypeChanges(ContentTypeChangeEvent[] events) {
		try {
			CModelOperation op = new ContentTypeProcessor(events);
			op.runOperation(null);
		} catch (CModelException e) {
			// 
		}
	}

	private boolean isRegisteredContentTypeId(String id) {
		// bailout early
		if (id == null || id.length() == 0) {
			return false;
		}
		String[] ids = CoreModel.getRegistedContentTypeIds();
		for (int i = 0; i < ids.length; i++) {
			if (ids[i].equals(id)) {
				return true;
			}
		}
		return false;
	}

	protected void processContentType(ICElement celement, IContentType contentType, IScopeContext context) {
		/*
		 * We only know that an association was added/removed from the contentType.
		 * we need to go to all the members that are in the cache(known ICElement) and recheck there
		 * state to see if there were affected.  That also include children that were non-celement
		 * the new assiociation may have turn them to new ICElement.
		 * algo:
		 *   if ICContainer check if we have a CElementinfo(children)
		 *   	if yes, 
		 *      	look at the members(IResource) and get there contentType
		 *      	if the type is the same as the event
		 *      		check if element was already a celement
		 *      		if yes,
		 *      			check if the element as the same contentType
		 *      			if yes,
		 *      				do nothing
		 *      			else
		 *      				if the content ID is registered ID
		 *      					fired changed event
		 *      				else
		 *      					fire remove event
		 *      	check for the CElement
		 *      		get the new oldID and the newID
		 *      		if not equal
		 *      			check if the newID is registered
		 *      			if yes
		 *      				fire changed event
		 *      			else
		 *      				fire remove event
		 */
		if (celement instanceof IOpenable) {
			int type = celement.getElementType();
			// if the type is not a TranslationUnit
			switch (type) {
			case ICElement.C_PROJECT: {
				CElementInfo info = (CElementInfo)fManager.peekAtInfo(celement);
				if (info != null) {
					ICElement[] celements = info.getChildren();
					for (int i = 0; i < celements.length; ++i) {
						processContentType(celements[i], contentType, context);
					}
				}
				break;
			}
			case ICElement.C_CCONTAINER: {
				CElementInfo info = (CElementInfo)fManager.peekAtInfo(celement);
				if (info != null) {
					try {
						ICElement[] celements = info.getChildren();
						IResource resource = celement.getResource();
						IResource[] members = null;
						if (resource instanceof IContainer) {
							members = ((IContainer)resource).members();
						}
						if (members != null) {
							for (int i = 0; i < members.length; ++i) {
								if (members[i] instanceof IFile) {
									IFile file = (IFile) members[i];
									String name = file.getName();
									IContentType cType = CCorePlugin.getContentType(file.getProject(), name);
									if (cType != null && cType.equals(contentType)) {
										boolean found = false;
										for (int j = 0; j < celements.length; ++j) {
											if (celements[j].getElementName().equals(name)) {
												if (celements[j].getElementType() == ICElement.C_UNIT) {
													ITranslationUnit unit = (ITranslationUnit)celements[j];
													if (!cType.getId().equals(unit.getContentTypeId())) {
														if (isRegisteredContentTypeId(cType.getId())) {
															elementChanged(celements[j]);
														}
													}
												}
												found = true;
												break;
											}
										}
										if (! found) {
											ICElement newElement = CoreModel.getDefault().create(file);
											if (newElement != null) {
												elementAdded(newElement, celement);
											}
										}
									}
								}
							}
						}
						for (int i = 0; i < celements.length; ++i) {
							processContentType(celements[i], contentType, context);
						}
					} catch (CoreException e) {
						//
					}
				}
			}
			break;
			case ICElement.C_UNIT: {
				String oldId =  ((ITranslationUnit)celement).getContentTypeId();
				if (contentType.getId().equals(oldId)) {
					try {
						IContentType cType = CCorePlugin.getContentType(celement.getCProject().getProject(), celement.getElementName());
						String newId = (cType != null) ? cType.getId() : ""; //$NON-NLS-1$
						if (isRegisteredContentTypeId(newId)) {
							if (!oldId.equals(newId)) {
								elementChanged(celement);
							}
						} else {
							elementRemoved(celement, celement.getParent());
						}
					} catch (CoreException e) {
						//
					}
				}
				break;
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
				break;
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

	private void elementChanged(ICElement element) throws CModelException {
		// For Binary/Archive We can not call close() to do the work
		// closing will remove the element from the {Binary,Archive}Container
		// We neef to clear the cache explicitely
//		if (element instanceof IBinary || element instanceof IArchive) {
//			closeBinary(element);
//		} else if (element instanceof Openable) {
//			close((Openable)element);
//		}
//		fCurrentDelta.changed(element, ICElementDelta.F_CONTENT);
		if (element instanceof IOpenable) {
			((IOpenable)element).close();
		}
		fCurrentDelta.changed(element, ICElementDelta.F_CONTENT |ICElementDelta.F_CONTENT_TYPE);
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

	private ICProject[] getAffectedProjects(ContentTypeChangeEvent event) {
		try {
			ICModel cmodel = CoreModel.getDefault().getCModel();
			ICProject[] cprojects = cmodel.getCProjects();

			IScopeContext context = event.getContext();
			if (context != null) {
				if (ProjectScope.SCOPE.equals((context.getName()))) {
					IPath location = context.getLocation();
					for (int i = 0; i < cprojects.length; i++) {
						if (cprojects[i].getProject().getLocation().isPrefixOf(location)) {
							return new ICProject[] { cprojects[i] };
						}
					}
					return new ICProject[0];
				}
			}
			// Assume a workspace resolver
			List list = new ArrayList(cprojects.length);
			for (int i = 0; i < cprojects.length; ++i) {
				list.add(cprojects[i]);
			}
			return (ICProject[]) list.toArray(new ICProject[list.size()]);
		} catch (CModelException e) {
			//
		}
		return new ICProject[0];
	}

}

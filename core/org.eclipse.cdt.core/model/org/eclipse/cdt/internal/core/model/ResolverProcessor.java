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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.filetype.ICFileTypeAssociation;
import org.eclipse.cdt.core.filetype.ResolverChangeEvent;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IOpenable;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;

/**
 * ResolverProcessor
 */
public class ResolverProcessor  {

	CModelManager fManager;
	CElementDelta fCurrentDelta;

	public void processResolverChanges(ResolverChangeEvent[] events) {
		ICElement root = CModelManager.getDefault().getCModel();
		fCurrentDelta = new CElementDelta(root);
		fManager = CModelManager.getDefault();

		// Regroup the events per container.
		Map map = new HashMap();
		for (int i = 0; i < events.length; ++i) {
			IContainer container = events[i].getContainer();
			List list = (List)map.get(container);
			if (list == null) {
				list = new ArrayList();
				map.put(container, list);
			}
			list.add(events[i]);
		}
		
		// Go through the events and generate deltas
		CModelManager manager = CModelManager.getDefault();
		Iterator entries = map.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry entry = (Map.Entry)entries.next();
			List list = (List)entry.getValue();
			for (int i = 0; i < list.size(); ++i) {
				ResolverChangeEvent event = (ResolverChangeEvent)list.get(i);
				if (event.getElementType() ==ResolverChangeEvent.ELEMENT_ASSOCIATION) {
					IContainer container = (IContainer)entry.getKey();
					ICElement celement = CoreModel.getDefault().create(container);
					ICFileTypeAssociation association = (ICFileTypeAssociation)event.getElement();
					try {
						switch (event.getEventType()) {
							case ResolverChangeEvent.EVENT_ADD:
								add(celement, association);
							break;
							case ResolverChangeEvent.EVENT_REMOVE:
								remove(celement, association);
							break;
						}
					} catch (CModelException e) {
						//
					}
				}
			}
		}
		fManager.fire(fCurrentDelta, ElementChangedEvent.POST_CHANGE);
	}

	void add(ICElement celement, ICFileTypeAssociation association) throws CModelException {
		if (celement instanceof IOpenable) {
			int type = celement.getElementType();
			if (type < ICElement.C_UNIT) {
				Object[] elements = null;
				CElementInfo info = (CElementInfo)fManager.peekAtInfo(celement);
				if (info != null) {
					switch (celement.getElementType()) {
						case ICElement.C_MODEL:
							elements = ((CModelInfo)info).getNonCResources();
							break;
						case ICElement.C_PROJECT:
							elements = ((CProjectInfo)info).getNonCResources(celement.getResource());
							break;
						case ICElement.C_CCONTAINER:
							elements = ((CContainerInfo)info).getNonCResources(celement.getResource());
							break;
					}
				}
				if (elements != null) {
					for (int i = 0; i < elements.length; ++i) {
						if (elements[i] instanceof IFile) {
							IFile file = (IFile) elements[i];
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
			}
		}
	}

	void remove(ICElement celement, ICFileTypeAssociation association) throws CModelException {
		if (celement instanceof IOpenable) {
			int type = celement.getElementType();
			if (type < ICElement.C_UNIT) {
				CElementInfo info = (CElementInfo)fManager.peekAtInfo(celement);
				if (info instanceof CElementInfo) {
					CElementInfo cinfo = (CElementInfo)info;
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


}

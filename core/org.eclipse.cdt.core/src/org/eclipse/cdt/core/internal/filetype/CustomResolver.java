/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.internal.filetype;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.filetype.ICFileTypeAssociation;
import org.eclipse.cdt.core.filetype.IResolverModel;
import org.eclipse.cdt.core.filetype.ResolverChangeEvent;
import org.eclipse.cdt.core.filetype.ResolverDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 */
public class CustomResolver extends CFileTypeResolver {

	// XML tag names, etc.
	private static final String OLD_RESOLVER 	= "cdt_resolver"; //$NON-NLS-1$
	private static final String CDT_RESOLVER 	= CCorePlugin.PLUGIN_ID + ".resolver"; //$NON-NLS-1$
	private static final String TAG_CUSTOM 		= "custom"; //$NON-NLS-1$
	private static final String TAG_ASSOC 		= "associations"; //$NON-NLS-1$
	private static final String TAG_ENTRY 		= "entry"; //$NON-NLS-1$
	private static final String ATTR_TYPE 		= "type"; //$NON-NLS-1$
	private static final String ATTR_PATTERN 	= "pattern"; //$NON-NLS-1$
	private static final String ATTR_VALUE 		= "value"; //$NON-NLS-1$

	private ResolverModel fModel;

	public CustomResolver(ResolverModel model, IProject p) {
		super(p);
		fModel = model;
	}

	public static boolean hasCustomResolver(IProject project) {
		Boolean custom	= new Boolean(false);
		// Check for old custom resolver but do not convert

		Element data = getProjectOldResolverData(project, false);
		custom = hasCustomTag(data);

		if (!custom.booleanValue()) {
			data	= getProjectResolverData(project, false);
			custom = hasCustomTag(data);
		}
		
		return custom.booleanValue();
	}

	/**
	 * @param project
	 */
	public static void removeCustomResover(IProject project) {
		Element	root	= getProjectResolverData(project, false);
		if (root != null) {
			Node child	= root.getFirstChild();
			Element element	= null;

			// Clear the old stuff.
			while (child != null) {
				root.removeChild(child);
				child = root.getFirstChild();
			}
			try {
				getProjectDescriptor(project, true).saveProjectData();
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
	}

	protected void doAdjustAssociations(ICFileTypeAssociation[] addAssocs, ICFileTypeAssociation[] delAssocs,
			boolean triggerEvent) {
		IProject project = (IProject)getContainer();
		List deltas = new ArrayList();

		// add
		if (triggerEvent && null != addAssocs && addAssocs.length > 0) {
			for (int i = 0; i < addAssocs.length; i++) {
				deltas.add(new ResolverDelta(addAssocs[i], ResolverDelta.EVENT_ADD));
			}
		}

		// remove
		if (triggerEvent && null != delAssocs && delAssocs.length > 0) {
			for (int i = 0; i < delAssocs.length; i++) {
				deltas.add(new ResolverDelta(delAssocs[i], ResolverDelta.EVENT_REMOVE));
			}
		}

		// fire the deltas.
		if (triggerEvent && !deltas.isEmpty()) {
			ResolverChangeEvent event = new ResolverChangeEvent(fModel, this);
			for (int i = 0; i < deltas.size(); ++i) {
				ResolverDelta delta = (ResolverDelta)deltas.get(i);
				event.addDelta(delta);
			}
			fModel.fireEvent(event);
		}

		// Save to the file.
		
		Element	root	= getProjectResolverData(project, true);
		Document doc 	= root.getOwnerDocument();
		Node child	= root.getFirstChild();
		Element element	= null;

		// Clear the old stuff.
		while (child != null) {
			root.removeChild(child);
			child = root.getFirstChild();
		}
		
		element = doc.createElement(TAG_CUSTOM);
		element.setAttribute(ATTR_VALUE, Boolean.TRUE.toString());
		root.appendChild(element);

		element = doc.createElement(TAG_ASSOC);
		root.appendChild(element);
		
		root = element; // Note that root changes...
		
		ICFileTypeAssociation[] assoc = getFileTypeAssociations();
		
		for (int i = 0; i < assoc.length; i++) {
			element = doc.createElement(TAG_ENTRY);
			element.setAttribute(ATTR_PATTERN, assoc[i].getPattern());
			element.setAttribute(ATTR_TYPE, assoc[i].getType().getId());
			root.appendChild(element);
		}
		
		try {
			getProjectDescriptor(project, true).saveProjectData();
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.internal.filetype.CFileTypeResolver#loadAssociations()
	 */
	protected ICFileTypeAssociation[] loadAssociations() {

		IProject project = (IProject)getContainer();

		convert20(project);

		List assocs		= new ArrayList();
		Element	data		= getProjectResolverData(project, false);
		Node child 		= ((null != data) ? data.getFirstChild() : null);
		while (child != null) {
			if (child.getNodeName().equals(TAG_ASSOC)) {
				Node assoc = child.getFirstChild();
				while (assoc != null) {
					if (assoc.getNodeName().equals(TAG_ENTRY)) {
						Element element	= (Element) assoc;
						String	pattern	= element.getAttribute(ATTR_PATTERN);
						String 	typeId	= element.getAttribute(ATTR_TYPE);
						try {
							assocs.add(fModel.createAssocation(pattern, fModel.getFileTypeById(typeId)));
						} catch (IllegalArgumentException e) {
							CCorePlugin.log(e);
						}
					}
					assoc = assoc.getNextSibling();
				}
			}
			child = child.getNextSibling();
		}
		return (ICFileTypeAssociation[]) assocs.toArray(new ICFileTypeAssociation[assocs.size()]);
	}

	private static Element getProjectResolverData(IProject project, boolean create) {
		Element data = null;
		try {
			ICDescriptor desc = getProjectDescriptor(project, create);
			if (desc != null) {
				data = desc.getProjectData(CDT_RESOLVER);
			}
		} catch (CoreException e) {
			// ignore
		}
		return data;
	}
	
	private static ICDescriptor getProjectDescriptor(IProject project, boolean create) throws CoreException {
		ICDescriptor descriptor = null;
		descriptor = CCorePlugin.getDefault().getCProjectDescription(project, create);
		return descriptor;
	}

	private static Element getProjectOldResolverData(IProject project, boolean create) {
		Element data = null;
		try {
			ICDescriptor desc = getProjectDescriptor(project, create);
			if (desc != null) {
				data = desc.getProjectData(OLD_RESOLVER);
			}
		} catch (CoreException e) {
			// ignore
		}
		return data;
	}

	private static void convert20(IProject project) {
		Element root = getProjectOldResolverData(project, false);
		if (root != null) {
			IResolverModel model = ResolverModel.getDefault();
			List assocList		= new ArrayList();

			// 1 - get the old stuff
			Node child 		= root.getFirstChild();
			while (child != null) {
				if (child.getNodeName().equals(TAG_ASSOC)) {
					Node assoc = child.getFirstChild();
					while (assoc != null) {
						if (assoc.getNodeName().equals(TAG_ENTRY)) {
							Element element	= (Element) assoc;
							String	pattern	= element.getAttribute(ATTR_PATTERN);
							String 	typeId	= element.getAttribute(ATTR_TYPE);
							try {
								assocList.add(model.createAssocation(pattern, model.getFileTypeById(typeId)));
							} catch (IllegalArgumentException e) {
								CCorePlugin.log(e);
							}
						}
						assoc = assoc.getNextSibling();
					}
				}
				child = child.getNextSibling();
			}

			// 2 - Clear the old stuff.
			child	= root.getFirstChild();
			while (child != null) {
				root.removeChild(child);
				child = root.getFirstChild();
			}

			ICFileTypeAssociation[] assocs = (ICFileTypeAssociation[]) assocList.toArray(new ICFileTypeAssociation[assocList.size()]);
			
			if (assocs.length > 0) {
				// 3 - save the old stuff on the new id 
				root = getProjectResolverData(project, true);
				if (root != null) {
					Document doc 	= root.getOwnerDocument();
					child	= root.getFirstChild();
					Element element	= null;
					
					// Clear the old stuff.
					while (child != null) {
						root.removeChild(child);
						child = root.getFirstChild();
					}
					
					element = doc.createElement(TAG_CUSTOM);
					element.setAttribute(ATTR_VALUE, Boolean.TRUE.toString());
					root.appendChild(element);
					
					element = doc.createElement(TAG_ASSOC);
					root.appendChild(element);
					
					root = element; // Note that root changes...
					
					for (int i = 0; i < assocs.length; i++) {
						element = doc.createElement(TAG_ENTRY);
						element.setAttribute(ATTR_PATTERN, assocs[i].getPattern());
						element.setAttribute(ATTR_TYPE, assocs[i].getType().getId());
						root.appendChild(element);
					}
					
				}
			}
			try {
				getProjectDescriptor(project, true).saveProjectData();
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
	}

	private static Boolean hasCustomTag(Element data) {
		Node	child	= ((null != data) ? data.getFirstChild() : null);
		Boolean custom	= new Boolean(false);
		
		while (child != null) {
			if (child.getNodeName().equals(TAG_CUSTOM)) { 
				return  Boolean.valueOf(((Element)child).getAttribute(ATTR_VALUE));
			}
			child = child.getNextSibling();
		}
		return Boolean.FALSE;
	}

}

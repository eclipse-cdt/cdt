/*******************************************************************************
 * Copyright (c) 2002, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.cview;

import java.util.ArrayList;
import java.util.TreeSet;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IIncludeReference;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CElementGrouping;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * IncludeRefContainer
 */
public class IncludeRefContainer extends CElementGrouping {

	ICProject fCProject;

	private final static String groupProp = "IncludeGrouping"; //$NON-NLS-1$

	public static QualifiedName qname = new QualifiedName(CCorePlugin.PLUGIN_ID, groupProp);

	public enum Representation {
		List, Single, Compact;
	}

	public Representation representation = Representation.List;

	/**
	 *
	 */
	public IncludeRefContainer(ICProject cproject) {
		super(INCLUDE_REF_CONTAINER);
		fCProject = cproject;
		try {
			var res = cproject.getResource();
			var prop = res.getPersistentProperty(qname);
			representation = Representation.valueOf(prop);
		} catch (Exception e) {
			//No config - ignore
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == IWorkbenchAdapter.class) {
			return (T) this;
		}
		if (adapter == ICProject.class) {
			return (T) fCProject;
		}
		return null;
	}

	class Subtree implements Comparable<Subtree> {
		Representation rep;
		TreeSet<Subtree> children = new TreeSet<>();
		IIncludeReference ref = null;
		IPath pathName = Path.EMPTY;
		boolean hasref = false;

		Subtree(IPath p) {
			pathName = p;
		}

		public Subtree(Representation representation) {
			rep = representation;
		}

		@Override
		public int compareTo(Subtree o) {
			return compareTo(o.pathName);
		}

		public int compareTo(IPath o) {
			return pathName.toFile().compareTo(o.toFile());
		}

		public void addPath(IPath p) {
			if (p.segmentCount() == 0) {
				hasref = true;
				return;
			}

			Subtree child = null;

			var seg1 = p.uptoSegment(1);

			for (var c : children) {
				if (c.compareTo(seg1) == 0) {
					child = c;
					break;
				}
			}

			if (child == null) {
				child = new Subtree(seg1);
				children.add(child);
			}

			child.addPath(p.removeFirstSegments(1));
		}

		public void compact() {
			if (children.isEmpty())
				return;

			for (var child : children) {
				child.compact();
			}

			// Root node
			if (pathName.isEmpty())
				return;

			if (hasref)
				return;

			if (children.size() != 1) {
				return;
			}

			var child = children.first();
			children = child.children;
			pathName = pathName.append(child.pathName);
		}

		public void split(int maxseg) {
			if (children.isEmpty())
				return;

			for (var child : children) {
				child.split(maxseg);
			}

			if (hasref)
				return;

			for (var child : children) {
				if (child.children.isEmpty())
					continue;

				boolean split = true;

				for (var cc : child.children) {
					if (!cc.hasref) {
						split = false;
						break;
					}

					if (cc.pathName.segmentCount() >= maxseg) {
						split = false;
						break;
					}
				}

				if (!split)
					continue;

				children.remove(child);

				for (var cc : child.children) {
					cc.pathName = child.pathName.append(cc.pathName);
					children.add(cc);
				}

			}

		}

		public void injectRef(IIncludeReference r, int level) {
			var abspath = r.getPath();
			assert abspath.segmentCount() >= level;
			if (abspath.segmentCount() == level) {
				assert ref == null;
				//assert children.size() == 0;
				ref = r;
				return;
			}

			var workp = abspath.removeFirstSegments(level);
			for (var child : children) {
				var matchcnt = workp.matchingFirstSegments(child.pathName);
				if (matchcnt > 0) {
					child.injectRef(r, level + matchcnt);
					return;
				}
			}
			assert false : "We should never reach this point!"; //$NON-NLS-1$

		}

		public IncludeReferenceProxy toProxy(CElementGrouping parent) {

			var refCont = parent instanceof IncludeReferenceProxy
					? ((IncludeReferenceProxy) parent).getIncludeRefContainer()
					: (IncludeRefContainer) parent;
			var irefp = new IncludeReferenceProxy(parent, refCont, ref, pathName.toString());

			if (children.isEmpty())
				return irefp;

			for (var c : children) {
				irefp.addChild(c.toProxy(irefp));
			}
			return irefp;

		}

	}

	private Object[] getAggrChildren(IIncludeReference[] references) {

		var rootnode = new Subtree(representation);
		for (var ref : references) {
			rootnode.addPath(ref.getPath());
		}

		rootnode.compact();

		for (var ref : references) {
			rootnode.injectRef(ref, 0);
		}

		// Remove empty root-node
		var l1children = rootnode.children;

		var rv = new IncludeReferenceProxy[l1children.size()];

		int pos = 0;
		for (var n : l1children) {
			rv[pos] = n.toProxy(this);
			pos += 1;
		}

		return rv;

	}

	private Object[] getList(IIncludeReference[] references) {
		var rv = new ArrayList<IncludeReferenceProxy>(references.length);
		for (var r : references) {
			rv.add(new IncludeReferenceProxy(this, this, r, r.toString()));
		}
		return rv.toArray();
	}

	private Object[] getFirstCommon(IIncludeReference[] references) {
		assert references.length > 0;
		var list = new ArrayList<IncludeReferenceProxy>(references.length);
		final var commonref = references[0].getPath();
		int matchcnt = Integer.MAX_VALUE;

		for (var r : references) {
			matchcnt = Math.min(matchcnt, commonref.matchingFirstSegments(r.getPath()));
		}
		if (matchcnt == 0)
			return getList(references);

		var common = commonref.removeLastSegments(commonref.segmentCount() - matchcnt);
		var rv = new IncludeReferenceProxy(this, this, null, common.toString());

		for (var r : references) {
			var cld = new IncludeReferenceProxy(rv, this, r, r.getPath().removeFirstSegments(matchcnt).toString());
			rv.addChild(cld);
		}

		return new Object[] { rv };
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object o) {

		IIncludeReference[] references;
		try {
			references = fCProject.getIncludeReferences();
		} catch (CModelException e) {
			//Log?
			return NO_CHILDREN;
		}

		if (references.length == 0) {
			return NO_CHILDREN;
		}

		switch (representation) {
		case List:
			return getList(references);
		case Single:
			return getFirstCommon(references);
		case Compact:
			return getAggrChildren(references);
		}
		assert false;
		return NO_CHILDREN;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
	 */
	@Override
	public ImageDescriptor getImageDescriptor(Object object) {
		return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_INCLUDES_CONTAINER);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
	 */
	@Override
	public String getLabel(Object o) {
		return CViewMessages.IncludeRefContainer_Includes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(Object o) {
		return getCProject();
	}

	public ICProject getCProject() {
		return fCProject;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IncludeRefContainer) {
			IncludeRefContainer other = (IncludeRefContainer) obj;
			return fCProject.equals(other.getCProject());
		}
		return super.equals(obj);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (fCProject != null) {
			return fCProject.hashCode();
		}
		return super.hashCode();
	}

}

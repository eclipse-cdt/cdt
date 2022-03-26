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
import java.util.Arrays;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IIncludeReference;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CElementGrouping;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * IncludeRefContainer
 */
public class IncludeRefContainer extends CElementGrouping {

	ICProject fCProject;

	private final static String groupProp = "IncludeGrouping"; //$NON-NLS-1$

	// https://wiki.eclipse.org/FAQ_How_do_I_use_the_platform_debug_tracing_facility
	private static boolean DEBUG = Boolean
			.parseBoolean(Platform.getDebugOption(CUIPlugin.PLUGIN_ID + "/debug/include_presentation")); //$NON-NLS-1$

	private static void dbgPrintln(String s) {
		if (DEBUG) {
			System.out.println(s);
		}
	}

	public static QualifiedName qname = new QualifiedName(CCorePlugin.PLUGIN_ID, groupProp);

	public enum Representation {
		/** The classic list; preserve include order */
		List,
		/** Extract a common path if possible; preserve include order */
		Single,
		/** Aggregate common path segments; include order is lost */
		Compact,
		/** Like Compact but only aggregate if there are more than 3 segments in common */
		Smart;
	}

	public Representation representation = Representation.List;

	public IncludeRefContainer(ICProject cproject) {
		super(INCLUDE_REF_CONTAINER);
		fCProject = cproject;
		try {
			var prj = cproject.getResource();
			var prop = prj.getPersistentProperty(qname);
			representation = Representation.valueOf(prop);
		} catch (Exception e) {
			//No config, use default
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

	static private IPath ir2p(IIncludeReference ir) {
		final var p = ir.getPath().toString();
		if (p.charAt(p.length() - 1) != '/') {
			return Path.forPosix(p);
		} else {
			/** Those paths probably never have a trailing slash, but test-cases triggered this
			 * and caused bad behavior. An assert is probably overkill in case this actually happens **/
			CUIPlugin.logError("Include paths musn't have a trailing slash."); //$NON-NLS-1$
			return Path.forPosix(p.substring(0, p.length() - 1));
		}
	}

	public class Subtree implements Comparable<Subtree> {
		Subtree m_parent = null;
		TreeSet<Subtree> m_children = new TreeSet<>();
		IIncludeReference m_ref = null;
		IPath m_pathName;
		boolean m_hasref;

		@Override
		public String toString() {
			String rv;
			if (m_hasref)
				rv = m_ref != null ? "R" : "r";//$NON-NLS-1$ //$NON-NLS-2$
			else
				rv = "N";//$NON-NLS-1$
			return rv + " " + m_pathName; //$NON-NLS-1$
		}

		Subtree(IPath p) {
			this(p, false);
		}

		Subtree(IPath p, boolean hasref) {
			m_pathName = p;
			m_hasref = hasref;
		}

		Subtree() {
			this(Path.EMPTY, false);
		}

		@Override
		public int compareTo(Subtree o) {
			int rv = compareTo(o.m_pathName);
			if (rv != 0)
				return rv;
			if (m_hasref == o.m_hasref)
				return 0;
			return (m_hasref) ? -1 : 1;
		}

		public int compareTo(IPath o) {
			return m_pathName.toFile().compareTo(o.toFile());
		}

		public void addPath(IPath p) {
			var pseg = p.segmentCount();

			assert pseg != 0;

			Subtree child = null;

			var seg1 = p.uptoSegment(1);

			// If we are the ref node, we need our own node
			if (pseg == 1) {
				child = new Subtree(seg1, true);
				addChild(child);
				return;
			}

			for (var c : m_children) {
				// Child has ref, must not attach
				if (c.m_hasref)
					continue;
				if (c.compareTo(seg1) == 0) {
					child = c;
					break;
				}
			}

			if (child == null) {
				child = new Subtree(seg1);
				addChild(child);
			}

			child.addPath(p.removeFirstSegments(1));
		}

		public void addChild(Subtree c) {
			c.m_parent = this;
			m_children.add(c);
		}

		public void compact() {
			if (m_children.isEmpty())
				return;

			for (var child : m_children) {
				child.compact();
			}

			// Root node
			if (m_pathName.isEmpty())
				return;

			if (m_hasref)
				return;

			//Can't aggregate with multiple children
			if (m_children.size() != 1) {
				return;
			}

			var child = m_children.first();
			m_children = child.m_children;
			m_children.stream().forEach(p -> p.m_parent = this);
			m_pathName = m_pathName.append(child.m_pathName);
			m_hasref = child.m_hasref;
		}

		public boolean split(int maxseg) {
			if (m_children.isEmpty())
				return false;

			boolean csplit = false;

			// A clone is needed here, as our children might modify us
			var cclone = m_children.stream().collect(Collectors.toList());
			for (var child : cclone) {
				csplit |= child.split(maxseg);
			}

			// Our child has not split, but that should not keep out parent from splitting
			if (csplit) {
				return false;
			}

			// Root node
			if (m_parent == null) {
				return false;
			}

			if (m_hasref)
				return false;

			// Do not split if more than maxseg segments
			if (m_pathName.segmentCount() > maxseg) {
				return false;
			}

			// Add our children to our parent and remove ourselves
			for (var child : m_children) {
				child.m_pathName = m_pathName.append(child.m_pathName);
				m_parent.addChild(child);
			}
			m_parent.m_children.remove(this);

			return true;

		}

		public void injectRef(IIncludeReference r, int level) {
			var abspath = ir2p(r);
			assert abspath.segmentCount() >= level;
			if (abspath.segmentCount() == level) {
				assert m_hasref;
				assert m_ref == null;
				//assert children.size() == 0;
				m_ref = r;
				return;
			}

			var workp = abspath.removeFirstSegments(level);
			for (var child : m_children) {
				if (!child.m_pathName.isPrefixOf(workp))
					continue;

				var reslevel = level + child.m_pathName.segmentCount();
				if ((reslevel == abspath.segmentCount()) == child.m_hasref) {
					child.injectRef(r, reslevel);
					return;
				}

			}
			assert false : "We should never reach this point!"; //$NON-NLS-1$

		}

		public IncludeReferenceProxy toProxy(CElementGrouping parent) {

			var refCont = parent instanceof IncludeReferenceProxy
					? ((IncludeReferenceProxy) parent).getIncludeRefContainer()
					: (IncludeRefContainer) parent;
			var irefp = new IncludeReferenceProxy(parent, refCont, m_ref, m_pathName.toString());

			if (m_children.isEmpty())
				return irefp;

			for (var c : m_children) {
				irefp.addChild(c.toProxy(irefp));
			}
			return irefp;

		}

		public String dump() {
			return dump(0);
		}

		String dump(int level) {
			String rv = "  ".repeat(level) + toString() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
			for (var c : m_children) {
				rv += c.dump(level + 1);
			}
			return rv;
		}

	}

	private Object[] getAggrChildren(IIncludeReference[] references) {

		var rootnode = new Subtree();
		// Make sure there are no duplicates
		references = Arrays.stream(references).distinct().toArray(IIncludeReference[]::new);

		for (var ref : references) {
			final var p = ir2p(ref);
			rootnode.addPath(p);
		}

		dbgPrintln("INS:\n" + rootnode.dump()); //$NON-NLS-1$
		rootnode.compact();
		dbgPrintln("Comp:\n" + rootnode.dump()); //$NON-NLS-1$

		if (representation == Representation.Smart) {
			rootnode.split(3);
			dbgPrintln("Split:\n" + rootnode.dump()); //$NON-NLS-1$
		}

		for (var ref : references) {
			rootnode.injectRef(ref, 0);
			dbgPrintln("Inj:\n" + rootnode.dump());//$NON-NLS-1$
		}

		// Remove empty root-node
		var l1children = rootnode.m_children;

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
		final var commonref = ir2p(references[0]);
		int matchcnt = Integer.MAX_VALUE;

		for (var r : references) {
			matchcnt = Math.min(matchcnt, commonref.matchingFirstSegments(ir2p(r)));
		}
		if (matchcnt == 0)
			return getList(references);

		var common = commonref.removeLastSegments(commonref.segmentCount() - matchcnt);
		// This is not sorted
		var rv = new IncludeReferenceProxy(this, this, null, common.toString(), false);

		for (var r : references) {
			var cld = new IncludeReferenceProxy(rv, this, r, ir2p(r).removeFirstSegments(matchcnt).toString());
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
			CUIPlugin.log(e);
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
		case Smart:
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

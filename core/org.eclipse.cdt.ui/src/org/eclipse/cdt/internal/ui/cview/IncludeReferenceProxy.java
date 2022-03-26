/*******************************************************************************
 * Copyright (c) 2000, 2022 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.cview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IIncludeReference;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CElementGrouping;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;

public class IncludeReferenceProxy extends CElementGrouping implements Comparable<IncludeReferenceProxy> {
	private final List<IncludeReferenceProxy> children = new ArrayList<>();
	private String segment;
	private Object parent;
	private boolean sorted;
	IncludeRefContainer includeRefContainer;
	IIncludeReference reference;

	@Override
	public int compareTo(IncludeReferenceProxy o) {
		var rv = segment.compareTo(o.segment);
		if (rv != 0)
			return rv;
		if (isIncludePath() != o.isIncludePath()) {
			return isIncludePath() ? -1 : 1;
		}
		return 0;
	}

	public IncludeReferenceProxy(Object parent, IncludeRefContainer includeRefContainer, IIncludeReference reference,
			String segment, boolean sorted) {
		super(0);
		this.parent = parent;
		this.includeRefContainer = includeRefContainer;
		this.reference = reference;
		this.segment = segment;
		this.sorted = sorted;
	}

	public IncludeReferenceProxy(Object parent, IncludeRefContainer includeRefContainer, IIncludeReference reference,
			String segment) {
		this(parent, includeRefContainer, reference, segment, true);
	}

	/**
	 * Whether this is an actual include path or just a dummy within the path hierarchy
	 * @return true is is an include path
	 */
	public boolean isIncludePath() {
		return reference != null;
	}

	/**
	 * Returns the Proxy's reference or that of one of its childs, if it is not an include path
	 * @see isIncludePath
	 * @return a Reference - never null
	 */
	public IIncludeReference getReference() {
		if (reference != null)
			return reference;
		for (var c : children) {
			var cr = c.getReference();
			if (cr != null)
				return cr;
		}
		assert false : "No reference found - tree broken"; //$NON-NLS-1$
		return null;
	}

	public void addChild(IncludeReferenceProxy cld) {
		assert cld.parent == this;
		children.add(cld);
		if (sorted)
			Collections.sort(children);
	}

	public void setIncludeReference(IIncludeReference reference) {
		// Probably an error for two points in the hierarchy to have same IIncludeReference?
		assert this.reference == null : "Ref is: " + this.toString() + "but getting " + reference.toString(); //$NON-NLS-1$ //$NON-NLS-2$
		this.reference = reference;
	}

	public IPath getPath() {
		if (parent instanceof IncludeReferenceProxy) {
			IncludeReferenceProxy includeReferenceProxy = (IncludeReferenceProxy) parent;
			IPath path = includeReferenceProxy.getPath();
			return path.append(segment);
		}
		return new Path(segment);
	}

	@Override
	public Object[] getChildren(Object object) {
		if (reference == null) {
			return children.toArray();
		} else {
			assert children.isEmpty();
			try {
				return reference.getChildren();
			} catch (CModelException e) {
				e.printStackTrace();
				return NO_CHILDREN;
			}
		}

	}

	@Override
	public ImageDescriptor getImageDescriptor(Object object) {
		return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_INCLUDES_FOLDER);
	}

	@Override
	public Object getParent(Object object) {
		return parent;
	}

	public IncludeRefContainer getIncludeRefContainer() {
		return includeRefContainer;
	}

	@Override
	public int hashCode() {
		return Objects.hash(parent, reference, segment);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IncludeReferenceProxy other = (IncludeReferenceProxy) obj;
		return Objects.equals(parent, other.parent) && Objects.equals(reference, other.reference)
				&& Objects.equals(segment, other.segment);
	}

	public boolean hasChildren() {
		return !children.isEmpty() || (reference != null && reference.hasChildren());
	}

	@Override
	public String toString() {
		if (segment != null) {
			return segment;
		}
		assert reference != null;
		return reference.toString();
	}
}

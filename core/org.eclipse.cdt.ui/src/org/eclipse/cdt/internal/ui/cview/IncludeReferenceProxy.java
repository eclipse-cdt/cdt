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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IIncludeReference;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CElementGrouping;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;

public class IncludeReferenceProxy extends CElementGrouping {
	private final Map<String, IncludeReferenceProxy> children = new TreeMap<>();
	private String segment;
	private Object parent;
	IncludeRefContainer includeRefContainer;
	IIncludeReference reference;

	public IncludeReferenceProxy(Object parent, IncludeRefContainer includeRefContainer, IIncludeReference reference,
			String segment) {
		super(0);
		this.parent = parent;
		this.includeRefContainer = includeRefContainer;
		this.reference = reference;
		this.segment = segment;
	}

	public boolean isIncludePath() {
		return reference != null;
	}

	public void addChild(IncludeReferenceProxy cld) {
		assert cld.parent == this;
		children.put(cld.segment, cld);
	}

	public void setIncludeReference(IIncludeReference reference) {
		// Probably an error for two points in the hierarchy to have same IIncludeReference?
		//assert this.reference == null : "Ref is: " + this.toString() + "but getting " + reference.toString(); //$NON-NLS-1$ //$NON-NLS-2$
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
		try {
			List<Object> combined = new ArrayList<>();
			// TODO the sorter puts the children after the include files/subdirs.
			// So case where /usr/include and /usr/include/x86_64-linux-gnu are both in the include
			// paths I think that x86_64-linux-gnu should be at the top, but it
			// currently is at the bottom
			combined.addAll(children.values());
			if (reference != null) {
				combined.addAll(Arrays.asList(reference.getChildren()));
			}
			return combined.toArray();
		} catch (CModelException e) {
			// We should log the error. // XXX: Maybe 18 years later we can decide we don't need to :-)
		}
		return NO_CHILDREN;
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

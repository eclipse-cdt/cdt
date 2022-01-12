/*
 * Copyright (c) 2013 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core.pdom;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNamedNode;
import org.eclipse.core.runtime.CoreException;

/**
 * A Qt-specific specialization of the generic PDOMVisitor.  This class provides
 * an empty implementation of {@link #leave(IPDOMNode)}, but required implementations to
 * provide {@link #visit(IPDOMNode)}.  The class also provides a few commonly required
 * implementations.
 */
@SuppressWarnings("restriction")
public abstract class QtPDOMVisitor implements IPDOMVisitor {

	/**
	 * Collects all nodes that match the given type.  This could be used, for example, to get
	 * all QtPDOMQObject's from the index.
	 */
	public static class All<T> extends QtPDOMVisitor {

		private final Class<T> cls;
		public final ArrayList<T> list = new ArrayList<>();

		public All(Class<T> cls) {
			this.cls = cls;
		}

		@Override
		@SuppressWarnings("unchecked")
		public boolean visit(IPDOMNode node) throws CoreException {
			if (node != null && cls.isAssignableFrom(node.getClass()))
				list.add((T) node);
			return true;
		}
	}

	/**
	 * A simple interface that is used to select node's from the index based on specific
	 * criteria.
	 */
	public static interface IFilter {
		public boolean matches(IPDOMNode node) throws CoreException;
	}

	/**
	 * A filter that selects nodes based on their name.
	 */
	public static class PDOMNamedNodeFilter implements IFilter {

		private final char[] name;

		public PDOMNamedNodeFilter(String name) {
			this.name = name.toCharArray();
		}

		@Override
		public boolean matches(IPDOMNode node) throws CoreException {
			if (node instanceof PDOMNamedNode)
				return Arrays.equals(name, ((PDOMNamedNode) node).getNameCharArray());
			return false;
		}
	}

	/**
	 * A utility class that searches the index for all nodes that match the given filter.
	 */
	public static class Find<T> extends QtPDOMVisitor {

		private final Class<T> cls;
		private final IFilter filter;

		public T element;

		public Find(Class<T> cls, IFilter filter) {
			this.cls = cls;
			this.filter = filter;
		}

		@Override
		@SuppressWarnings("unchecked")
		public boolean visit(IPDOMNode node) throws CoreException {
			if (element != null)
				return false;

			if (cls.isAssignableFrom(node.getClass()) && filter.matches(node))
				element = (T) node;

			return element == null;
		}
	}

	@Override
	public void leave(IPDOMNode node) throws CoreException {
	}
}

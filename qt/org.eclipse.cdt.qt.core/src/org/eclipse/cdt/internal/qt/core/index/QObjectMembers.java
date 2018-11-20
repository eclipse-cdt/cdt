/*
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class QObjectMembers<T extends IQObject.IMember> implements IQObject.IMembers<T> {

	private final List<T> all;
	private final Collection<T> locals;
	private Collection<T> withoutOverrides;

	public static <T extends IQObject.IMember> QObjectMembers<T> create(Collection<T> locals, Collection<T> inherited) {
		// NOTE: All must be ordered with the locals before the inherited members.  This ensures that
		//       the algorithm for computing #withoutOverrides will filter out the parent members and
		//       not the local ones.
		// @see withoutOverrides()
		ArrayList<T> all = new ArrayList<>(locals.size() + inherited.size());
		all.addAll(locals);
		all.addAll(inherited);
		return new QObjectMembers<>(all, locals);
	}

	private QObjectMembers(List<T> all, Collection<T> locals) {
		this.all = Collections.unmodifiableList(all);
		this.locals = Collections.unmodifiableCollection(locals);
	}

	@Override
	public Collection<T> all() {
		return all;
	}

	@Override
	public Collection<T> locals() {
		return locals;
	}

	@Override
	public Collection<T> withoutOverrides() {

		if (withoutOverrides == null)
			synchronized (all) {
				if (withoutOverrides == null) {

					// Naively tests each existing element for override before inserting the new
					// element.  Most member lists have less than 3 elements, and the largest that
					// I've found (in the Qt impl) is about 20; so performance may not be as bad
					// as it seems.
					//
					// An earlier approach tried to use a SortedSet with the #isOverride result in
					// the Comparator.  The problem with the approach is finding a stable sort order
					// when the members don't override each other.
					// E.g., if o1 and o2 override each other and m is unrelated, we could get a
					// tree like:
					//        m
					//       / \
					//      o1 o2

					ArrayList<T> filtered = new ArrayList<>(all.size());
					for (T member : all) {
						boolean isOverridden = false;
						for (Iterator<T> i = filtered.iterator(); !isOverridden && i.hasNext();)
							isOverridden = member.isOverride(i.next());
						if (!isOverridden)
							filtered.add(member);
					}

					withoutOverrides = Collections.unmodifiableCollection(filtered);
				}
			}

		return withoutOverrides;
	}
}

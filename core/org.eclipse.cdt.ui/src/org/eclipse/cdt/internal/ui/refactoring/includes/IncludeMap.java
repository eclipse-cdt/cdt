/*******************************************************************************
 * Copyright (c) 2012, 2014 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.includes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.cdt.internal.corext.codemanipulation.IncludeInfo;
import org.eclipse.ui.IMemento;

/**
 * A set of header file substitution rules.
 */
public class IncludeMap {
	private static final String TAG_MAPPING = "mapping"; //$NON-NLS-1$
	private static final String TAG_KEY = "key"; //$NON-NLS-1$
	private static final String TAG_VALUE = "value"; //$NON-NLS-1$

	private final boolean unconditionalSubstitution; // Not serialized when saving to a memento.
	// The order is not crucial but can make a difference when calculating transitive closure.
	private final LinkedHashMap<IncludeInfo, List<IncludeInfo>> map;

	public IncludeMap(boolean unconditionalSubstitution) {
		this.unconditionalSubstitution = unconditionalSubstitution;
		this.map = new LinkedHashMap<>();
	}

	/**
	 * @param keysAndValues an array of keys and values: [key1, value1, key2, value2, ...].
	 *     Keys and values may be optionally surrounded by double quotes or angle brackets.
	 *     Angle brackets indicate a system include.
	 */
	public IncludeMap(boolean unconditionalSubstitution, String[] keysAndValues) {
		if (keysAndValues.length % 2 != 0)
			throw new IllegalArgumentException("More keys than values"); //$NON-NLS-1$
		this.unconditionalSubstitution = unconditionalSubstitution;
		this.map = new LinkedHashMap<>(keysAndValues.length / 2);
		for (int i = 0; i < keysAndValues.length;) {
			String key = keysAndValues[i++];
			addMapping(key, keysAndValues[i++]);
		}
	}

	public IncludeMap(IncludeMap other) {
		this.unconditionalSubstitution = other.unconditionalSubstitution;
		this.map = new LinkedHashMap<>(other.map.size());
		addAllMappings(other);
	}

	/**
	 * Indicates that header file {@code to} should be used instead of {@code from}.
	 *
	 * @param from The header file to be replaced.
	 * @param to The header file to be used.
	 */
	protected void addMapping(IncludeInfo from, IncludeInfo to) {
		if (from.equals(to))
			return; // Don't allow mapping to itself.
		List<IncludeInfo> list = map.get(from);
		if (list == null) {
			list = new ArrayList<>(2);
			map.put(from, list);
		}
		list.add(to);
	}

	/**
	 * Indicates that header file {@code to} should be used instead of {@code from}.

	 * @param from The header file to be replaced. The header is represented by an include name
	 *     optionally surrounded by double quotes or angle brackets. Angle brackets indicate
	 *     a system include.
	 * @param to The header file to be used. The header is represented by an include name optionally
	 *     surrounded by double quotes or angle brackets. Angle brackets indicate a system include.
	 */
	public void addMapping(String from, String to) {
		addMapping(new IncludeInfo(from), new IncludeInfo(to));
	}

	/**
	 * Returns header files that should be used instead of the given one.
	 *
	 * @param from The header file to be replaced. A system header has to match exactly.
	 *     A non-system header matches both, non-system and system headers.
	 * @return The list of header files ordered by decreasing preference.
	 */
	public List<IncludeInfo> getMapping(IncludeInfo from) {
		List<IncludeInfo> list = map.get(from);
		if (list == null) {
			if (!from.isSystem())
				list = map.get(new IncludeInfo(from.getName(), true));
			if (list == null)
				return Collections.emptyList();
		}
		return list;
	}

	/**
	 * Removes substitutions for a given header file.
	 *
	 * @param from the header file to remove substitutions for
	 * @return the previous substitutions associated with the header file, or
	 *         {@code null} if there were no substitutions for the header.
	 */
	public List<IncludeInfo> removeMapping(String from) {
		return removeMapping(new IncludeInfo(from));
	}

	/**
	 * Removes substitutions for a given header file.
	 *
	 * @param from the header file to remove substitutions for
	 * @return the previous substitutions associated with the header file, or
	 *         {@code null} if there were no substitutions for the header.
	 */
	public List<IncludeInfo> removeMapping(IncludeInfo from) {
		return map.remove(from);
	}

	/**
	 * Returns header files that should be used instead of the given one.
	 *
	 * @param from The header file to be replaced. A system header has to match exactly.
	 *     A non-system header matches both, non-system and system headers.
	 * @return The list of header files ordered by decreasing preference.
	 */
	public List<IncludeInfo> getMapping(String from) {
		return getMapping(new IncludeInfo(from));
	}

	public IncludeInfo getPreferredMapping(IncludeInfo from) {
		List<IncludeInfo> list = getMapping(from);
		return list == null ? null : list.get(0);
	}

	public boolean isUnconditionalSubstitution() {
		return unconditionalSubstitution;
	}

	/**
	 * Writes the map to a memento. The {@link #isUnconditionalSubstitution()} flag is not written.
	 */
	public void saveToMemento(IMemento memento) {
		List<IncludeInfo> keys = new ArrayList<>(map.keySet());
		Collections.sort(keys);
		for (IncludeInfo key : keys) {
			for (IncludeInfo value : map.get(key)) {
				IMemento mapping = memento.createChild(TAG_MAPPING);
				mapping.putString(TAG_KEY, key.toString());
				mapping.putString(TAG_VALUE, value.toString());
			}
		}
	}

	public static IncludeMap fromMemento(boolean unconditionalSubstitution, IMemento memento) {
		IncludeMap includeMap = new IncludeMap(unconditionalSubstitution);
		Set<String> keys = unconditionalSubstitution ? new HashSet<>() : Collections.<String>emptySet();
		for (IMemento mapping : memento.getChildren(TAG_MAPPING)) {
			String key = mapping.getString(TAG_KEY);
			// There can be no more than one unconditional substitution for any header file.
			if (!unconditionalSubstitution || keys.add(key))
				includeMap.addMapping(key, mapping.getString(TAG_VALUE));
		}
		return includeMap;
	}

	public void addAllMappings(IncludeMap other) {
		if (other.unconditionalSubstitution != unconditionalSubstitution)
			throw new IllegalArgumentException();
		for (Entry<IncludeInfo, List<IncludeInfo>> entry : other.map.entrySet()) {
			IncludeInfo source = entry.getKey();
			List<IncludeInfo> otherTargets = entry.getValue();
			List<IncludeInfo> targets = map.get(source);
			if (targets == null) {
				targets = new ArrayList<>(otherTargets);
				map.put(source, targets);
			} else {
				targets.addAll(otherTargets);
			}
		}
	}

	public void transitivelyClose() {
		for (Entry<IncludeInfo, List<IncludeInfo>> entry : map.entrySet()) {
			IncludeInfo source = entry.getKey();
			List<IncludeInfo> targets = entry.getValue();
			ArrayDeque<IncludeInfo> queue = new ArrayDeque<>(targets);
			targets.clear();
			HashSet<IncludeInfo> processed = new HashSet<>();
			if (!unconditionalSubstitution)
				processed.add(source); // Don't allow mapping to itself.
			HashSet<IncludeInfo> seenTargets = new HashSet<>();
			IncludeInfo target;
			queueLoop: while ((target = queue.pollFirst()) != null) {
				if (processed.contains(target))
					continue;
				List<IncludeInfo> newTargets = map.get(target);
				if (newTargets != null) {
					queue.addFirst(target);
					boolean added = false;
					// Check if we saw the same target earlier to protect against an infinite loop.
					if (seenTargets.add(target)) {
						for (int i = newTargets.size(); --i >= 0;) {
							IncludeInfo newTarget = newTargets.get(i);
							if (!processed.contains(newTarget)) {
								if (unconditionalSubstitution && newTarget.equals(source)) {
									break queueLoop; // Leave the mapping empty.
								}
								queue.addFirst(newTarget);
								added = true;
							}
						}
					}
					if (!added) {
						target = queue.pollFirst();
						targets.add(target);
						if (unconditionalSubstitution)
							break;
						processed.add(target);
						seenTargets.clear();
					}
				} else {
					targets.add(target);
					if (unconditionalSubstitution)
						break;
					processed.add(target);
					seenTargets.clear();
				}
			}
		}
		if (unconditionalSubstitution) {
			// Remove trivial mappings.
			for (Iterator<Entry<IncludeInfo, List<IncludeInfo>>> iter = map.entrySet().iterator(); iter.hasNext();) {
				Entry<IncludeInfo, List<IncludeInfo>> entry = iter.next();
				IncludeInfo source = entry.getKey();
				List<IncludeInfo> targets = entry.getValue();
				if (targets.isEmpty() || (targets.size() == 1 && source.equals(targets.get(0)))) {
					iter.remove();
				}
			}
		}
	}

	/** For debugging only. */
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("upconditionalSubstitution = ").append(unconditionalSubstitution); //$NON-NLS-1$
		ArrayList<IncludeInfo> sources = new ArrayList<>(map.keySet());
		Collections.sort(sources);
		for (IncludeInfo source : sources) {
			buf.append('\n');
			buf.append(source);
			buf.append(" -> "); //$NON-NLS-1$
			List<IncludeInfo> targets = map.get(source);
			for (int i = 0; i < targets.size(); i++) {
				if (i > 0)
					buf.append(", "); //$NON-NLS-1$
				buf.append(targets.get(i));
			}
		}
		return buf.toString();
	}

	public Map<IncludeInfo, List<IncludeInfo>> getMap() {
		return Collections.unmodifiableMap(map);
	}
}
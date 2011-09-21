/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.preferences;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.Assert;

public final class StringSetSerializer {
	private static final String DELIM= "\0"; //$NON-NLS-1$
	private StringSetSerializer() {
	}

	public static String serialize(Set<String> strings) {
		Assert.isLegal(strings != null);
		@SuppressWarnings("null")
		StringBuffer buf= new StringBuffer(strings.size() * 20);
		for (Iterator<String> it= strings.iterator(); it.hasNext();) {
			buf.append(it.next());
			if (it.hasNext())
				buf.append(DELIM);
		}
		return buf.toString();
	}

	public static Set<String> deserialize(String serialized) {
		Assert.isLegal(serialized != null);
		Set<String> marked= new HashSet<String>();
		StringTokenizer tok= new StringTokenizer(serialized, DELIM);
		while (tok.hasMoreTokens())
			marked.add(tok.nextToken());
		return marked;
	}

	public static String[] getDifference(String oldValue, String newValue) {
		Set<String> oldSet= deserialize(oldValue);
		Set<String> newSet= deserialize(newValue);
		Set<String> intersection= new HashSet<String>(oldSet);
		intersection.retainAll(newSet);
		oldSet.removeAll(intersection);
		newSet.removeAll(intersection);
		oldSet.addAll(newSet);
		return oldSet.toArray(new String[oldSet.size()]);
	}
}

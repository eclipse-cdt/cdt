/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Uwe Stieber (Wind River) - initial API and implementation.
 *******************************************************************************/
package org.eclipse.rse.core;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Shared system type id list parser and matcher. Parses a given
 * list of system type id's, separated by semicolon and possibly
 * containing the wildcards '*' and '?. 
 */
public final class SystemTypeMatcher  {
	private final class SystemTypeIdPattern {
		private final Pattern pattern;
		
		/**
		 * Constructor.
		 */
		public SystemTypeIdPattern(Pattern pattern) {
			assert pattern != null;
			this.pattern = pattern;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.rse.core.internal.subsystems.SubSystemConfigurationProxy.ISystemTypePattern#matches(org.eclipse.rse.core.IRSESystemType)
		 */
		public boolean matches(IRSESystemType systemType) {
			assert systemType != null;
			return pattern.matcher(systemType.getId()).matches();
		}
	}
	
	// List of patterns to match. The order is preserved.
	private final List patterns = new LinkedList();
	private boolean matchAllTypes = false;
	
	/**
	 * Constructor. 
	 * 
	 * @param declaredSystemTypeIds  The list of declared system type ids. Might be <code>null</code>.
	 */
	public SystemTypeMatcher(String declaredSystemTypeIds) {
		// Compile the list of patterns out of given lists of declared system types
		if (declaredSystemTypeIds != null) {
			String[] ids = declaredSystemTypeIds.split(";"); //$NON-NLS-1$
			if (ids != null && ids.length > 0) {
				for (int i = 0; i < ids.length; i++) {
					String id = ids[i].trim();
					if (id.equals("*")) { //$NON-NLS-1$
						matchAllTypes = true;
						patterns.clear();
						return;
					} else if(id.length()>0) {
						SystemTypeIdPattern pattern = new SystemTypeIdPattern(Pattern.compile(makeRegex(id)));
						patterns.add(pattern);
					}
				}
			}
		}
	}
	
	private String makeRegex(String pattern) {
		assert pattern != null;
		String translated = pattern;
		if (translated.indexOf('.') != -1) translated = translated.replaceAll("\\.", "\\."); //$NON-NLS-1$ //$NON-NLS-2$
		if (translated.indexOf('*') != -1) translated = translated.replaceAll("\\*", ".*"); //$NON-NLS-1$ //$NON-NLS-2$
		if (translated.indexOf('?') != -1) translated = translated.replaceAll("\\?", "."); //$NON-NLS-1$ //$NON-NLS-2$
		return translated;
	}
	
	/**
	 * @return true if this matcher supports all system types.
	 */
	public boolean supportsAllSystemTypes() {
		return matchAllTypes;
	}
	
	/**
	 * Checks if the specified system type is matched by this pattern.
	 */
	public boolean matches(IRSESystemType systemType) {
		assert systemType != null;
		if (matchAllTypes) return true;
		Iterator iterator = patterns.iterator();
		while (iterator.hasNext()) {
			SystemTypeIdPattern matcher = (SystemTypeIdPattern)iterator.next();
			if (matcher.matches(systemType)) return true;
		}
		return false;
	}
}

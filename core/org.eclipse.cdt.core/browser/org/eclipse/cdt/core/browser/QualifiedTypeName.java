/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.browser;

import java.util.ArrayList;

import org.eclipse.cdt.core.CConventions;
import org.eclipse.core.runtime.IStatus;

public class QualifiedTypeName implements IQualifiedTypeName {

	private String[] fSegments;
	private int fHashCode;

	public QualifiedTypeName(IQualifiedTypeName typeName) {
		this(typeName.segments());
	}

	public QualifiedTypeName(String[] names) {
		fSegments = new String[names.length];
		System.arraycopy(names, 0, fSegments, 0, names.length);
	}

	public QualifiedTypeName(String name, String[] enclosingNames) {
		if (enclosingNames != null) {
			fSegments = new String[enclosingNames.length + 1];
			System.arraycopy(enclosingNames, 0, fSegments, 0, enclosingNames.length);
			fSegments[fSegments.length - 1] = name;
		} else {
			fSegments = new String[] { name };
		}
	}

	public QualifiedTypeName(String qualifiedName) {
		int qualifierIndex = qualifiedName.indexOf(QUALIFIER, 0);
		if (qualifierIndex == -1) {
			fSegments = new String[] { qualifiedName };
		} else {
			ArrayList namesList = new ArrayList(5);
			int lastIndex = 0;
			String nextName;
			while (qualifierIndex >= 0) {
				nextName = qualifiedName.substring(lastIndex, qualifierIndex);
				lastIndex = qualifierIndex + QUALIFIER.length();
				namesList.add(nextName);
				qualifierIndex = qualifiedName.indexOf(QUALIFIER, lastIndex);
			}
			nextName = qualifiedName.substring(lastIndex);
			namesList.add(nextName);
			fSegments = (String[]) namesList.toArray(new String[namesList.size()]);
		}
	}

	public String getName() {
		if (fSegments.length > 0) {
			return fSegments[fSegments.length - 1];
		}
		return null;
	}

	public String[] getEnclosingNames() {
		if (fSegments.length > 1) {
			String[] enclosingNames = new String[fSegments.length - 1];
			System.arraycopy(fSegments, 0, enclosingNames, 0, fSegments.length - 1);
			return enclosingNames;
		}
		return null;
	}
	
	public String getFullyQualifiedName() {
		if (fSegments.length > 0) {
			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < fSegments.length; ++i) {
				if (i > 0) {
					buf.append(QUALIFIER);
				}
				buf.append(fSegments[i]);
			}
			return buf.toString();
		}
		return null;
	}

	public IQualifiedTypeName getEnclosingTypeName() {
		String[] enclosingNames = getEnclosingNames();
		if (enclosingNames != null) {
			return new QualifiedTypeName(enclosingNames);
		}
		return null;
	}

	public boolean isEmpty() {
		return fSegments.length == 0;
	}

	public boolean isGlobal() {
		if (fSegments.length <= 1) {
			return true;
		} else if (fSegments[0] == null || fSegments[0].length() == 0) {
			return true;
		}
		return false;
	}


	public int segmentCount() {
		return fSegments.length;
	}

	public String[] segments() {
		return fSegments;
	}
	
	public String segment(int index) {
		if (index >= fSegments.length) {
			return null;
		}
		return fSegments[index];
	}
	
	public String lastSegment() {
		if (fSegments.length > 0) {
			return fSegments[fSegments.length - 1];
		}
		return null;
	}
	
	public int matchingFirstSegments(IQualifiedTypeName typeName) {
		int max = Math.min(fSegments.length, typeName.segmentCount());
		int count = 0;
		for (int i = 0; i < max; ++i) {
			if (!fSegments[i].equals(typeName.segment(i))) {
				return count;
			}
			count++;
		}
		return count;
	}

	public boolean isPrefixOf(IQualifiedTypeName typeName) {
		if (isEmpty())
			return true;
		
		if (fSegments.length > typeName.segmentCount()) {
			return false;
		}

		for (int i = 0; i < fSegments.length; ++i) {
			if (!fSegments[i].equals(typeName.segment(i))) {
				return false;
			}
		}
		return true;
	}

	public IQualifiedTypeName append(String[] names) {
		String[] newNames = new String[fSegments.length + names.length];
		System.arraycopy(fSegments, 0, newNames, 0, fSegments.length);
		System.arraycopy(names, 0, newNames, fSegments.length, names.length);
		return new QualifiedTypeName(newNames);
	}

	public IQualifiedTypeName append(IQualifiedTypeName typeName) {
		return append(typeName.segments());
	}

	public IQualifiedTypeName append(String qualifiedName) {
		return append(new QualifiedTypeName(qualifiedName));
	}
	
	public IQualifiedTypeName removeFirstSegments(int count) {
		if (count == 0) {
			return this;
		} else if (count >= fSegments.length || count < 0) {
			return new QualifiedTypeName(new String[0]);
		} else {
			int newSize = fSegments.length - count;
			String[] newNames = new String[newSize];
			System.arraycopy(fSegments, count, newNames, 0, newSize);
			return new QualifiedTypeName(newNames);
		}
	}

	public IQualifiedTypeName removeLastSegments(int count) {
		if (count == 0) {
			return this;
		} else if (count >= fSegments.length || count < 0) {
			return new QualifiedTypeName(new String[0]);
		} else {
			int newSize = fSegments.length - count;
			String[] newNames = new String[newSize];
			System.arraycopy(fSegments, 0, newNames, 0, newSize);
			return new QualifiedTypeName(newNames);
		}
	}

	public boolean isLowLevel() {
		for (int i = 0; i < fSegments.length; ++i) {
			if (fSegments[i].startsWith("_")) { //$NON-NLS-1$
				return true;
			}
		}
		return false;
	}

	public boolean validate() {
		for (int i = 0; i < fSegments.length; ++i) {
			if (!isValidSegment(fSegments[i])) {
				return false;
			}
		}
		return true;
	}
	
	private static boolean isValidSegment(String segment) {
		// type name must follow C conventions
		IStatus val= CConventions.validateIdentifier(segment);
		return (val.getSeverity() != IStatus.ERROR);
	}

	public int hashCode() {
		if (fHashCode == 0) {
			String name = getFullyQualifiedName();
			if (name != null)
				fHashCode = name.hashCode();
		}
		return fHashCode;
	}

	public String toString() {
		return getFullyQualifiedName();
	}

	public int compareTo(Object obj) {
		if (obj == this) {
			return 0;
		}
		if (!(obj instanceof IQualifiedTypeName)) {
			throw new ClassCastException();
		}
		IQualifiedTypeName typeName = (IQualifiedTypeName) obj;
		return getFullyQualifiedName().compareTo(typeName.getFullyQualifiedName());
	}

	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof IQualifiedTypeName)) {
			return false;
		}
		IQualifiedTypeName typeName = (IQualifiedTypeName) obj;
		return matchSegments(fSegments, typeName.segments());
	}

	private static boolean matchSegments(String[] a, String[] b) {
		if (a == null && b == null)
			return true;
		if (a == null || b == null)
			return false;
		if (a.length != b.length)
			return false;
		for (int i = 0; i < a.length; ++i) {
			if (!a[i].equals(b[i]))
				return false;
		}
		return true;
	}
}

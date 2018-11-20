/*******************************************************************************
 * Copyright (c) 2004, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.browser;

import org.eclipse.cdt.core.CConventions;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.core.runtime.IStatus;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class QualifiedTypeName implements IQualifiedTypeName {
	private static final String[] NO_SEGMENTS = {};
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private static final int INITIAL_SEGMENT_LENGTH = 12;
	private static final int HASH_INIT = 17;
	private static final int HASH_MULTIPLIER = 37;

	private String[] fSegments = NO_SEGMENTS;
	private int fHashCode;

	public static final QualifiedTypeName EMPTY = new QualifiedTypeName();

	public QualifiedTypeName(IQualifiedTypeName typeName) {
		fSegments = typeName.segments();
	}

	public QualifiedTypeName(String qualifiedName) {
		fSegments = createSegments(qualifiedName);
	}

	public QualifiedTypeName(String[] names) {
		fSegments = createSegments(names);
	}

	public QualifiedTypeName(String name, String[] enclosingNames) {
		if (enclosingNames == null) {
			fSegments = createSegments(name);
		} else {
			fSegments = createSegments(name, enclosingNames);
		}
	}

	private QualifiedTypeName() {
	}

	private String[] createSegments(String qualifiedName) {
		String[] segments;
		int qualifierIndex = qualifiedName.indexOf(QUALIFIER, 0);
		if (qualifierIndex == -1) {
			segments = new String[] { qualifiedName };
		} else {
			int maxSegments = 1;
			int lastIndex = 0;
			while (qualifierIndex >= 0) {
				lastIndex = qualifierIndex + QUALIFIER.length();
				++maxSegments;
				qualifierIndex = qualifiedName.indexOf(QUALIFIER, lastIndex);
			}
			segments = new String[maxSegments];
			int segmentCount = 0;
			lastIndex = 0;
			qualifierIndex = qualifiedName.indexOf(QUALIFIER, 0);
			while (qualifierIndex >= 0) {
				// note: we allocate a new string rather than use the returned substring,
				// otherwise we're holding a reference to the entire original string
				segments[segmentCount] = new String(qualifiedName.substring(lastIndex, qualifierIndex));
				++segmentCount;
				lastIndex = qualifierIndex + QUALIFIER.length();
				qualifierIndex = qualifiedName.indexOf(QUALIFIER, lastIndex);
			}
			// note: we allocate a new string rather than use the returned substring,
			// otherwise we're holding a reference to the entire original string
			segments[segmentCount] = new String(qualifiedName.substring(lastIndex));
		}
		return segments;
	}

	private String[] createSegments(String[] names) {
		String[] segments = new String[names.length];
		System.arraycopy(names, 0, segments, 0, names.length);
		return segments;
	}

	private String[] createSegments(String name, String[] enclosingNames) {
		String[] segments = new String[enclosingNames.length + 1];
		System.arraycopy(enclosingNames, 0, segments, 0, enclosingNames.length);
		segments[segments.length - 1] = name;
		return segments;
	}

	@Override
	public String getName() {
		if (fSegments.length > 0) {
			return fSegments[fSegments.length - 1];
		}
		return EMPTY_STRING;
	}

	@Override
	public String[] getEnclosingNames() {
		if (fSegments.length > 1) {
			String[] enclosingNames = new String[fSegments.length - 1];
			System.arraycopy(fSegments, 0, enclosingNames, 0, fSegments.length - 1);
			return enclosingNames;
		}
		return NO_SEGMENTS;
	}

	@Override
	public String getFullyQualifiedName() {
		if (fSegments.length > 0) {
			StringBuilder buf = new StringBuilder(fSegments.length * INITIAL_SEGMENT_LENGTH);
			for (int i = 0; i < fSegments.length; ++i) {
				if (i > 0) {
					buf.append(QUALIFIER);
				}
				buf.append(fSegments[i]);
			}
			return buf.toString();
		}
		return EMPTY_STRING;
	}

	@Override
	public IQualifiedTypeName getEnclosingTypeName() {
		String[] enclosingNames = getEnclosingNames();
		if (enclosingNames.length > 0) {
			QualifiedTypeName enclosingTypeName = new QualifiedTypeName();
			enclosingTypeName.fSegments = enclosingNames;
			return enclosingTypeName;
		}
		return null;
	}

	@Override
	public boolean isQualified() {
		return (fSegments.length > 1);
	}

	@Override
	public boolean isEmpty() {
		return (fSegments.length == 0);
	}

	@Override
	public boolean isGlobal() {
		return (fSegments.length <= 1 || fSegments[0].length() == 0);
	}

	@Override
	public int segmentCount() {
		return fSegments.length;
	}

	@Override
	public String[] segments() {
		String[] segmentCopy = new String[fSegments.length];
		System.arraycopy(fSegments, 0, segmentCopy, 0, fSegments.length);
		return segmentCopy;
	}

	@Override
	public String segment(int index) {
		if (index >= fSegments.length) {
			return null;
		}
		return fSegments[index];
	}

	@Override
	public String lastSegment() {
		if (fSegments.length > 0) {
			return fSegments[fSegments.length - 1];
		}
		return null;
	}

	@Override
	public int matchingFirstSegments(IQualifiedTypeName typeName) {
		int max = Math.min(fSegments.length, typeName.segmentCount());
		int count = 0;
		for (int i = 0; i < max; ++i) {
			if (!fSegments[i].equals(typeName.segment(i))) {
				return count;
			}
			++count;
		}
		return count;
	}

	@Override
	public boolean isPrefixOf(IQualifiedTypeName typeName) {
		if (fSegments.length == 0)
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

	@Override
	public IQualifiedTypeName append(String[] names) {
		int length = fSegments.length;
		int typeNameLength = names.length;
		String[] newSegments = new String[length + typeNameLength];
		System.arraycopy(fSegments, 0, newSegments, 0, length);
		System.arraycopy(names, 0, newSegments, length, typeNameLength);
		QualifiedTypeName newTypeName = new QualifiedTypeName();
		newTypeName.fSegments = newSegments;
		return newTypeName;
	}

	@Override
	public IQualifiedTypeName append(IQualifiedTypeName typeName) {
		int length = fSegments.length;
		int typeNameLength = typeName.segmentCount();
		String[] newSegments = new String[length + typeNameLength];
		System.arraycopy(fSegments, 0, newSegments, 0, length);
		for (int i = 0; i < typeNameLength; ++i) {
			newSegments[i + length] = typeName.segment(i);
		}
		QualifiedTypeName newTypeName = new QualifiedTypeName();
		newTypeName.fSegments = newSegments;
		return newTypeName;
	}

	@Override
	public IQualifiedTypeName append(String qualifiedName) {
		return append(createSegments(qualifiedName));
	}

	@Override
	public IQualifiedTypeName removeFirstSegments(int count) {
		if (count == 0) {
			return this;
		} else if (count >= fSegments.length || count < 0) {
			return EMPTY;
		} else {
			int newSize = fSegments.length - count;
			String[] newSegments = new String[newSize];
			System.arraycopy(fSegments, count, newSegments, 0, newSize);
			QualifiedTypeName newTypeName = new QualifiedTypeName();
			newTypeName.fSegments = newSegments;
			return newTypeName;
		}
	}

	@Override
	public IQualifiedTypeName removeLastSegments(int count) {
		if (count == 0) {
			return this;
		} else if (count >= fSegments.length || count < 0) {
			return EMPTY;
		} else {
			int newSize = fSegments.length - count;
			String[] newSegments = new String[newSize];
			System.arraycopy(fSegments, 0, newSegments, 0, newSize);
			QualifiedTypeName newTypeName = new QualifiedTypeName();
			newTypeName.fSegments = newSegments;
			return newTypeName;
		}
	}

	@Override
	public boolean isLowLevel() {
		for (int i = 0; i < fSegments.length; ++i) {
			if (fSegments[i].startsWith("_")) { //$NON-NLS-1$
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isValid() {
		for (int i = 0; i < fSegments.length; ++i) {
			String segment = fSegments[i];
			// type name must follow C conventions
			IStatus val = CConventions.validateIdentifier(segment, GPPLanguage.getDefault());
			if (val.getSeverity() == IStatus.ERROR)
				return false;
		}
		return true;
	}

	@Override
	public boolean isValidSegment(String segment) {
		if (segment.indexOf(QUALIFIER) != -1)
			return false;
		// type name must follow C conventions
		IStatus val = CConventions.validateIdentifier(segment, GPPLanguage.getDefault());
		return (val.getSeverity() != IStatus.ERROR);
	}

	@Override
	public int hashCode() {
		if (fHashCode == 0) {
			fHashCode = HASH_INIT;
			for (int i = 0; i < fSegments.length; ++i) {
				fHashCode = fHashCode * HASH_MULTIPLIER + fSegments[i].hashCode();
			}
		}
		return fHashCode;
	}

	@Override
	public String toString() {
		return getFullyQualifiedName();
	}

	@Override
	public int compareTo(IQualifiedTypeName typeName) {
		if (typeName == this)
			return 0;
		if (typeName == null)
			return 1;

		int length = fSegments.length;
		int typeNameLength = typeName.segmentCount();
		int len = Math.min(length, typeNameLength);
		int result = 0;
		for (int i = 0; result == 0 && i < len; ++i) {
			result = fSegments[i].compareTo(typeName.segment(i));
		}
		if (result == 0 && length != typeNameLength) {
			result = (length < typeNameLength) ? -1 : 1;
		}
		return result;
	}

	@Override
	public int compareToIgnoreCase(IQualifiedTypeName typeName) {
		if (typeName == this)
			return 0;
		if (typeName == null)
			return 1;

		int length = fSegments.length;
		int typeNameLength = typeName.segmentCount();
		int len = Math.min(length, typeNameLength);
		int result = 0;
		for (int i = 0; result == 0 && i < len; ++i) {
			result = fSegments[i].compareToIgnoreCase(typeName.segment(i));
		}
		if (result == 0 && length != typeNameLength) {
			result = (length < typeNameLength) ? -1 : 1;
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof IQualifiedTypeName)) {
			return false;
		}
		return equals((IQualifiedTypeName) obj);
	}

	@Override
	public boolean equals(IQualifiedTypeName typeName) {
		if (typeName == this)
			return true;
		if (typeName == null)
			return false;

		int length = fSegments.length;
		int typeNameLength = typeName.segmentCount();
		if (length != typeNameLength)
			return false;
		for (int i = 0; i < length; ++i) {
			if (!fSegments[i].equals(typeName.segment(i)))
				return false;
		}
		return true;
	}

	@Override
	public boolean equalsIgnoreCase(IQualifiedTypeName typeName) {
		if (typeName == this)
			return true;
		if (typeName == null)
			return false;

		int length = fSegments.length;
		int typeNameLength = typeName.segmentCount();
		if (length != typeNameLength)
			return false;
		for (int i = 0; i < length; ++i) {
			if (!fSegments[i].equalsIgnoreCase(typeName.segment(i)))
				return false;
		}
		return true;
	}
}

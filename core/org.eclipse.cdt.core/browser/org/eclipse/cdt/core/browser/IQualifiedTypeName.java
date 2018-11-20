/*******************************************************************************
 * Copyright (c) 2004, 2012 QNX Software Systems and others.
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

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IQualifiedTypeName extends Comparable<IQualifiedTypeName> {
	public final static String QUALIFIER = "::"; //$NON-NLS-1$

	public String getName();

	public String getFullyQualifiedName();

	public IQualifiedTypeName getEnclosingTypeName();

	public String[] getEnclosingNames();

	public boolean isQualified();

	public boolean isEmpty();

	public boolean isGlobal();

	public IQualifiedTypeName append(String qualifiedName);

	public IQualifiedTypeName append(String[] names);

	public IQualifiedTypeName append(IQualifiedTypeName typeName);

	public String[] segments();

	public String segment(int index);

	public int segmentCount();

	public String lastSegment();

	public int matchingFirstSegments(IQualifiedTypeName typeName);

	public IQualifiedTypeName removeFirstSegments(int count);

	public IQualifiedTypeName removeLastSegments(int count);

	public boolean isPrefixOf(IQualifiedTypeName typeName);

	public boolean isLowLevel();

	public boolean isValidSegment(String segment);

	public boolean isValid();

	public boolean equals(IQualifiedTypeName typeName);

	public boolean equalsIgnoreCase(IQualifiedTypeName typeName);

	@Override
	public int compareTo(IQualifiedTypeName typeName);

	public int compareToIgnoreCase(IQualifiedTypeName typeName);
}

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

public interface IQualifiedTypeName extends Comparable {

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
	public boolean validate();
	
	public boolean equals(IQualifiedTypeName typeName);
	public boolean equalsIgnoreCase(IQualifiedTypeName typeName);
	public int compareTo(IQualifiedTypeName typeName);
	public int compareToIgnoreCase(IQualifiedTypeName typeName);
}

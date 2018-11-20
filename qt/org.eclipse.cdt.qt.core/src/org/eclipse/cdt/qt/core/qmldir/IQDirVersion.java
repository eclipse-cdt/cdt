/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.qt.core.qmldir;

/**
 * An AST Node representing a version String of the form &ltMajorVersion&gt.&ltMinorVersion&gt
 */
public interface IQDirVersion extends IQDirASTNode {
	/**
	 * Gets the String value of this version. The result will always be of the form "&ltMajorVersion&gt.&ltMinorVersion&gt"
	 *
	 * @return a string value of this version
	 */
	public String getVersionString();
}

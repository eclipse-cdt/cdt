/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.qt.core.qmljs;

public interface IQmlImport extends IQmlHeaderItem {
	@Override
	default public String getType() {
		return "QMLImport"; //$NON-NLS-1$
	}

	public IQmlModule getModule();

	public IJSLiteral getDirectory();

	public IQmlQualifier getQualifier();
}

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
package org.eclipse.cdt.internal.qt.core.qmltypes;

public class QMLExportInfo {
	private String type;
	private String version;

	QMLExportInfo(QMLModelBuilder builder, String export) {
		String[] info = export.split("\\h+"); //$NON-NLS-1$
		switch (info.length) {
		case 2:
			this.type = info[0];
			this.version = info[1];
			break;
		case 1:
			this.type = info[0];
			break;
		}
	}

	public String getType() {
		return type;
	}

	public String getVersion() {
		return version;
	}
}

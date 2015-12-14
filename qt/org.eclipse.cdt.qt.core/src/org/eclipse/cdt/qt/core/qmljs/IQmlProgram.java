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
package org.eclipse.cdt.qt.core.qmljs;

public interface IQmlProgram extends IQmlASTNode {
	public static enum Modes {
		QML("qml"), QMLTypes("qmltypes"), JavaScript("js"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		private final String ident;

		private Modes(String s) {
			this.ident = s;
		}

		public String getModeIdentifier() {
			return this.ident;
		}
	}

	@Override
	default public String getType() {
		return "QMLProgram"; //$NON-NLS-1$
	};

	public Modes getMode();

	public IQmlHeaderItemList getHeaderItemList();

	public IQmlRootObject getRootObject();
}

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

/**
 * A JavaScript identifier from the <a href="https://github.com/estree/estree/blob/master/spec.md#identifier">ESTree
 * Specification</a>
 */
public interface IJSIdentifier extends IJSExpression, IJSPattern {
	@Override
	default public String getType() {
		return "Identifier"; //$NON-NLS-1$
	};

	public String getName();
}

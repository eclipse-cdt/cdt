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

import java.util.List;

/**
 * A JavaScript switch case from the <a href="https://github.com/estree/estree/blob/master/spec.md#switchcase">ESTree
 * Specification</a>
 */
public interface IJSSwitchCase extends IQmlASTNode {
	@Override
	default String getType() {
		return "SwitchCase"; //$NON-NLS-1$
	}

	public IJSExpression getTest();

	public List<IJSStatement> getConsequent();
}

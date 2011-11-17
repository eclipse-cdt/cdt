/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model.ext;

import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.model.ICElement;

public class EnumeratorHandle extends CElementHandle implements org.eclipse.cdt.core.model.IEnumerator {

	public EnumeratorHandle(ICElement parent, IEnumerator enumerator) {
		super(parent, ICElement.C_ENUMERATOR, enumerator.getName());
	}

	@Override
	public String getConstantExpression() {
		return null;
	}
}

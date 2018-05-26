/*******************************************************************************
 * Copyright (c) 2018 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Hansruedi Patzen (IFS) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCapture;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * Base class for C++ Lambda Captures
 */
public abstract class CPPASTCaptureBase extends ASTNode implements ICPPASTCapture {
	private boolean fPackExpansion;

	protected <T extends CPPASTCaptureBase> T copy(T copy, CopyStyle style) {
		copy.setIsPackExpansion(fPackExpansion);
		return super.copy(copy, style);
	}

	@Override
	public boolean isPackExpansion() {
		return fPackExpansion;
	}

	@Override
	public void setIsPackExpansion(boolean val) {
		assertNotFrozen();
		fPackExpansion = val;
	}
}

/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.makefile.gnu;

import org.eclipse.cdt.make.internal.core.makefile.Directive;

public class Endif extends Terminal {

	public Endif(Directive parent) {
		super(parent);
	}

	public boolean isEndif() {
		return true;
	}
	
    public String toString() {
        return GNUMakefileConstants.TERMINAL_ENDIF;
    }
}

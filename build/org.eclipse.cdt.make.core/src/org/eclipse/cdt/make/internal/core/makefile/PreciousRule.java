/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.internal.core.makefile;

import org.eclipse.cdt.make.core.makefile.IPreciousRule;

/**
 * .PRECIOUS
 * Prerequisites of this special target shall not be removed if make recieves an
 * asynchronous events.
 */
public class PreciousRule extends SpecialRule implements IPreciousRule {

	public PreciousRule(Directive parent, String[] reqs) {
		super(parent, new Target(".PRECIOUS"), reqs, new Command[0]); //$NON-NLS-1$
	}

}

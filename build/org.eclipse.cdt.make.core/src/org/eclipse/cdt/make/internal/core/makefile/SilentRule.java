/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.makefile;

import org.eclipse.cdt.make.core.makefile.ISilentRule;

/**
 * .SILENT
 * Prerequisties of this special target are targets themselves; this shall cause
 * commands associated with them not to be written to the standard output before
 * they are executed.
 */
public class SilentRule extends SpecialRule implements ISilentRule {

	public SilentRule(Directive parent, String[] reqs) {
		super(parent, new Target(MakeFileConstants.RULE_SILENT), reqs, new Command[0]);
	}

}

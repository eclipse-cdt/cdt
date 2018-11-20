/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

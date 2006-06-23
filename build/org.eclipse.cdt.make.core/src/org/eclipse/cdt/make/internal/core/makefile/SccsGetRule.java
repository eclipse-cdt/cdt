/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.makefile;

import org.eclipse.cdt.make.core.makefile.ISccsGetRule;

/**
 * .SCCS_GET
 * The application shall ensure that this special target is specified without
 * prerequesites.
 * The commands specifeied with this target shall replace the default
 * commands associated with this special target.
 */
public class SccsGetRule extends SpecialRule implements ISccsGetRule {

	public SccsGetRule(Directive parent, Command[] cmds) {
		super(parent, new Target(MakeFileConstants.RULE_SCCS_GET), new String[0], cmds);
	}

}

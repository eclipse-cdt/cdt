/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import org.eclipse.cdt.make.core.makefile.IPosixRule;

/**
 * .POSIX
 * The appliation shall ensure that this special target is specified without
 * prerequisites or commands.
 */
public class PosixRule extends SpecialRule implements IPosixRule {

	public PosixRule(Directive parent) {
		super(parent, new Target(MakeFileConstants.RULE_POSIX), new String[0], new Command[0]);
	}
}

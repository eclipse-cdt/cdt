/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
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

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

import org.eclipse.cdt.make.core.makefile.gnu.INotParallelRule;

/**
 * .NOTPARALLEL
 *  If `.NOTPARALLEL' is mentioned as a target, then this invocation of
 *  `make' will be run serially, even if the `-j' option is given.
 *  Any recursively invoked `make' command will still be run in
 *  parallel (unless its makefile contains this target).  Any
 *  prerequisites on this target are ignored.
 */
public class NotParallelRule extends SpecialRule implements INotParallelRule {

	public NotParallelRule(Directive parent, String[] reqs) {
		super(parent, new Target(GNUMakefileConstants.RULE_NOT_PARALLEL), reqs, new Command[0]);
	}

}

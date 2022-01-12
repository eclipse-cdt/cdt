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
package org.eclipse.cdt.make.internal.core.makefile.gnu;

import org.eclipse.cdt.make.core.makefile.gnu.IPhonyRule;
import org.eclipse.cdt.make.internal.core.makefile.Command;
import org.eclipse.cdt.make.internal.core.makefile.Directive;
import org.eclipse.cdt.make.internal.core.makefile.SpecialRule;
import org.eclipse.cdt.make.internal.core.makefile.Target;

/**
 * .PHONY
 *     The prerequisites of the special target `.PHONY' are considered to be phony targets.
 *     When it is time to consider such a target, `make' will run its commands unconditionally, regardless of
 *     whether a file with that name exists or what its last-modification time is.
 */
public class PhonyRule extends SpecialRule implements IPhonyRule {

	public PhonyRule(Directive parent, String[] reqs) {
		super(parent, new Target(GNUMakefileConstants.RULE_PHONY), reqs, new Command[0]);
	}

}

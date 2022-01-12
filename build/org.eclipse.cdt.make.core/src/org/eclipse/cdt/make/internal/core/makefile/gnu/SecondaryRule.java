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

import org.eclipse.cdt.make.core.makefile.gnu.ISecondaryRule;
import org.eclipse.cdt.make.internal.core.makefile.Command;
import org.eclipse.cdt.make.internal.core.makefile.Directive;
import org.eclipse.cdt.make.internal.core.makefile.SpecialRule;
import org.eclipse.cdt.make.internal.core.makefile.Target;

/**
 * .SECONDARY
 *  The targets which `.SECONDARY' depends on are treated as
 *  intermediate files, except that they are never automatically deleted.
 *
 *  `.SECONDARY' with no prerequisites causes all targets to be treated
 *  as secondary (i.e., no target is removed because it is considered intermediate).
 */
public class SecondaryRule extends SpecialRule implements ISecondaryRule {

	public SecondaryRule(Directive parent, String[] reqs) {
		super(parent, new Target(GNUMakefileConstants.RULE_SECONDARY), reqs, new Command[0]);
	}

}

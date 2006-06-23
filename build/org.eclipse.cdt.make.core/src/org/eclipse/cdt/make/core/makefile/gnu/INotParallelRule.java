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
package org.eclipse.cdt.make.core.makefile.gnu;

import org.eclipse.cdt.make.core.makefile.ISpecialRule;

/**
 * .NOTPARALLEL
 *  If `.NOTPARALLEL' is mentioned as a target, then this invocation of
 *  `make' will be run serially, even if the `-j' option is given.
 *  Any recursively invoked `make' command will still be run in
 *  parallel (unless its makefile contains this target).  Any
 *  prerequisites on this target are ignored.
 */
public interface INotParallelRule extends ISpecialRule {
}

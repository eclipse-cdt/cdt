/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
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
package org.eclipse.cdt.make.core.makefile.gnu;

import org.eclipse.cdt.make.core.makefile.ISpecialRule;

/**
 * .DELETE_ON_ERROR'
 *  If `.DELETE_ON_ERROR' is mentioned as a target anywhere in the
 *  makefile, then `make' will delete the target of a rule if it has
 *  changed and its commands exit with a nonzero exit status, just as
 *  it does when it receives a signal.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IDeleteOnErrorRule extends ISpecialRule {
}

/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
 */
public interface IDeleteOnErrorRule extends ISpecialRule {
}

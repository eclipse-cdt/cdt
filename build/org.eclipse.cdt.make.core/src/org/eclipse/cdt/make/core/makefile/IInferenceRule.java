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
package org.eclipse.cdt.make.core.makefile;

/**
 * IInferenceRules are formated as follows:
 * target:
 * <tab>command
 * [<tab>command]
 * 
 * The target is of the form .s2 or .s1.s2
 * There are no prerequisites.
 */
public interface IInferenceRule extends IRule {
}

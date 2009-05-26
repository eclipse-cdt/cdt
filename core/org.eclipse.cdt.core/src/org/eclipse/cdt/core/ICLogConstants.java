/*******************************************************************************
 *  Copyright (c) 2002, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *  IBM Rational Software - Initial API and implementation
 *  Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.core;

/**
 * @author bgheorgh
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICLogConstants {
    public class LogConst {
        private LogConst() {
        }
    }

    public static final LogConst PDE = new LogConst();
    public static final LogConst CDT = new LogConst();
}

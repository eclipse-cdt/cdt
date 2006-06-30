/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.ui.util;

import java.text.MessageFormat;

public class Messages {
    public static String format(String pattern, Object[] args) {
        return MessageFormat.format(pattern, args);
    }
    
    public static String format(String pattern, Object arg0) {
        return format(pattern, new Object[] {arg0});
    }

    public static String format(String pattern, Object arg0, Object arg1) {
        return format(pattern, new Object[] {arg0, arg1});
    }

    public static String format(String pattern, Object arg0, Object arg1, Object arg2) {
        return format(pattern, new Object[] {arg0, arg1, arg2});
    }

    public static String format(String pattern, Object arg0, Object arg1, Object arg2, Object arg3) {
        return format(pattern, new Object[] {arg0, arg1, arg2, arg3});
    }
}

package org.eclipse.cdt.core.resources;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.ConsoleOutputStream;


public interface IConsole {
    void clear();
    ConsoleOutputStream getOutputStream();
}


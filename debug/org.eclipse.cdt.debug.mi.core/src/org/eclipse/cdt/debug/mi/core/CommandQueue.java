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
package org.eclipse.cdt.debug.mi.core;
 
import org.eclipse.cdt.debug.mi.core.command.Command;

/**
 * Simple thread-safe Queue implemetation.
 */
public class CommandQueue extends Queue {


	public CommandQueue() {
		super();
	}

	public Command removeCommand(int id) {
		//print("in removeCommand(" + id + ") - entering");
		synchronized (list) {
			int size = list.size();
			for (int i = 0; i < size; i++) {
				Command cmd = (Command)list.get(i);
				int token = cmd.getToken();
				if (token == id) {
					list.remove(cmd);
					return cmd;
				} 
			}
		}
		return null;
	}

	public Command removeCommand() throws InterruptedException {
		//print("in removeCommand() - entering");
		return (Command)removeItem();
	}

	public void addCommand(Command cmd) {
		//print("in addCommand() - entering");
		addItem(cmd);
	}

	public Command[] clearCommands() {
		Object[] objs = clearItems();
		Command[] cmds = new Command[objs.length];
		System.arraycopy(objs, 0, cmds, 0, objs.length);
		return cmds;
	}

//	private static void print(String msg) {
//		String name = Thread.currentThread().getName();
//		System.out.println(name + ": " + msg);
//	}
}

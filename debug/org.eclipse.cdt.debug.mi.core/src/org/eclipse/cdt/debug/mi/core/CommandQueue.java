/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core;
 
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.debug.mi.core.command.Command;

/**
 * Simple thread-safe Queue implemetation.
 */
public class CommandQueue extends Queue{


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

	private static void print(String msg) {
		String name = Thread.currentThread().getName();
		System.out.println(name + ": " + msg);
	}
}

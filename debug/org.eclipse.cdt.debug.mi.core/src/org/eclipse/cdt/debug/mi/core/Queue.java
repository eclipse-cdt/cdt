package org.eclipse.cdt.debug.mi.core;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.debug.mi.core.command.Command;

public class Queue {

	private List list;

	public Queue() {
		list = Collections.synchronizedList(new LinkedList());
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
		synchronized (list) {
			while (list.isEmpty()) {
				//print("in removeCommand() - about to wait()");
				list.wait();
				//print("in removeCommand() - done with wait()");
			}

			// extract the new first cmd
			Command cmd = (Command)list.remove(0);

			//print("in removeCommand() - leaving");
			return cmd;
		}
	}
	public void addCommand(Command cmd) {
		//print("in addCommand() - entering");
		synchronized (list) {
			// There will always be room to add to this List
			// because it expands as needed.
			list.add(cmd);
			//print("in addCommand - just added: '" + cmd + "'");

			// After adding, notify any and all waiting
			// threads that the list has changed.
			list.notifyAll();
			//print("in addCommand() - just notified");
		}
		//print("in addCommand() - leaving");
	}

	private static void print(String msg) {
		String name = Thread.currentThread().getName();
		System.out.println(name + ": " + msg);
	}
}

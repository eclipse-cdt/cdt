package org.eclipse.cdt.internal.core.index;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IResource;

public class RequestList {

	private List list;

	public RequestList() {
		list = Collections.synchronizedList(new LinkedList());
	}

	public IResource removeItem() throws InterruptedException {
		//print("in removeItem() - entering");
		synchronized (list) {
			while (list.isEmpty()) {
				//print("in removeItem() - about to wait()");
				list.wait();
				//print("in removeItem() - done with wait()");
			}

			// extract the new first item
			IResource item = (IResource)list.remove(0);

			//print("in removeItem() - leaving");
			return item;
		}
	}

	public boolean removeItem(Object key) {
		return list.remove(key);
	}

	public void addItem(IResource item) {
		//print("in addItem() - entering");
		synchronized (list) {
			// There will always be room to add to this List
			// because it expands as needed.
			list.add(item);
			//print("in addItem - just added: '" + item + "'");

			// After adding, notify any and all waiting
			// threads that the list has changed.
			list.notifyAll();
			//print("in addItem() - just notified");
		}
		//print("in addItem() - leaving");
	}

//	private static void print(String msg) {
//		String name = Thread.currentThread().getName();
//		System.out.println(name + ": " + msg);
//	}
}

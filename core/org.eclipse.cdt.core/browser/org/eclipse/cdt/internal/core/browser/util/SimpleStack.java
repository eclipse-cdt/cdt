/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.browser.util;

import java.util.ArrayList;

/**
 * A helper class which allows you to perform some simple
 * stack operations.  Avoids the extra overhead of
 * synchronization in Java.Util.Stack.
 */
public class SimpleStack {

	private static int INITIAL_STACK_SIZE = 10;
	private ArrayList items;
	private static boolean VERBOSE = false;

    public SimpleStack() {
    	items = new ArrayList(INITIAL_STACK_SIZE);
    }
    
    public SimpleStack(int initialSize) {
    	items = new ArrayList(initialSize);
    }
    
    public void clear() {
    	items.clear();
    }
    
    public Object push(Object item) {
    	items.add(item);
    	if (VERBOSE)
    		trace("push on stack: " + item); //$NON-NLS-1$
    	return item;
    }
    
    public Object pop() {
    	int top = items.size()-1;
    	if (top < 0)
    		return null;
    	Object item = items.get(top);
    	items.remove(top);
    	if (VERBOSE)
    		trace("pop from stack: " + item); //$NON-NLS-1$
    	return item;
    }
    
    public Object top() {
    	int top = items.size()-1;
    	if (top < 0)
    		return null;
    	return items.get(top);
    }
    
    public Object bottom() {
    	if (items.size() == 0)
    		return null;
    	return items.get(0);
    }

    public boolean isEmpty() {
    	return (items.size() == 0);
    }
    
    public Object[] toArray() {
    	return items.toArray();
    }

    public Object[] toArray(Object a[]) {
    	return items.toArray(a);
    }

    private static void trace(String msg) {
	  System.out.println("(" + Thread.currentThread() + ") " + msg);  //$NON-NLS-1$ //$NON-NLS-2$
	}
}

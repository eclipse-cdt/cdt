/********************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * David Dykstal (IBM) - initial API and implementation
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 ********************************************************************************/

package org.eclipse.rse.core.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.logging.Logger;
import org.eclipse.rse.persistence.IRSEPersistenceManager;

/**
 * Provides a transaction boundary for RSE model operations. All operations that 
 * modify model objects should be done within an RSEModelOperation. These operations
 * may be nested. Changes made to the model will be persisted when the outermost 
 * operation of a particular thread is exited.
 * <p>
 * The usage idiom is to create an anonymous subclass of this class just prior to 
 * use, overriding the {@link #execute()} method, and then invoke the {@link #run()} method.
 * <pre>
 * RSEModelOperation m = new RSEModelOperation() {
 *     public void execute() {
 *         ... do work here ...
 *     }
 * };
 * m.run();
 * </pre>
 * <p>
 * under development - provisional
 * @since RSE 2.0
 */
public abstract class RSEModelOperation {

	private static Map threads = new HashMap();

	/**
	 * Checks the current thread to see if there is a model transaction in progress.
	 * Should be used inside model objects prior to a change to a persistent property.
	 */
	public static void check() {
		if (getDepth() == 0) {
			Logger logger = RSECorePlugin.getDefault().getLogger();
			logger.logInfo("not inside transaction"); //$NON-NLS-1$
		}
	}

	/**
	 * @return the depth of the nesting for transactions in the current thread
	 */
	private static int getDepth() {
		Thread myThread = Thread.currentThread();
		if (threads.get(myThread) == null) {
			threads.put(myThread, new Integer(0));
		}
		int depth = ((Integer) threads.get(myThread)).intValue();
		return depth;
	}

	/**
	 * Begins a transaction. 
	 */
	private static void beginTransaction() {
	}

	/**
	 * Ends a transaction. Schedules all changed profiles for save.
	 */
	private static void endTransaction() {
		IRSEPersistenceManager persistenceManager = RSECorePlugin.getThePersistenceManager();
		persistenceManager.commitProfiles(5000);
	}

	/**
	 * Enters a new nested level of operation.
	 */
	private static void enterLevel() {
		int depth = getDepth();
		try {
			if (depth == 0) {
				beginTransaction();
			}
		} finally {
			Thread myThread = Thread.currentThread();
			threads.put(myThread, new Integer(depth + 1));
		}
	}

	/**
	 * Leaves the current nesting level. If leaving the outermost nesting level then 
	 * ends the transaction.
	 */
	private static void leaveLevel() {
		int depth = getDepth();
		try {
			if (depth == 1) {
				endTransaction();
			}
		} finally {
			Thread myThread = Thread.currentThread();
			threads.put(myThread, new Integer(depth - 1));
		}
	}

	/**
	 * Create a new operation scoped to the current thread.
	 */
	public RSEModelOperation() {
	}

	/**
	 * Perform the work of this operation. This is where the work of modifying several model
	 * properties or objects can be done.
	 */
	public abstract void execute();

	/**
	 * Runs this operation. This will cause the {@link #execute()} method to be invoked inside
	 * a transaction boundary.
	 */
	public void run() {
		enterLevel();
		try {
			execute();
		} finally {
			leaveLevel();
		}
	}

}

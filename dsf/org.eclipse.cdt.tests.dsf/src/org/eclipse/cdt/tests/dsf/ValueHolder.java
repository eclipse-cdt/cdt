/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf;

/**
 * Utility class to hold a value retrieved in a runnable.
 * Usage in a test is as follows:
 * <pre>
 *     final ValueHolder<Integer> value = new ValueHolder<Integer>();
 *     fExecutor.execute(new Runnable() {
 *         public void run() {
 *             value.fValue = 1;
 *         }
 *     });
 *     Assert.assertTrue(value.fValue == 1);
 * </pre>
 */
public class ValueHolder<V> {
	public V fValue;
}

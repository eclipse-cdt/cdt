/*******************************************************************************
 * Copyright (c) 2018, 2019 Kichwa Coders and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Baha El-Kassaby - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.debugsources;

import java.util.function.Function;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

/**
 * Comparator used for the Debug Sources Table viewer
 *
 */
public class DebugSourcesViewComparator<T> extends ViewerComparator {

	private Function<T, Comparable<?>> func;

	private int propertyIndex;

	private enum Direction {
		ASCENDING, DESCENDING;

		/**
		 *
		 * @param direction the previous direction
		 * @return the new direction
		 */
		public static Direction toggle(Direction direction) {
			return direction == ASCENDING ? DESCENDING : ASCENDING;
		}
	}

	private Direction direction = Direction.DESCENDING;

	public DebugSourcesViewComparator(Function<T, Comparable<?>> func) {
		this.func = func;
	}

	public DebugSourcesViewComparator() {
		this.propertyIndex = 0;
		direction = Direction.DESCENDING;
	}

	public int getDirection() {
		return direction == Direction.DESCENDING ? SWT.DOWN : SWT.UP;
	}

	public void setColumn(Function<T, Comparable<?>> column, int idx) {
		this.func = column;
		if (idx == this.propertyIndex) {
			// Same column as last sort; toggle the direction
			direction = Direction.toggle(direction);
		} else {
			// New column
			this.propertyIndex = idx;
			direction = Direction.DESCENDING;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		Comparable s1 = func.apply((T) e1);
		Comparable s2 = func.apply((T) e2);
		int rc = s1.compareTo(s2);
		// If descending order, flip the direction
		if (direction.equals(Direction.DESCENDING)) {
			rc = -rc;
		}
		return rc;
	}

}

/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.debug.ui.disassembly.rulers.IColumnSupport;
import org.eclipse.cdt.debug.ui.disassembly.rulers.IContributedRulerColumn;
import org.eclipse.cdt.debug.ui.disassembly.rulers.RulerColumnDescriptor;
import org.eclipse.cdt.debug.ui.disassembly.rulers.RulerColumnRegistry;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IVerticalRulerColumn;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.util.SafeRunnable;

/**
 * Implements the ruler column support of for the given disassembly part.
 * <p>
 * This is currently only used to support vertical ruler columns.
 * </p>
 */
class DisassemblyColumnSupport implements IColumnSupport {
	private final DisassemblyPart fDisassembly;
	private final RulerColumnRegistry fRegistry;
	private final List<IContributedRulerColumn> fColumns;

	/**
	 * Creates a new column support for the given disassembly part. Only the disassembly part itself should normally
	 * create such an instance.
	 *
	 * @param editor the disassembly part
	 * @param registry the contribution registry to refer to
	 */
	public DisassemblyColumnSupport(DisassemblyPart disassembly, RulerColumnRegistry registry) {
		Assert.isLegal(disassembly != null);
		Assert.isLegal(registry != null);
		fDisassembly= disassembly;
		fRegistry= registry;
		fColumns= new ArrayList<IContributedRulerColumn>();
	}

	/*
	 * @see org.eclipse.ui.texteditor.IColumnSupport#setColumnVisible(java.lang.String, boolean)
	 */
	@Override
	public final void setColumnVisible(RulerColumnDescriptor descriptor, boolean visible) {
		Assert.isLegal(descriptor != null);

		final CompositeRuler ruler= getRuler();
		if (ruler == null)
			return;

		if (!isColumnSupported(descriptor))
			visible= false;

		if (isColumnVisible(descriptor)) {
			if (!visible)
				removeColumn(ruler, descriptor);
		} else {
			if (visible)
				addColumn(ruler, descriptor);
		}
	}

	private void addColumn(final CompositeRuler ruler, final RulerColumnDescriptor descriptor) {

		final int idx= computeIndex(ruler, descriptor);

		SafeRunnable runnable= new SafeRunnable() {
			@Override
			public void run() throws Exception {
				IContributedRulerColumn column= descriptor.createColumn(fDisassembly);
				fColumns.add(column);
				initializeColumn(column);
				ruler.addDecorator(idx, column);
			}
		};
		SafeRunner.run(runnable);
	}

	/**
	 * Hook to let subclasses initialize a newly created column.
	 * <p>
	 * Subclasses may extend this method.</p>
	 *
	 * @param column the created column
	 */
	protected void initializeColumn(IContributedRulerColumn column) {
	}

	private void removeColumn(final CompositeRuler ruler, final RulerColumnDescriptor descriptor) {
		removeColumn(ruler, getVisibleColumn(ruler, descriptor));
	}

	private void removeColumn(final CompositeRuler ruler, final IContributedRulerColumn rulerColumn) {
		if (rulerColumn != null) {
			SafeRunnable runnable= new SafeRunnable() {
				@Override
				public void run() throws Exception {
					if (ruler != null)
						ruler.removeDecorator(rulerColumn);
					rulerColumn.columnRemoved();
				}
			};
			SafeRunner.run(runnable);
		}
	}

	/**
	 * Returns the currently visible column matching <code>id</code>, <code>null</code> if
	 * none.
	 *
	 * @param ruler the composite ruler to scan
	 * @param descriptor the descriptor of the column of interest
	 * @return the matching column or <code>null</code>
	 */
	private IContributedRulerColumn getVisibleColumn(CompositeRuler ruler, RulerColumnDescriptor descriptor) {
		for (Iterator<?> it= ruler.getDecoratorIterator(); it.hasNext();) {
			IVerticalRulerColumn column= (IVerticalRulerColumn)it.next();
			if (column instanceof IContributedRulerColumn) {
				IContributedRulerColumn rulerColumn= (IContributedRulerColumn)column;
				RulerColumnDescriptor rcd= rulerColumn.getDescriptor();
				if (descriptor.equals(rcd))
					return rulerColumn;
			}
		}
		return null;
	}

	/**
	 * Computes the insertion index for a column contribution into the currently visible columns.
	 *
	 * @param ruler the composite ruler into which to insert the column
	 * @param descriptor the descriptor to compute the index for
	 * @return the insertion index for a new column
	 */
	private int computeIndex(CompositeRuler ruler, RulerColumnDescriptor descriptor) {
		int index= 0;
		List<?> all= fRegistry.getColumnDescriptors();
		int newPos= all.indexOf(descriptor);
		for (Iterator<?> it= ruler.getDecoratorIterator(); it.hasNext();) {
			IVerticalRulerColumn column= (IVerticalRulerColumn) it.next();
			if (column instanceof IContributedRulerColumn) {
				RulerColumnDescriptor rcd= ((IContributedRulerColumn)column).getDescriptor();
				if (rcd != null && all.indexOf(rcd) > newPos)
					break;
			}
			index++;
		}
		return index;
	}

	@Override
	public final boolean isColumnVisible(RulerColumnDescriptor descriptor) {
		Assert.isLegal(descriptor != null);
		CompositeRuler ruler= getRuler();
		return ruler != null && getVisibleColumn(ruler, descriptor) != null;
	}

	@Override
	public final boolean isColumnSupported(RulerColumnDescriptor descriptor) {
		Assert.isLegal(descriptor != null);
		if (getRuler() == null)
			return false;

		if (descriptor == null)
			return false;

		return descriptor.matchesPart(fDisassembly);
	}

	/**
	 * Returns the disassembly part's vertical ruler, if it is a {@link CompositeRuler}, <code>null</code>
	 * otherwise.
	 *
	 * @return the disassembly part's {@link CompositeRuler} or <code>null</code>
	 */
	private CompositeRuler getRuler() {
		Object ruler= fDisassembly.getAdapter(IVerticalRulerInfo.class);
		if (ruler instanceof CompositeRuler)
			return (CompositeRuler) ruler;
		return null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Subclasses may extend this method.</p>
	 *
	 */
	@Override
	public void dispose() {
		for (Iterator<IContributedRulerColumn> iter= new ArrayList<IContributedRulerColumn>(fColumns).iterator(); iter.hasNext();)
			removeColumn(getRuler(), iter.next());
		fColumns.clear();
	}
}

/*******************************************************************************
 * Copyright (c) 2001 Rational Software Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     Rational Software - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;

/**
 * A special composite to layout columns inside a table. The composite is needed since we have
 * to layout the columns "before" the actual table gets layouted. Hence we can't use a normal
 * layout manager.
 */
public class TableLayoutComposite extends Composite {

	private List columns= new ArrayList();

	/**
	 * Creates a new <code>TableLayoutComposite</code>.
	 */
	public TableLayoutComposite(Composite parent, int style) {
		super(parent, style);
		addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				Rectangle area= getClientArea();
				Table table= (Table)getChildren()[0];
				Point preferredSize= computeTableSize(table);
				int width= area.width - 2 * table.getBorderWidth();
				if (preferredSize.y > area.height) {
					// Subtract the scrollbar width from the total column width
					// if a vertical scrollbar will be required
					Point vBarSize = table.getVerticalBar().getSize();
					width -= vBarSize.x;
				}
				layoutTable(table, width, area, table.getSize().x < area.width);
			}
		});
	}
	
	/**
	 * Adds a new column of data to this table layout.
	 *
	 * @param data the column layout data
	 */
	public void addColumnData(ColumnLayoutData data) {
		columns.add(data);
	}
	
	//---- Helpers -------------------------------------------------------------------------------------
	
	Point computeTableSize(Table table) {
		Point result= table.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		
		int width= 0;
		int size= columns.size();
		for (int i= 0; i < size; ++i) {
			ColumnLayoutData layoutData= (ColumnLayoutData) columns.get(i);
			if (layoutData instanceof ColumnPixelData) {
				ColumnPixelData col= (ColumnPixelData) layoutData;
				width += col.width;
			} else if (layoutData instanceof ColumnWeightData) {
				ColumnWeightData col= (ColumnWeightData) layoutData;
				width += col.minimumWidth;
			} else {
				Assert.isTrue(false, "Unknown column layout data"); //$NON-NLS-1$
			}
		}
		if (width > result.x)
			result.x= width;
		return result;
	}
	
	void layoutTable(Table table, int width, Rectangle area, boolean increase) {
		// XXX: Layout is being called with an invalid value the first time
		// it is being called on Linux. This method resets the
		// Layout to null so we make sure we run it only when
		// the value is OK.
		if (width <= 1)
			return;

		TableColumn[] tableColumns= table.getColumns();
		int size= Math.min(columns.size(), tableColumns.length);
		int[] widths= new int[size];
		int fixedWidth= 0;
		int numberOfWeightColumns= 0;
		int totalWeight= 0;

		// First calc space occupied by fixed columns
		for (int i= 0; i < size; i++) {
			ColumnLayoutData col= (ColumnLayoutData) columns.get(i);
			if (col instanceof ColumnPixelData) {
				int pixels= ((ColumnPixelData) col).width;
				widths[i]= pixels;
				fixedWidth += pixels;
			} else if (col instanceof ColumnWeightData) {
				ColumnWeightData cw= (ColumnWeightData) col;
				numberOfWeightColumns++;
				// first time, use the weight specified by the column data, otherwise use the actual width as the weight
				// int weight = firstTime ? cw.weight : tableColumns[i].getWidth();
				int weight= cw.weight;
				totalWeight += weight;
			} else {
				Assert.isTrue(false, "Unknown column layout data"); //$NON-NLS-1$
			}
		}

		// Do we have columns that have a weight
		if (numberOfWeightColumns > 0) {
			// Now distribute the rest to the columns with weight.
			int rest= width - fixedWidth;
			int totalDistributed= 0;
			for (int i= 0; i < size; ++i) {
				ColumnLayoutData col= (ColumnLayoutData) columns.get(i);
				if (col instanceof ColumnWeightData) {
					ColumnWeightData cw= (ColumnWeightData) col;
					// calculate weight as above
					// int weight = firstTime ? cw.weight : tableColumns[i].getWidth();
					int weight= cw.weight;
					int pixels= totalWeight == 0 ? 0 : weight * rest / totalWeight;
					if (pixels < cw.minimumWidth)
						pixels= cw.minimumWidth;
					totalDistributed += pixels;
					widths[i]= pixels;
				}
			}

			// Distribute any remaining pixels to columns with weight.
			int diff= rest - totalDistributed;
			for (int i= 0; diff > 0; ++i) {
				if (i == size)
					i= 0;
				ColumnLayoutData col= (ColumnLayoutData) columns.get(i);
				if (col instanceof ColumnWeightData) {
					++widths[i];
					--diff;
				}
			}
		}
		
		if (increase) {
			table.setSize(area.width, area.height);
		}
		for (int i= 0; i < size; i++) {
			tableColumns[i].setWidth(widths[i]);
		}
		if (!increase) {
			table.setSize(area.width, area.height);
		}
	}
}

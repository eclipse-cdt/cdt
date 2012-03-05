/*******************************************************************************
 * Copyright (c) 2012 Tilera Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     William R. Swanson (Tilera Corporation)
 *******************************************************************************/

package org.eclipse.cdt.visualizer.examples.sourcegraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

import org.eclipse.cdt.visualizer.ui.canvas.BufferedCanvas;
import org.eclipse.cdt.visualizer.ui.util.Colors;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

// ---------------------------------------------------------------------------
// SourceGraphControl
// ---------------------------------------------------------------------------

/** Simple control that displays a graph based on a source text selection. */
public class SourceGraphControl extends BufferedCanvas
{	
	// --- constants ---
	
	/** Margin used in drawing graph and computing control height. */
	public static final int MARGIN = 10;	
	
	/** Line height used in drawing graph and computing control height. */
	public static final int LINE_HEIGHT = 20;	
	
	// --- members ---
	
	/** Text we're currently displaying. */
	protected String m_sourceText = ""; //$NON-NLS-1$
	
	/** Data structure used to hold character stats. */
	class CharStat
		implements Comparable<CharStat>
	{
		public String characters;
		public int count;
		public CharStat(String c) {
			characters = c;
			count = 0;
		}
		@Override
		public int compareTo(CharStat o) {
			int c1 = count;
			int c2 = o.count;
			int cmp = (c1 < c2) ? -1 : (c1 > c2) ? 1 : 0;
			// we want to sort in descending order, so negate result
			return -cmp;
		};	
	};
	
	/** List of characters we discovered and their occurrences. */
	ArrayList<CharStat> m_characters;
	
	
	// --- constructors/destructors ---
	
	/** Constructor. */
	public SourceGraphControl(Composite parent) {
		super(parent);
		m_characters = new ArrayList<CharStat>();
	}
	
	/** Dispose method. */
	@Override
	public void dispose() {
		super.dispose();
	}

	
	// --- accessors ---
	
	/** Sets source text to graph. */
	public void setSourceText(String text)
	{
		processText(text);
		SourceGraphControl.this.update();
	}

	
	// --- text processing methods ---
	
	/** Processes text into digested display form. */
	public void processText(String text)
	{
		if (text == null) text = ""; //$NON-NLS-1$
		m_sourceText = text;

		// TODO: reuse the array/hashtable and stat objects
		
		Hashtable<String, CharStat> characters = 
			new Hashtable<String, CharStat>();
		
		int len = m_sourceText.length();
		int fragment_length = 2;
		if (len >= fragment_length) {
			for (int i = 0; i<len-fragment_length+1; ++i) {
				String c = m_sourceText.substring(i,i+fragment_length);
				
				// Don't bother with fragments containing spaces
				// and non-printing chars.
				boolean skip = false;
				for (int j=0; j<c.length(); ++j)
				{
					if (c.charAt(j) <= 32 || c.charAt(j) > 127) {
						skip = true;
						break;
					}
				}
				if (skip) continue;
				
				CharStat cs = characters.get(c);
				if (cs == null) {
					cs = new CharStat(c);
					characters.put(c, cs);
				}
				++cs.count;
			}
		}
		
		m_characters.clear();
		m_characters.addAll(characters.values());
		Collections.sort(m_characters);
		
		characters.clear();
		
		Rectangle bounds = getBounds();
		int height = MARGIN * 2 + m_characters.size() * LINE_HEIGHT;
		bounds.height = height;
		setBounds(bounds);
		
	}
	
	// --- painting methods ---

	/** Invoked when canvas repaint event is raised.
	 *  Default implementation clears canvas to background color.
	 */
	@Override
	public void paintCanvas(GC gc) {
		gc.setBackground(Colors.BLACK);
		gc.setForeground(Colors.WHITE);

		clearCanvas(gc);

		int margin = MARGIN;
		int tw  = 90;
		int tw2 = 45;
		int lh = LINE_HEIGHT;
		
		int x = margin;
		int y = margin;
		
		Rectangle area = getClientArea();
		int w  = area.width - margin*2 - tw;

		int maxcount = 0;
		for (CharStat cs : m_characters)
		{
			// We're sorted in descending order, so first element
			// that we draw will always have the largest count.
			if (maxcount == 0) maxcount = cs.count;
			
			gc.drawText("[" + cs.characters + "]",  x, y); //$NON-NLS-1$ //$NON-NLS-2$
			gc.drawText("(" + cs.count + ")", x + tw2, y); //$NON-NLS-1$ //$NON-NLS-2$

			double proportion = cs.count * 1.0 / maxcount;
			int bw = (int) Math.round(proportion * w);
			Color oldb = gc.getBackground();
			if (proportion > .70)
				gc.setBackground(Colors.GREEN);
			else if (proportion > .40)
				gc.setBackground(Colors.YELLOW);
			else 
				gc.setBackground(Colors.RED);
			gc.fillRectangle(x + tw, y, bw, lh-5);
			gc.setBackground(oldb);

			y += lh;
		}
	}


	// --- update methods ---

	/**
	 * Refresh the control's content based on current data.
	 */
	public void refresh() {
		SourceGraphControl.this.update();
	}

	// --- event handlers ---

	/** Invoked when panel is resized. */
	@Override
	public void resized(Rectangle bounds) {
		refresh();
	}
}

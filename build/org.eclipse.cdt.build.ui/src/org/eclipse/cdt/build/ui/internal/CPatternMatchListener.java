/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.build.ui.internal;

import org.eclipse.cdt.build.core.CConsoleParser;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;

public class CPatternMatchListener implements IPatternMatchListener {

	protected final CConsoleService console;
	protected final CConsoleParser parser;

	protected TextConsole textConsole;

	public CPatternMatchListener(CConsoleService console, CConsoleParser parser) {
		this.console = console;
		this.parser = parser;
	}

	@Override
	public void connect(TextConsole console) {
		this.textConsole = console;
	}

	@Override
	public void disconnect() {
	}

	@Override
	public void matchFound(PatternMatchEvent event) {
		try {
			String text = textConsole.getDocument().get(event.getOffset(), event.getLength());
			IMarker marker = parser.generateMarker(console.getBuildDirectory(), text);
			if (marker != null) {
				textConsole.addHyperlink(new CHyperlink(marker),
						event.getOffset() + marker.getAttribute(CConsoleParser.LINK_OFFSET, 0),
						marker.getAttribute(CConsoleParser.LINK_LENGTH, event.getLength()));
			}
		} catch (BadLocationException e) {
			Activator.log(e);
		}
	}

	@Override
	public String getPattern() {
		return parser.getPattern();
	}

	@Override
	public int getCompilerFlags() {
		return parser.getCompilerFlags();
	}

	@Override
	public String getLineQualifier() {
		return parser.getLineQualifier();
	}

}

package org.eclipse.cdt.internal.ui.build;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;

import org.eclipse.cdt.core.build.CConsoleParser;
import org.eclipse.cdt.ui.CUIPlugin;

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
			CUIPlugin.log(e);
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

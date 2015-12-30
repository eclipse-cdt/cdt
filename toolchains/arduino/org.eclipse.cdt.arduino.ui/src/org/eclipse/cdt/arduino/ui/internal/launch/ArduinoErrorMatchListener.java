package org.eclipse.cdt.arduino.ui.internal.launch;

import org.eclipse.cdt.arduino.core.internal.console.ArduinoErrorParser;
import org.eclipse.cdt.arduino.ui.internal.Activator;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.console.PatternMatchEvent;

public class ArduinoErrorMatchListener extends ArduinoPatternMatchListener {

	public ArduinoErrorMatchListener(ArduinoConsole arduinoConsole, ArduinoErrorParser parser) {
		super(arduinoConsole, parser);
	}

	@Override
	public void matchFound(PatternMatchEvent event) {
		try {
			String text = textConsole.getDocument().get(event.getOffset(), event.getLength());
			IMarker marker = ((ArduinoErrorParser) parser).generateMarker(arduinoConsole.getBuildDirectory(), text);
			if (marker != null) {
				textConsole.addHyperlink(new ArduinoHyperlink(marker),
						event.getOffset() + marker.getAttribute(ArduinoErrorParser.LINK_OFFSET, 0),
						marker.getAttribute(ArduinoErrorParser.LINK_LENGTH, event.getLength()));
			}
		} catch (BadLocationException | CoreException e) {
			Activator.log(e);
		}
	}

}

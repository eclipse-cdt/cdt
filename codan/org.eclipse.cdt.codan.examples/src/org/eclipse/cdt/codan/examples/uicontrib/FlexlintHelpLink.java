package org.eclipse.cdt.codan.examples.uicontrib;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.codan.ui.AbstractCodanProblemDetailsProvider;

public class FlexlintHelpLink extends AbstractCodanProblemDetailsProvider {
	@Override
	public boolean isApplicable(String id) {
		String helpId = parseHelpId(getProblemMessage());
		return helpId != null;
	}
	private Pattern messagePattern = Pattern.compile("(Warning|Error|Note|Info) #([0-9]+)"); //$NON-NLS-1$

	private String parseHelpId(String problemMessage) {
		// Warning #613: 
		// Note #613: 
		// Error #613: 
		String helpId = null;
		Matcher matcher = messagePattern.matcher(problemMessage);
		if (matcher.find()) {
			helpId = matcher.group(2);
		}
		return helpId;
	}

	@Override
	public URL getHelpURL() {
		String helpId = parseHelpId(getProblemMessage());
		try {
			return new URL("http://www.gimpel-online.com/MsgRef.html#" + helpId); //$NON-NLS-1$
		} catch (MalformedURLException e) {
			return null;
		}
	}
}

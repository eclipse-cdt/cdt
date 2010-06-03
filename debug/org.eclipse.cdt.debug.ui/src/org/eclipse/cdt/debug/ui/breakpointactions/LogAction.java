/*******************************************************************************
 * Copyright (c) 2007, 2009 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.breakpointactions;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import com.ibm.icu.text.MessageFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.breakpointactions.AbstractBreakpointAction;
import org.eclipse.cdt.debug.core.breakpointactions.ILogActionEnabler;
import org.eclipse.cdt.debug.internal.core.ICDebugInternalConstants;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

public class LogAction extends AbstractBreakpointAction {

	private String message = ""; //$NON-NLS-1$
	private boolean evaluateExpression;
	private MessageConsole console;

	public boolean isEvaluateExpression() {
		return evaluateExpression;
	}

	public void setEvaluateExpression(boolean evaluateExpression) {
		this.evaluateExpression = evaluateExpression;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.breakpointactions.IBreakpointAction#execute(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.runtime.IAdaptable, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus execute(IBreakpoint breakpoint, IAdaptable context, IProgressMonitor monitor) {
		IStatus result = Status.OK_STATUS;
		try {
			openConsole(Messages.getString("LogAction.ConsoleTitle")); //$NON-NLS-1$
			String logMessage = getMessage();

			if (isEvaluateExpression()) {
				ILogActionEnabler enabler = (ILogActionEnabler) context.getAdapter(ILogActionEnabler.class);
				if (enabler != null)
					logMessage = enabler.evaluateExpression(logMessage);
			}

			MessageConsoleStream stream = console.newMessageStream();
			stream.println(logMessage);
			stream.close();
		} catch (Exception e) {
			String errorMsg = MessageFormat.format(Messages.getString("LogAction.error.0"), new Object[] {getSummary()}); //$NON-NLS-1$
			result = new Status( IStatus.ERROR, CDIDebugModel.getPluginIdentifier(), ICDebugInternalConstants.STATUS_CODE_ERROR, errorMsg, e );
		}
		return result;
	}

	private void openConsole(String consoleName) {
		// add it if necessary
		boolean found = false;

		IConsole[] consoles = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
		for (int i = 0; i < consoles.length; i++) {
			if (consoleName.equals(consoles[i].getName())) {
				console = (MessageConsole) consoles[i];
				found = true;
				break;
			}
		}

		if (!found) {
			console = new MessageConsole(consoleName, null);
			ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { console });
		}

		ConsolePlugin.getDefault().getConsoleManager().showConsoleView(console);
	}

	public String getDefaultName() {
		return Messages.getString("LogAction.UntitledName"); //$NON-NLS-1$
	}

	public String getIdentifier() {
		return "org.eclipse.cdt.debug.ui.breakpointactions.LogAction"; //$NON-NLS-1$
	}

	public String getMemento() {
		String logData = new String(""); //$NON-NLS-1$

		DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = dfactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			Element rootElement = doc.createElement("logData"); //$NON-NLS-1$
			rootElement.setAttribute("message", message); //$NON-NLS-1$
			rootElement.setAttribute("evalExpr", Boolean.toString(evaluateExpression)); //$NON-NLS-1$

			doc.appendChild(rootElement);

			ByteArrayOutputStream s = new ByteArrayOutputStream();

			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$

			DOMSource source = new DOMSource(doc);
			StreamResult outputTarget = new StreamResult(s);
			transformer.transform(source, outputTarget);

			logData = s.toString("UTF8"); //$NON-NLS-1$

		} catch (Exception e) {
			e.printStackTrace();
		}
		return logData;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getSummary() {
		String summary = getMessage();
		if (summary.length() > 32)
			summary = getMessage().substring(0, 32);
		return summary;
	}

	public String getTypeName() {
		return Messages.getString("LogAction.TypeName"); //$NON-NLS-1$
	}

	public void initializeFromMemento(String data) {
		Element root = null;
		DocumentBuilder parser;
		try {
			parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			parser.setErrorHandler(new DefaultHandler());
			root = parser.parse(new InputSource(new StringReader(data))).getDocumentElement();
			String value = root.getAttribute("message"); //$NON-NLS-1$
			if (value == null)
				throw new Exception();
			message = value;
			value = root.getAttribute("evalExpr"); //$NON-NLS-1$
			if (value == null)
				throw new Exception();
			evaluateExpression = Boolean.valueOf(value).booleanValue();
			value = root.getAttribute("resume"); //$NON-NLS-1$
			if (value == null)
				throw new Exception();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

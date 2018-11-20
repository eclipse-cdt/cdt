/*******************************************************************************
 * Copyright (c) 2007, 2015 Nokia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.breakpointactions;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.breakpointactions.AbstractBreakpointAction;
import org.eclipse.cdt.debug.core.breakpointactions.IReverseDebugEnabler;
import org.eclipse.cdt.debug.internal.core.ICDebugInternalConstants;
import org.eclipse.cdt.debug.internal.ui.IInternalCDebugUIConstants;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.IBreakpoint;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Implements the reverse debug breakpoint action
 *
 *@since 7.3
 */
public class ReverseDebugAction extends AbstractBreakpointAction {
	/**
	 * The available reverse debug action modes: enable, disable and toggle.
	 */
	public static enum REVERSE_DEBUG_ACTIONS_ENUM {

		ENABLE, DISABLE, TOGGLE;

		/**
		 * @param index
		 * @return the enum value for the given index
		 */
		public static REVERSE_DEBUG_ACTIONS_ENUM getValue(int index) {
			return REVERSE_DEBUG_ACTIONS_ENUM.values()[index];
		}
	}

	private REVERSE_DEBUG_ACTIONS_ENUM fOperation;

	/**
	 * @return the currently configured reverse debug mode, for this BP action
	 */
	public REVERSE_DEBUG_ACTIONS_ENUM getOperation() {
		return fOperation;
	}

	/**
	 * Sets the currently configured reverse debug mode, for this BP action
	 * @param operation
	 */
	public void setOperation(REVERSE_DEBUG_ACTIONS_ENUM operation) {
		this.fOperation = operation;
	}

	@Override
	public IStatus execute(IBreakpoint breakpoint, IAdaptable context, IProgressMonitor monitor) {
		IStatus errorStatus = null;

		IReverseDebugEnabler enabler = context.getAdapter(IReverseDebugEnabler.class);
		if (enabler != null) {
			try {
				switch (fOperation) {
				case TOGGLE:
					enabler.toggle();
					break;
				case ENABLE:
					enabler.enable();
					break;
				case DISABLE:
					enabler.disable();
					break;
				}

			} catch (Exception e) {
				errorStatus = new Status(IStatus.ERROR, CDIDebugModel.getPluginIdentifier(),
						ICDebugInternalConstants.STATUS_CODE_ERROR, e.getMessage(), e);
			}
		} else
			errorStatus = new Status(IStatus.ERROR, CDebugUIPlugin.getUniqueIdentifier(),
					IInternalCDebugUIConstants.INTERNAL_ERROR, Messages.getString("ReverseDebugAction.error.0"), null); //$NON-NLS-1$

		if (errorStatus != null) {
			MultiStatus ms = new MultiStatus(CDIDebugModel.getPluginIdentifier(),
					ICDebugInternalConstants.STATUS_CODE_ERROR, Messages.getString("ReverseDebugAction.error.1"), null); //$NON-NLS-1$
			ms.add(errorStatus);
			errorStatus = ms;
		} else {
			errorStatus = monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
		}

		return errorStatus;
	}

	@Override
	public String getMemento() {
		String reverseDebugData = ""; //$NON-NLS-1$

		DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = dfactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			Element rootElement = doc.createElement("reverseDebugData"); //$NON-NLS-1$
			rootElement.setAttribute("operation", fOperation.toString()); //$NON-NLS-1$

			doc.appendChild(rootElement);

			ByteArrayOutputStream s = new ByteArrayOutputStream();

			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$

			DOMSource source = new DOMSource(doc);
			StreamResult outputTarget = new StreamResult(s);
			transformer.transform(source, outputTarget);

			reverseDebugData = s.toString("UTF8"); //$NON-NLS-1$

		} catch (Exception e) {
			e.printStackTrace();
		}
		return reverseDebugData;

	}

	@Override
	public void initializeFromMemento(String data) {
		Element root = null;
		DocumentBuilder parser;
		try {
			parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			parser.setErrorHandler(new DefaultHandler());
			root = parser.parse(new InputSource(new StringReader(data))).getDocumentElement();
			String value = root.getAttribute("operation"); //$NON-NLS-1$
			if (value == null)
				throw new Exception();
			fOperation = REVERSE_DEBUG_ACTIONS_ENUM.valueOf(value);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public String getDefaultName() {
		return Messages.getString("ReverseDebugAction.UntitledName"); //$NON-NLS-1$
	}

	@Override
	public String getSummary() {
		// get translated operation
		String operation = Messages.getString("ReverseDebugAction." + fOperation.toString().toLowerCase()); //$NON-NLS-1$

		return operation + " " + Messages.getString("ReverseDebugAction.Summary"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public String getTypeName() {
		return Messages.getString("ReverseDebugAction.TypeName"); //$NON-NLS-1$
	}

	@Override
	public String getIdentifier() {
		return "org.eclipse.cdt.debug.ui.breakpointactions.ReverseDebugAction"; //$NON-NLS-1$
	}

}

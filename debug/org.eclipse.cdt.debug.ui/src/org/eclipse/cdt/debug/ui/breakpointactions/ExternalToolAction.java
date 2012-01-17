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
import org.eclipse.cdt.debug.internal.core.ICDebugInternalConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.DebugUITools;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

public class ExternalToolAction extends AbstractBreakpointAction {

	private String externalToolName = ""; //$NON-NLS-1$

	@Override
	public IStatus execute(IBreakpoint breakpoint, IAdaptable context, IProgressMonitor monitor) {
		IStatus errorStatus = null;
		ILaunchManager lcm = DebugPlugin.getDefault().getLaunchManager();
		try {
			boolean launched = false;
			ILaunchConfiguration[] launchConfigurations = lcm.getLaunchConfigurations();
			for (int i = 0; i < launchConfigurations.length; i++) {
				if (launchConfigurations[i].getName().equals(externalToolName)) {
					DebugUITools.launch(launchConfigurations[i], ILaunchManager.RUN_MODE);
					launched = true;
					break;
				}
			}
			if (!launched) {
				String errorMsg = MessageFormat.format(Messages.getString("ExternalToolAction.error.0"), new Object[] { externalToolName }); //$NON-NLS-1$
				errorStatus = new Status( IStatus.ERROR, CDIDebugModel.getPluginIdentifier(), ICDebugInternalConstants.STATUS_CODE_ERROR, errorMsg, null); 
			}
			
		} catch (CoreException e) {
			errorStatus = e.getStatus();
		} catch (Exception e) {
			errorStatus = new Status( IStatus.ERROR, CDIDebugModel.getPluginIdentifier(), ICDebugInternalConstants.STATUS_CODE_ERROR, e.getMessage(), e );
		}
		
		if (errorStatus != null) {
			String errorMsg = MessageFormat.format(Messages.getString("ExternalToolAction.error.1"), new Object[] { externalToolName }); //$NON-NLS-1$
			MultiStatus ms = new MultiStatus( CDIDebugModel.getPluginIdentifier(), ICDebugInternalConstants.STATUS_CODE_ERROR, errorMsg, null ); 
			ms.add(errorStatus);
			return ms;
		}

		return Status.OK_STATUS;
	}

	@Override
	public String getDefaultName() {
		return "Untitled External Tool Action"; //$NON-NLS-1$
	}

	public String getExternalToolName() {
		return externalToolName;
	}

	public void setExternalToolName(String launchConfigName) {
		this.externalToolName = launchConfigName;
	}

	@Override
	public String getIdentifier() {
		return "org.eclipse.cdt.debug.ui.breakpointactions.ExternalToolAction"; //$NON-NLS-1$
	}

	@Override
	public String getMemento() {
		String executeData = ""; //$NON-NLS-1$
		if (externalToolName != null) {
			DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = null;
			try {
				docBuilder = dfactory.newDocumentBuilder();
				Document doc = docBuilder.newDocument();

				Element rootElement = doc.createElement("launchConfigName"); //$NON-NLS-1$
				rootElement.setAttribute("configName", externalToolName); //$NON-NLS-1$

				doc.appendChild(rootElement);

				ByteArrayOutputStream s = new ByteArrayOutputStream();

				TransformerFactory factory = TransformerFactory.newInstance();
				Transformer transformer = factory.newTransformer();
				transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
				transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$

				DOMSource source = new DOMSource(doc);
				StreamResult outputTarget = new StreamResult(s);
				transformer.transform(source, outputTarget);

				executeData = s.toString("UTF8"); //$NON-NLS-1$

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return executeData;
	}

	@Override
	public String getSummary() {
		return MessageFormat.format(Messages.getString("ExternalToolAction.Summary"), new Object[] { externalToolName }); //$NON-NLS-1$
	}

	@Override
	public String getTypeName() {
		return Messages.getString("ExternalToolAction.TypeName"); //$NON-NLS-1$
	}

	@Override
	public void initializeFromMemento(String data) {
		Element root = null;
		DocumentBuilder parser;
		try {
			parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			parser.setErrorHandler(new DefaultHandler());
			root = parser.parse(new InputSource(new StringReader(data))).getDocumentElement();
			String value = root.getAttribute("configName"); //$NON-NLS-1$
			if (value == null)
				throw new Exception();
			externalToolName = value;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

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
import org.eclipse.cdt.debug.core.breakpointactions.IResumeActionEnabler;
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

public class ResumeAction extends AbstractBreakpointAction {

	final static int INCRIMENT_MSEC = 100;
	
	int pauseTime = 0;
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.breakpointactions.IBreakpointAction#execute(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.runtime.IAdaptable, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IStatus execute(IBreakpoint breakpoint, IAdaptable context, IProgressMonitor monitor) {
		IStatus errorStatus = null;
		long endTime = System.currentTimeMillis() + getPauseTime()*1000;
		IResumeActionEnabler enabler = (IResumeActionEnabler) context.getAdapter(IResumeActionEnabler.class);

		if (enabler != null) {
			try {
				monitor.beginTask(getName(), getPauseTime()*1000/INCRIMENT_MSEC);
				
				long currentTime = System.currentTimeMillis();
            	while (!monitor.isCanceled() && currentTime < endTime ){
            		monitor.setTaskName(MessageFormat.format(Messages.getString("ResumeAction.SummaryResumeTime"), new Object[] { Long.valueOf((endTime - currentTime)/1000) })); //$NON-NLS-1$)
            		monitor.worked(1);
            		Thread.sleep(INCRIMENT_MSEC);
            		currentTime = System.currentTimeMillis();
                }
            	
				if (!monitor.isCanceled()) {
					monitor.setTaskName( Messages.getString("ResumeAction.SummaryImmediately")); //$NON-NLS-1$)
					enabler.resume();
				}
				monitor.worked(1);
			} catch (Exception e) {
				errorStatus = new Status( IStatus.ERROR, CDIDebugModel.getPluginIdentifier(), ICDebugInternalConstants.STATUS_CODE_ERROR, e.getMessage(), e );
			}
		} else
			errorStatus = new Status( IStatus.ERROR, CDebugUIPlugin.getUniqueIdentifier(),  IInternalCDebugUIConstants.INTERNAL_ERROR, Messages.getString("ResumeAction.error.0"), null ); //$NON-NLS-1$
		
		if (errorStatus != null) {
			MultiStatus ms = new MultiStatus( CDIDebugModel.getPluginIdentifier(), ICDebugInternalConstants.STATUS_CODE_ERROR, Messages.getString("ResumeAction.error.1"), null ); //$NON-NLS-1$
			ms.add( errorStatus);
			errorStatus = ms;			
		} else {
			errorStatus = monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
		}	
		return errorStatus;
	}

	@Override
	public String getDefaultName() {
		return Messages.getString("ResumeAction.UntitledName"); //$NON-NLS-1$
	}

	public int getPauseTime() {
		return pauseTime;
	}

	public void setPauseTime(int pauseTime) {
		this.pauseTime = pauseTime;
	}

	@Override
	public String getIdentifier() {
		return "org.eclipse.cdt.debug.ui.breakpointactions.ResumeAction"; //$NON-NLS-1$
	}

	@Override
	public String getMemento() {
		String resumeData = ""; //$NON-NLS-1$

		DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = dfactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			Element rootElement = doc.createElement("resumeData"); //$NON-NLS-1$
			rootElement.setAttribute("pauseTime", Integer.toString(pauseTime)); //$NON-NLS-1$

			doc.appendChild(rootElement);

			ByteArrayOutputStream s = new ByteArrayOutputStream();

			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$

			DOMSource source = new DOMSource(doc);
			StreamResult outputTarget = new StreamResult(s);
			transformer.transform(source, outputTarget);

			resumeData = s.toString("UTF8"); //$NON-NLS-1$

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resumeData;
	}

	@Override
	public String getSummary() {
		if (pauseTime == 0)
			return Messages.getString("ResumeAction.SummaryImmediately"); //$NON-NLS-1$
		return MessageFormat.format(Messages.getString("ResumeAction.SummaryResumeTime"), new Object[] { Integer.valueOf(pauseTime) }); //$NON-NLS-1$
	}

	@Override
	public String getTypeName() {
		return Messages.getString("ResumeAction.TypeName"); //$NON-NLS-1$
	}

	@Override
	public void initializeFromMemento(String data) {
		Element root = null;
		DocumentBuilder parser;
		try {
			parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			parser.setErrorHandler(new DefaultHandler());
			root = parser.parse(new InputSource(new StringReader(data))).getDocumentElement();
			String value = root.getAttribute("pauseTime"); //$NON-NLS-1$
			if (value == null)
				throw new Exception();
			pauseTime = Integer.parseInt(value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

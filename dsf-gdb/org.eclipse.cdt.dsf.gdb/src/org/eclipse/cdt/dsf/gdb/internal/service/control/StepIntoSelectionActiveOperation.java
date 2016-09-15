/*******************************************************************************
 * Copyright (c) 2013, 2016 Ericsson AB and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Alvaro Sanchez-Leon (Ericsson AB) - Support for Step into selection (bug 244865)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.service.control;

import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIFrame;

/**
 * @since 4.2
 */
public class StepIntoSelectionActiveOperation {
	private final IFunctionDeclaration fTargetFunction;
	private final IMIExecutionDMContext fThreadContext;
	private String fBaseFileLocation = null;
	private int fBaseLine = 0;
	private int fOriginalStackDepth=0;
	private String fFunctionSignature = null;
	private MIFrame fRunToLineFrame = null;

	public StepIntoSelectionActiveOperation(IMIExecutionDMContext threadContext, int line, IFunctionDeclaration targetFunction,
			int stackDepth, MIFrame runToLineFrame) {
		fThreadContext = threadContext;
		fBaseLine = line;
		fTargetFunction = targetFunction;
		fOriginalStackDepth = stackDepth;

		fRunToLineFrame = runToLineFrame;
		init();
	}

	private void init() {
		if (fRunToLineFrame == null) {
			return;
		}

		fBaseFileLocation = fRunToLineFrame.getFile() + ":" + fBaseLine; //$NON-NLS-1$
	}

	public IFunctionDeclaration getTargetFunctionDeclaration() {
		return fTargetFunction;
	}

	public IMIExecutionDMContext getThreadContext() {
		return fThreadContext;
	}

	public String getFileLocation() {
		return fBaseFileLocation;
	}

	public int getLine() {
		return fBaseLine;
	}

	public int getOriginalStackDepth() {
		return fOriginalStackDepth;
	}

	public void setOriginalStackDepth(Integer originalStackDepth) {
		fOriginalStackDepth = originalStackDepth;
	}

	public MIFrame getRunToLineFrame() {
		return fRunToLineFrame;
	}

	public void setRunToLineFrame(MIFrame runToLineFrame) {
		if (runToLineFrame != null) {
			fRunToLineFrame = runToLineFrame;
			init();
		}
	}

	public String getTargetFunctionSignature() {
		if (fFunctionSignature != null) {
			return fFunctionSignature;
		} else {
			if (fTargetFunction != null) {
				StringBuilder sb = null;
				sb = new StringBuilder();
				if (fTargetFunction.getParent() != null) {
					sb.append(fTargetFunction.getParent().getElementName()).append(StepIntoSelectionUtils.cppSep);
				}

				sb.append(fTargetFunction.getElementName());
				fFunctionSignature = sb.toString();
			}
		}

		return fFunctionSignature;
	}
}

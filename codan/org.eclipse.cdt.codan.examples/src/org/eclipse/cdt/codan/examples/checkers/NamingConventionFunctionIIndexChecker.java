/*******************************************************************************
 * Copyright (c) 2009, 2010 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.examples.checkers;

import java.util.regex.Pattern;

import org.eclipse.cdt.codan.core.cxx.model.AbstractCIndexChecker;
import org.eclipse.cdt.codan.core.model.ICheckerWithPreferences;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.codan.core.param.BasicProblemPreference;
import org.eclipse.cdt.codan.core.param.IProblemPreference;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementVisitor;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Alena
 * 
 */
public class NamingConventionFunctionIIndexChecker extends
		AbstractCIndexChecker implements ICheckerWithPreferences {
	private static final String DEFAULT_PATTERN = "^[a-z]"; // name starts with english lowercase letter //$NON-NLS-1$
	public static final String PARAM_KEY = "pattern"; //$NON-NLS-1$
	private static final String ER_ID = "org.eclipse.cdt.codan.examples.checkers.NamingConventionFunctionProblem"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.codan.core.model.ICIndexChecker#processUnit(org.eclipse
	 * .cdt.core.model.ITranslationUnit)
	 */
	public void processUnit(ITranslationUnit unit) {
		final IProblem pt = getProblemById(ER_ID, getFile());
		try {
			unit.accept(new ICElementVisitor() {
				public boolean visit(ICElement element) throws CoreException {
					if (element.getElementType() == ICElement.C_FUNCTION) {
						String parameter = (String) pt.getPreference()
								.getValue();
						Pattern pattern = Pattern.compile(parameter);
						String name = element.getElementName();
						if (!pattern.matcher(name).find()) {
							reportProblem(ER_ID, getFile(), 1, // TODO: line
																// number
									name, parameter);
						}
						return false;
					}
					return true;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.codan.core.model.ICheckerWithPreferences#initParameters
	 * (org.eclipse.cdt.codan.core.model.IProblemWorkingCopy)
	 */
	public void initPreferences(IProblemWorkingCopy problem) {
		IProblemPreference info = new BasicProblemPreference(PARAM_KEY,
				"Name Pattern");
		info.setValue(DEFAULT_PATTERN);
		problem.setPreference(info);
	}

	@Override
	public boolean runInEditor() {
		return false;
	}
}

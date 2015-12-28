/*******************************************************************************
 * Copyright (c) 2015 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Lukas Wegmann (IFS) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

/**
 * Partial specialization of a variable template.
 *
 * @since 5.12
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPVariableTemplatePartialSpecialization
		extends ICPPVariableTemplate, ICPPPartialSpecialization {
}

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.CDIException;

/**
 */
public interface ICDIReferenceValue extends ICDIDerivedValue {

	long referenceValue() throws CDIException;
}

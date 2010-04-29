/*******************************************************************************
 * $QNXLicenseC:
 * Copyright 2008, QNX Software Systems. All Rights Reserved.
 * 
 * You must obtain a written license from and pay applicable license fees to QNX 
 * Software Systems before you may reproduce, modify or distribute this software, 
 * or any work that includes all or part of this software.   Free development 
 * licenses are available for evaluation and non-commercial purposes.  For more 
 * information visit http://licensing.qnx.com or email licensing@qnx.com.
 *  
 * This file may contain contributions from others.  Please review this entire 
 * file for other proprietary rights or license notices, as well as the QNX 
 * Development Suite License Guide at http://licensing.qnx.com/license-guide/ 
 * for other information.
 * $
 *******************************************************************************/


/*
 * Created by: Elena Laskavaia
 * Created on: 2010-04-29
 * Last modified by: $Author$
 */
package org.eclipse.cdt.codan.internal.ui.views;

import org.eclipse.cdt.codan.ui.AbstractCodanProblemDetailsProvider;

/**
 * This provides details for errors that do not have own details provider
 */
public class GenericCodanProblemDetailsProvider extends AbstractCodanProblemDetailsProvider {
	@Override
	public boolean isApplicable(String id) {
		return true;
	}
}

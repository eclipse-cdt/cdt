/*******************************************************************************
 * Copyright (c) 2008 Radoslav Gerganov
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Radoslav Gerganov - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.internal.services.wince.files;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
  private static final String BUNDLE_NAME = "org.eclipse.rse.internal.services.wince.files.messages"; //$NON-NLS-1$
  public static String WinCEFileService_0;
  public static String WinCEFileService_1;
  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }

  private Messages() {
  }
}

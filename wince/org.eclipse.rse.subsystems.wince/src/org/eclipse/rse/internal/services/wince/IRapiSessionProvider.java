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
package org.eclipse.rse.internal.services.wince;

import org.eclipse.tm.rapi.IRapiSession;

public interface IRapiSessionProvider {
  
  /**
   * Returns an active RAPI2 session from a ConnectorService.
   */
  public IRapiSession getSession();

}

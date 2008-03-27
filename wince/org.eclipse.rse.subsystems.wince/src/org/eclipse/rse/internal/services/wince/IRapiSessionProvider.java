/*******************************************************************************
 * Copyright (c) 2008 Radoslav Gerganov
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

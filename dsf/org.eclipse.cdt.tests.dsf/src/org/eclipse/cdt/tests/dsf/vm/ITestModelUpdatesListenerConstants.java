/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.vm;

/**
 * Convenience interface with constants used by the test model update listener.
 * 
 * @since 3.6
 */
public interface ITestModelUpdatesListenerConstants {
    
    public static final int LABEL_UPDATES_COMPLETE = 0X0001;
    public static final int CONTENT_UPDATES_COMPLETE = 0X0002;
    public static final int LABEL_UPDATES = 0X0004;
    public static final int HAS_CHILDREN_UPDATES = 0X0008;
    public static final int CHILDREN_COUNT_UPDATES = 0X0010;
    public static final int CHILDREN_UPDATES = 0X0020;
    public static final int MODEL_CHANGED_COMPLETE = 0X0040; 
    public static final int MODEL_PROXIES_INSTALLED = 0X0080;
    public static final int STATE_SAVE_COMPLETE = 0X0100;
    public static final int STATE_RESTORE_COMPLETE = 0X0200;
    public static final int STATE_UPDATES = 0X0400;
    
    public static final int VIEWER_UPDATES_RUNNING = 0X0800; 
    public static final int LABEL_UPDATES_RUNNING = 0X1000;
    
    public static final int LABEL_COMPLETE = LABEL_UPDATES_COMPLETE | LABEL_UPDATES;
    public static final int CONTENT_COMPLETE = 
        CONTENT_UPDATES_COMPLETE | HAS_CHILDREN_UPDATES | CHILDREN_COUNT_UPDATES | CHILDREN_UPDATES;
    
    public static final int ALL_UPDATES_COMPLETE = LABEL_COMPLETE | CONTENT_COMPLETE | LABEL_UPDATES_RUNNING | VIEWER_UPDATES_RUNNING;
}

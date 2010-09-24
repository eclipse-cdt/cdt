/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf;

/**
 * Convenience interface with constants used by the test model update listener.
 * @since 2.2
 */
public interface IViewerUpdatesListenerConstants {
    
    public static final int LABEL_SEQUENCE_COMPLETE =       0X00000001;
    public static final int CONTENT_SEQUENCE_COMPLETE =    0X00000002;
    public static final int CONTENT_SEQUENCE_STARTED =     0X00020000;
    public static final int LABEL_UPDATES =                0X00000004;
    public static final int LABEL_SEQUENCE_STARTED =       0X00040000;
    public static final int HAS_CHILDREN_UPDATES =         0X00000008;
    public static final int HAS_CHILDREN_UPDATES_STARTED = 0X00080000;
    public static final int CHILD_COUNT_UPDATES =          0X00000010;
    public static final int CHILD_COUNT_UPDATES_STARTED =  0X00100000;
    public static final int CHILDREN_UPDATES =             0X00000020;
    public static final int CHILDREN_UPDATES_STARTED =     0X00200000;
    public static final int MODEL_CHANGED_COMPLETE =       0X00000040; 
    public static final int MODEL_PROXIES_INSTALLED =      0X00000080;
    public static final int STATE_SAVE_COMPLETE =          0X00000100;
    public static final int STATE_SAVE_STARTED =           0X01000000;
    public static final int STATE_RESTORE_COMPLETE =       0X00000200;
    public static final int STATE_RESTORE_STARTED =        0X02000000;
    public static final int STATE_UPDATES =                0X00000400;
    public static final int STATE_UPDATES_STARTED =        0X04000000;
    public static final int PROPERTY_UPDATES =             0X00000800;
    public static final int PROPERTY_UPDATES_STARTED =     0X08000000;
    
    public static final int VIEWER_UPDATES_RUNNING =       0X00001000;
    public static final int LABEL_UPDATES_RUNNING =        0X00002000;

    public static final int VIEWER_UPDATES_STARTED = HAS_CHILDREN_UPDATES_STARTED | CHILD_COUNT_UPDATES_STARTED | CHILDREN_UPDATES_STARTED; 

    public static final int LABEL_COMPLETE = LABEL_SEQUENCE_COMPLETE | LABEL_UPDATES | LABEL_UPDATES_RUNNING;
    public static final int CONTENT_UPDATES = HAS_CHILDREN_UPDATES | CHILD_COUNT_UPDATES | CHILDREN_UPDATES;
    public static final int CONTENT_COMPLETE = CONTENT_UPDATES | CONTENT_SEQUENCE_COMPLETE | VIEWER_UPDATES_RUNNING;
    
    public static final int ALL_UPDATES_COMPLETE = LABEL_COMPLETE | CONTENT_COMPLETE | MODEL_PROXIES_INSTALLED | LABEL_UPDATES_RUNNING | VIEWER_UPDATES_RUNNING;
}

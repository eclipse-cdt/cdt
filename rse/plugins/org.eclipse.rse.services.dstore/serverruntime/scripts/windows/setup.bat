@echo off
@rem ***************************************************************************
@rem Copyright (c) 2005, 2006 IBM Corporation and others.
@rem All rights reserved. This program and the accompanying materials
@rem are made available under the terms of the Eclipse Public License v1.0
@rem which accompanies this distribution, and is available at
@rem http://www.eclipse.org/legal/epl-v10.html
@rem
@rem Contributors:
@rem IBM Corporation - initial API and implementation
@rem ***************************************************************************
@echo on
set ECLIPSEDIR=.
set CORE=%ECLIPSEDIR%\dstore_core.jar
set MINERS=%ECLIPSEDIR%\dstore_miners.jar
set EXTRA=%ECLIPSEDIR%\dstore_extra_server.jar
set CLIENTSERVER=%ECLIPSEDIR%\clientserver.jar
set A_PLUGIN_PATH=%ECLIPSEDIR%
set CLASSPATH=%CORE%;%MINERS%;%UNIVERSAL%;%UNIVERSALJAR%;%CLIENTSERVER%;%EXTRA%;%CLASSPATH%

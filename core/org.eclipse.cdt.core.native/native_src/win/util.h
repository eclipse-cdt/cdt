/*******************************************************************************
 * Copyright (c) 2020 Torbjörn Svensson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Torbjörn Svensson - initial API and implementation
 *******************************************************************************/

#ifndef UTIL_H
#define UTIL_H

#include <stdbool.h>
#include <windows.h>

typedef enum { CDT_TRACE_MONITOR, CDT_TRACE_MONITOR_DETAILS, CDT_TRACE_READ_REPORT } TraceKind_t;

bool isTraceEnabled(const TraceKind_t traceKind);
void cdtTrace(const wchar_t *fmt, ...);

#endif /* UTIL_H */

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

#define BUILD_PIPE_NAME(pipe, name, pid, counter)                                                                      \
    do {                                                                                                               \
        swprintf(pipe, sizeof(pipe) / sizeof(pipe[0]), L"\\\\.\\pipe\\%s%08i%010i", name, pid, counter);               \
    } while (0)

#define CLOSE_HANDLES(handles)                                                                                         \
    do {                                                                                                               \
        for (int i = 0; i < sizeof(handles) / sizeof(handles[0]); i++) {                                               \
            if (INVALID_HANDLE_VALUE != handles[i]) {                                                                  \
                CloseHandle(handles[i]);                                                                               \
                handles[i] = INVALID_HANDLE_VALUE;                                                                     \
            }                                                                                                          \
        }                                                                                                              \
    } while (0)

int copyTo(wchar_t *target, const wchar_t *source, int cpyLength, int availSpace);

#endif /* UTIL_H */

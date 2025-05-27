## Overview

This document captures the manual tests that should be completed for Core Build toolchains.

Added:

  20251010 John Moule
  Set CC and CXX environment variables for clang toolchain #1169
  https://github.com/eclipse-cdt/cdt/pull/1169
  https://github.com/eclipse-cdt/cdt/issues/1140

## Tests
The manual test cases below are useful to show the expected toolchain is used for a build.

The following JUnits are also added:
-  org.eclipse.cdt.build.gcc.core.tests.TestGCCToolChain
-  org.eclipse.cdt.build.gcc.core.tests.TestClangToolChain

## Test Case Summary
- A) Clang toolchain can be used to build when selected in Build Settings > Toolchain.
- B) GCC toolchain can be used to build when selected in Build Settings > Toolchain.
- C) Clang toolchain can be used to build when CC/CXX defined externally to gcc/g++
- D) GCC toolchain can be used to build when CC/CXX defined externally to clang/clang++

Tests should be performed on Linux & Windows.

### Prerequisites
- Make sure cmake, make, ninja, GCC and clang toolchains are installed on the host - see [1].
- Make sure the environment variables CC/CXX are not defined in the system environment, unless specified in the test.


## Test case A) Clang toolchain can be used to build when selected in Build Settings > Toolchain.
- Create a CMake Hello World project
- From the Launch Bar, open the Launch Configuration (gear icon)
- Select Build Settings tab and in the Toolchain dropdown, change to clang toolchain and click OK.
- From the Launch Bar, build the project.

Expected: The project builds successfully and in the Console build output, it can be seen that the C and CXX compiler are identified as "Clang".

Actual: The following shows an example of Console build output:


    Configuring in: C:\Users\a5107948\cdt-main20250110b\runtime-cdtruntime_clean\cmake_hw\build\cmake.debug.win32.x86_64.Local
    cmake -DCMAKE_BUILD_TYPE=Debug -DCMAKE_EXPORT_COMPILE_COMMANDS=ON -G Ninja C:\Users\a5107948\cdt-main20250110b\runtime-cdtruntime_clean\cmake_hw
    -- The C compiler identification is Clang 19.1.7
    -- The CXX compiler identification is Clang 19.1.7
    -- Detecting C compiler ABI info
    -- Detecting C compiler ABI info - done
    -- Check for working C compiler: C:/msys64/ucrt64/bin/clang.exe - skipped
    -- Detecting C compile features
    -- Detecting C compile features - done
    -- Detecting CXX compiler ABI info
    -- Detecting CXX compiler ABI info - done
    -- Check for working CXX compiler: C:/msys64/ucrt64/bin/clang++.exe - skipped
    -- Detecting CXX compile features
    -- Detecting CXX compile features - done
    -- Configuring done (3.1s)
    -- Generating done (0.1s)
    -- Build files have been written to: C:/Users/a5107948/cdt-main20250110b/runtime-cdtruntime_clean/cmake_hw/build/cmake.debug.win32.x86_64.Local
    Building in: C:\Users\a5107948\cdt-main20250110b\runtime-cdtruntime_clean\cmake_hw\build\cmake.debug.win32.x86_64.Local
    cmake --build . --target all
    [1/2] Building CXX object CMakeFiles/cmake_hw.dir/cmake_hw.cpp.obj
    [2/2] Linking CXX executable cmake_hw.exe
    Build complete (0 errors, 0 warnings): C:\Users\a5107948\cdt-main20250110b\runtime-cdtruntime_clean\cmake_hw\build\cmake.debug.win32.x86_64.Local


## Test case B) GCC toolchain can be used to build when selected in Build Settings > Toolchain.
- From the Launch Bar, open the Launch Configuration (gear icon)
- Select Build Settings tab and in the Toolchain dropdown, change to gcc toolchain and click OK.
- Delete the project's build folder.
- From the Launch Bar, build the project.

Expected: The project builds successfully and in the Console build output, it can be seen that the C and CXX compiler are identified as "gnu".

Actual: The following shows an example of Console build output:

    Configuring in: C:\Users\a5107948\cdt-main20250110b\runtime-cdtruntime_clean\cmake_hw\build\cmake.debug.win32.x86_64.Local
    cmake -DCMAKE_BUILD_TYPE=Debug -DCMAKE_EXPORT_COMPILE_COMMANDS=ON -G Ninja C:\Users\a5107948\cdt-main20250110b\runtime-cdtruntime_clean\cmake_hw
    -- The C compiler identification is GNU 14.2.0
    -- The CXX compiler identification is GNU 14.2.0
    -- Detecting C compiler ABI info
    -- Detecting C compiler ABI info - done
    -- Check for working C compiler: C:/msys64/ucrt64/bin/gcc.exe - skipped
    -- Detecting C compile features
    -- Detecting C compile features - done
    -- Detecting CXX compiler ABI info
    -- Detecting CXX compiler ABI info - done
    -- Check for working CXX compiler: C:/msys64/ucrt64/bin/g++.exe - skipped
    -- Detecting CXX compile features
    -- Detecting CXX compile features - done
    -- Configuring done (3.4s)
    -- Generating done (0.1s)
    -- Build files have been written to: C:/Users/a5107948/cdt-main20250110b/runtime-cdtruntime_clean/cmake_hw/build/cmake.debug.win32.x86_64.Local
    Building in: C:\Users\a5107948\cdt-main20250110b\runtime-cdtruntime_clean\cmake_hw\build\cmake.debug.win32.x86_64.Local
    cmake --build . --target all
    [1/2] Building CXX object CMakeFiles/cmake_hw.dir/cmake_hw.cpp.obj
    [2/2] Linking CXX executable cmake_hw.exe
    Build complete (0 errors, 0 warnings): C:\Users\a5107948\cdt-main20250110b\runtime-cdtruntime_clean\cmake_hw\build\cmake.debug.win32.x86_64.Local



## Test case C) Clang toolchain can be used to build when CC/CXX defined externally to gcc/g++
- Exit Eclipse and in a command shell, define the environment variables CC and CXX, for example on Windows:
- - set CC=gcc
- - set CXX=g++
- Launch Eclipse from this command shell, so it inherits the environment.
- From the Launch Bar, open the Launch Configuration (gear icon)
- Select Build Settings tab and in the Toolchain dropdown, change to clang toolchain and click OK.
- From the Launch Bar, build the project.

Expected: The project builds successfully and in the Console build output, it can be seen that the C and CXX compiler are identified as "Clang".

Actual: same output as Test case A).

## Test case D) GCC toolchain can be used to build when CC/CXX defined externally to clang/clang++
- Exit Eclipse and in a command shell, define the environment variables CC and CXX, for example on Windows:
- - set CC=clang
- - set CXX=clang++
- Launch Eclipse from this command shell, so it inherits the environment.
- From the Launch Bar, open the Launch Configuration (gear icon)
- Select Build Settings tab and in the Toolchain dropdown, change to gcc toolchain and click OK.
- From the Launch Bar, build the project.

Expected: The project builds successfully and in the Console build output, it can be seen that the C and CXX compiler are identified as "gnu".

Actual: same output as Test case B).

----
[1]: Before you begin
C/C++ Development User Guide > Before you begin
https://github.com/eclipse-cdt/cdt/blob/abe4d1b8e16b03ceafbeb25f063cb728c7487862/doc/org.eclipse.cdt.doc.user/concepts/cdt_c_before_you_begin.htm



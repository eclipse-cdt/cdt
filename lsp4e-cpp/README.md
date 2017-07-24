# LSP4E-CPP: C/C++ Language Server Support in Eclipse IDE

Support for C/C++ edition in Eclipse IDE using the Language Server Protocol. Relies on Clangd and the Language Server Protocol.

## Prerequisites

You need `Clangd` working on CLI (visible in `PATH` environment variable). Since `Clangd` is very young, you will most likely need to compile it from source, see the [Clang documentation](http://clang.llvm.org/get_started.html) for more instructions. Make sure you you do checkout the `Clang extra Tools`.

## Build from source

### Requirements
* `Maven 3.3` or greater
* `Java 8` (make sure it is seen by Maven with ```mvn -version```)

With the repository cloned, simply execute ```mvn clean package```. The p2 repository will be in ```$LSP4E_CPP_ROOT/org.eclipse.lsp4e.cpp.site/target/repository```.

## Installation in Eclipse IDE

Go to Help > Install new Software. Add the local repository ```$LSP4E_CPP_ROOT/org.eclipse.lsp4e.cpp.site/target/repository```.

## Usage ##

Once the plug-in is installed, right-click on a C/C++ source file and open with the `Generic Text Editor`.

## Concept

LSP4E-CPP uses the [lsp4e](https://projects.eclipse.org/projects/technology.lsp4e) project to integrate with [Clangd (part of Clang "extra" tools)](http://clang.llvm.org/extra/) with the goal to provide a rich C/C++ editor in the Eclipse IDE.

Keep in mind that `Clangd` is very young and just getting started so not many features are functional at this moment.

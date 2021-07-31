cmake_minimum_required(VERSION 3.10)

# Set the project name
project(NestedProject)

include(ExternalProject)
ExternalProject_Add(
    App1 
    SOURCE_DIR "${CMAKE_CURRENT_SOURCE_DIR}/App1"
    PREFIX App1
    INSTALL_COMMAND ""
)
ExternalProject_Add(
    App2 
    SOURCE_DIR "${CMAKE_CURRENT_SOURCE_DIR}/App2"
    PREFIX App2
    INSTALL_COMMAND ""
)

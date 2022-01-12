cmake_minimum_required(VERSION 3.10)

# Set the project name and version
project(App1 VERSION 1.0)

set(CMAKE_EXPORT_COMPILE_COMMANDS "true")

# Configuration header
configure_file(app1.h.in app1.h)

# Add project executable
add_executable(${PROJECT_NAME} app1.c)

# Include the configuration header
target_include_directories(${PROJECT_NAME} PUBLIC "${PROJECT_BINARY_DIR}")

# - Try to find TSServerSdk
# Once done, this will define
#
#  TSServerSdk_FOUND - system has TSServerSdk
#  TSServerSdk_INCLUDE_DIRS - the TSServerSdk include directories
#  TSServerSdk_LIBRARIES - link these to use TSServerSdk

message("adding TeamSpeak Server SDK")

# Include dir
set(TS_INCLUDE_DIR_HINT "${CMAKE_CURRENT_LIST_DIR}/../include/")

find_path(TSServerSdk_INCLUDE_DIR
    NAMES teamspeak/public_definitions.h
    PATHS "${TSServerSdk_INCLUDE_DIRS}" "${TS_INCLUDE_DIR_HINT}"
    REQUIRED
)

# Find Library
message("CMAKE_SYSTEM_NAME: ${CMAKE_SYSTEM_NAME}")
message("CMAKE_SYSTEM_PROCESSOR ${CMAKE_SYSTEM_PROCESSOR}")

if (CMAKE_SYSTEM_NAME STREQUAL "Darwin")
    set(TS_LIB_PATH "mac/x86_64")
elseif (CMAKE_SYSTEM_NAME STREQUAL "Linux")
    if ("${CMAKE_SYSTEM_PROCESSOR}" STREQUAL "arm")
        set(TS_LIB_PATH "linux/armv7")
    elseif ("${CMAKE_SYSTEM_PROCESSOR}" STREQUAL "aarch64")
        set(TS_LIB_PATH "linux/armv8")
    elseif("${CMAKE_SYSTEM_PROCESSOR}" STREQUAL "x86_64")
        set(TS_LIB_PATH "linux/amd64")
    elseif("${CMAKE_SYSTEM_PROCESSOR}" STREQUAL "x86")
        set(TS_LIB_PATH "linux/x86")
    endif()
elseif (CMAKE_SYSTEM_NAME STREQUAL "Windows")
    if("${CMAKE_GENERATOR_PLATFORM}" STREQUAL "x64" OR "${CMAKE_VS_PLATFORM_NAME}" STREQUAL "x64")
        set(TS_LIB_PATH "windows/win64")
    elseif("${CMAKE_GENERATOR_PLATFORM}" STREQUAL "Win32" OR "${CMAKE_VS_PLATFORM_NAME}" STREQUAL "Win32")
        set(TS_LIB_PATH "windows/win32")
    endif()
endif()
message("TS_LIB_PATH: ${TS_LIB_PATH}")

find_library(TSServerSdk_LIBRARY
    NAMES ts3server libts3server
    HINTS ${PC_TSSERVERSDK_LIBDIR} ${PC_TSSERVERSDK_LIBRARY_DIRS} "${CMAKE_CURRENT_LIST_DIR}/../bin/${TS_LIB_PATH}" "${CMAKE_CURRENT_LIST_DIR}/../lib/${TS_LIB_PATH}" "${CMAKE_CURRENT_LIST_DIR}/../frameworks/${TS_LIB_PATH}"
)

include(FindPackageHandleStandardArgs)
find_package_handle_standard_args(TSServerSdk
    REQUIRED_VARS TSServerSdk_INCLUDE_DIR TSServerSdk_LIBRARY
)

mark_as_advanced(TSServerSdk_FOUND TSServerSdk_INCLUDE_DIR TSServerSdk_LIBRARY)

if (TSServerSdk_FOUND)
    set(TSServerSdk_INCLUDE_DIRS ${TSServerSdk_INCLUDE_DIR})
    set(TSServerSdk_LIBRARIES ${TSServerSdk_LIBRARY})
endif()

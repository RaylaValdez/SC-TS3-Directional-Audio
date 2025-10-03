# - Try to find TSClientSdk
# Once done, this will define
#
#  TSClientSdk_FOUND - system has TSClientSdk
#  TSClientSdk_INCLUDE_DIRS - the TSClientSdk include directories
#  TSClientSdk_LIBRARIES - link these to use TSClientSdk

message("adding TeamSpeak Client SDK")

# Include dir

# if (CMAKE_SYSTEM_NAME STREQUAL "Darwin")
#     set(TS_INCLUDE_DIR_HINT "${CMAKE_CURRENT_LIST_DIR}/../frameworks/ts3client.xcframework/macos-arm64_x86_64/ts3client.framework/Headers")
# else()
set(TS_INCLUDE_DIR_HINT "${CMAKE_CURRENT_LIST_DIR}/../include/")
# endif()

find_path(TSClientSdk_INCLUDE_DIR
    NAMES teamspeak/public_definitions.h
    PATHS "${TSClientSdk_INCLUDE_DIRS}" "${TS_INCLUDE_DIR_HINT}"
    REQUIRED
)

# Find Library
message("CMAKE_SYSTEM_NAME: ${CMAKE_SYSTEM_NAME}")
message("CMAKE_SYSTEM_PROCESSOR ${CMAKE_SYSTEM_PROCESSOR}")
message("CMAKE_GENERATOR_PLATFORM ${CMAKE_GENERATOR_PLATFORM}")

if (CMAKE_SYSTEM_NAME STREQUAL "Darwin")
    set(TS_LIB_PATH "mac")
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

find_library(TSClientSdk_LIBRARY
    NAMES ts3client libts3client
    HINTS ${PC_TSCLIENTSDK_LIBDIR} ${PC_TSCLIENTSDK_LIBRARY_DIRS} "${CMAKE_CURRENT_LIST_DIR}/../bin/${TS_LIB_PATH}" "${CMAKE_CURRENT_LIST_DIR}/../lib/${TS_LIB_PATH}" "${CMAKE_CURRENT_LIST_DIR}/../bin/${TS_LIB_PATH}/arm64_x86_64" "${CMAKE_CURRENT_LIST_DIR}/../frameworks/${TS_LIB_PATH}" "${CMAKE_CURRENT_LIST_DIR}/../frameworks/ts3client.xcframework/macos-arm64_x86_64"
)

include(FindPackageHandleStandardArgs)
find_package_handle_standard_args(TSClientSdk
    REQUIRED_VARS TSClientSdk_INCLUDE_DIR TSClientSdk_LIBRARY
)

mark_as_advanced(TSClientSdk_FOUND TSClientSdk_INCLUDE_DIR TSClientSdk_LIBRARY)

if (TSClientSdk_FOUND)
    set(TSClientSdk_INCLUDE_DIRS ${TSClientSdk_INCLUDE_DIR})
    set(TSClientSdk_LIBRARIES ${TSClientSdk_LIBRARY})
endif()

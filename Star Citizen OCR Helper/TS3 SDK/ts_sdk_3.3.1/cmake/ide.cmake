#---------------------------------------------------------------------------------------
# IDE support for headers
#---------------------------------------------------------------------------------------
message("adding IDE support for TeamSpeak SDK headers ${sample_type}")

# if (CMAKE_SYSTEM_NAME STREQUAL "Darwin" AND "${sample_type}" STREQUAL "client")
#     set(TSSDK_HEADERS_DIR "${CMAKE_CURRENT_LIST_DIR}/../frameworks/ts3client.xcframework/macos-arm64_x86_64/ts3client.framework/Headers")
# else()
set(TSSDK_HEADERS_DIR "${CMAKE_CURRENT_LIST_DIR}/../include")
# endif()

file(GLOB TSSDK_LOG_HEADERS "${TSSDK_HEADERS_DIR}/teamlog/*.h")

# file(GLOB TSSDK_HEADERS "${TSSDK_HEADERS_DIR}/teamspeak/*.h")
set (TS_COMMON_SDK_IDE
    ${TSSDK_LOG_HEADERS}
    "${TSSDK_HEADERS_DIR}/teamspeak/public_definitions.h"
    "${TSSDK_HEADERS_DIR}/teamspeak/public_errors.h"
)

if ("${sample_type}" STREQUAL "client")
    message("Adding sdk client headers...")
    set (TS_SDK_IDE
        "${TSSDK_HEADERS_DIR}/teamspeak/clientlib.h"
    )
elseif("${sample_type}" STREQUAL "server")
    message("Adding sdk server headers...")
    set (TS_SDK_IDE
        "${TSSDK_HEADERS_DIR}/teamspeak/server_commands_file_transfer.h"
        "${TSSDK_HEADERS_DIR}/teamspeak/serverlib.h"
        "${TSSDK_HEADERS_DIR}/teamspeak/serverlib_publicdefinitions.h"
    )
else()
    message("example type not specified.")
endif()

source_group("teamspeak" FILES ${TS_COMMON_SDK_IDE} ${TS_SDK_IDE})

package com.teamspeak.ts3sdkclient.ts3sdk.states;

import android.util.SparseArray;

/**
 * TeamSpeak 3 sdk client sample
 *
 * Copyright (c) 2007-2017 TeamSpeak-Systems
 *
 * @author Anna
 * Creation date: 13.02.17
 */
public class PublicError {
    //general
    public static final int ERROR_ok                     = 0x0000;
    public static final int ERROR_undefined              = 0x0001;
    public static final int ERROR_not_implemented        = 0x0002;
    public static final int ERROR_ok_no_update           = 0x0003;
    public static final int ERROR_dont_notify            = 0x0004;
    public static final int ERROR_lib_time_limit_reached = 0x0005;

    //dunno
    public static final int ERROR_command_not_found                      = 0x0100;
    public static final int ERROR_unable_to_bind_network_port            = 0x0101;
    public static final int ERROR_no_network_port_available              = 0x0102;
    public static final int ERROR_port_already_in_use                    = 0x0103;

    //client
    public static final int ERROR_client_invalid_id                  = 0x0200;
    public static final int ERROR_client_nickname_inuse              = 0x0201;
    public static final int ERROR_client_protocol_limit_reached      = 0x0203;
    public static final int ERROR_client_invalid_type                = 0x0204;
    public static final int ERROR_client_already_subscribed          = 0x0205;
    public static final int ERROR_client_not_logged_in               = 0x0206;
    public static final int ERROR_client_could_not_validate_identity = 0x0207;
    public static final int ERROR_client_version_outdated            = 0x020a;
    public static final int ERROR_client_is_flooding                 = 0x020c;
    public static final int ERROR_client_hacked                      = 0x020d;
    public static final int ERROR_client_cannot_verify_now           = 0x020e;
    public static final int ERROR_client_login_not_permitted         = 0x020f;
    public static final int ERROR_client_not_subscribed              = 0x0210;

    //channel
    public static final int ERROR_channel_invalid_id                = 0x0300;
    public static final int ERROR_channel_protocol_limit_reached    = 0x0301;
    public static final int ERROR_channel_already_in                = 0x0302;
    public static final int ERROR_channel_name_inuse                = 0x0303;
    public static final int ERROR_channel_not_empty                 = 0x0304;
    public static final int ERROR_channel_can_not_delete_default    = 0x0305;
    public static final int ERROR_channel_default_require_permanent = 0x0306;
    public static final int ERROR_channel_invalid_flags             = 0x0307;
    public static final int ERROR_channel_parent_not_permanent      = 0x0308;
    public static final int ERROR_channel_maxclients_reached        = 0x0309;
    public static final int ERROR_channel_maxfamily_reached         = 0x030a;
    public static final int ERROR_channel_invalid_order             = 0x030b;
    public static final int ERROR_channel_no_filetransfer_supported = 0x030c;
    public static final int ERROR_channel_invalid_password          = 0x030d;
    public static final int ERROR_channel_invalid_security_hash     = 0x030f;

    //server
    public static final int ERROR_server_invalid_id         = 0x0400;
    public static final int ERROR_server_running            = 0x0401;
    public static final int ERROR_server_is_shutting_down   = 0x0402;
    public static final int ERROR_server_maxclients_reached = 0x0403;
    public static final int ERROR_server_invalid_password   = 0x0404;
    public static final int ERROR_server_is_virtual         = 0x0407;
    public static final int ERROR_server_is_not_running     = 0x0409;
    public static final int ERROR_server_is_booting         = 0x040a;
    public static final int ERROR_server_status_invalid     = 0x040b;
    public static final int ERROR_server_version_outdated   = 0x040d;
    public static final int ERROR_server_duplicate_running  = 0x040e;

    //parameter
    public static final int ERROR_parameter_quote         = 0x0600;
    public static final int ERROR_parameter_invalid_count = 0x0601;
    public static final int ERROR_parameter_invalid       = 0x0602;
    public static final int ERROR_parameter_not_found     = 0x0603;
    public static final int ERROR_parameter_convert       = 0x0604;
    public static final int ERROR_parameter_invalid_size  = 0x0605;
    public static final int ERROR_parameter_missing       = 0x0606;
    public static final int ERROR_parameter_checksum      = 0x0607;

    //unsorted, need further investigation
    public static final int ERROR_vs_critical                          = 0x0700;
    public static final int ERROR_connection_lost                      = 0x0701;
    public static final int ERROR_not_connected                        = 0x0702;
    public static final int ERROR_no_cached_connection_info            = 0x0703;
    public static final int ERROR_currently_not_possible               = 0x0704;
    public static final int ERROR_failed_connection_initialisation     = 0x0705;
    public static final int ERROR_could_not_resolve_hostname           = 0x0706;
    public static final int ERROR_invalid_server_connection_handler_id = 0x0707;
    public static final int ERROR_could_not_initialise_input_manager   = 0x0708;
    public static final int ERROR_clientlibrary_not_initialised        = 0x0709;
    public static final int ERROR_serverlibrary_not_initialised        = 0x070a;
    public static final int ERROR_whisper_too_many_targets             = 0x070b;
    public static final int ERROR_whisper_no_targets                   = 0x070c;
    public static final int ERROR_connection_ip_protocol_missing       = 0x070d;

    //file transfer
    public static final int ERROR_file_invalid_name                     = 0x0800;
    public static final int ERROR_file_invalid_permissions              = 0x0801;
    public static final int ERROR_file_already_exists                   = 0x0802;
    public static final int ERROR_file_not_found                        = 0x0803;
    public static final int ERROR_file_io_error                         = 0x0804;
    public static final int ERROR_file_invalid_transfer_id              = 0x0805;
    public static final int ERROR_file_invalid_path                     = 0x0806;
    public static final int ERROR_file_no_files_available               = 0x0807;
    public static final int ERROR_file_overwrite_excludes_resume        = 0x0808;
    public static final int ERROR_file_invalid_size                     = 0x0809;
    public static final int ERROR_file_already_in_use                   = 0x080a;
    public static final int ERROR_file_could_not_open_connection        = 0x080b;
    public static final int ERROR_file_no_space_left_on_device          = 0x080c;
    public static final int ERROR_file_exceeds_file_system_maximum_size = 0x080d;
    public static final int ERROR_file_transfer_connection_timeout      = 0x080e;
    public static final int ERROR_file_connection_lost                  = 0x080f;
    public static final int ERROR_file_exceeds_supplied_size            = 0x0810;
    public static final int ERROR_file_transfer_complete                = 0x0811;
    public static final int ERROR_file_transfer_canceled                = 0x0812;
    public static final int ERROR_file_transfer_interrupted             = 0x0813;
    public static final int ERROR_file_transfer_server_quota_exceeded   = 0x0814;
    public static final int ERROR_file_transfer_client_quota_exceeded   = 0x0815;
    public static final int ERROR_file_transfer_reset                   = 0x0816;
    public static final int ERROR_file_transfer_limit_reached           = 0x0817;

    //sound
    public static final int ERROR_sound_preprocessor_disabled          = 0x0900;
    public static final int ERROR_sound_internal_preprocessor          = 0x0901;
    public static final int ERROR_sound_internal_encoder               = 0x0902;
    public static final int ERROR_sound_internal_playback              = 0x0903;
    public static final int ERROR_sound_no_capture_device_available    = 0x0904;
    public static final int ERROR_sound_no_playback_device_available   = 0x0905;
    public static final int ERROR_sound_could_not_open_capture_device  = 0x0906;
    public static final int ERROR_sound_could_not_open_playback_device = 0x0907;
    public static final int ERROR_sound_handler_has_device             = 0x0908;
    public static final int ERROR_sound_invalid_capture_device         = 0x0909;
    public static final int ERROR_sound_invalid_playback_device        = 0x090a;
    public static final int ERROR_sound_invalid_wave                   = 0x090b;
    public static final int ERROR_sound_unsupported_wave               = 0x090c;
    public static final int ERROR_sound_open_wave                      = 0x090d;
    public static final int ERROR_sound_internal_capture               = 0x090e;
    public static final int ERROR_sound_device_in_use                  = 0x090f;
    public static final int ERROR_sound_device_already_registerred     = 0x0910;
    public static final int ERROR_sound_unknown_device                 = 0x0911;
    public static final int ERROR_sound_unsupported_frequency          = 0x0912;
    public static final int ERROR_sound_invalid_channel_count          = 0x0913;
    public static final int ERROR_sound_read_wave                      = 0x0914;
    public static final int ERROR_sound_need_more_data                 = 0x0915; //for internal purposes only
    public static final int ERROR_sound_device_busy                    = 0x0916; //for internal purposes only
    public static final int ERROR_sound_no_data                        = 0x0917;
    public static final int ERROR_sound_channel_mask_mismatch          = 0x0918;


    //permissions
    public static final int ERROR_permissions_client_insufficient = 0x0a08;
    public static final int ERROR_permissions                     = 0x0a0c;

    //accounting
    public static final int ERROR_accounting_virtualserver_limit_reached = 0x0b00;
    public static final int ERROR_accounting_slot_limit_reached          = 0x0b01;
    public static final int ERROR_accounting_license_file_not_found      = 0x0b02;
    public static final int ERROR_accounting_license_date_not_ok         = 0x0b03;
    public static final int ERROR_accounting_unable_to_connect_to_server = 0x0b04;
    public static final int ERROR_accounting_unknown_error               = 0x0b05;
    public static final int ERROR_accounting_server_error                = 0x0b06;
    public static final int ERROR_accounting_instance_limit_reached      = 0x0b07;
    public static final int ERROR_accounting_instance_check_error        = 0x0b08;
    public static final int ERROR_accounting_license_file_invalid        = 0x0b09;
    public static final int ERROR_accounting_running_elsewhere           = 0x0b0a;
    public static final int ERROR_accounting_instance_duplicated         = 0x0b0b;
    public static final int ERROR_accounting_already_started             = 0x0b0c;
    public static final int ERROR_accounting_not_started                 = 0x0b0d;
    public static final int ERROR_accounting_to_many_starts              = 0x0b0e;

    //provisioning server
    public static final int ERROR_provisioning_invalid_password          = 0x1100;
    public static final int ERROR_provisioning_invalid_request           = 0x1101;
    public static final int ERROR_provisioning_no_slots_available        = 0x1102;
    public static final int ERROR_provisioning_pool_missing              = 0x1103;
    public static final int ERROR_provisioning_pool_unknown              = 0x1104;
    public static final int ERROR_provisioning_unknown_ip_location       = 0x1105;
    public static final int ERROR_provisioning_internal_tries_exceeded   = 0x1106;
    public static final int ERROR_provisioning_too_many_slots_requested  = 0x1107;
    public static final int ERROR_provisioning_too_many_reserved         = 0x1108;
    public static final int ERROR_provisioning_could_not_connect         = 0x1109;
    public static final int ERROR_provisioning_auth_server_not_connected = 0x1110;
    public static final int ERROR_provisioning_auth_data_too_large       = 0x1111;
    public static final int ERROR_provisioning_already_initialized       = 0x1112;
    public static final int ERROR_provisioning_not_initialized           = 0x1113;
    public static final int ERROR_provisioning_connecting                = 0x1114;
    public static final int ERROR_provisioning_already_connected         = 0x1115;
    public static final int ERROR_provisioning_not_connected             = 0x1116;
    public static final int ERROR_provisioning_io_error                  = 0x1117;
    public static final int ERROR_provisioning_invalid_timeout           = 0x1118;
    public static final int ERROR_provisioning_ts3server_not_found       = 0x1119;
    public static final int ERROR_provisioning_no_permission             = 0x111A;

    private static final SparseArray<String> code = new SparseArray<>();

    static {
        // general
        code.put(ERROR_ok, "ERROR_ok");
        code.put(ERROR_undefined, "ERROR_undefined");
        code.put(ERROR_not_implemented, "ERROR_not_implemented");
        code.put(ERROR_ok_no_update, "ERROR_ok_no_update");
        code.put(ERROR_dont_notify, "ERROR_dont_notify");
        code.put(ERROR_lib_time_limit_reached, "ERROR_lib_time_limit_reached");

        // dunno
        code.put(ERROR_command_not_found, "ERROR_command_not_found");
        code.put(ERROR_unable_to_bind_network_port, "ERROR_unable_to_bind_network_port");
        code.put(ERROR_no_network_port_available, "ERROR_no_network_port_available");
        code.put(ERROR_port_already_in_use, "ERROR_port_already_in_use");

        // client
        code.put(ERROR_client_invalid_id, "ERROR_client_invalid_id");
        code.put(ERROR_client_nickname_inuse, "ERROR_client_nickname_inuse");
        code.put(ERROR_client_protocol_limit_reached, "ERROR_client_protocol_limit_reached");
        code.put(ERROR_client_invalid_type, "ERROR_client_invalid_type");
        code.put(ERROR_client_already_subscribed, "ERROR_client_already_subscribed");
        code.put(ERROR_client_not_logged_in, "ERROR_client_not_logged_in");
        code.put(ERROR_client_could_not_validate_identity,
                "ERROR_client_could_not_validate_identity");
        code.put(ERROR_client_version_outdated, "ERROR_client_version_outdated");
        code.put(ERROR_client_is_flooding, "ERROR_client_is_flooding");
        code.put(ERROR_client_hacked, "ERROR_client_hacked");
        code.put(ERROR_client_cannot_verify_now, "ERROR_client_cannot_verify_now");
        code.put(ERROR_client_login_not_permitted, "ERROR_client_login_not_permitted");
        code.put(ERROR_client_not_subscribed, "ERROR_client_not_subscribed");

        // channel
        code.put(ERROR_channel_invalid_id, "ERROR_channel_invalid_id");
        code.put(ERROR_channel_protocol_limit_reached, "ERROR_channel_protocol_limit_reached");
        code.put(ERROR_channel_already_in, "ERROR_channel_already_in");
        code.put(ERROR_channel_name_inuse, "ERROR_channel_name_inuse");
        code.put(ERROR_channel_not_empty, "ERROR_channel_not_empty");
        code.put(ERROR_channel_can_not_delete_default, "ERROR_channel_can_not_delete_default");
        code.put(ERROR_channel_default_require_permanent,
                "ERROR_channel_default_require_permanent");
        code.put(ERROR_channel_invalid_flags, "ERROR_channel_invalid_flags");
        code.put(ERROR_channel_parent_not_permanent, "ERROR_channel_parent_not_permanent");
        code.put(ERROR_channel_maxclients_reached, "ERROR_channel_maxclients_reached");
        code.put(ERROR_channel_maxfamily_reached, "ERROR_channel_maxfamily_reached");
        code.put(ERROR_channel_invalid_order, "ERROR_channel_invalid_order");
        code.put(ERROR_channel_no_filetransfer_supported,
                "ERROR_channel_no_filetransfer_supported");
        code.put(ERROR_channel_invalid_password, "ERROR_channel_invalid_password");
        code.put(ERROR_channel_invalid_security_hash, "ERROR_channel_invalid_security_hash");

        // server
        code.put(ERROR_server_invalid_id, "ERROR_server_invalid_id");
        code.put(ERROR_server_running, "ERROR_server_running");
        code.put(ERROR_server_is_shutting_down, "ERROR_server_is_shutting_down");
        code.put(ERROR_server_maxclients_reached, "ERROR_server_maxclients_reached");
        code.put(ERROR_server_invalid_password, "ERROR_server_invalid_password");
        code.put(ERROR_server_is_virtual, "ERROR_server_is_virtual");
        code.put(ERROR_server_is_not_running, "ERROR_server_is_not_running");
        code.put(ERROR_server_is_booting, "ERROR_server_is_booting");
        code.put(ERROR_server_status_invalid, "ERROR_server_status_invalid");
        code.put(ERROR_server_version_outdated, "ERROR_server_version_outdated");
        code.put(ERROR_server_duplicate_running, "ERROR_server_duplicate_running");

        // parameter
        code.put(ERROR_parameter_quote, "ERROR_parameter_quote");
        code.put(ERROR_parameter_invalid_count, "ERROR_parameter_invalid_count");
        code.put(ERROR_parameter_invalid, "ERROR_parameter_invalid");
        code.put(ERROR_parameter_not_found, "ERROR_parameter_not_found");
        code.put(ERROR_parameter_convert, "ERROR_parameter_convert");
        code.put(ERROR_parameter_invalid_size, "ERROR_parameter_invalid_size");
        code.put(ERROR_parameter_missing, "ERROR_parameter_missing");
        code.put(ERROR_parameter_checksum, "ERROR_parameter_checksum");

        // unsorted," need further investigation
        code.put(ERROR_vs_critical, "ERROR_vs_critical");
        code.put(ERROR_connection_lost, "ERROR_connection_lost");
        code.put(ERROR_not_connected, "ERROR_not_connected");
        code.put(ERROR_no_cached_connection_info, "ERROR_no_cached_connection_info");
        code.put(ERROR_currently_not_possible, "ERROR_currently_not_possible");
        code.put(ERROR_failed_connection_initialisation, "ERROR_failed_connection_initialisation");
        code.put(ERROR_could_not_resolve_hostname, "ERROR_could_not_resolve_hostname");
        code.put(ERROR_invalid_server_connection_handler_id,
                "ERROR_invalid_server_connection_handler_id");
        code.put(ERROR_could_not_initialise_input_manager,
                "ERROR_could_not_initialise_input_manager");
        code.put(ERROR_clientlibrary_not_initialised, "ERROR_clientlibrary_not_initialised");
        code.put(ERROR_serverlibrary_not_initialised, "ERROR_serverlibrary_not_initialised");
        code.put(ERROR_whisper_too_many_targets, "ERROR_whisper_too_many_targets");
        code.put(ERROR_whisper_no_targets, "ERROR_whisper_no_targets");
        code.put(ERROR_connection_ip_protocol_missing, "ERROR_connection_ip_protocol_missing");


        //file transfer
        code.put(ERROR_file_invalid_name, "ERROR_file_invalid_name");
        code.put(ERROR_file_invalid_permissions, "ERROR_file_invalid_permissions");
        code.put(ERROR_file_already_exists, "ERROR_file_already_exists");
        code.put(ERROR_file_not_found, "ERROR_file_not_found");
        code.put(ERROR_file_io_error, "ERROR_file_io_error");
        code.put(ERROR_file_invalid_transfer_id, "ERROR_file_invalid_transfer_id");
        code.put(ERROR_file_invalid_path, "ERROR_file_invalid_path");
        code.put(ERROR_file_no_files_available, "ERROR_file_no_files_available");
        code.put(ERROR_file_overwrite_excludes_resume, "ERROR_file_overwrite_excludes_resume");
        code.put(ERROR_file_invalid_size, "ERROR_file_invalid_size");
        code.put(ERROR_file_already_in_use, "ERROR_file_already_in_use");
        code.put(ERROR_file_could_not_open_connection, "ERROR_file_could_not_open_connection");
        code.put(ERROR_file_no_space_left_on_device, "ERROR_file_no_space_left_on_device");
        code.put(ERROR_file_exceeds_file_system_maximum_size,
                "ERROR_file_exceeds_file_system_maximum_size");
        code.put(ERROR_file_transfer_connection_timeout, "ERROR_file_transfer_connection_timeout");
        code.put(ERROR_file_connection_lost, "ERROR_file_connection_lost");
        code.put(ERROR_file_exceeds_supplied_size, "ERROR_file_exceeds_supplied_size");
        code.put(ERROR_file_transfer_complete, "ERROR_file_transfer_complete");
        code.put(ERROR_file_transfer_canceled, "ERROR_file_transfer_canceled");
        code.put(ERROR_file_transfer_interrupted, "ERROR_file_transfer_interrupted");
        code.put(ERROR_file_transfer_server_quota_exceeded,
                "ERROR_file_transfer_server_quota_exceeded");
        code.put(ERROR_file_transfer_client_quota_exceeded,
                "ERROR_file_transfer_client_quota_exceeded");
        code.put(ERROR_file_transfer_reset, "ERROR_file_transfer_reset");
        code.put(ERROR_file_transfer_limit_reached, "ERROR_file_transfer_limit_reached");

        // sound
        code.put(ERROR_sound_preprocessor_disabled, "ERROR_sound_preprocessor_disabled");
        code.put(ERROR_sound_internal_preprocessor, "ERROR_sound_internal_preprocessor");
        code.put(ERROR_sound_internal_encoder, "ERROR_sound_internal_encoder");
        code.put(ERROR_sound_internal_playback, "ERROR_sound_internal_playback");
        code.put(ERROR_sound_no_capture_device_available,
                "ERROR_sound_no_capture_device_available");
        code.put(ERROR_sound_no_playback_device_available,
                "ERROR_sound_no_playback_device_available");
        code.put(ERROR_sound_could_not_open_capture_device,
                "ERROR_sound_could_not_open_capture_device");
        code.put(ERROR_sound_could_not_open_playback_device,
                "ERROR_sound_could_not_open_playback_device");
        code.put(ERROR_sound_handler_has_device, "ERROR_sound_handler_has_device");
        code.put(ERROR_sound_invalid_capture_device, "ERROR_sound_invalid_capture_device");
        code.put(ERROR_sound_invalid_playback_device, "ERROR_sound_invalid_playback_device");
        code.put(ERROR_sound_invalid_wave, "ERROR_sound_invalid_wave");
        code.put(ERROR_sound_unsupported_wave, "ERROR_sound_unsupported_wave");
        code.put(ERROR_sound_open_wave, "ERROR_sound_open_wave");
        code.put(ERROR_sound_internal_capture, "ERROR_sound_internal_capture");
        code.put(ERROR_sound_device_in_use, "ERROR_sound_device_in_use");
        code.put(ERROR_sound_device_already_registerred, "ERROR_sound_device_already_registerred");
        code.put(ERROR_sound_unknown_device, "ERROR_sound_unknown_device");
        code.put(ERROR_sound_unsupported_frequency, "ERROR_sound_unsupported_frequency");
        code.put(ERROR_sound_invalid_channel_count, "ERROR_sound_invalid_channel_count");
        code.put(ERROR_sound_read_wave, "ERROR_sound_read_wave");
        code.put(ERROR_sound_need_more_data, "ERROR_sound_need_more_data"); // for internal purposes
        // only
        code.put(ERROR_sound_device_busy, "ERROR_sound_device_busy"); // for internal purposes
        // only
        code.put(ERROR_sound_no_data, "ERROR_sound_no_data");
        code.put(ERROR_sound_channel_mask_mismatch, "ERROR_sound_channel_mask_mismatch");

        // permissions
        code.put(ERROR_permissions_client_insufficient, "ERROR_permissions_client_insufficient");
        code.put(ERROR_permissions, "ERROR_permissions");

        // accounting
        code.put(ERROR_accounting_virtualserver_limit_reached,
                "ERROR_accounting_virtualserver_limit_reached");
        code.put(ERROR_accounting_slot_limit_reached, "ERROR_accounting_slot_limit_reached");
        code.put(ERROR_accounting_license_file_not_found,
                "ERROR_accounting_license_file_not_found");
        code.put(ERROR_accounting_license_date_not_ok, "ERROR_accounting_license_date_not_ok");
        code.put(ERROR_accounting_unable_to_connect_to_server,
                "ERROR_accounting_unable_to_connect_to_server");
        code.put(ERROR_accounting_unknown_error, "ERROR_accounting_unknown_error");
        code.put(ERROR_accounting_server_error, "ERROR_accounting_server_error");
        code.put(ERROR_accounting_instance_limit_reached,
                "ERROR_accounting_instance_limit_reached");
        code.put(ERROR_accounting_instance_check_error, "ERROR_accounting_instance_check_error");
        code.put(ERROR_accounting_license_file_invalid, "ERROR_accounting_license_file_invalid");
        code.put(ERROR_accounting_running_elsewhere, "ERROR_accounting_running_elsewhere");
        code.put(ERROR_accounting_instance_duplicated, "ERROR_accounting_instance_duplicated");
        code.put(ERROR_accounting_already_started, "ERROR_accounting_already_started");
        code.put(ERROR_accounting_not_started, "ERROR_accounting_not_started");
        code.put(ERROR_accounting_to_many_starts, "ERROR_accounting_to_many_starts");

        // provisioning server
        code.put(ERROR_provisioning_invalid_password, "ERROR_provisioning_invalid_password");
        code.put(ERROR_provisioning_invalid_request, "ERROR_provisioning_invalid_request");
        code.put(ERROR_provisioning_no_slots_available, "ERROR_provisioning_no_slots_available");
        code.put(ERROR_provisioning_pool_missing, "ERROR_provisioning_pool_missing");
        code.put(ERROR_provisioning_pool_unknown, "ERROR_provisioning_pool_unknown");
        code.put(ERROR_provisioning_unknown_ip_location, "ERROR_provisioning_unknown_ip_location");
        code.put(ERROR_provisioning_internal_tries_exceeded,
                "ERROR_provisioning_internal_tries_exceeded");
        code.put(ERROR_provisioning_too_many_slots_requested,
                "ERROR_provisioning_too_many_slots_requested");
        code.put(ERROR_provisioning_too_many_reserved, "ERROR_provisioning_too_many_reserved");
        code.put(ERROR_provisioning_could_not_connect, "ERROR_provisioning_could_not_connect");
        code.put(ERROR_provisioning_auth_server_not_connected,
                "ERROR_provisioning_auth_server_not_connected");
        code.put(ERROR_provisioning_auth_data_too_large, "ERROR_provisioning_auth_data_too_large");
        code.put(ERROR_provisioning_already_initialized, "ERROR_provisioning_already_initialized");
        code.put(ERROR_provisioning_not_initialized, "ERROR_provisioning_not_initialized");
        code.put(ERROR_provisioning_connecting, "ERROR_provisioning_connecting");
        code.put(ERROR_provisioning_already_connected, "ERROR_provisioning_already_connected");
        code.put(ERROR_provisioning_not_connected, "ERROR_provisioning_not_connected");
        code.put(ERROR_provisioning_io_error, "ERROR_provisioning_io_error");
        code.put(ERROR_provisioning_invalid_timeout, "ERROR_provisioning_invalid_timeout");
        code.put(ERROR_provisioning_ts3server_not_found, "ERROR_provisioning_ts3server_not_found");
        code.put(ERROR_provisioning_no_permission, "ERROR_provisioning_no_permission");

    }

    public static String getPublicErrorString(int codeID) {
        return code.get(codeID);
    }
}

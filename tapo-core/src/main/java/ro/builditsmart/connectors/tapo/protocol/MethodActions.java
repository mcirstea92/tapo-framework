package ro.builditsmart.connectors.tapo.protocol;

public enum MethodActions {
    securePassthrough,
    set_device_info, //
    get_device_info, // 011
    login_device, // 002
    get_energy_usage, // 013
    add_countdown_rule, // 022
    get_device_running_info, // 014
    get_device_usage, // 012
    get_led_info, // 015
    get_schedule_rules, // 017
    set_schedule_rules,
    edit_schedule_rule,
    add_schedule_rule,
    remove_schedule_rules,
    get_notifications_settings,
    set_notifications_settings,
    get_firmware_version,
    set_firmware_version,
    get_countdown_rules, // 016
    edit_countdown_rule,
    remove_countdown_rules,
    heart_beat,
    get_device_log,
    get_device_time,
    set_device_time,
    get_factory_info,
    get_wireless_scan_info,
    get_schedule_day_runtime,
    get_schedule_month_runtime,
    get_inherit_info,
    device_reboot,
    setAlias,
    get_latest_fw,
    getWifiBasic,
    get_diagnose_status,
    get_ffs_info,
    getFwCurrentVer;

    public static String getName(MethodActions methodAction) {
        return methodAction.name();
    }

}

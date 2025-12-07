package com.roro.lgthinq

data class TVDevice(
    val ip: String,
    val name: String? = null,
    val model: String? = null,
    val clientKey: String? = null
)

data class SSAPCommand(
    val type: String = "request",
    val id: String,
    val uri: String,
    val payload: Map<String, Any>? = null
)

object SSAPUris {
    // System
    const val TURN_OFF = "ssap://system/turnOff"
    const val TURN_ON_SCREEN = "ssap://com.webos.service.tvpower/power/turnOnScreen"
    const val TURN_OFF_SCREEN = "ssap://com.webos.service.tvpower/power/turnOffScreen"
    
    // Launcher
    const val LAUNCH_APP = "ssap://system.launcher/launch"
    const val CLOSE_APP = "ssap://system.launcher/close"
    const val GET_APP_LIST = "ssap://com.webos.applicationManager/listApps"
    
    // Audio
    const val VOLUME_UP = "ssap://audio/volumeUp"
    const val VOLUME_DOWN = "ssap://audio/volumeDown"
    const val SET_VOLUME = "ssap://audio/setVolume"
    const val GET_VOLUME = "ssap://audio/getVolume"
    const val SET_MUTE = "ssap://audio/setMute"
    const val GET_MUTE = "ssap://audio/getStatus"
    
    // TV
    const val CHANNEL_UP = "ssap://tv/channelUp"
    const val CHANNEL_DOWN = "ssap://tv/channelDown"
    const val CHANNEL_LIST = "ssap://tv/getChannelList"
    const val CURRENT_CHANNEL = "ssap://tv/getCurrentChannel"
    const val OPEN_CHANNEL = "ssap://tv/openChannel"
    
    // Input
    const val GET_INPUT_LIST = "ssap://tv/getExternalInputList"
    const val SWITCH_INPUT = "ssap://tv/switchInput"
    
    // IME (Keyboard)
    const val INSERT_TEXT = "ssap://com.webos.service.ime/insertText"
    const val SEND_ENTER = "ssap://com.webos.service.ime/sendEnterKey"
    const val DELETE_CHARACTERS = "ssap://com.webos.service.ime/deleteCharacters"
    
    // Mouse/Pointer
    const val MOVE_POINTER = "ssap://com.webos.service.networkinput/getPointerInputSocket"
    const val CLICK = "ssap://com.webos.service.networkinput/getPointerInputSocket"
    
    // Media
    const val PLAY = "ssap://media.controls/play"
    const val PAUSE = "ssap://media.controls/pause"
    const val STOP = "ssap://media.controls/stop"
    const val REWIND = "ssap://media.controls/rewind"
    const val FAST_FORWARD = "ssap://media.controls/fastForward"
    
    // Notifications
    const val CREATE_TOAST = "ssap://system.notifications/createToast"
    const val CREATE_ALERT = "ssap://system.notifications/createAlert"
}

object AppIds {
    const val HOME = "com.webos.app.home"
    const val NETFLIX = "netflix"
    const val YOUTUBE = "youtube.leanback.v4"
    const val AMAZON = "amazon"
    const val DISNEY_PLUS = "com.disney.disneyplus-prod"
    const val PRIME_VIDEO = "amazon"
    const val HBO_MAX = "hboMax"
    const val APPLE_TV = "com.apple.appletv"
    const val SPOTIFY = "spotify-beehive"
    const val PLEX = "cdp-30"
    const val BROWSER = "com.webos.app.browser"
    const val LIVE_TV = "com.webos.app.livetv"
}

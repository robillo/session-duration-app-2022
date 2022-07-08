@file:Suppress("unused")

package com.firstapp.robinpc.tongue_twisters_deluxe.utils

class Constants {
    companion object {

        //DYNAMIC LINKS
        const val PARAMETER_LEVEL_NUMBER = "level"
        const val PARAMETER_TWISTER_NUMBER = "twister"
        /*
        deep link for opening a specific tongue twister:
        https://tonguetwistersdeluxe.page.link/?link=https://tonguetwistersdeluxe.com/home?twister=1
        deep link for opening a specific difficulty level:
        https://tonguetwistersdeluxe.page.link/?link=https://tonguetwistersdeluxe.com/home?level=1
        deep link for app share:
        https://tonguetwistersdeluxe.page.link

        note that only link query param is received after within the app:
        https://tonguetwistersdeluxe.com/home?twister=1
        https://tonguetwistersdeluxe.com/home?level=1

        from these received links, we can extract query parameters:
        parameter("twister")
        parameter("level")

        based on the received parameter, we can open twister / open level / do nothing
         */

        //ROOM DATABASE
        const val APP_DATABASE = "twister_database"

        //DEFAULTS
        const val DEFAULT_STRING = "DEFAULT"
        const val DEFAULT_BOOLEAN = false

        //GENERIC CONSTANTS
        const val TYPE_LENGTH = 0
        const val TYPE_DIFFICULTY = 1
        const val TYPE_DAY_TWISTER = 2

        //LEVEL HEADER IN READING SCREEN
        const val DEFAULT_LEVEL_HEADER = "RANDOM TWISTER"

        const val CHARSET_UTF_8 = "UTF-8"
        const val DEFAULT_VALUE_INT = 0
        const val DEFAULT_VALUE_BOOLEAN = false
        const val DEFAULT_VALUE_FLOAT = 0f
        const val DEFAULT_VALUE_LONG = 0L
        const val DEFAULT_VALUE_STRING = ""

        //PREFERENCES CONSTANTS
        const val EXTRA_PREFERENCES_TWISTER = "PREFERENCES_TWISTER"
        const val EXTRA_LAST_OPENED_LENGTH_LEVEL = "LAST_OPENED_LENGTH_LEVEL"
        const val EXTRA_LAST_OPENED_DIFFICULTY_LEVEL = "LAST_OPENED_DIFFICULTY_LEVEL"
        const val EXTRA_IS_DATA_LOAD_COMPLETE = "IS_DATA_LOAD_COMPLETE"

        //JSON DB CONSTANTS
        const val ALL_TWISTERS = "twister"
        const val LEVELS_BY_LENGTH = "length"
        const val LEVELS_BY_DIFFICULTY = "difficulty"
        const val MAIN_DB_PATH = "storage/twisters_storage.json"

        //OTHER DB CONSTANTS
        const val TWISTER_COUNT = 440
        const val LENGTH_LEVEL_COUNT = 3
        const val DIFFICULTY_LEVEL_COUNT = 10

        //MIN AND MAX
        const val MIN_TWISTER_INDEX = 1
        const val MAX_TWISTER_INDEX = 440

        //UNIT VALUES
        const val UNIT_VALUE_INT = 1

        //COUNTERS
        const val RESET_CONSTANT_FOR_INTERSTITIAL_AD = 8
        
        //firebase (storage)
        const val firebaseStorageMediaQuery = "alt=media"
        const val firebaseStorageToken = "0e8a132e-85df-4016-b67a-af354bd8ab9c"
    }
}
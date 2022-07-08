package com.firstapp.robinpc.tongue_twisters_deluxe.di.module.others

import android.content.Context
import com.firstapp.robinpc.tongue_twisters_deluxe.di.qualifier.MainDatabaseString
import com.firstapp.robinpc.tongue_twisters_deluxe.di.scope.TwisterAppScope
import com.firstapp.robinpc.tongue_twisters_deluxe.utils.Constants
import dagger.Module
import dagger.Provides
import java.io.InputStream

//(includes = [ContextModule::class])
@Module
class JsonModule {

    @Provides
    @TwisterAppScope
    fun tagsDatabaseInputStream(@MainDatabaseString databaseFilePath: String, context: Context): InputStream {
        return context.assets.open(databaseFilePath)
    }

    @Provides
    @MainDatabaseString
    @TwisterAppScope
    fun mainDatabaseFilePath(): String {
        return Constants.MAIN_DB_PATH
    }
}
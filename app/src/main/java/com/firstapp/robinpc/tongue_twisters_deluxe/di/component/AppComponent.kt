package com.firstapp.robinpc.tongue_twisters_deluxe.di.component

import android.content.Context
import com.firstapp.robinpc.tongue_twisters_deluxe.TwisterApp
import com.firstapp.robinpc.tongue_twisters_deluxe.data.database.TwisterDatabase
import com.firstapp.robinpc.tongue_twisters_deluxe.di.module.activity.ActivityBindingModule
import com.firstapp.robinpc.tongue_twisters_deluxe.di.module.fragment.FragmentBindingModule
import com.firstapp.robinpc.tongue_twisters_deluxe.di.module.others.*
import com.firstapp.robinpc.tongue_twisters_deluxe.di.module.service.ServiceBindingModule
import com.firstapp.robinpc.tongue_twisters_deluxe.di.scope.TwisterAppScope
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.base.common_adapter.DiffUtilCallBack
import com.firstapp.robinpc.tongue_twisters_deluxe.utils.TwisterPreferences
import com.google.gson.Gson
import dagger.BindsInstance

import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import java.io.InputStream

@TwisterAppScope
@Component(modules = [
    ActivityBindingModule::class,
    FragmentBindingModule::class,
    AndroidInjectionModule::class,
    SharedPreferenceModule::class,
    AndroidSupportInjectionModule::class,
    DatabaseModule::class,
    JsonModule::class,
    ViewModelFactoryModule::class,
    ServiceBindingModule::class
])
interface AppComponent: AndroidInjector<TwisterApp> {

    fun getGson(): Gson
    fun getInputStream(): InputStream
    fun twisterPreferences(): TwisterPreferences
    fun getDatabase(): TwisterDatabase
    fun inject(diffUtilCallBack: DiffUtilCallBack)

    override fun inject(instance: TwisterApp)

    @Component.Builder
    interface Builder {
        fun build(): AppComponent

        @BindsInstance
        fun application(application: TwisterApp): Builder

        @BindsInstance
        fun context(context: Context): Builder
    }
}

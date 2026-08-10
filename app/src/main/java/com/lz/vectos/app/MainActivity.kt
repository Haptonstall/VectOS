package com.lz.vectos.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.lz.runtime.api.RuntimeEnvironment
import com.lz.vectos.app.navigation.AppNavGraph
import com.lz.vectos.app.platform.RuntimeInitializer
import com.lz.ui.theme.VectOSTheme
import dagger.hilt.android.AndroidEntryPoint

import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var runtime: RuntimeEnvironment

    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen()

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

            VectOSTheme {

                val navController =
                    rememberNavController()

                AppNavGraph(

                    runtime = runtime,

                    navController = navController

                )

            }

        }

    }

    override fun onDestroy() {

        super.onDestroy()

        /*
         * Eventually this will move to the Application
         * lifecycle when background execution is added.
         */
        RuntimeInitializer.shutdown()

    }

}
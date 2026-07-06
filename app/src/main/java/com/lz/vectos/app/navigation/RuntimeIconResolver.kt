package com.lz.vectos.app.navigation

import android.content.Context

object RuntimeIconResolver {

    fun resolve(

        context: Context,

        iconKey: String

    ): Int {

        return context.resources.getIdentifier(

            iconKey,

            "drawable",

            context.packageName

        )
    }
}
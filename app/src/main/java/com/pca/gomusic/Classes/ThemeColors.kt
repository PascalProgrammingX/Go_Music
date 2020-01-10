package com.pca.gomusic.Classes

import com.pca.gomusic.ModelClass.Theme

class ThemeColors {

    fun themeColors():ArrayList<Theme>{

        val colors = ArrayList<Theme>()
        colors.add(Theme(1))
        colors.add(Theme(2))

        return colors
    }
}
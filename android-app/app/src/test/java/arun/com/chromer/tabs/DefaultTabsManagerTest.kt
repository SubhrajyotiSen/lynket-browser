/*
 * Lynket
 *
 * Copyright (C) 2018 Arunkumar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.tabs

import android.content.Intent
import arun.com.chromer.ChromerRobolectricSuite
import arun.com.chromer.browsing.amp.AmpResolverActivity
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.home.HomeActivity
import arun.com.chromer.webheads.WebHeadService
import org.junit.Before
import org.junit.Test
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowApplication
import javax.inject.Inject


/**
 * Created by Arunkumar on 15-12-2017.
 */
class DefaultTabsManagerTest : ChromerRobolectricSuite() {
    @Inject
    lateinit var tabs: DefaultTabsManager

    private val url = "https://www.example.com"

    @Before
    fun setUp() {
        testAppComponent.inject(this)
    }

    @Test
    fun testInject() {
        assert(::tabs.isInitialized)
    }

    @Test
    fun testHomeActivityClearedOnExternalIntent() {
        clearPreferences()
        preferences.mergeTabs(false)
        preferences.webHeads(false)

        val homeActivity = Robolectric.buildActivity(HomeActivity::class.java).create().get()
        val homeActivityShadow = shadowOf(homeActivity)

        tabs.openUrl(application, Website(url), fromApp = false)
        assert(homeActivityShadow.isFinishing)
    }

    @Test
    fun testFromWebheadsDoesNotLaunchNewWebHead() {
        clearPreferences()
        preferences.webHeads(true)
        val shadowApp = Shadows.shadowOf(application)

        assertWebHeadServiceLaunched(shadowApp)

        tabs.openUrl(application, Website(url), fromApp = false, fromWebHeads = true)
        assert(shadowApp.nextStartedService == null)
    }

    @Test
    fun testAmpResolverOpened() {
        clearPreferences()
        preferences.ampMode(true)

        val shadowApp = Shadows.shadowOf(application)
        tabs.openUrl(application, Website(url), fromApp = false, fromWebHeads = false)
        assert(shadowApp.nextStartedActivity.component == Intent(application, AmpResolverActivity::class.java).component)
    }

    private fun assertWebHeadServiceLaunched(shadowApp: ShadowApplication) {
        tabs.openUrl(application, Website(url), fromApp = false, fromWebHeads = false)
        assert(shadowApp.peekNextStartedService().component == Intent(application, WebHeadService::class.java).component)
        assert(shadowApp.nextStartedService.dataString == url)
    }
}
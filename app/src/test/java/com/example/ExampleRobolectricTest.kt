package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.utils.BanglaNumberUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Bangladesh Laws", appName)
    }

    @Test
    fun `test bangla to english and english to bangla digit conversion`() {
        assertEquals("৩০২", BanglaNumberUtils.toBanglaDigits("302"))
        assertEquals("302", BanglaNumberUtils.toEnglishDigits("৩০২"))
        assertEquals("১৮৬০", BanglaNumberUtils.toBanglaDigits(1860))
        assertEquals("54", BanglaNumberUtils.toEnglishDigits("৫৪"))

        val queries = BanglaNumberUtils.getNormalizedQueries("302")
        assertTrue(queries.contains("302"))
        assertTrue(queries.contains("৩০২"))
    }
}

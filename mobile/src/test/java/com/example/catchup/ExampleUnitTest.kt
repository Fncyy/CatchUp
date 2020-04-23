package com.example.catchup

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}

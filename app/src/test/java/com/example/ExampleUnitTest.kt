package com.example

import com.example.ui.theme.CharcoalTone
import com.example.ui.theme.WorkstationBevelStyle
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun `verify workstation bevel styles and charcoal tone definitions`() {
    assertEquals(4, WorkstationBevelStyle.entries.size)
    assertTrue(WorkstationBevelStyle.entries.contains(WorkstationBevelStyle.RAISED))
    assertTrue(WorkstationBevelStyle.entries.contains(WorkstationBevelStyle.RECESSED))
    assertTrue(WorkstationBevelStyle.entries.contains(WorkstationBevelStyle.FLUSH))
    assertTrue(WorkstationBevelStyle.entries.contains(WorkstationBevelStyle.ENGRAVED))

    assertEquals(4, CharcoalTone.entries.size)
    assertNotNull(CharcoalTone.DEEP_VOID.topColor)
    assertNotNull(CharcoalTone.CHASSIS_SLATE.topColor)
    assertNotNull(CharcoalTone.PANEL_DARK.topColor)
    assertNotNull(CharcoalTone.ANODIZED_STEEL.topColor)
  }
}

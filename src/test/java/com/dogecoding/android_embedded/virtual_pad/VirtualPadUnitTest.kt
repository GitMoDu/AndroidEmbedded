package com.dogecoding.android_embedded.virtual_pad

import com.dogecoding.android_embedded.virtual_pad.model.MainButtonEnum
import com.dogecoding.android_embedded.virtual_pad.model.MenuButtonEnum
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class VirtualPadUnitTest {

    @Test
    fun writeAndReadBackButtonsTest() {
        assert(testWriteReadMenuButton(MenuButtonEnum.Start))
        assert(testWriteReadMenuButton(MenuButtonEnum.Select))
        assert(testWriteReadMenuButton(MenuButtonEnum.Home))
        assert(testWriteReadMenuButton(MenuButtonEnum.Share))

        assert(testWriteReadMainButton(MainButtonEnum.A))
        assert(testWriteReadMainButton(MainButtonEnum.B))
        assert(testWriteReadMainButton(MainButtonEnum.X))
        assert(testWriteReadMainButton(MainButtonEnum.Y))
        assert(testWriteReadMainButton(MainButtonEnum.L1))
        assert(testWriteReadMainButton(MainButtonEnum.R1))
        assert(testWriteReadMainButton(MainButtonEnum.L3))
        assert(testWriteReadMainButton(MainButtonEnum.R3))


    }

    private fun testWriteReadMainButton(button: MainButtonEnum): Boolean {
        val pad = WriteVirtualPad()

        when (button) {
            MainButtonEnum.A -> {
                if (pad.getA()) {
                    return false
                }
                pad.setA(true)
                if (!pad.getA()) {
                    return false
                }
                pad.setA(false)
                if (pad.getA()) {
                    return false
                }
            }

            MainButtonEnum.B -> {
                if (pad.getB()) {
                    return false
                }
                pad.setB(true)
                if (!pad.getB()) {
                    return false
                }
                pad.setB(false)
                if (pad.getB()) {
                    return false
                }
            }

            MainButtonEnum.X -> {
                if (pad.getX()) {
                    return false
                }
                pad.setX(true)
                if (!pad.getX()) {
                    return false
                }
                pad.setX(false)
                if (pad.getX()) {
                    return false
                }
            }

            MainButtonEnum.Y -> {
                if (pad.getY()) {
                    return false
                }
                pad.setY(true)
                if (!pad.getY()) {
                    return false
                }
                pad.setY(false)
                if (pad.getY()) {
                    return false
                }
            }

            MainButtonEnum.L1 -> {
                if (pad.getL1()) {
                    return false
                }
                pad.setL1(true)
                if (!pad.getL1()) {
                    return false
                }
                pad.setL1(false)
                if (pad.getL1()) {
                    return false
                }
            }

            MainButtonEnum.R1 -> {
                if (pad.getR1()) {
                    return false
                }
                pad.setR1(true)
                if (!pad.getR1()) {
                    return false
                }
                pad.setR1(false)
                if (pad.getR1()) {
                    return false
                }
            }

            MainButtonEnum.L3 -> {
                if (pad.getL3()) {
                    return false
                }
                pad.setL3(true)
                if (!pad.getL3()) {
                    return false
                }
                pad.setL3(false)
                if (pad.getL3()) {
                    return false
                }
            }

            MainButtonEnum.R3 -> {
                if (pad.getR3()) {
                    return false
                }
                pad.setR3(true)
                if (!pad.getR3()) {
                    return false
                }
                pad.setR3(false)
                if (pad.getR3()) {
                    return false
                }
            }

            else -> {
                return false
            }
        }

        return true
    }

    private fun testWriteReadMenuButton(button: MenuButtonEnum): Boolean {
        val pad = WriteVirtualPad()

        when (button) {
            MenuButtonEnum.Start -> {
                if (pad.getStart()) {
                    return false
                }
                pad.setStart(true)
                if (!pad.getStart()) {
                    return false
                }
                pad.setStart(false)
                if (pad.getStart()) {
                    return false
                }
            }

            MenuButtonEnum.Select -> {
                if (pad.getSelect()) {
                    return false
                }
                pad.setSelect(true)
                if (!pad.getSelect()) {
                    return false
                }
                pad.setSelect(false)
                if (pad.getSelect()) {
                    return false
                }
            }

            MenuButtonEnum.Home -> {
                if (pad.getHome()) {
                    return false
                }
                pad.setHome(true)
                if (!pad.getHome()) {
                    return false
                }
                pad.setHome(false)
                if (pad.getHome()) {
                    return false
                }
            }

            MenuButtonEnum.Share -> {
                if (pad.getShare()) {
                    return false
                }
                pad.setShare(true)
                if (!pad.getShare()) {
                    return false
                }
                pad.setShare(false)
                if (pad.getShare()) {
                    return false
                }
            }

            else -> {
                return false
            }
        }

        return true
    }
}
package cn.cheneya.skypvp.event.events

import cn.cheneya.skypvp.api.utils.client.misc.Nameable
import cn.cheneya.skypvp.api.utils.client.misc.WebSocketEvent
import cn.cheneya.skypvp.event.CancellableEvent
import cn.cheneya.skypvp.event.Event
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.util.InputUtil

@Nameable("windowResize")
class WindowResizeEvent(val width: Int, val height: Int) : Event()

@Nameable("frameBufferResize")
class FrameBufferResizeEvent(val width: Int, val height: Int) : Event()

@Nameable("mouseButton")
@WebSocketEvent
class MouseButtonEvent(
    val key: InputUtil.Key,
    val button: Int,
    val action: Int,
    val mods: Int,
    val screen: Screen? = null
) : Event()

@Nameable("mouseScroll")
class MouseScrollEvent(val horizontal: Double, val vertical: Double) : Event()

@Nameable("mouseScrollInHotbar")
class MouseScrollInHotbarEvent(val speed: Int) : CancellableEvent()

@Nameable("mouseCursor")
class MouseCursorEvent(val x: Double, val y: Double) : Event()

@Nameable("keyboardKey")
@WebSocketEvent
class KeyboardKeyEvent(
    val key: InputUtil.Key,
    val keyCode: Int,
    val scanCode: Int,
    val action: Int,
    val mods: Int,
    val screen: Screen? = null
) : Event()

@Nameable("keyboardChar")
@WebSocketEvent
class KeyboardCharEvent(val codePoint: Int, val modifiers: Int) : Event()

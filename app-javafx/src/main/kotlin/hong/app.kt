package hong

import tornadofx.*

fun main(vararg args: String) {
    launch<MainApp>(*args)
}

class MainApp : App(Main::class)

class Main : View() {
    override val root = hbox {
        label {
            text = "Hello World"
        }
    }
}
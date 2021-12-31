package xyz.theprogramsrc.dependencydownloadermodule

import xyz.theprogramsrc.simplecoreapi.global.module.Module

class Main: Module() {

    override fun onEnable() {
        DependencyDownloader()
    }
}
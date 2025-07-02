package blora.modules

import blora.exception.ModuleDuplicatedException
import blora.exception.ModuleNotFoundException

object ModuleManager {

    private fun enable_cleanNotAvailableModules(cleanedUpModules: MutableList<String>, modules: MutableList<String>, module: Module) {
        if (!modules.containsAll(module.dependencies)) {
            modules.remove(module.id)
            for (storedModule in this.modules) {
                if (module == storedModule.value || module.id == storedModule.key)
                    continue
                if (cleanedUpModules.contains(storedModule.key))
                    continue
                if (storedModule.value.dependencies.contains(module.id)) {
                    cleanedUpModules.add(storedModule.key)
                    enable_cleanNotAvailableModules(cleanedUpModules, modules, storedModule.value)
                }
            }
        }
    }

    fun enable() {
        val cleanedUpModules = mutableListOf<String>()
        val modulesToEnable = this.modules.values.map { it.id }.toMutableList()
        for (module in this.modules.values) {
            this.enable_cleanNotAvailableModules(cleanedUpModules, modulesToEnable, module)
        }

    }

    fun disable() {
        this.listModules().forEach(Module::disable)
    }

    internal val modules: MutableMap<String, Module> = mutableMapOf()

    fun <T: Module> registerModule(module: T) {
        if (modules.contains(module.id)) {
            throw ModuleDuplicatedException("Duplicated module: ${module.id}")
        }
        this.modules[module.id] = module
    }

    fun listModules(): List<Module> {
        return this.modules.values.toList()
    }

    inline fun <reified T: Module> module(id: String): T {
        for (module in this.listModules()) {
            if (module.id == id) {
                if (module !is T)
                    break
                return module
            }
        }
        throw ModuleNotFoundException("Module with id $id not found")
    }

}
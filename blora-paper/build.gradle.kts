plugins {
    alias(libs.plugins.paperweight.userdev)
}

dependencies {
    paperweight.paperDevBundle("1.21.8-R0.1-SNAPSHOT")
    compileOnly(libs.paper.api)
    compileOnly(libs.papi)
    compileOnly(libs.playerpoints)
    compileOnly(libs.craftengine.core)
    compileOnly(libs.craftengine.bukkit)
    compileOnly(libs.aurora.lib)
    compileOnly(libs.aurora.levels)
    compileOnly(libs.xconomy)
    compileOnly(files("libs/Vault-1.7.3.jar"))

    compileOnly(libs.kotlinx.coroutines)
    compileOnly(libs.krontab)

    compileOnly(libs.kotlinx.datetime)

    compileOnly(libs.kotlinx.serialization.core)
    compileOnly(libs.kotlinx.serialization.json)
    compileOnly(libs.tomlkt)
    compileOnly(libs.knbt)

    compileOnly(libs.exposed.core)
    compileOnly(libs.exposed.jdbc)
    compileOnly(libs.exposed.dao)
    compileOnly(libs.exposed.javatime)
    compileOnly(libs.exposed.json)
    compileOnly(libs.hikaricp)
    compileOnly(libs.postgresql)

    compileOnly(libs.adventure.nbt)
    compileOnly(libs.advkt)
}

tasks.assemble {
    dependsOn(tasks.reobfJar)
}
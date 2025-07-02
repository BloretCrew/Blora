plugins {
    alias(libs.plugins.paperweight.userdev)
}

dependencies {
    compileOnly(files("./ProtocolLib-5.4.0.jar"))
    paperweight.paperDevBundle("1.21.6-R0.1-SNAPSHOT")
    compileOnly(libs.paper.api)

    compileOnly(libs.kotlinx.coroutines)

    compileOnly(libs.kotlinx.serialization.core)
    compileOnly(libs.kotlinx.serialization.json)

    compileOnly(libs.exposed.core)
    compileOnly(libs.exposed.jdbc)

    compileOnly(libs.hikaricp)

    compileOnly(libs.tomlkt)
    compileOnly(libs.advkt)
}

tasks.assemble {
    dependsOn(tasks.reobfJar)
}
plugins {
    alias(libs.plugins.shadow)
}

dependencies {
    compileOnly(libs.velocity.api)
    compileOnly(libs.velocity.proxy)

    compileOnly(files("./libs/VPacketEvents-1.1.0.jar"))
    compileOnly(libs.netty)

    implementation(libs.kotlinx.coroutines)

    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.javatime)

    implementation(libs.hikaricp)
    implementation(libs.mariadb)

    implementation(libs.okhttp3)

    implementation(libs.tomlkt)
    implementation(libs.knbt)

    implementation(libs.minedown)
    implementation(libs.advkt)

    kapt(libs.velocity.api)
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.shadowJar {
    archiveClassifier.set("")
}
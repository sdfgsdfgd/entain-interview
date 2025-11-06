plugins {
    alias(libs.plugins.kotlinJvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(projects.core.common)
    implementation(libs.kotlinx.datetime)
    implementation(libs.coroutines.test)
    implementation(libs.turbine)
    implementation(libs.truth)
    implementation(libs.mockk)
    implementation(libs.junit)
}

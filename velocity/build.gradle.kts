plugins {
    alias(libs.plugins.blossom)
}

dependencies {
    compileOnly(libs.velocity.api)
    annotationProcessor(libs.velocity.api)
    compileOnly(libs.miniplaceholders)
    compileOnly(libs.luckperms)
    implementation(projects.common)
}

blossom {
    replaceTokenIn("src/main/java/io/github/miniplaceholders/expansion/luckperms/velocity/Constants.java")
    replaceToken("{version}", project.version)
}
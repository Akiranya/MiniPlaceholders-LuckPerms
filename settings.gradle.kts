enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
rootProject.name = "MiniPlaceholders-Luckperms"

arrayOf("paper", "velocity", "common").forEach {
    include("luckperms-expansion-$it")
    project(":luckperms-expansion-$it").projectDir = file(it)
}

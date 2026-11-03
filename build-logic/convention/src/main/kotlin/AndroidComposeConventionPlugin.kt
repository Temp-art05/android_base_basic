import com.android.build.api.dsl.CommonExtension
import com.base.app.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.kotlin.dsl.dependencies

/**
 * Áp lên module đã có base.android.library / base.android.application.
 * Gom toàn bộ dependency Compose dùng chung về một chỗ.
 */
class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        val extension = extensions.getByType(CommonExtension::class.java)
        extension.buildFeatures.compose = true

        addComposeDependencies(libs)
    }

    private fun Project.addComposeDependencies(libs: VersionCatalog) = dependencies {
        val bom = platform(libs.findLibrary("androidx-compose-bom").get())
        add("implementation", bom)
        add("androidTestImplementation", bom)

        add("implementation", libs.findLibrary("androidx-compose-ui").get())
        add("implementation", libs.findLibrary("androidx-compose-ui-graphics").get())
        add("implementation", libs.findLibrary("androidx-compose-ui-tooling-preview").get())
        add("implementation", libs.findLibrary("androidx-compose-material3").get())
        add("implementation", libs.findLibrary("androidx-compose-foundation").get())
        add("implementation", libs.findLibrary("androidx-compose-runtime").get())
        add("debugImplementation", libs.findLibrary("androidx-compose-ui-tooling").get())
    }
}

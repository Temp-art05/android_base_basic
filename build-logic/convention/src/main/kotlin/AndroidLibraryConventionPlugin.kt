import com.android.build.api.dsl.LibraryExtension
import com.base.app.convention.configureAndroidCommon
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")

        extensions.configure<LibraryExtension> {
            configureAndroidCommon(this)

            defaultConfig.apply {
                consumerProguardFiles("consumer-rules.pro")
            }

            buildTypes {
                release {
                    // Library không tự minify — app module lo việc đó.
                    isMinifyEnabled = false
                }
            }
        }
    }
}

package com.base.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * `domain`, `data`, `feature` là package trong `:app`, nên Gradle không chặn import chéo tầng như khi còn
 * là module riêng. Test này thay Gradle giữ chiều phụ thuộc `presentation → domain ← data`:
 * - `domain` thuần Kotlin: không Android/Compose, không biết `data`/`feature`.
 * - `feature` không import `data` (chỉ thấy interface ở `domain`), feature không import feature khác.
 * - `data` không import `feature`.
 */
class ArchitectureTest {

    private val root = File("src/main/java/com/base/app")

    @Test
    fun `domain is pure Kotlin and knows no other layer`() {
        assertNoImports(
            layer = "domain",
            forbidden = listOf("android.", "androidx.", "com.base.app.data.", "com.base.app.feature.", "com.base.app.core."),
            allowed = listOf("com.base.app.core.utils.error.", "com.base.app.core.utils.config."),
        )
    }

    @Test
    fun `feature does not import data`() {
        assertNoImports(layer = "feature", forbidden = listOf("com.base.app.data."))
    }

    @Test
    fun `data does not import feature`() {
        assertNoImports(layer = "data", forbidden = listOf("com.base.app.feature."))
    }

    @Test
    fun `features do not import each other`() {
        val features = File(root, "feature").listFiles { file -> file.isDirectory }.orEmpty().map { it.name }
        val violations = features.flatMap { feature ->
            val others = (features - feature).map { "com.base.app.feature.$it." }
            importsIn(File(root, "feature/$feature")).filter { (_, import) -> others.any(import::startsWith) }
        }
        assertTrue(violations.report("feature imports another feature"), violations.isEmpty())
    }

    private fun assertNoImports(layer: String, forbidden: List<String>, allowed: List<String> = emptyList()) {
        val dir = File(root, layer)
        assertTrue("Missing $dir — test must run with :app as working directory", dir.isDirectory)
        val violations = importsIn(dir).filter { (_, import) ->
            forbidden.any(import::startsWith) && allowed.none(import::startsWith)
        }
        assertTrue(violations.report("$layer imports a forbidden layer"), violations.isEmpty())
    }

    private fun importsIn(dir: File): List<Pair<String, String>> =
        dir.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .flatMap { file ->
                file.readLines()
                    .filter { it.startsWith("import ") }
                    .map { file.relativeTo(root).path to it.removePrefix("import ").trim() }
            }
            .toList()

    private fun List<Pair<String, String>>.report(title: String) =
        "$title:\n" + joinToString("\n") { (file, import) -> "  $file → $import" }
}

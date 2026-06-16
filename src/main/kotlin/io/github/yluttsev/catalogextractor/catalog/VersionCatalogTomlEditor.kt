package io.github.yluttsev.catalogextractor.catalog

import io.github.yluttsev.catalogextractor.model.DependencyInfo

object VersionCatalogTomlEditor {

    private const val LIBRARIES_SECTION = "libraries"
    private const val VERSIONS_SECTION = "versions"

    private val SECTION_HEADER_REGEX = Regex("""^\s*\[[^\]]+]\s*$""")
    private val LIBRARY_ENTRY_REGEX = Regex("""^\s*([^\s=]+)\s*=\s*\{(.*)}\s*(?:#.*)?$""")
    private val INLINE_TABLE_FIELD_REGEX = Regex("""([\w.-]+)\s*=\s*"([^"]*)"""")

    private val EMPTY_TOML = """
        [libraries]

        [plugins]

        [versions]

    """.trimIndent()

    fun findExistingAlias(content: String, module: String): String? {
        val (group, name) = module.split(':', limit = 2).takeIf { it.size == 2 } ?: return null
        val librariesContent = extractSection(content, LIBRARIES_SECTION) ?: return null

        return LIBRARY_ENTRY_REGEX.findAll(librariesContent)
            .firstOrNull { match ->
                val fields = parseInlineTableFields(match.groupValues[2])
                fields["module"] == module || (fields["group"] == group && fields["name"] == name)
            }
            ?.groupValues
            ?.get(1)
    }

    fun getAllAliases(content: String): Set<String> {
        val librariesContent = extractSection(content, LIBRARIES_SECTION) ?: return emptySet()
        val regex = Regex("""^(\S+)\s*=""", RegexOption.MULTILINE)
        return regex.findAll(librariesContent).map { it.groupValues[1] }.toSet()
    }

    fun addEntry(content: String, alias: String, info: DependencyInfo): String {
        val libraryLine = """$alias = { module = "${info.module}", version.ref = "$alias" }"""
        val versionLine = """$alias = "${info.version}""""

        val withRequiredSections = ensureSections(content, listOf(LIBRARIES_SECTION, VERSIONS_SECTION))
        val withLibrary = insertIntoSection(withRequiredSections, LIBRARIES_SECTION, libraryLine)
        return insertIntoSection(withLibrary, VERSIONS_SECTION, versionLine)
    }

    fun emptyToml(): String = EMPTY_TOML

    private fun extractSection(content: String, section: String): String? {
        val lines = content.lines()
        val tomlSection = findSection(lines, section) ?: return null
        return lines.subList(tomlSection.startLine + 1, tomlSection.endLineExclusive).joinToString("\n")
    }

    private fun insertIntoSection(content: String, section: String, line: String): String {
        val lines = content.lines().toMutableList()
        val tomlSection = findSection(lines, section) ?: return content

        var pos = tomlSection.endLineExclusive
        while (pos > tomlSection.startLine + 1 && lines[pos - 1].isBlank()) pos--

        lines.add(pos, line)
        return lines.joinToString("\n")
    }

    private fun ensureSections(content: String, sections: List<String>): String {
        val lines = content.lines().toMutableList()

        sections.forEach { section ->
            if (findSection(lines, section) == null) {
                if (lines.isNotEmpty() && lines.last().isNotBlank()) {
                    lines.add("")
                }
                lines.add("[$section]")
                lines.add("")
            }
        }

        return lines.joinToString("\n")
    }

    private fun findSection(lines: List<String>, section: String): TomlSection? {
        val startLine = lines.indexOfFirst { it.trim() == "[$section]" }
        if (startLine == -1) return null

        val nextSectionOffset = lines.drop(startLine + 1).indexOfFirst { SECTION_HEADER_REGEX.matches(it) }
        val endLineExclusive = if (nextSectionOffset == -1) {
            lines.size
        } else {
            startLine + 1 + nextSectionOffset
        }

        return TomlSection(startLine, endLineExclusive)
    }

    private fun parseInlineTableFields(body: String): Map<String, String> =
        INLINE_TABLE_FIELD_REGEX.findAll(body).associate { match ->
            match.groupValues[1] to match.groupValues[2]
        }

    private data class TomlSection(
        val startLine: Int,
        val endLineExclusive: Int
    )
}

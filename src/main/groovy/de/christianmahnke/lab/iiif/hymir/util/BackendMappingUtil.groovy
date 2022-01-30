/**
 * IIIF Image Services
 * Copyright (C) 2022  Christian Mahnke
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.christianmahnke.lab.iiif.hymir.util

import de.digitalcollections.commons.file.backend.api.IdentifierToFileResourceUriResolver
import de.digitalcollections.commons.file.backend.impl.IdentifierPatternToFileResourceUriResolverImpl
import de.digitalcollections.commons.file.backend.impl.IdentifierPatternToFileResourceUriResolvingConfig
import de.digitalcollections.commons.file.config.SpringConfigCommonsFile
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Service

@CompileStatic
@TypeChecked
@Service
@Import(SpringConfigCommonsFile.class)
class BackendMappingUtil {

    IdentifierPatternToFileResourceUriResolvingConfig resourceRepository

    BackendMappingUtil(@Autowired IdentifierPatternToFileResourceUriResolvingConfig resourceRepository) {
        this.resourceRepository = resourceRepository
    }

    Map<String, List<String>> getMappings() {
        Map<String, List<String>> mappings = new HashMap<String, List<String>>()
        for (IdentifierToFileResourceUriResolver resolver : resourceRepository.getPatterns()) {
            if (resolver instanceof IdentifierPatternToFileResourceUriResolverImpl) {
                String pattern = ((IdentifierPatternToFileResourceUriResolverImpl) resolver).getPattern()
                List<String> substitutions = ((IdentifierPatternToFileResourceUriResolverImpl) resolver).getSubstitutions()
                substitutions = substitutions.findAll { String str -> str.startsWith("http") }
                mappings.put(pattern, substitutions)
            } else {
                mappings.put(resolver.toString(), null)
            }
        }
        mappings
    }

    Map<String, String> mappingPatterns(String newPrefix) {
        Map<String, String> patterns = new HashMap<String, String>()
        getMappings().each { pattern, replacements ->
            String suffix = pattern
            if (suffix.startsWith("^")) {
                suffix = suffix.substring(1)
            }
            if (suffix.endsWith('$')) {
                suffix = suffix[0..-2]
            }

            if (!suffix.startsWith("(")) {
                suffix = "(" + suffix + ")"
                for (String resolved : replacements) {
                    resolved.replaceAll(/(\$\d+)/) { String[] it -> '$' + ((it[0]).substring(1).toInteger() + 1).toString() }
                }
            }

            for (String resolved : replacements) {
                def parts = resolved =~ /^(?<base>.*)(\$\d+)(?<rest>.*?)$/
                parts.matches()
                resolved = parts.group("base")
                String rest = parts.group("rest")
                patterns.put(resolved + suffix, newPrefix + '$1')
            }
        }
        patterns
    }

    /*
    public Map<String, List<String>> getPrefixedPatterns(String newPrefix) {
        Map<String, List<String>> patterns = new HashMap<String, List<String>>()
        getMappings().each { pattern, replacements ->
            replacements.eachWithIndex { String resolved, int i ->

                String prefix = resolved.substring(0, resolved.indexOf('$1'))
                String suffix = pattern
                if (suffix.startsWith("^")) {
                    suffix = suffix.substring(1)
                }
                if (suffix.endsWith('$')) {
                    suffix = suffix[0..-2]
                }
                if (!suffix.startsWith("(")) {
                    suffix = "(" + suffix + ")"
                    resolved.replaceAll(/(\$\d+)/) { String[] it -> '$' + ((it[0]).substring(1).toInteger() + 1).toString() }
                    replacements.set(i, resolved)
                }

                patterns.put(prefix + suffix, replacements)
            }
        }
        return patterns
    }
    */
}

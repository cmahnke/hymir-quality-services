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
package de.christianmahnke.lab.json

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import groovy.yaml.YamlSlurper
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MutablePropertySources
import org.springframework.core.env.PropertiesPropertySource
import org.springframework.core.env.PropertySource
import org.springframework.core.io.ByteArrayResource
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.util.ResourceUtils

import java.nio.charset.StandardCharsets

@Slf4j
@TypeChecked
@Import(ManifestTestBase.DynamicRulesPropertySourceConfig.class)
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ContextConfiguration
@ActiveProfiles(profiles = "test,plugins")
class ManifestTestBase {
    static List<File> files = new ArrayList<File>()
    static String defaultCharset = StandardCharsets.UTF_8
    static List<String> identifiers = ['record_DE-MUS-062622_kenom_127703/record_DE-MUS-062622_kenom_127703.json', 'DE-611-HS-3461927/DE-611-HS-3461927.json']
    static String base = 'data/'

    List<Map<String, ?>> patterns
    Multimap<String, String> substitutions

    protected Multimap<String, String> extractPatterns(List<Map<String, ?>> patterns) {
        Multimap<String, String> substitutions = ArrayListMultimap.create()
        for (pattern in patterns) {
            String from = pattern.get("pattern")
            for (String subst in pattern.get("substitutions")) {
                substitutions.put(from, subst)
            }
        }
        return substitutions
    }

    static PropertySource<?> rulesYml(String name = 'rules.yml') {
        String ident = '  '
        def rulesYml = new StringBuilder()
        rulesYml.append("resourceRepository:\n${ident}resolved:\n${ident}${ident}patterns:\n")
        for (identifier in identifiers) {
            rulesYml.append("${ident}${ident}${ident}- pattern: ${identifier}\n")
            rulesYml.append("${ident}${ident}${ident}  substitutions:\n")
            String abs = ResourceUtils.getFile("classpath:${base}${identifier}").getAbsolutePath()
            rulesYml.append("${ident}${ident}${ident}${ident}- ${abs}\n")
        }

        rulesYml.append("spring:\n")
        rulesYml.append("${ident}config:\n")
        rulesYml.append("${ident}${ident}activate:\n")
        rulesYml.append("${ident}${ident}${ident}on-profile: test\n")

        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean()
        factory.setResources(new ByteArrayResource(rulesYml.toString().getBytes(defaultCharset)))
        Properties properties = factory.getObject()
        return new PropertiesPropertySource(name, properties)
    }

    @BeforeEach
    void setup() {
        for (identifier in identifiers) {
            files.add(ResourceUtils.getFile("classpath:${base}${identifier}"))
        }
        Map<String, Map<String, Map<String, ?>>> rules = new YamlSlurper().parse(ResourceUtils.getFile("classpath:rules.yml")) as Map<String, Map<String, Map<String, ?>>>
        patterns = rules.get("resourceRepository").get("resolved").get("patterns") as List<Map<String, ?>>
        substitutions = extractPatterns(patterns)
    }

    @Configuration
    static class DynamicRulesPropertySourceConfig {
        @Autowired
        private ConfigurableEnvironment env

        @Bean
        PropertySource dynamicRilesPropertySource() {
            def ret = rulesYml('generated-rules.yml')
            MutablePropertySources sources = env.getPropertySources()
            sources.addFirst(ret)
            return ret
        }
    }
}

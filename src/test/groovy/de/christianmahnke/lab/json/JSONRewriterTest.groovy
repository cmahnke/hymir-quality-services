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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectReader
import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import de.digitalcollections.iiif.model.jackson.IiifObjectMapper
import de.digitalcollections.iiif.model.sharedcanvas.Manifest
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import groovy.yaml.YamlSlurper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.util.ResourceUtils

import java.nio.charset.StandardCharsets

import static org.junit.jupiter.api.Assertions.assertNotNull

@Slf4j
@TypeChecked
class JSONRewriterTest {

    static List<File> files = new ArrayList<File>()
    String defaultCharset = StandardCharsets.UTF_8
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

    @BeforeEach
    void setup() {
        files[0] = ResourceUtils.getFile("classpath:data/record_DE-MUS-062622_kenom_127703/record_DE-MUS-062622_kenom_127703.json")
        files[1] = ResourceUtils.getFile("classpath:data/DE-611-HS-3461927/DE-611-HS-3461927.json")
        Map<String, Map<String, Map<String, ?>>> rules = new YamlSlurper().parse(ResourceUtils.getFile("classpath:rules.yml")) as Map<String, Map<String, Map<String, ?>>>
        patterns = rules.get("resourceRepository").get("resolved").get("patterns") as List<Map<String, ?>>
        substitutions = extractPatterns(patterns)
    }

    @Test
    void testRewriteURL() {
        List<JSONRewriteOperation> ops = new ArrayList<>()
        substitutions.forEach (from, to) -> {
            IIIFURLRewriteOperation iuro = new IIIFURLRewriteOperation(from, to)
            ops.add(iuro)
        }
        JSONRewriter jr = new JSONRewriter(ops)
        files.each (f) -> {
            InputStream result = jr.rewrite(new FileInputStream(f))
            assertNotNull(result)
        }
    }

    @Test
    void testIIIFApiLoad() {
        files.forEach (file) -> {
            ObjectMapper mapper = new IiifObjectMapper()
            ObjectReader r = mapper.readerFor(Manifest.class)
            //def content = file.getText()
            InputStream is = new FileInputStream(file)
            if (!file.getAbsolutePath().contains('record')) {
                Manifest manifest = r.readValue(is)
                assertNotNull(manifest)
            }
        }
    }

    @Test
    void testAttributionFix() {
        files.forEach (file) -> {

            List<JSONRewriteOperation> ops = new ArrayList<>()
            ops.add(new GoobiViewerLanguageRewriteOperation())
            ops.add(new DeletePathRewriteOperation('$.service.label'))
            JSONRewriter jr = new JSONRewriter(ops)
            InputStream result = jr.rewrite(new FileInputStream(file))

            ObjectMapper mapper = new IiifObjectMapper()
            ObjectReader r = mapper.readerFor(Manifest.class)

            log.debug("Content:\n ${new String(result.readAllBytes(), defaultCharset)}")
            result.reset()

            Manifest manifest = r.readValue(result)
            assertNotNull(manifest)
        }
    }

    @Test
    void testRewriteAndLoad() {
        files.forEach (file) -> {
            List<JSONRewriteOperation> ops = new ArrayList<>()
            ops.add(new GoobiViewerLanguageRewriteOperation())
            ops.add(new DeletePathRewriteOperation('$.service.label'))
            substitutions.forEach (from, to) -> {
                IIIFURLRewriteOperation iuro = new IIIFURLRewriteOperation(from, to)
                ops.add(iuro)
            }
            JSONRewriter jr = new JSONRewriter(ops)
            InputStream result = jr.rewrite(new FileInputStream(file))
            ObjectMapper mapper = new IiifObjectMapper()
            ObjectReader r = mapper.readerFor(Manifest.class)
            Manifest manifest = r.readValue(result)
            assertNotNull(manifest)
        }
    }

}

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

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectReader
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.Option
import com.jayway.jsonpath.TypeRef
import de.digitalcollections.iiif.model.jackson.IiifObjectMapper
import de.digitalcollections.iiif.model.sharedcanvas.Manifest
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import net.sf.saxon.s9api.*
import org.junit.jupiter.api.Test
import org.springframework.util.ResourceUtils

import javax.xml.transform.stream.StreamSource

import static org.junit.jupiter.api.Assertions.assertNotNull

@Slf4j
@TypeChecked
class JSONRewriterTest extends ManifestTestBase {

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
            result.reset()
            log.debug("Content:\n ${new String(result.readAllBytes(), defaultCharset)}")
            assertNotNull(manifest)
        }
    }


    @Test
    void testSeeAlso() {
        JSONRewriter.setupJSONPath()
        Configuration pathConf = Configuration.builder().options(Option.AS_PATH_LIST).build()
        def transformation = ResourceUtils.getFile("classpath:xslt/lido2json.xsl")
        files.forEach (file) -> {
            List<JSONRewriteOperation> ops = new ArrayList<>()
            ops.add(new GoobiViewerLanguageRewriteOperation())
            ops.add(new DeletePathRewriteOperation('$.service.label'))
            ops.add(new XSLTSeeAlsoRewriteOperation('LIDO', new FileInputStream(transformation)))
            JSONRewriter jr = new JSONRewriter(ops)
            InputStream result = jr.rewrite(new FileInputStream(file))
            ObjectMapper mapper = new IiifObjectMapper()
            ObjectReader r = mapper.readerFor(Manifest.class)
            Manifest manifest = r.readValue(result)
            result.reset()
            log.debug("Content:\n ${new String(result.readAllBytes(), defaultCharset)}")
            assertNotNull(manifest)
        }
    }

}

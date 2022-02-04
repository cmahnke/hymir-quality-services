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
package de.christianmahnke.lab.iiif.hymir.presentation

import com.fasterxml.jackson.databind.ObjectReader
import de.christianmahnke.lab.iiif.hymir.ProxyIntrospectionController
import de.christianmahnke.lab.iiif.hymir.util.BackendMappingUtil
import de.christianmahnke.lab.json.*
import de.christianmahnke.lab.spring.YamlPropertySourceFactory
import de.digitalcollections.iiif.model.jackson.IiifObjectMapper
import de.digitalcollections.iiif.model.sharedcanvas.Manifest
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.PropertySource
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles

import static org.junit.jupiter.api.Assertions.assertNotNull

@TypeChecked
@Slf4j
@ActiveProfiles("plugins,test")
@SpringBootTest
@EnableAutoConfiguration
@ComponentScan(basePackages = ['de.christianmahnke.lab.iiif.hymir', 'de.digitalcollections.commons.file', 'de.digitalcollections.iiif.model'])
@PropertySource(value = ['classpath:rules.yml', 'classpath:application.yml'], factory = YamlPropertySourceFactory)
class RewritingPresentationRepositoryTest extends ManifestTestBase {

    @Autowired
    BackendMappingUtil bmu

    @Autowired
    RewritingPresentationRepository rpr

    IiifObjectMapper objectMapper

    //TODO :Remove me
    @Autowired
    ApplicationContext context

    MockHttpServletRequest request
    String newPrefix

    @BeforeEach
    void mockRequest() {
        request = new MockHttpServletRequest()
        request.setServerName("localhost")
        request.setRequestURI(ProxyIntrospectionController.iiifImageApiUrlPrefix)
        request.setScheme("http")
        newPrefix = ProxyIntrospectionController.getUrlBase(request)
    }

    @BeforeEach
    void getMapper() {
        this.objectMapper = rpr.objectMapper
    }

    @Test
    void testRewriteURLAndLoad() {
        files.forEach (file) -> {
            List<JSONRewriteOperation> ops = new ArrayList<>()
            ops.add(new GoobiViewerLanguageRewriteOperation())
            ops.add(new DeletePathRewriteOperation('$.service.label'))
            IIIFURLRewriteOperation iuro = new IIIFURLRewriteOperation(bmu.mappingPatterns(newPrefix))
            JSONRewriter jr = new JSONRewriter(ops)
            InputStream result = jr.rewrite(new FileInputStream(file))
            ObjectReader r = objectMapper.readerFor(Manifest.class)
            Manifest manifest = r.readValue(result)
            log.debug("Result ist:\n${objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(manifest)}")
            assertNotNull(result)
            assertNotNull(manifest)
        }
    }

    @Test
    @DirtiesContext
    @Disabled
    void testRepositoryLoad() {
        log.debug("Using generated rules.yml file:\n" + rulesYml().toString())
        for (String identifier in identifiers) {
            Manifest manifest = rpr.getManifest(identifier)
        }

    }
}

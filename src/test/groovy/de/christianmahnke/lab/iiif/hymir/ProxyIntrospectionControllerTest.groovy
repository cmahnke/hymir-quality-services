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
package de.christianmahnke.lab.iiif.hymir

import de.christianmahnke.lab.spring.YamlPropertySourceFactory
import groovy.json.JsonSlurper
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.PropertySource
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

import static org.junit.jupiter.api.Assertions.*

@TypeChecked
@Slf4j
@ActiveProfiles("plugins,test")
@WebMvcTest
@ComponentScan(basePackages = ['de.christianmahnke.lab.iiif.hymir', 'de.digitalcollections.commons.file'])
@PropertySource(value = ['classpath:rules.yml', 'classpath:application.yml'], factory = YamlPropertySourceFactory)

class ProxyIntrospectionControllerTest {

    @Autowired
    private MockMvc mockMvc

    protected ScriptEngine getEngine() {
        ScriptEngineManager factory = new ScriptEngineManager()
        return factory.getEngineByName("javascript")
    }

    @Test
    void testJS() {
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.get("/mappings/js")).andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
        String js = result.getResponse().getContentAsString()
        ScriptEngine engine = getEngine()
        log.debug("Result is:\n${js}")

        try {
            engine.eval(js)
            assertTrue(true)
        } catch (final ScriptException se) {
            fail(se)
        }
    }

    @Test
    void testJSON() {
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.get("/mappings/json")).andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
        String json = result.getResponse().getContentAsString()

        log.debug("Result is:\n${json}")

        def slurper = new JsonSlurper()
        Object object = slurper.parseText(json)

        assertTrue(object instanceof Map)
        assertNotNull(object)
    }
}

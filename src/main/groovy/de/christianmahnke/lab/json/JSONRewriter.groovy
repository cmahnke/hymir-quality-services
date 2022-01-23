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

import com.jayway.jsonpath.*
import com.jayway.jsonpath.spi.json.JacksonJsonProvider
import com.jayway.jsonpath.spi.json.JsonProvider
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider
import com.jayway.jsonpath.spi.mapper.MappingException
import com.jayway.jsonpath.spi.mapper.MappingProvider
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j

import java.nio.charset.StandardCharsets

@Slf4j
@TypeChecked
@CompileStatic
class JSONRewriter {
    List<JSONRewriteOperation> operations = new ArrayList<JSONRewriteOperation>()
    String defaultCharset = StandardCharsets.UTF_8
    private TypeRef<List<String>> typeRef = new TypeRef<List<String>>() {}
    private Configuration pathConf = Configuration.builder().options(Option.AS_PATH_LIST).build()

    static {
        Configuration.setDefaults(new Configuration.Defaults() {

            private final JsonProvider jsonProvider = new JacksonJsonProvider()
            private final MappingProvider mappingProvider = new JacksonMappingProvider()

            @Override
            JsonProvider jsonProvider() {
                return jsonProvider
            }

            @Override
            MappingProvider mappingProvider() {
                return mappingProvider
            }

            @Override
            Set<Option> options() {
                return EnumSet.noneOf(Option.class)
            }
        })
    }

    private DocumentContext pathCtx, valueCtx

    JSONRewriter(JSONRewriteOperation operation) {
        operations.add(operation)
    }

    JSONRewriter(List<JSONRewriteOperation> operations) {
        if (operations != null) {
            this.operations = operations
        }
    }

    InputStream rewrite(InputStream json) {
        DocumentContext pathCtx, valueCtx
        ByteArrayInputStream bais = new ByteArrayInputStream(json.getBytes())
        // Path context is only used for queries
        pathCtx = JsonPath.using(pathConf).parse(bais)
        //use this context for updates and deletes
        bais.reset()
        valueCtx = JsonPath.parse(bais)

        for (JSONRewriteOperation op in operations) {
            for (qPath in op.path()) {
                List<String> paths
                try {
                    paths = pathCtx.read(qPath, typeRef)
                } catch (com.jayway.jsonpath.PathNotFoundException e) {
                    //Path ot found, nothing to do
                    log.trace("${qPath} not found", e)
                    continue
                }

                for (String path : paths) {
                    Object oldValue
                    try {
                        oldValue = valueCtx.read(path, op.typeHint())
                    } catch (MappingException e) {
                        //Type mismatch, nothing to do
                        log.trace("${qPath} doesn't return ${op.typeHint()}", e)
                        continue
                    }

                    Object newValue = op.rewrite(path, oldValue)
                    if (newValue == null) {
                        valueCtx.delete(path)
                    } else if (newValue instanceof String) {
                        valueCtx.set(path, newValue)
                    } else {
                        valueCtx.set(path, newValue)
                    }
                }
            }
        }
        return new ByteArrayInputStream(valueCtx.jsonString().getBytes())

    }

    String rewrite(String json) {
        InputStream is = new ByteArrayInputStream(json.getBytes())
        return new String(rewrite(is).readAllBytes(), defaultCharset)
        return new ByteArrayInputStream(json.getBytes())
    }


}

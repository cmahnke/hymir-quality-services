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

import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.TypeRef
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import net.sf.saxon.s9api.*

import javax.xml.transform.stream.StreamSource

@TypeChecked
@CompileStatic
@Slf4j
class XSLTSeeAlsoRewriteOperation implements JSONRewriteOperation {
    List<String> jsonPath = Arrays.asList('$')
    String label
    InputStream transformation

    XSLTSeeAlsoRewriteOperation(String label, InputStream transformation) {
        this.label = label
        this.transformation = transformation
    }

    @Override
    List<String> path() {
        return jsonPath
    }

    @Override
    Object rewrite(String path, Object value) {
        def root = value as Map<String, Object>
        def seeAlso = root.get('seeAlso')

        URL url
        if (seeAlso instanceof Map) {
            if (seeAlso.get('label').equals(label) && seeAlso.get('format').equals('text/xml')) {
                url = new URL(seeAlso.get('@id') as String)
            }
        } else if (seeAlso instanceof List) {
            for (entry in seeAlso as List<Map>) {
                if (entry.containsKey('label') && entry.get('label').equals(label) && entry.get('format').equals('text/xml')) {
                    url = new URL(entry.get('@id') as String)
                }
            }
        } else {
            throw new IllegalStateException('List of seeAlso references not implemented')
        }
        if (url == null) {
            log.info("JSON fragments contain LIDO reference")
            return root
        }
        log.debug("Downloading and transforming ${url}")
        InputStream xml = url.openStream()
        Processor processor = new Processor(false)
        XsltCompiler compiler = processor.newXsltCompiler()
        XsltExecutable stylesheet = compiler.compile(new StreamSource(transformation))
        Xslt30Transformer transformer = stylesheet.load30()
        Serializer dest = processor.newSerializer()
        StringWriter sw = new StringWriter()
        dest.setOutputWriter(sw)
        transformer.transform(new StreamSource(xml), dest)
        String result = sw.toString()
        if (result != "") {
            def parser = JsonPath.parse(result)
            def newValues = parser.read('$', new TypeRef<List<Map<String, Object>>>() {})
            ((List) root.get('metadata')).addAll(newValues)
        }
        return root
    }

    @Override
    TypeRef typeHint() {
        return new TypeRef<Map<String, Object>>() {}
    }

}

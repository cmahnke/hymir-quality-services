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

import de.christianmahnke.lab.iiif.hymir.util.BackendMappingUtil
import de.digitalcollections.iiif.hymir.model.api.HymirPlugin
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping(value = "/mappings")
@CompileStatic
@TypeChecked
class ProxyIntrospectionController implements HymirPlugin {

    @Value('${custom.iiif.image.urlPrefix:/image/v2/}')
    String iiifImageApiUrlPrefix

    @Autowired
    BackendMappingUtil bmu

    // Taken from de.digitalcollections.iiif.hymir.image.frontend.IIIFImageApiController - why such a method is private is beyond me
    public static String getUrlBase(HttpServletRequest request) {
        String scheme = request.getHeader("X-Forwarded-Proto")
        if (scheme == null) {
            scheme = request.getScheme()
        }

        String host = request.getHeader("X-Forwarded-Host")
        if (host == null) {
            host = request.getHeader("Host")
        }
        if (host == null) {
            host = request.getRemoteHost()
        }
        if (host == null) {
            host = request.getRemoteAddr()
        }
        String base = String.format("%s://%s", scheme, host)
        if (!request.getContextPath().isEmpty()) {
            base += request.getContextPath()
        }
        return base
    }

    @CrossOrigin
    @RequestMapping(value = "/json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map> listBackendsJSON() {
        return new ResponseEntity<Map>(bmu.getMappings(), HttpStatus.OK)
    }

    @RequestMapping(value = "/json/patterns", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map> listPatternsJSON(HttpServletRequest request) {
        String newPrefix = getUrlBase(request) + iiifImageApiUrlPrefix
        return new ResponseEntity<Map>(bmu.mappingPatterns(newPrefix), HttpStatus.OK)
    }

    @RequestMapping(value = "/js", method = RequestMethod.GET, produces = "text/javascript")
    @CrossOrigin
    ResponseEntity<String> listBackendsJS(HttpServletRequest request) {
        String newPrefix = getUrlBase(request) + iiifImageApiUrlPrefix
        StringBuilder js = new StringBuilder()
        js.append("function rewriteURL(url) {\n")
        bmu.mappingPatterns(newPrefix).each { from, to ->
            from = from.replace('/', '\\/')
            // We could check for '/info.json' suffix here but then this wouldn't be API agnostic anymore
            js.append("\t" + '// url = url.replace(/' + from + '/mg, "' + to + '");' + "\n")
            js.append("\t" + 'if (url.match(/' + from + '/mg)) {' + "\n")
            js.append("\t\t" + 'return url.replace(/' + from + '/mg, "' + to + '");' + "\n")
            js.append("\t}\n")
        }
        js.append("\treturn url;\n")
        js.append("}\n")
        return new ResponseEntity<String>(js.toString(), HttpStatus.OK);
    }

    @Override
    String name() {
        return "Mapping generator"
    }
}

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

import com.jayway.jsonpath.TypeRef
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

import java.util.regex.Pattern

@TypeChecked
@CompileStatic
class RegexRewriteOperation implements JSONRewriteOperation {
    List<String> jsonPath = ['']
    Pattern from
    String to

    protected RegexRewriteOperation(String from, String to) {
        this.from = Pattern.compile(from)
        this.to = to
    }

    RegexRewriteOperation(String jsonPath, String from, String to) {
        this(from, to)
        this.jsonPath = Arrays.asList(jsonPath)
    }

    @Override
    List<String> path() {
        return jsonPath
    }

    @Override
    Object rewrite(String path, Object value) {
        if (value instanceof String) {
            return ((String) value).replaceAll(from, to)
        }
        throw new IllegalStateException(this.getClass().getSimpleName() + ' can only replace string values')
    }

    @Override
    TypeRef typeHint() {
        return new TypeRef<String>() {}
    }

}

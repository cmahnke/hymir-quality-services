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

@TypeChecked
@CompileStatic
class GoobiViewerLanguageRewriteOperation implements JSONRewriteOperation {

    List<String> jsonPath = Arrays.asList('$.attribution', '$.service.label')

    GoobiViewerLanguageRewriteOperation() {

    }

    @Override
    List<String> path() {
        return jsonPath
    }

    @Override
    Object rewrite(String path, Object value) {
        if (value !instanceof Map) {
            throw new IllegalStateException(this.getClass().getSimpleName() + ' can only work on Map\'s')
        }
        List<Map<String, String>> rewritten = new ArrayList<Map<String, String>>()
        Map<String, List> attributions = value as Map<String, List>
        attributions.forEach (String lang, List<String> strings) -> {
            Map<String, String> attribution = new HashMap<>()
            attribution.put('@value', String.join(' ', strings))
            attribution.put('@language', lang)
            rewritten.add(attribution)
        }
        return rewritten
    }

    @Override
    TypeRef typeHint() {
        return new TypeRef<Map<String, ?>>() {}
    }

}

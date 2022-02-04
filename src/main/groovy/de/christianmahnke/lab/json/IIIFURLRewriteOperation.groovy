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

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

import java.util.regex.Pattern

@TypeChecked
@CompileStatic
class IIIFURLRewriteOperation extends RegexRewriteOperation {

    IIIFURLRewriteOperation(String from, String to) {
        super('$..@id', from, to)
    }

    IIIFURLRewriteOperation(Map<String, String> replacements) {
        super(replacements)
    }

    RegexRewriteOperation addURL(String from, String to) {
        this.replacements.put(Pattern.compile(from), to)
        return this
    }

    RegexRewriteOperation addURLs(Map<String, String> replacements) {
        for (String from: replacements.keySet()) {
            this.replacements.put(Pattern.compile(from), replacements.get(from))
        }
        return this
    }
}

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

import de.digitalcollections.commons.file.business.api.FileResourceService
import de.digitalcollections.commons.file.config.SpringConfigCommonsFile
import de.digitalcollections.iiif.hymir.config.SpringConfig
import de.digitalcollections.iiif.hymir.model.api.HymirPlugin
import de.digitalcollections.iiif.hymir.model.exception.InvalidDataException
import de.digitalcollections.iiif.hymir.model.exception.ResolvingException
import de.digitalcollections.iiif.hymir.presentation.backend.PresentationRepositoryImpl
import de.digitalcollections.iiif.model.jackson.IiifObjectMapper
import de.digitalcollections.iiif.model.sharedcanvas.Manifest
import de.digitalcollections.model.exception.ResourceIOException
import de.digitalcollections.model.file.MimeType
import de.digitalcollections.model.identifiable.resource.FileResource
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository

@Slf4j
@CompileStatic
@TypeChecked
@Primary
@Repository
@Import([SpringConfigCommonsFile.class, SpringConfig.class])
class RewritingPresentationRepository extends PresentationRepositoryImpl implements HymirPlugin {

    @Autowired
    IiifObjectMapper objectMapper

    @Autowired
    FileResourceService fileResourceService

    @Override
    String name() {
        return "Rewriting Presentation Manifest backend"
    }

    @Override
    Manifest getManifest(String identifier) {
        FileResource resource
        try {
            resource = fileResourceService.find(identifier, MimeType.MIME_APPLICATION_JSON)
        } catch (ResourceIOException ex) {
            log.error("Error getting manifest for identifier ${identifier}", ex)
            throw new ResolvingException("No manifest for identifier " + identifier)
        }
        //TODO: Add rewriting here
        try {
            return objectMapper.readValue(getResourceJson(resource), Manifest.class)
        } catch (IOException ex) {
            log.error("Manifest ${identifier} can not be parsed", ex)
            throw new InvalidDataException("Manifest " + identifier + " can not be parsed", ex)
        }
    }
}

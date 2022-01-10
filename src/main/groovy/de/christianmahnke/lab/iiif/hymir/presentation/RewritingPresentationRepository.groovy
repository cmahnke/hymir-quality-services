package de.christianmahnke.lab.iiif.hymir.presentation

import de.digitalcollections.commons.file.business.api.FileResourceService
import de.digitalcollections.commons.file.config.SpringConfigCommonsFile
import de.digitalcollections.iiif.hymir.model.api.HymirPlugin
import de.digitalcollections.iiif.hymir.model.exception.ResolvingException
import de.digitalcollections.iiif.hymir.presentation.backend.PresentationRepositoryImpl
import de.digitalcollections.iiif.model.sharedcanvas.Manifest
import de.digitalcollections.model.exception.ResourceIOException
import de.digitalcollections.model.file.MimeType
import de.digitalcollections.model.identifiable.resource.FileResource
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Repository

@CompileStatic
@TypeChecked
//@Repository
//@Import(SpringConfigCommonsFile.class)
class RewritingPresentationRepository extends PresentationRepositoryImpl implements HymirPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(RewritingPresentationRepository.class);

    @Autowired private FileResourceService fileResourceService

    @Override
    String name() {
        return "Rewriting Presentation Manifest backend"
    }

    @Override
    public Manifest getManifest(String identifier) {
        FileResource resource;
        try {
            resource = fileResourceService.find(identifier, MimeType.MIME_APPLICATION_JSON);
        } catch (ResourceIOException ex) {
            LOGGER.error("Error getting manifest for identifier {}", identifier, ex);
            throw new ResolvingException("No manifest for identifier " + identifier);
        }

        return null;
    }
}

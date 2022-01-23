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

import de.christianmahnke.lab.images.opencv.imageio.OpenCVImageReader
import de.digitalcollections.iiif.hymir.image.business.api.ImageQualityService
import de.digitalcollections.iiif.model.image.ImageApiProfile
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import javax.imageio.ImageReader

@Slf4j
@Service
@TypeChecked
@CompileStatic
class StackImageQualityService implements ImageQualityService.Source {

    @Value('${custom.image.quality.stack.enabled:false}')
    private boolean enabled

    @Value('${custom.image.quality.stack.name:stack}')
    private String name

    @Value('${custom.image.quality.stack.plugins:#{null}}')
    private String plugins

    //TODO: Check how this Stack could be used with Tile's
    private Deque<ImageQualityService.Source> services = new LinkedList<ImageQualityService.Source>()

    private Boolean hasAlpha = false

    StackImageQualityService(@Value('${custom.image.quality.stack.plugins:#{null}}') String plugins, @Autowired List<ImageQualityService> imageQualityServices) {
        if (plugins != null && imageQualityServices != null) {
            List<String> enabledPlugins = Arrays.asList(plugins.split(","))
            Map<String, ImageQualityService> pluginNames = new HashMap<String, ImageQualityService>()
            for (ImageQualityService iqs in imageQualityServices) {
                pluginNames.put(iqs.getQuality().toString(), iqs)
            }
            for (plugin in enabledPlugins) {
                if (pluginNames.containsKey(plugin)) {
                    ImageQualityService iqs = pluginNames.get(plugin)
                    if (iqs instanceof ImageQualityService.Source && iqs.enabled()) {
                        log.info("Added quality '{}' found - {}, provided by '{} to stack'", plugin, (iqs.enabled() ? "enabled" : "disabled"), iqs.getClass().getName())
                        services.add(iqs)
                        if (!hasAlpha && iqs.hasAlpha()) {
                            hasAlpha = true
                        }
                    }
                }
            }
        }
    }

    @Override
    ImageApiProfile.Quality getQuality() {
        return new ImageApiProfile.Quality(name)
    }

    @Override
    boolean enabled() {
        return this.enabled
    }

    @Override
    boolean hasAlpha() {
        return hasAlpha
    }

    @Override
    String name() {
        return "Stack of different plugins: ${plugins}"
    }

    @Override
    ImageReader processStream(String identifier, InputStream inputStream) {
        ImageReader reader
        int i = 0
        for (service in services) {
            reader = service.processStream(identifier, inputStream)
            if (i + 1 == services.size()) {
                return reader
            } else {
                inputStream = OpenCVImageReader.getInputStreamFromImageReader(reader)
            }
            i++
        }
        log.warn("${this.getClass().getSimpleName()} failed, returning null!")
        return null
    }

}

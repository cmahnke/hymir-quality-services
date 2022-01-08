Hymir Quality services
======================

This repository contains implementations of custom IIIG `quality`'s for [Hymir](https://github.com/dbmdz/iiif-server-hymir). You need a [hacked version of it](https://github.com/cmahnke/iiif-server-hymir) to use them. 

# Configuration

You can either enable or disable specific qualities in your `application.yml` file by setting the `enabled` param. It's also possible to change the name of the `quality` with `name`.

Also make sure to set the `package` in `plugins`.

```yaml
custom:
  image:
    quality:
      noop:
        enabled: false
      background:
        enabled: true
        name: transparent_backgrond
      fold:
        enabled: false
plugins:
  packages: de.christianmahnke.lab.iiif.hymir
```

Additionally you need to change the Spring MVC Path matcher if you want to use identifiers caontaining unascaped forward slashes with the changed Hymir.

```yaml
spring:
  mvc:
    pathmatch:
      matching-strategy: ant_path_matcher
```

# Starting Hymir with additional plugins

```shell
java -cp hymir-exec.jar -Dloader.path=hymir-quality-services-1.0-SNAPSHOT.jar org.springframework.boot.loader.PropertiesLauncher --spring.config.additional-location=plugins.yml --spring.profiles.active=plugins
```

* Make sure to pass the jar file containing the plugins by a additional system property (`-Dloader.path=`), not by class path (`-cp`), otherwise Spring wont find them.
* Use `org.springframework.boot.loader.PropertiesLauncher` as main class (this requires that Hymir has been build with the `ZIP` layout).
* Pass either an updated `application.yml` or provide the settings with an additional Spring configuration (`--spring.config.additional-location=plugins.yml`).
* Activate the Spring profile `plugins` (`--spring.profiles.active=plugins`) in addition to the one(s) you would use.
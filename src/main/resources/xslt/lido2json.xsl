<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:lido="http://www.lido-schema.org" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns="http://www.w3.org/2005/xpath-functions" xmlns:xs="http://www.w3.org/2001/XMLSchema"
    version="3.0" default-mode="xml-to-json" exclude-result-prefixes="xsi xs xsl lido">

    <xsl:output indent="yes" method="text"/>
    <xsl:param name="collection" select="''" as="xs:string"/>

    <xsl:template match="/">
        <xsl:variable name="json">
            <array>
                <xsl:apply-templates/>
            </array>
        </xsl:variable>
        <xsl:copy-of select="xml-to-json($json)"/>
    </xsl:template>

    <xsl:template match="//lido:lidoRecID" priority="10">
        <map>
        <string key="label">id</string>
        <string key="value">
            <xsl:value-of select="./text()"/>
        </string>
        </map>
    </xsl:template>
    <xsl:template
        match="//lido:category | //lido:recordInfoSet | //lido:recordRights | //lido:recordSource | //lido:rightsResource | //lido:repositoryWrap | //lido:objectRelationWrap"
        mode="#all"/>
    <xsl:template match="//lido:resourceMeasurementsSet | //lido:resourceRepresentation" mode="#all"/>

    <xsl:template match="//lido:descriptiveMetadata">
        <xsl:apply-templates
            select="lido:objectIdentificationWrap/lido:objectMeasurementsWrap | lido:objectClassificationWrap/lido:classificationWrap | lido:eventWrap/lido:eventSet"
            mode="#current"/>
    </xsl:template>

    <xsl:template
        match="lido:classification[@lido:type = 'nominal'] | lido:classification[@lido:type = 'authenticity']">
        <map>
            <string key="label">
                <xsl:value-of select="@lido:type"/>
            </string>
            <string key="value">
                <xsl:value-of select="./lido:term/text()"/>
            </string>
        </map>
    </xsl:template>
    <xsl:template match="lido:eventSet">
        <xsl:apply-templates select="lido:event" mode="#current"/>
    </xsl:template>

    <!-- This template matches the commissioning event -->
    <xsl:template
        match="lido:event[child::lido:eventType/lido:conceptID[@lido:type = 'http://terminology.lido-schema.org/identifier_type/uri'][./text() = 'http://terminology.lido-schema.org/eventType/commissioning']]">
        <xsl:if
            test="//lido:eventActor[child::lido:actorInRole/lido:roleActor/lido:conceptID[@lido:type = 'http://terminology.lido-schema.org/identifier_type/uri'][./text() = 'http://d-nb.info/gnd/1051614252']]">
            <xsl:variable name="commisioningActor" as="element(lido:eventActor)"
                select="//lido:eventActor[child::lido:actorInRole/lido:roleActor/lido:conceptID[@lido:type = 'http://terminology.lido-schema.org/identifier_type/uri'][./text() = 'http://d-nb.info/gnd/1051614252']]"/>
            <map>
                <string key="label">actor</string>
                <!--
            <xsl:value-of select="$commisiningActor//lido:displayActorInRole/text()"/>
            -->
                <string key="value">
                    <xsl:value-of
                        select="$commisioningActor//lido:actorInRole/lido:actor/lido:nameActorSet/lido:appellationValue/text()"
                    />
                </string>
            </map>
            <map>
                <string key="label">actor_uri</string>
                <string key="value">
                    <xsl:value-of
                        select="$commisioningActor//lido:actorInRole/lido:actor/lido:actorID[@lido:type = 'http://terminology.lido-schema.org/identifier_type/uri']/text()"
                    />
                </string>
            </map>
        </xsl:if>
    </xsl:template>

    <!-- This template mactches the production event -->
    <xsl:template
        match="lido:event[child::lido:eventType/lido:conceptID[@lido:type = 'http://terminology.lido-schema.org/identifier_type/uri'][./text() = 'http://terminology.lido-schema.org/eventType/production']]">
        <xsl:variable name="productionDate" as="element(lido:eventDate)" select="//lido:eventDate"/>
        <map>
            <string key="label">date</string>
            <string key="value">
                <xsl:value-of select="$productionDate/lido:displayDate/text()"/>
            </string>
        </map>
        <xsl:variable name="productionPlace" as="element(lido:eventPlace)*"
            select="//lido:eventPlace"/>

        <xsl:choose>
            <!-- Dot matches lido:eventPlace -->
            <xsl:when
                test="$productionPlace//.[child::lido:place[@lido:politicalEntity = 'minting_place'][child::lido:placeID]]">
                <map>
                    <string key="label">place</string>
                    <string key="value">
                        <xsl:value-of
                            select="$productionPlace//.[child::lido:place[@lido:politicalEntity = 'minting_place'][child::lido:placeID]]//lido:appellationValue/text()"
                        />
                    </string>
                </map>
                <map>
                    <string key="label">place_uri</string>
                    <string key="value">
                        <xsl:value-of
                            select="$productionPlace//.[child::lido:place[@lido:politicalEntity = 'minting_place'][child::lido:placeID]]//lido:placeID/text()"
                        />
                    </string>
                </map>

            </xsl:when>
            <xsl:when
                test="$productionPlace//.[child::lido:place[@lido:politicalEntity = 'minting_authority'][child::lido:placeID]]">
                <string key="label">place</string>
                <string key="value">
                    <xsl:value-of
                        select="$productionPlace/.[child::lido:place[@lido:politicalEntity = 'minting_authority'][child::lido:placeID]]//lido:appellationValue/text()"
                    />
                </string>

                <map>
                    <string key="label">place_uri</string>
                    <string key="value">
                        <xsl:value-of
                            select="$productionPlace//.[child::lido:place[@lido:politicalEntity = 'minting_authority'][child::lido:placeID]]//lido:placeID/text()"
                        />
                    </string>
                </map>
            </xsl:when>
            <xsl:when
                test="$productionPlace//.[child::lido:place[@lido:politicalEntity = 'minting_place']]">
                <map>
                    <string key="label">place</string>
                    <string key="value">
                        <xsl:value-of
                            select="$productionPlace//.[child::lido:place[@lido:politicalEntity = 'minting_place']]//lido:appellationValue/text()"
                        />
                    </string>
                </map>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message terminate="yes">No valid minting place found!</xsl:message>
            </xsl:otherwise>
        </xsl:choose>

        <xsl:if
            test="//lido:materialsTech[child::lido:termMaterialsTech[@lido:type = 'http://terminology.lido-schema.org/termMaterialsTech_type/material']]">
            <xsl:variable name="productionMaterial" as="element(lido:materialsTech)"
                select="//lido:materialsTech[child::lido:termMaterialsTech[@lido:type = 'http://terminology.lido-schema.org/termMaterialsTech_type/material']]"/>
            <map>
                <string key="label">material</string>
                <string key="value">
                    <xsl:value-of
                        select="$productionMaterial//lido:term[not(@lido:addedSearchTerm = 'yes')]/text()"
                    />
                </string>
            </map>
            <array key="material_uri">
                <xsl:for-each
                    select="$productionMaterial//lido:conceptID[@lido:type = 'http://terminology.lido-schema.org/identifier_type/uri']">
                    <string>
                        <xsl:value-of select="./text()"/>
                    </string>
                </xsl:for-each>
            </array>
        </xsl:if>
        <xsl:if test="//lido:materialsTech[child::lido:termMaterialsTech[not(@lido:type)]]">
            <xsl:variable name="productionProcess" as="element(lido:materialsTech)"
                select="//lido:materialsTech[child::lido:termMaterialsTech[not(@lido:type)]]"/>
            <map>
                <string key="label">process</string>
                <string key="value">
                    <xsl:value-of select="$productionProcess//lido:term/text()"/>
                </string>
            </map>
        </xsl:if>

    </xsl:template>

    <xsl:template match="//lido:objectMeasurementsWrap">
        <xsl:variable name="weight"
            select=".//lido:measurementsSet[.//lido:measurementType[@xml:lang = 'en']/text() = 'weight']"/>
        <map>
            <string key="label">weight</string>
            <string key="value">
                <xsl:value-of
                    select="concat($weight//lido:measurementValue/text(), $weight//lido:measurementUnit/text())"
                />
            </string>
        </map>
        <xsl:variable name="weight"
            select=".//lido:measurementsSet[.//lido:measurementType[@xml:lang = 'en']/text() = 'orientation']"/>
        <map>
            <string key="label">orientation</string>
            <string key="value">
                <xsl:value-of
                    select="concat($weight//lido:measurementValue/text(), $weight//lido:measurementUnit/text())"
                />
            </string>
        </map>
    </xsl:template>
    <!--
    <xsl:template match="//lido:*[not(*)]" >
        <field>
            <xsl:attribute name="name">
                <xsl:variable name="parents" as="node()*">
                    <xsl:for-each select="subsequence(ancestor::*, 2)">
                        <xsl:value-of select="local-name(.)"/>
                    </xsl:for-each>
                </xsl:variable>
                <xsl:value-of select="string-join(($parents, local-name(.)), '/')"/>
            </xsl:attribute>
            <xsl:attribute name="short_name">
                <xsl:value-of select="local-name(.)"/>
            </xsl:attribute>
            <xsl:value-of select="normalize-space(./text())"/>
        </field>
    </xsl:template>
-->
    <xsl:template match="text()" mode="#all"/>
</xsl:stylesheet>

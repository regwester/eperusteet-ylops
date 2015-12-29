<xsl:stylesheet
        version="1.0"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:fo="http://www.w3.org/1999/XSL/Format">
    <xsl:output indent="yes"/>
    <xsl:strip-space elements="*"/>

    <!-- Attributes -->
    <xsl:attribute-set name="company.block">
        <xsl:attribute name="border-after-color">navy</xsl:attribute>
        <xsl:attribute name="border-after-style">solid</xsl:attribute>
        <xsl:attribute name="border-after-width">0.7mm</xsl:attribute>
        <xsl:attribute name="padding">2mm</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="test">
        <xsl:attribute name="color">#007ec5</xsl:attribute>
    </xsl:attribute-set>

    <!-- Title page -->
    <xsl:template match="doc">
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format"
                 xmlns:fox="http://xmlgraphics.apache.org/fop/extensions">
            <fo:layout-master-set>
                <!-- Testi ulkoasu -->
                <fo:simple-page-master master-name="template-A4-page" page-width="210mm" page-height="297mm">
                    <fo:region-body margin="30mm"
                                    background-color="yellow"
                                    border-style="dashed"
                                    border-width="thick"/>
                    <fo:region-before extent="30mm" background-color="lightblue" />
                    <fo:region-after extent="30mm" background-color="lightblue" />
                    <fo:region-start extent="30mm" background-color="lightgreen" />
                    <fo:region-end extent="30mm" background-color="lightgreen" />
                </fo:simple-page-master>

                <!-- Kansilehti ulkoasu -->
                <fo:simple-page-master master-name="blank-A4-page" page-width="210mm" page-height="297mm">
                    <fo:region-body margin="30mm" />
                </fo:simple-page-master>

                <!-- Sisältö ulkoasu -->
                <fo:simple-page-master master-name="A4-page" page-width="210mm" page-height="297mm">
                    <fo:region-body margin="30mm" />
                    <fo:region-before border-after-style="solid" border-width="0.3mm" border-color="lightgray"
                                      extent="20mm" display-align="after" />
                    <fo:region-after border-before-style="solid" border-width="0.3mm" border-color="lightgray"
                                     extent="20mm" display-align="before" />
                    <fo:region-start region-name="left-side" extent="30mm" reference-orientation="90" />
                    <fo:region-end region-name="right-side" extent="30mm" reference-orientation="90" />
                </fo:simple-page-master>
            </fo:layout-master-set>

            <!-- Testisivu -->
            <fo:page-sequence master-reference="template-A4-page">
                <fo:flow flow-name="xsl-region-body">
                    <fo:block/>
                </fo:flow>
            </fo:page-sequence>

            <!-- Kansisivu -->
            <fo:page-sequence master-reference="blank-A4-page">
                <fo:flow flow-name="xsl-region-body">
                    <xsl:apply-templates select="meta" />
                    <fo:block id="end" />
                </fo:flow>
            </fo:page-sequence>

            <!-- Infosivu -->
            <fo:page-sequence master-reference="A4-page">
                <fo:flow flow-name="xsl-region-body">
                    <xsl:apply-templates select="info" />
                </fo:flow>
            </fo:page-sequence>

            <!-- Sisällysluettelo -->
            <fo:page-sequence master-reference="A4-page">
                <fo:flow flow-name="xsl-region-body">
                    <xsl:call-template name="generateTOC"/>
                </fo:flow>
            </fo:page-sequence>

            <!-- Luvut -->
            <xsl:apply-templates select="/doc/chapter"/>

            <!-- Luvut -->
            <!--<fo:page-sequence master-reference="A4-page" initial-page-number="1">
                <fo:static-content flow-name="xsl-region-after">
                    <fo:block text-align="center">
                        Page <fo:page-number /> of <fo:page-number-citation ref-id="end" />
                    </fo:block>
                </fo:static-content>
                <fo:flow flow-name="xsl-region-body">
                    <fo:block id="end" />
                </fo:flow>
            </fo:page-sequence>-->
        </fo:root>
    </xsl:template>

    <!-- Templatet alkavat tästä -->

    <xsl:template match="meta">
        <fo:block text-align="center" font-size="28pt" font-weight="bold">
            <xsl:value-of select="/doc/meta/title" />
        </fo:block>
        <fo:block text-align="center" font-size="18pt" font-weight="bold">
            <xsl:value-of select="/doc/meta/educationType" />
        </fo:block>
    </xsl:template>

    <xsl:template match="info">
        <!-- Tiivistelmän otsikko -->
        <fo:block font-size="18pt" font-weight="bold" padding-bottom="12pt">
            <xsl:value-of select="/doc/meta/title" />: <xsl:value-of select="/doc/meta/educationType" />
        </fo:block>

        <!-- Tiivistelmän Kuvas -->
        <fo:block padding-bottom="12pt">
            <xsl:value-of select="description" />
        </fo:block>

        <xsl:apply-templates select="infoTable" />
    </xsl:template>

    <!-- Info page -->

    <xsl:template match="infoTable">
        <fo:table table-layout="fixed" width="100%" border-collapse="separate">
            <fo:table-column column-width="50%"/>
            <fo:table-column column-width="50%"/>
            <fo:table-body>
                <xsl:for-each select=".">
                    <xsl:apply-templates select="tr"/>
                </xsl:for-each>
            </fo:table-body>
        </fo:table>
    </xsl:template>

    <xsl:template match="tr">
        <fo:table-row>
            <fo:table-cell>
                <fo:block font-weight="bold">
                    <xsl:value-of select="td[1]" />
                </fo:block>
            </fo:table-cell>
            <fo:table-cell>
                <fo:block>
                    <xsl:value-of select="td[2]" />
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template match="td">
        <fo:table-cell>
            <fo:block>
                <xsl:value-of select="." />
            </fo:block>
        </fo:table-cell>
    </xsl:template>

    <!-- Table of contents page -->

    <xsl:template name="generateTOC">
        <fo:block break-before='page'>
            <fo:block font-size="16pt" font-weight="bold">Sisällys</fo:block>
            <xsl:for-each select="//chapter">
                <fo:block text-align-last="justify">
                    <fo:basic-link internal-destination="{generate-id(.)}">
                        <xsl:value-of select="count(preceding::chapter) + 1" />
                        <xsl:text>. </xsl:text>
                        <xsl:value-of select="title" />
                        <fo:leader leader-pattern="dots" />
                        <fo:page-number-citation ref-id="{generate-id(.)}" />
                    </fo:basic-link>
                </fo:block>
            </xsl:for-each>
        </fo:block>
    </xsl:template>

    <xsl:template match="chapter">
        <fo:page-sequence master-reference="A4-page" id="{generate-id(.)}" initial-page-number="1">
            <fo:static-content flow-name="xsl-region-after">
                <fo:block text-align="center">
                    <fo:page-number />
                    <!--<xsl:text> / </xsl:text>
                    <fo:page-number-citation ref-id="end" />-->
                </fo:block>
            </fo:static-content>

            <fo:static-content flow-name="left-side">
                <fo:table>
                    <fo:table-column />
                    <fo:table-column />

                    <fo:table-body>
                        <fo:table-row>
                            <fo:table-cell>
                                <fo:block text-align="start" use-attribute-sets="test" color="#007ec5" margin-left="20mm" margin-top="5mm">
                                    <xsl:value-of select="title" />
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell>
                                <fo:block text-align="end">
                                    <fo:instream-foreign-object>
                                        <svg xmlns="http://www.w3.org/2000/svg" width="100mm" height="15mm">
                                            <defs>
                                                <linearGradient id="two_stops">
                                                    <stop offset="0%" style="stop-color: #ffffff;" />
                                                    <stop offset="100%" style="stop-color: #bed6ee;" />
                                                </linearGradient>
                                            </defs>
                                            <rect x="0" y="-15" width="100%" height="100%" style="fill:url(#two_stops);" />
                                        </svg>
                                    </fo:instream-foreign-object>
                                </fo:block>
                            </fo:table-cell>
                        </fo:table-row>
                    </fo:table-body>
                </fo:table>
            </fo:static-content>

            <fo:flow flow-name="xsl-region-body">
                <fo:block font-size="18pt" font-weight="bold">
                    <xsl:value-of select="count(preceding::chapter) + 1" />
                    <xsl:text>. </xsl:text>
                    <xsl:value-of select="title" />
                </fo:block>
                <!--<fo:block id="end" />-->
            </fo:flow>

        </fo:page-sequence>
    </xsl:template>

    <xsl:template match="title|para">
        <fo:block><xsl:value-of select="."/></fo:block>
    </xsl:template>

</xsl:stylesheet>